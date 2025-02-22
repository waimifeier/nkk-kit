package org.nkk.web.autoconfigure.encrypt.service;


import cn.hutool.core.util.CharsetUtil;
import cn.hutool.crypto.Mode;
import cn.hutool.crypto.symmetric.DES;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nkk.web.autoconfigure.encrypt.EncryptProperties;

@Slf4j
@RequiredArgsConstructor
public class DESEncryptor implements Encryptor {

    /**
     * 建议：秘钥为8位
     */

    private final EncryptProperties config;

    @Override
    public String encryptBase64(String data) {
        String key = config.getKey(), iv = config.getIv();
        DES aes = new DES(Mode.CBC.name(), "PKCS7Padding", key.getBytes(), iv.getBytes());
        return aes.encryptBase64(data, CharsetUtil.CHARSET_UTF_8);
    }

    @Override
    public String decryptStr(String data) {
        String key = config.getKey(), iv = config.getIv();
        DES aes = new DES(Mode.CBC.name(), "PKCS7Padding", key.getBytes(), iv.getBytes());
        return aes.decryptStr(data, CharsetUtil.CHARSET_UTF_8);
    }


}
