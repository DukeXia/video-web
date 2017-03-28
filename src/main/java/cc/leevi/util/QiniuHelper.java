package cc.leevi.util;

import cc.leevi.autoconfigure.QiniuProperties;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by jiang on 2017-03-25.
 */
@Component
public class QiniuHelper {

    private Logger logger =  LoggerFactory.getLogger(this.getClass());

    @Autowired
    private QiniuProperties qiniuProperties;

    private Auth auth = null;
    private String upToken = null;
    private Configuration cfg = null;
    private UploadManager uploadManager = null;

    @PostConstruct
    public void init(){
        auth = Auth.create(qiniuProperties.getAccesskey(), qiniuProperties.getSecretkey());
        upToken = auth.uploadToken(qiniuProperties.getBucket());
        cfg = new Configuration(Zone.zone0());
        uploadManager = new UploadManager(cfg);
    }




    /**
     * 保存封面图
     * @param coverPath
     * @return
     */
    public String uploadCover(String coverPath) {
        try {
            String key = getUUID();
            String body = uploadManager.put(coverPath, key,upToken).bodyString();
            String coverUrl = qiniuProperties.getPrefix()+"/"+key;
            logger.info("上传七牛完成，地址为："+coverUrl);
            return coverUrl;
        } catch (IOException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return null;
    }

    public String getUUID(){
        return UUID.randomUUID().toString().replaceAll("-","");
    }
}
