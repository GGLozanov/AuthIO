package com.example.authio.utils;

import android.util.Base64;

import com.example.authio.BuildConfig;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;

public class TokenUtils {
    /**
     * Decodes a given JWT and returns its payload
     * @param jwtToken - JWT whose payload is to be decoded and returned
     * @return
     */
    public static int getTokenUserIdFromPayload(String jwtToken) {
        RSAPublicKey rsaPublicKey;
        try {
            byte[] decodedPublicKey = Base64.decode(BuildConfig.JWT_PUBLIC_KEY, Base64.DEFAULT);
            X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(decodedPublicKey); // ASN.1 encoding of public key
            KeyFactory keyFactory = KeyFactory.getInstance("RSA"); // key factory for generating RSA keys
            rsaPublicKey = (RSAPublicKey) keyFactory.generatePublic(keySpecX509);
        } catch(NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Bad key parsing");
        }

        JwtParser parser = Jwts.parserBuilder()
                .setSigningKey(rsaPublicKey)
                .build();

        return parser.parseClaimsJws(jwtToken)
                .getBody()
                .get("userId", Integer.class);
    }
}
