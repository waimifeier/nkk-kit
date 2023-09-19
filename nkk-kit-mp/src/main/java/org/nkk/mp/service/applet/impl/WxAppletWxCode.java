package org.nkk.mp.service.applet.impl;

import cn.hutool.http.HttpRequest;

import java.awt.*;

public class WxAppletWxCode {

    public static Image createQr(){
        HttpRequest.post("https://api.weixin.qq.com/cgi-bin/wxaapp/createwxaqrcode?access_token=ACCESS_TOKEN")
                .execute().body();

        return null;
    }


}
