package cc.leevi.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by jiang on 2017-03-24.
 */
@Component
@ConfigurationProperties(prefix = "ffmped")
public class FfmpegProperties {
    /**
     * 路径
     */
    private String path;
    /**
     * 转ts命令
     */
    private String convert;
    /**
     * 拆分命令
     */
    private String split;
    /**
     * 获取缩略图命令
     */
    private String cover;

    /**
     * 扫描路径
     */
    private String scanPath;

    /**
     * 视频信息
     */
    private String info;

    List<String> supportFormats;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getConvert() {
        return convert;
    }

    public void setConvert(String convert) {
        this.convert = convert;
    }

    public String getSplit() {
        return split;
    }

    public void setSplit(String split) {
        this.split = split;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getScanPath() {
        return scanPath;
    }

    public void setScanPath(String scanPath) {
        this.scanPath = scanPath;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public List<String> getSupportFormats() {
        return supportFormats;
    }

    public void setSupportFormats(List<String> supportFormats) {
        this.supportFormats = supportFormats;
    }
}
