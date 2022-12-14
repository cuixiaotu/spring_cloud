//package com.xiaotu.rsa;
//
//import cn.hutool.core.util.CharsetUtil;
//import cn.hutool.core.util.StrUtil;
//import cn.hutool.crypto.asymmetric.KeyType;
//import cn.hutool.crypto.asymmetric.RSA;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//
//@SpringBootTest
//public class RsaTest {
//
//    @Test
//    public void testRsa() throws Exception {
//        RSA rsa = new RSA();
//
//        //获得私钥
//        rsa.getPrivateKey();
//        rsa.getPrivateKeyBase64();
//        //获得公钥
//        rsa.getPublicKey();
//        rsa.getPublicKeyBase64();
//
//        //公钥加密，私钥解密
//        byte[] encrypt = rsa.encrypt(StrUtil.bytes("我是一段测试aaaa", CharsetUtil.CHARSET_UTF_8), KeyType.PublicKey);
//        byte[] decrypt = rsa.decrypt(encrypt, KeyType.PrivateKey);
//
//        //Junit单元测试
//        //Assert.assertEquals("我是一段测试aaaa", StrUtil.str(decrypt, CharsetUtil.CHARSET_UTF_8));
//
//        //私钥加密，公钥解密
//        byte[] encrypt2 = rsa.encrypt(StrUtil.bytes("我是一段测试aaaa", CharsetUtil.CHARSET_UTF_8), KeyType.PrivateKey);
//        byte[] decrypt2 = rsa.decrypt(encrypt2, KeyType.PublicKey);
//
//    }
//
//
//}
