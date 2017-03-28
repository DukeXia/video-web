package cc.leevi.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by jiang on 2017-03-25.
 */
@Component
@ConfigurationProperties(prefix = "global")
public class GlobalProperties {
    private String schema;
    private String domain;
    private String media;

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getMedia() {
        return media;
    }

    public void setMedia(String media) {
        this.media = media;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getPrefix(){
        return schema+domain+media+"/";
    }
}
