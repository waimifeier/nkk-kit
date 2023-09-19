package org.nkk.mp.service.applet;

public interface WxAppletService {


    /**
     *  获取接口访问凭证 默认过期时间为 :<b>7200秒</b> 需定时刷新
     *  <p>有效期內，查询，token不会更新。</p>
     * @param refresh 是否强制刷新
     * @return 返回凭证 {@link String}
     */
    String accessToken(Boolean refresh);
}
