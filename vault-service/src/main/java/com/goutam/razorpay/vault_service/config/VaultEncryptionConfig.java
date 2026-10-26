package com.goutam.razorpay.vault_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.encrypt.AesBytesEncryptor;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;

import javax.crypto.spec.SecretKeySpec;

@Configuration
public class VaultEncryptionConfig {


    public static BytesEncryptor panEncrypter(byte[] dek) {
        SecretKeySpec decKey = new SecretKeySpec(dek, "AES");
        return new AesBytesEncryptor(decKey, KeyGenerators.secureRandom(12),
                AesBytesEncryptor.CipherAlgorithm.GCM);
    }


//    @Bean
//    public BytesEncryptor dekEncryptor() {
//
//        byte[] masterKeyBytes = Base64.getDecoder().decode(masterKey);
//        SecretKeySpec masterKeySpec = new SecretKeySpec(masterKeyBytes, "AES");
//        return new AesBytesEncryptor(masterKeySpec, KeyGenerators.secureRandom(12),
//                AesBytesEncryptor.CipherAlgorithm.GCM);
//    }
}
