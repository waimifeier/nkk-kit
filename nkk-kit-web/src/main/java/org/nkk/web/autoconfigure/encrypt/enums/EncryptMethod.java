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
     *
     * <a href="https://tool.lvtao.net/des">DES在线测试工具</a>
     */
    DES,

    /**
     *
     * <a href="https://tool.lvtao.net/aes">AES在线测试工具</a>
     */
    AES,

    /**
     * [未实现] RSA、DSA (为非对称加密：私钥加密，公钥解密)
     *
     *  <a href="https://tool.lvtao.net/rsa">RSA在线测试工具</a>
     */
    RSA
}
