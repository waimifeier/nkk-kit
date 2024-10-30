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

    private final EncryptProperties encryptProperties;

    @Override
    public String encryptHex(String data) {
        DES aes = new DES(Mode.ECB.name(), "PKCS7Padding", encryptProperties.getKey().getBytes());
        return aes.encryptHex(data);
    }

    @Override
    public String decryptStr(String data) {
        DES aes = new DES(Mode.ECB.name(), "PKCS7Padding", encryptProperties.getKey().getBytes());
        return aes.decryptStr(data, CharsetUtil.CHARSET_UTF_8);
    }


}
