package org.nkk.mp.service.applet;

import cn.hutool.http.HttpRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nkk.mp.service.applet.impl.WxAppletUser;

@Slf4j
@RequiredArgsConstructor
public class WxAppletTemplate {

    private final WxAppletUser wxAppletUser;

    //new LambdaQueryWrapper<>()


    /**
     * 获取接口调用凭证
     * @param refresh 是否强制刷新
     * @return 返回接口调用凭据 {@link String}
     */
    public String getAccessToken(Boolean refresh){
        return null;
    }


    /**
     * 获取接口调用凭证
     *
     * @return 返回接口调用凭据 {@link String}
     */
    public String getAccessToken(){
        return getAccessToken(false);
    }


    /**
     * 获取<u>小程序用户</u>相关接口
     * @return
     */
    public WxAppletUser user(){
        return this.wxAppletUser;
    }

    /**
     * 获取<u>小程序支付</u>相关接口
     * @return
     */
    public WxAppletUser pay(){
        return new WxAppletUser();
    }

    /**
     * 获取<u>小程序二维码</u>相关接口
     * @return
     */
    public WxAppletUser qrcode(){
        return new WxAppletUser();
    }

    /**
     * 获取<u>即时配送</u>相关接口
     * @return
     */
    public WxAppletUser instantDelivery(){
        return new WxAppletUser();
    }

    /**
     * 发送Get请求
     * @param url 请求地址
     * @return 返回请求对象 {@link HttpRequest}
     */
    public HttpRequest get(String url){
        return HttpRequest.get(url).form("access_token",this.getAccessToken());
    }

    /**
     * 发送Post请求
     * @param url 请求地址
     * @return 返回请求对象 {@link HttpRequest}
     */
    public HttpRequest post(String url){
        return HttpRequest.post(url).form("access_token",this.getAccessToken());
    }

    /**
     * 发送put请求
     * @param url 请求地址
     * @return 返回请求对象 {@link HttpRequest}
     */
    public HttpRequest put(String url){
        return HttpRequest.put(url).form("access_token",this.getAccessToken());
    }

    /**
     * 发送delete请求
     * @param url 请求地址
     * @return 返回请求对象 {@link HttpRequest}
     */
    public HttpRequest delete(String url){
        return HttpRequest.delete(url).form("access_token",this.getAccessToken());
    }


    /**
     * 发送patch请求
     * @param url 请求地址
     * @return 返回请求对象 {@link HttpRequest}
     */
    public HttpRequest patch(String url){
        return HttpRequest.patch(url).form("access_token",this.getAccessToken());
    }


}
