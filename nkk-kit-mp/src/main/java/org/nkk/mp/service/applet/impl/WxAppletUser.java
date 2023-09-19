package org.nkk.mp.service.applet.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.nkk.mp.beans.dto.WxAppletPhoneDto;
import org.nkk.mp.beans.dto.WxAppletLoginDto;
import org.nkk.mp.constant.MpAppletApiUrl;
import org.nkk.mp.service.applet.BaseWxAppletService;

import java.util.Objects;

@Slf4j
public class WxAppletUser extends BaseWxAppletService {

    /**
     * <p>
     * 登录凭证校验。通过 wx.login 接口获得临时登录凭证 code 后传到开发者服务器调用此接口完成登录流程
     * </p>
     *
     * @param code 小程序登录凭证code
     * @return 返回登陆信息 {@link WxAppletLoginDto }
     */
    public WxAppletLoginDto jsCode2session(String code) {

        HttpResponse httpResponse = HttpRequest.get("https://api.weixin.qq.com/sns/jscode2session")
                .form("appid", "")
                .form("secret", "")
                .form("grant_type", "authorization_code")
                .form("js_code", code)
                .execute();

        String responseBody = httpResponse.body();
        log.debug("小程序登录结果：{}",responseBody);

        WxAppletLoginDto loginDto = JSON.parseObject(responseBody, WxAppletLoginDto.class);

        if(Objects.equals(loginDto.getErrCode(), 0)){
            return loginDto;
        }
        throw new RuntimeException(loginDto.getErrMsg());
    }


    /**
     * 调用接口获取手机号
     *
     * @param code 小程序中通过`getPhoneNumber`或 `getRealtimePhoneNumber` 返回的code
     * @return 返回手机号 {@link String}
     */
    public  String getPhoneNumber(String code){

        HttpResponse httpResponse = HttpRequest.post(MpAppletApiUrl.User.GET_PHONE_NUMBER_URL)
                .form("access_token", "ACCESS_TOKEN")
                .form("code", code)
                .execute();

        String responseBody = httpResponse.body();
        log.debug("小程序获取手机号结果：{}",responseBody);

        WxAppletPhoneDto phoneDto = JSON.parseObject(responseBody, WxAppletPhoneDto.class);
        if(Objects.equals(phoneDto.getErrCode(), 0)){
            return  phoneDto.getPhoneInfo().getPurePhoneNumber();
        }

        throw new RuntimeException(phoneDto.getErrMsg());
    }

    /**
     * 获取accessToken, 默认过期时间为 :<b>7200秒</b> 需定时刷新
     * <p>有效期內，查询，token不会更新。</p>
     * @return 返回accessToken
     */
    public String getAccessToken(){

        String body = HttpRequest.post("https://api.weixin.qq.com/cgi-bin/stable_token")
                .form("grant_type", "client_credential")
                .form("appid", "")
                .form("secret", "")
                .form("force_refresh", false) // 普通模式，access_token 有效期内重复调用该接口不会更新 access_token
                .execute()
                .body();

        log.debug("获取accessToken：{}",body);
        return JSON.parseObject(body).getString("access_token");
    }
}
