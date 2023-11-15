package org.nkk.web.utils;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * 获取地址类
 * 
 * @author admin
 */
public class AddressUtils
{
    private static final Logger log = LoggerFactory.getLogger(AddressUtils.class);

    // IP地址查询
    public static final String IP_URL = "http://whois.pconline.com.cn/ipJson.jsp";

    // 未知地址
    public static final String UNKNOWN = "XX XX";

    /**
     * 获取知道外网Ip的真实归属地
     * @param ip 外网IP
     * @return 真实归属地
     */
    public static String getRealAddressByIP(String ip)
    {
        // 内网不查询
        if (IpUtils.internalIp(ip))
        {
            return "内网IP";
        }
        try
        {
            String rspStr = HttpRequest.get(IP_URL)
                            .form("ip", ip)
                            .form("json", true)
                            .execute().body();

            if (StringUtils.isEmpty(rspStr))
            {
                log.error("获取地理位置异常 {}", ip);
                return UNKNOWN;
            }
            JSONObject obj = JSONObject.parseObject(rspStr);
            String region = obj.getString("pro");
            String city = obj.getString("city");
            return String.format("%s %s", region, city);
        }
        catch (Exception e)
        {
            log.error("获取地理位置异常 {}", ip);
        }
        return UNKNOWN;
    }
}