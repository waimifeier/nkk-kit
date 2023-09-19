package org.nkk.mp.config;


import org.nkk.mp.properties.MpAppletProperties;
import org.nkk.mp.service.applet.WxAppletTemplate;
import org.nkk.mp.service.applet.impl.WxAppletUser;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MpAppletProperties.class)
public class MpAutoConfig {

    @Bean
    public WxAppletTemplate mpAppletTemplate() {
        WxAppletUser wxAppletUser = new WxAppletUser();

        WxAppletTemplate appletTemplate = new WxAppletTemplate(wxAppletUser);

        return appletTemplate;
    }
}
