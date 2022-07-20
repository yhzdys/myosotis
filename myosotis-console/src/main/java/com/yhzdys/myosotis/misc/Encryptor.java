package com.yhzdys.myosotis.misc;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class Encryptor {

    public static String md5(String source) {
        return DigestUtils.md5Hex(source).toLowerCase();
    }

    /**
     * 生成rsa公私钥
     *
     * @return keyPair @left=公钥 @right=私钥
     */
    public static Pair<String, String> genRsaKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(512);
            KeyPair keyPair = generator.genKeyPair();
            String publicKey = Base64.encodeBase64String(keyPair.getPublic().getEncoded());
            String privateKey = Base64.encodeBase64String(keyPair.getPrivate().getEncoded());
            return Pair.of(publicKey, privateKey);
        } catch (Exception e) {
            throw new BizException(e.getMessage());
        }
    }

    /**
     * 通过公钥加密
     *
     * @param source    明文
     * @param publicKey 公钥
     * @return 密文
     */
    public static String encryptByPublicKey(String source, String publicKey) {
        try {
            PublicKey pubKey = getPublicKey(publicKey);
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, pubKey);
            byte[] enBytes = cipher.doFinal(source.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeBase64String(enBytes);
        } catch (Exception e) {
            throw new BizException(e.getMessage());
        }
    }

    /**
     * 通过私钥解密
     *
     * @param source     加密字符串
     * @param privateKey 私钥
     * @return 明文
     */
    public static String decryptByPrivateKey(String source, String privateKey) {
        try {
            PrivateKey priKey = getPrivateKey(privateKey);
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, priKey);
            byte[] deBytes = cipher.doFinal(Base64.decodeBase64(source));
            return new String(deBytes);
        } catch (Exception e) {
            throw new BizException(e.getMessage());
        }
    }

    /**
     * 从公钥字符串中获取公钥
     *
     * @param key Base64的公钥字符串
     * @return 公钥
     */
    private static PublicKey getPublicKey(String key) {
        try {
            byte[] keyBytes = Base64.decodeBase64(key);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            throw new BizException(e.getMessage());
        }
    }

    /**
     * 从私钥字符串中获取私钥
     *
     * @param key Base64的私钥字符串
     * @return 私钥
     */
    private static PrivateKey getPrivateKey(String key) {
        try {
            byte[] keyBytes = Base64.decodeBase64(key.getBytes(StandardCharsets.UTF_8));
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new BizException(e.getMessage());
        }
    }
}
