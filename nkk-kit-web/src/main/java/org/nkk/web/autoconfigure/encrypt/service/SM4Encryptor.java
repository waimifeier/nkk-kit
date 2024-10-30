package org.nkk.web.autoconfigure.encrypt.service;


import cn.hutool.core.util.CharsetUtil;
import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.symmetric.SM4;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nkk.web.autoconfigure.encrypt.EncryptProperties;

@Slf4j
@RequiredArgsConstructor
public class SM4Encryptor implements Encryptor {

    /**
     * key建议：秘钥为16位</br>
     */
    private final EncryptProperties config;

    @Override
    public String encryptBase64(String data) {
        String key = config.getKey(), iv = config.getIv();
        SM4 sm4 = new SM4(Mode.CBC, Padding.PKCS5Padding, key.getBytes(), iv.getBytes());
        return sm4.encryptBase64(data, CharsetUtil.CHARSET_UTF_8);
    }

    @Override
    public String decryptStr(String data) {
        String key = config.getKey(), iv = config.getIv();
        SM4 sm4 = new SM4(Mode.CBC, Padding.PKCS5Padding, key.getBytes(), iv.getBytes());
        return sm4.decryptStr(data, CharsetUtil.CHARSET_UTF_8);
    }


}
