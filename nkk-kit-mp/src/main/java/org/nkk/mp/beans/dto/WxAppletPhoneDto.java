package org.nkk.mp.beans.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

/**
 * 小程序手机号信息
 */
@Data
public class WxAppletPhoneDto implements Serializable {

    private static final long serialVersionUID = -3360606438289230302L;

    /**
     * 错误码
     */
    @JSONField(name = "errcode")
    private Integer errCode;

    /**
     * 错误信息
     */
    @JSONField(name = "errmsg")
    private String errMsg;

    /**
     * 手机号信息
     */
    @JSONField(name = "phone_info")
    private PhoneInfo phoneInfo;


    @Data
    public static class PhoneInfo implements Serializable {

        private static final long serialVersionUID = 6975356014534203405L;
        /**
         * 手机号
         */
        private String phoneNumber;

        /**
         * 水印
         */
        private Watermark watermark;

        /**
         * 纯粹电话号码
         */
        private String purePhoneNumber;

        /**
         * 国家代码
         */
        private Integer countryCode;


        @Data
        public static class Watermark implements Serializable {

            private static final long serialVersionUID = -501043947368727871L;

            private String appid;

            private Integer timestamp;


        }
    }
}
