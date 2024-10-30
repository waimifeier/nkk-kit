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
    private final EncryptProperties encryptProperties;

    @Override
    public String encryptHex(String data) {
        SM4 sm4 = new SM4(Mode.CBC, Padding.PKCS5Padding, encryptProperties.getKey().getBytes(), encryptProperties.getIv().getBytes());
        return sm4.encryptHex(data);
    }

    @Override
    public String decryptStr(String data) {
        SM4 sm4 = new SM4(Mode.CBC, Padding.PKCS5Padding, encryptProperties.getKey().getBytes(), encryptProperties.getIv().getBytes());
        return sm4.decryptStr(data, CharsetUtil.CHARSET_UTF_8);
    }


}
