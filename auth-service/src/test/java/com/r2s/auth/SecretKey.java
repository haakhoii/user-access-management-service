package com.r2s.auth;

import javax.crypto.KeyGenerator;
import java.util.Base64;

public class SecretKey {
    public static void main(String[] args) throws Exception {
        KeyGenerator key = KeyGenerator.getInstance("HmacSHA512");
        key.init(512);

        javax.crypto.SecretKey secretKey = key.generateKey();

        String base64 = Base64.getEncoder().encodeToString(secretKey.getEncoded());

        System.out.println("GENERATED SIGNER KEY:");
        System.out.println(base64);
    }
}
