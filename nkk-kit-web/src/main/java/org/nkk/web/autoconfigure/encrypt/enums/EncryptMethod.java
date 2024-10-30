package org.nkk.web.autoconfigure.encrypt.enums;

/**
 * 加密方式
 */
public enum EncryptMethod {

    /**
     * 国密算法 SM4 (对称加密)
     */
    SM4,

    /**
     * DES
     */
    DES,

    /**
     * AES
     */
    AES,

    /**
     * RSA、DSA (为非对称加密：私钥加密，公钥解密)
     */
    RSA
}
