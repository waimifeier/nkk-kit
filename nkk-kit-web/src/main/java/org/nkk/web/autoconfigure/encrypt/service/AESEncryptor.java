package org.nkk.web.autoconfigure.encrypt.service;


import cn.hutool.core.util.CharsetUtil;
import cn.hutool.crypto.Mode;
import cn.hutool.crypto.symmetric.AES;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nkk.web.autoconfigure.encrypt.EncryptProperties;

/**
 * <a href="https://config.net.cn/tools/AES.html">AES加密/解密</a>
 * 最流行的对称加密算法
 */
@Slf4j
@RequiredArgsConstructor
public class AESEncryptor implements Encryptor {
    /**
     * 秘钥为16/24/32位
     */

    private final EncryptProperties encryptProperties;

    @Override
    public String encryptHex(String data) {
        AES aes = new AES(Mode.ECB.name(), "PKCS7Padding", encryptProperties.getKey().getBytes());
        return aes.encryptHex(data);
    }

    @Override
    public String decryptStr(String data) {
        AES aes = new AES(Mode.ECB.name(), "PKCS7Padding", encryptProperties.getKey().getBytes());
        return aes.decryptStr(data, CharsetUtil.CHARSET_UTF_8);
    }


}
