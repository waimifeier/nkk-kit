package org.nkk.mp.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "wx.applet")
public class MpAppletProperties {

    /**
     * 小程序 appId
     */
    private String appKey;

    /**
     * 小程序 appSecret
     */
    private String secret;
}
