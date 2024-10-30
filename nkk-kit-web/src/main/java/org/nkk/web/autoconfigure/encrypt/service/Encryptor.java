package org.nkk.web.autoconfigure.encrypt.service;

/**
 * 对称加密接口
 */
public interface Encryptor {

    /**
     * 加密十六进制
     *
     * @param data 数据
     * @return {@link String}
     */
    String encryptHex(String data);

    /**
     * 解密str
     *
     * @param data 密文数据
     * @return {@link String}
     */
    String decryptStr(String data);
}
