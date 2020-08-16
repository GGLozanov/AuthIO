package com.example.authio.utils;

import com.example.authio.BuildConfig;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;

public class TokenUtils {
    /**
     * Decodes a given JWT and returns its payload
     * @param jwtToken - JWT whose payload is to be decoded and returned
     * @return
     */
    public static int getTokenUserIdFromPayload(String jwtToken) {
        JwtParser parser = Jwts.parserBuilder()
                .setSigningKey(BuildConfig.JWT_PUBLIC_KEY)
                .build();

        return parser.parseClaimsJws(jwtToken)
                .getBody()
                .get("userId", Integer.class);
    }
}
