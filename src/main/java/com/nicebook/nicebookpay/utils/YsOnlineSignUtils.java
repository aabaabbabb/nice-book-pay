package com.nicebook.nicebookpay.utils;


import com.eptok.yspay.opensdkjava.util.Base64Utils;
import com.eptok.yspay.opensdkjava.util.StringUtil;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class YsOnlineSignUtils {
    private static Logger log = Logger.getLogger("YsOnlineSignUtils");
    public static Map<String, Object> certMap = new ConcurrentHashMap();
    private static final int CACHE_SIZE = 2048;
    public static final String ALLCHAR = "0123456789ABCDEF";
    public static final String ALGORITHM = "SHA1withRSA";
    private static final String ENCODE = "UTF-8";

    public static Map<String, String> paraFilter(Map<String, String> sArray) {
        Map<String, String> result = new TreeMap();
        if (sArray != null && sArray.size() > 0) {
            for(String key : sArray.keySet()) {
                String value = (String)sArray.get(key);
                if (value != null && !StringUtil.isEmpty(value) && !key.equalsIgnoreCase("sign")) {
                    result.put(key, value);
                }
            }
            return result;
        } else {
            return result;
        }
    }

    public static String getSignContent(Map<String, String> sortedParams) {
        StringBuffer content = new StringBuffer();
        List<String> keys = new ArrayList(sortedParams.keySet());
        Collections.sort(keys);
        int index = 0;

        for(int i = 0; i < keys.size(); ++i) {
            String key = (String)keys.get(i);
            if (!"sign".equals(key)) {
                String value = (String)sortedParams.get(key);
                if (StringUtil.areNotEmpty(new String[]{key, value})) {
                    content.append((index == 0 ? "" : "&") + key + "=" + value);
                    ++index;
                }
            }
        }

        return content.toString();
    }

    public static String rsaSign(String content, String charset, String privateCerPwd, String privateCerPath, String rsaType) throws Exception {
        try {
            PrivateKey priKey = getPrivateKeyFromPKCS12(privateCerPwd, privateCerPath);
            Signature signature = Signature.getInstance(rsaType);
            signature.initSign(priKey);
            if (StringUtil.isEmpty(charset)) {
                signature.update(content.getBytes());
            } else {
                signature.update(content.getBytes(charset));
            }

            byte[] signed = signature.sign();
            String sign = new String(Base64Utils.encode(signed));
            return sign;
        } catch (Exception e) {
            log.info("数据签名异常" + e);
            throw new Exception("RSAcontent = " + content + "; charset = " + charset, e);
        }
    }

    public static boolean rsaCheckContent(Map<String, String> params, String sign, String charset, String publicKeyPath) throws Exception {
        String content = StringUtil.createLinkString(paraFilter(params));
        return rsaCheckContent(content, sign, charset, publicKeyPath);
    }

    public static boolean rsaCheckContent(String content, String sign, String charset, String publicKeyPath) throws Exception {
        boolean bFlag = false;
        Signature signetcheck = Signature.getInstance("SHA1withRSA");
        signetcheck.initVerify(getPublicKeyFromCert(publicKeyPath));
        signetcheck.update(content.getBytes(charset));
        if (signetcheck.verify(Base64Utils.decode(sign))) {
            bFlag = true;
        }

        return bFlag;
    }

    public static boolean rsaCheckContent(String content, String sign, String charset, PublicKey publicKey, String rsaType) throws Exception {
        boolean bFlag = false;
        Signature signetcheck = Signature.getInstance(rsaType);
        signetcheck.initVerify(publicKey);
        signetcheck.update(content.getBytes(charset));
        if (signetcheck.verify(Base64Utils.decode(sign))) {
            bFlag = true;
        }

        return bFlag;
    }

    public static boolean validateFileSign(FileInputStream fileInputStream, String sign, InputStream publicCertFileInputStream, String publicKeyPath) throws Exception {
        boolean result = false;
        PublicKey publicKey = getPublicKeyFromCert(publicKeyPath);
        Signature signature = Signature.getInstance("SHA1withRSA");
        signature.initVerify(publicKey);
        byte[] decodedSign = Base64Utils.decode(sign);
        byte[] cache = new byte[2048];
        int nRead = 0;

        while((nRead = fileInputStream.read(cache)) != -1) {
            signature.update(cache, 0, nRead);
        }

        fileInputStream.close();
        result = signature.verify(decodedSign);
        return result;
    }

    public static PublicKey getPublicKeyFromCert(String publicKeyPath) throws Exception {
        PublicKey pubKey = (PublicKey)certMap.get(publicKeyPath);
        if (pubKey != null) {
            return pubKey;
        } else {
            File privateKeyFile = new File(publicKeyPath);
            if (!privateKeyFile.exists()) {
                throw new Exception("publickeyFile is not found...");
            } else {
                FileInputStream ins = null;

                try {
                    CertificateFactory cf = CertificateFactory.getInstance("X.509");
                    ins = new FileInputStream(publicKeyPath);
                    Certificate cac = cf.generateCertificate(ins);
                    pubKey = cac.getPublicKey();
                    certMap.put(publicKeyPath, pubKey);
                } catch (Exception e) {
                    if (ins != null) {
                        ins.close();
                    }

                    throw e;
                } finally {
                    if (ins != null) {
                        ins.close();
                    }

                }

                return pubKey;
            }
        }
    }

    public static PrivateKey getPrivateKeyFromPKCS12(String password, String privateCerPath) throws Exception {
        PrivateKey priKey = null;
        if (certMap.get(privateCerPath) != null) {
            priKey = (PrivateKey)certMap.get(privateCerPath);
            if (priKey != null) {
                return priKey;
            }
        }

        KeyStore keystoreCA = KeyStore.getInstance("PKCS12");
        File privateKeyFile = new File(privateCerPath);
        if (!privateKeyFile.exists()) {
            throw new Exception("privateKeyFile is not found...");
        } else {
            InputStream ins = new FileInputStream(privateKeyFile);

            try {
                keystoreCA.load(ins, password.toCharArray());
                Enumeration<?> aliases = keystoreCA.aliases();
                String keyAlias = null;
                if (aliases != null) {
                    while(aliases.hasMoreElements()) {
                        keyAlias = (String)aliases.nextElement();
                        priKey = (PrivateKey)keystoreCA.getKey(keyAlias, password.toCharArray());
                        if (priKey != null) {
                            certMap.put(privateCerPath, priKey);
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                if (ins != null) {
                    ins.close();
                }

                throw e;
            } finally {
                if (ins != null) {
                    ins.close();
                }

            }

            return priKey;
        }
    }

    public static byte[] encrypt(Key key, byte[] data) throws Exception {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(1, key);
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw e;
        }
    }

    public static byte[] decrypt(Key Key, byte[] data) throws Exception {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(2, Key);
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw e;
        }
    }

    public static String encrypt(String data, String key) throws Exception {
        byte[] bt = encrypt(data.getBytes("UTF-8"), key.getBytes("UTF-8"));
        String strs = Base64Utils.encode(bt);
        return strs;
    }

    private static byte[] encrypt(byte[] data, byte[] key) throws Exception {
        SecureRandom sr = new SecureRandom();
        DESKeySpec dks = new DESKeySpec(key);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey securekey = keyFactory.generateSecret(dks);
        Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        cipher.init(1, securekey, sr);
        return cipher.doFinal(data);
    }

    public static String getSignStr(Map<String, String> map) {
        List<String> keys = new ArrayList(map.keySet());
        Collections.sort(keys);
        StringBuilder sb = new StringBuilder();

        for(String key : keys) {
            if (!"sign".equals(key)) {
                sb.append(key).append("=");
                sb.append((String)map.get(key));
                sb.append("&");
            }
        }

        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }

        return sb.toString();
    }

    public static String getPublicStrKeyFromCert(String publicKeyPath) throws Exception {
        FileInputStream ins = null;

        String var5;
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            ins = new FileInputStream(publicKeyPath);
            Certificate cac = cf.generateCertificate(ins);
            PublicKey pubKey = cac.getPublicKey();
            var5 = Base64Utils.encode(pubKey.getEncoded());
        } catch (Exception e) {
            if (ins != null) {
                ins.close();
            }

            throw e;
        } finally {
            if (ins != null) {
                ins.close();
            }

        }

        return var5;
    }

    public static String sign(Map<String, String> params, String privateCerPwd, String privateCerPath) throws Exception {
        Map<String, String> stringStringMap = paraFilter(params);
        String signContent = getSignContent(stringStringMap);
        String sign = rsaSign(signContent, (String)params.get("charset"), privateCerPwd, privateCerPath, "SHA1withRSA");
        return sign;
    }
}
