package org.nkk.web.autoconfigure.encrypt.enums;

/**
 * 加密方式
 */
public enum EncryptMethod {

    /**
     *
     * <a href="https://tool.hiofd.com/sm4-encrypt-online/">SM4(对称加密)在线测试工具</a>
     */
    SM4,

    /**
     *
     * <a href="https://tool.lvtao.net/des">DES(对称加密)在线测试工具</a>
     */
    DES,

    /**
     *
     * <a href="https://tool.lvtao.net/aes">AES(对称加密)在线测试工具</a>
     */
    AES,

    /**
     * [未实现] RSA、DSA (为非对称加密：私钥加密，公钥解密)
     *
     *  <a href="https://tool.lvtao.net/rsa">RSA在线测试工具</a>
     */
    RSA
}
