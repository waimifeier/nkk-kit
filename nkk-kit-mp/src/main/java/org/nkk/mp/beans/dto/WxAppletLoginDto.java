package org.nkk.mp.beans.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class WxAppletLoginDto implements Serializable {

    private static final long serialVersionUID = 6689040014027161007L;

    /**
     * 用户唯一标识
     */
    private String openid;

    /**
     * 会话密钥
     */
    @JSONField(name = "session_key")
    private String sessionKey;

    /**
     * 用户在开放平台的唯一标识符，若当前小程序已绑定到微信开放平台账号下会返回，
     */
    @JSONField(name = "unionid")
    private String unionId;

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

}
