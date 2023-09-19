package org.nkk.mp.service.applet;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson2.JSON;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.nkk.mp.constant.MpAppletApiUrl;
import org.nkk.mp.properties.MpAppletProperties;

@Slf4j
public class BaseWxAppletService implements WxAppletService{

    @Getter
    private MpAppletProperties appletProperties;


    @Override
    public String accessToken(Boolean refresh) {
        String body = HttpRequest.post(MpAppletApiUrl.Base.GET_ACCESS_TOKEN)
                .form("grant_type", "client_credential")
                .form("appid", appletProperties.getAppKey())
                .form("secret", appletProperties.getSecret())
                .form("force_refresh", refresh) // 普通模式，access_token 有效期内重复调用该接口不会更新 access_token
                .execute()
                .body();

        log.debug("获取accessToken：{}",body);
        return JSON.parseObject(body).getString("access_token");
    }

    /**
     *
     * @return 返回凭证 {@link String}
     */
    public String accessToken(){
        return this.accessToken(false);
    }

}
