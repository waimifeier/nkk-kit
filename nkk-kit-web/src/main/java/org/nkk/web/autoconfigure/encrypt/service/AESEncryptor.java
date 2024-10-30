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

    private final EncryptProperties config;

    @Override
    public String encryptBase64(String data) {
        String key = config.getKey(), iv = config.getIv();
        AES aes = new AES(Mode.CBC.name(), "PKCS7Padding", key.getBytes(), iv.getBytes());
        return aes.encryptBase64(data, CharsetUtil.CHARSET_UTF_8);
    }

    @Override
    public String decryptStr(String data) {
        String key = config.getKey(), iv = config.getIv();
        AES aes = new AES(Mode.CBC.name(), "PKCS7Padding", key.getBytes(), iv.getBytes());
        return aes.decryptStr(data, CharsetUtil.CHARSET_UTF_8);
    }


}
