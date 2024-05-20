package com.egov.icops_integrationkerala.util;

import com.egov.icops_integrationkerala.model.AuthToken;
import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtil {

    private final String secretKey = "mySecretKey";

    private final String TOKEN_HEADER = "Authorization";

    private final String TOKEN_PREFIX = "Bearer ";

    private long tokenExpirationTime = 30*60*1000;

    private final JwtParser jwtParser;

    public JwtUtil(){
        this.jwtParser = Jwts.parser().setSigningKey(secretKey);
    }

    public AuthToken generateToken(String serviceName) {
        Date tokenCreateTime = new Date();
        Date tokenValidity = new Date(tokenCreateTime.getTime() + TimeUnit.MINUTES.toMillis(tokenExpirationTime));
        String accessToken =  Jwts.builder()
                .setSubject(serviceName)
                .setExpiration(tokenValidity)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
        return AuthToken.builder()
                .accessToken(accessToken).expiresIn((int) tokenExpirationTime)
                .tokenType("Bearer").scope("").build();
    }

    public String getServiceNameFromToken(String token) {
        Claims claims = jwtParser.setSigningKey(secretKey).parseClaimsJws(token).getBody();
        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            jwtParser.setSigningKey(secretKey).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
