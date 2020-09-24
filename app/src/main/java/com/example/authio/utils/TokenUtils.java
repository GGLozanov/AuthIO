package com.example.authio.utils;

import android.util.Base64;
import android.util.Log;

import com.example.authio.BuildConfig;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;

public class TokenUtils {
    /**
     * Decodes a given JWT and returns the value of the 'userId' field in the payload
     * @param jwtToken - JWT whose payload is to be decoded and parsed
     * @return
     */
    public static int getTokenUserIdFromPayload(String jwtToken) throws ExpiredJwtException, MalformedJwtException {
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

    public static int getTokenUserIdFromStoredTokens(PrefConfig prefConfig) { // TODO: rename; absolutely horrid name
        int authId;
        try {
            authId = TokenUtils.getTokenUserIdFromPayload(prefConfig.readToken());
        } catch(ExpiredJwtException | MalformedJwtException e) {
            Log.w("UserRepository" , "getUsers —> User's token is expired & id cannot be retrieved. Attempting to get id from refresh token.");
            try {
                authId = TokenUtils.getTokenUserIdFromPayload(prefConfig.readRefreshToken());
            } catch(ExpiredJwtException | MalformedJwtException x) {
                Log.w("UserRepository" , "getUsers —> User needs to reauth. " +
                        "This method will suspend and the user will be logged out after the REAUTH_FLAG response has been handled from the network entity.");
                return 0;
            }
        }
        return authId;
    }
}
