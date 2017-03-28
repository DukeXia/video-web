package cc.leevi.ffmpeg;

import cc.leevi.autoconfigure.FfmpegProperties;
import cc.leevi.autoconfigure.GlobalProperties;
import cc.leevi.autoconfigure.QiniuProperties;
import cc.leevi.mapper.ResourceMapper;
import cc.leevi.model.Resource;
import cc.leevi.util.QiniuHelper;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import jdk.nashorn.internal.objects.Global;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jiang on 2017-03-24.
 */
@Component
public class VideoHandler {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private List<String> commads = new ArrayList<>();

    @Autowired
    private FfmpegProperties ffmpegProperties;

    @Autowired
    private GlobalProperties globalProperties;

    private String RESOURCE_SUFFIX = "_m3u8";

    @Autowired
    private ResourceMapper resourceMapper;

    @Autowired
    private QiniuHelper qiniuHelper;

    public void process() {
        long cur = System.currentTimeMillis();
        logger.info("开始扫描资源目录");
        String scanPath = ffmpegProperties.getScanPath();
        recursionProcess(0, scanPath);
        logger.info("所有视频处理完成，共耗时：" + (System.currentTimeMillis() - cur) / 1000 + "s");
        saveConvertCommods();
    }

    private void saveConvertCommods() {
        File file = new File("d:\\convert_commands.txt");
        FileWriter fw = null;
        BufferedWriter writer = null;
        try {
            fw = new FileWriter(file);
            writer = new BufferedWriter(fw);
            for (String commad : commads) {
                writer.write(commad);
                writer.newLine();//换行
            }
            writer.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void recursionProcess(int folderId, String scanPath) {
        File scanFolder = new File(scanPath);
        File[] fileList = scanFolder.listFiles();
        logger.info("扫描路径：" + scanPath);
        //资源
        boolean isCategory = false;
        for (File file : fileList) {
            if (!(file.isFile() || file.getName().contains(RESOURCE_SUFFIX))) {
                isCategory = true;
            }
        }
        if (isCategory) {
            for (File file : fileList) {
                String resourcePath = file.getAbsolutePath();
                int childId = saveFolder(folderId, resourcePath);
                recursionProcess(childId, resourcePath);
            }
        } else {
            for (File file : fileList) {
                String resourceName = file.getName();
                processVideo(folderId, file.getParent(), resourceName);
            }
        }
    }

    /**
     * 保存目录
     *
     * @param folderId
     * @param path
     * @return
     */
    private int saveFolder(int folderId, String path) {
        String folderName = path.substring(path.lastIndexOf("\\") + 1);
        Resource resource = new Resource();
        resource.setName(folderName);
        resource.setPath(path);
        resource.setParent(folderId);
        resource.setType("category");
        resourceMapper.insertSelective(resource);
        logger.info("保存目录：" + path);
        return resource.getId();
    }

    /**
     * 保存资源
     *
     * @param folderId
     * @param path
     * @param duration
     * @param coverUrl
     */
    private int saveMedia(int folderId, String path, String videoName, int duration, String coverUrl, String playUrl, long lastModified) {
        Resource resource = new Resource();
        resource.setName(videoName);
        resource.setPath(path);
        resource.setParent(folderId);
        resource.setPath(path);
        resource.setCover(coverUrl);
        resource.setPlayUrl(playUrl);
        resource.setDuration(duration);
        resource.setType("media");
        resource.setLastModified(new Date(lastModified));
        resourceMapper.insertSelective(resource);
        logger.info("保存资源：" + path);
        return resource.getId();
    }

    private void processVideo(Integer folderId, String path, String resourceName) {
        String suffix = resourceName.substring(resourceName.lastIndexOf("."));
        if (!ffmpegProperties.getSupportFormats().contains(suffix)) {
            return;
        }
        String videoName = resourceName.substring(0, resourceName.lastIndexOf("."));
        String mediaPath = path + "/" + videoName + RESOURCE_SUFFIX;
        File mediaFolder = new File(mediaPath);
        if (!mediaFolder.exists()) {
            mediaFolder.mkdir();
            String convertCmd = ffmpegProperties.getConvert();
            String coverCmd = ffmpegProperties.getCover();
            String infoCmd = ffmpegProperties.getInfo();
            String mediaFull = path + "\\" + resourceName;
            infoCmd = String.format(infoCmd, mediaFull);

            String mediaName = mediaPath + "/index";
            String coverPath = mediaName + ".jpg";
            String relativeUrl = mediaPath.replace(ffmpegProperties.getScanPath(), "").substring(1).replace("\\", "/");
            String playPath = mediaName + ".m3u8";
            String splitSuffix = "%03d.ts";
            convertCmd = String.format(convertCmd, mediaFull, playPath, mediaName + splitSuffix);
            coverCmd = String.format(coverCmd, mediaFull, coverPath);
            File media = new File(path + "\\" + resourceName);
            long lastModified = media.lastModified();
            int duration = 0;
            String coverUrl = null;
            try {
                long current = System.currentTimeMillis();
                Process process = null;
                logger.info("获取封面图");
                current = System.currentTimeMillis();
                logger.info(coverCmd);
                process = Runtime.getRuntime().exec(coverCmd);
                coverUrl = globalProperties.getPrefix() + relativeUrl + "/index.jpg";
                process.waitFor();
                logger.info("保存封面图成功：" + coverUrl);
                process = Runtime.getRuntime().exec(infoCmd);
                process.waitFor();
                String infoBody = IOUtils.toString(process.getErrorStream(), "UTF-8");
                duration = getDuration(infoBody);
                logger.info(String.format("【%s】时长：%ds", resourceName, duration));
//                logger.info(String.format("【%s】开始转换拆分，此操作非常耗时，耐心等待……",videoName ));
//                current = System.currentTimeMillis();
//                process = Runtime.getRuntime().exec(convertCmd);
//                process.waitFor();
                String playUrl = globalProperties.getPrefix() + relativeUrl + "/index.m3u8";
                commads.add(convertCmd);
//                logger.info(String.format("【%s】转换拆分耗时：%dms",videoName,System.currentTimeMillis() - current));
                saveMedia(folderId, mediaFull, videoName, duration, coverUrl, playUrl, lastModified);
            } catch (IOException e) {
                logger.error(ExceptionUtils.getStackTrace(e));
            } catch (InterruptedException e) {
                logger.error(ExceptionUtils.getStackTrace(e));
            }
        }

    }

    /**
     * 从cmd输入流中读取视频时长
     *
     * @param infoBody
     * @return
     */
    private int getDuration(String infoBody) {
        String regexDuration = "Duration: (.*?), start: (.*?), bitrate: (\\d*) kb\\/s";
        Pattern pattern = Pattern.compile(regexDuration);
        Matcher m = pattern.matcher(infoBody);
        if (m.find()) {
            int time = getTimeSecond(m.group(1));
            return time;
        }
        return 0;
    }

    //格式:"00:00:10.68"
    private static int getTimeSecond(String timeTemp) {
        int second = 0;
        String strs[] = timeTemp.split(":");
        if (strs[0].compareTo("0") > 0) {
            second += Integer.valueOf(strs[0]) * 60 * 60;//秒
        }
        if (strs[1].compareTo("0") > 0) {
            second += Integer.valueOf(strs[1]) * 60;
        }
        if (strs[2].compareTo("0") > 0) {
            second += Math.round(Float.valueOf(strs[2]));
        }
        return second;
    }
}
