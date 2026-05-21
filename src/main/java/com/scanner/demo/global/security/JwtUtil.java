package com.scanner.demo.global.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long accessTokenValidityInMilliseconds = 1000 * 60 * 60; // 1시간

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        // 프로퍼티에서 읽어온 시크릿을 기반으로 강력한 HMAC SHA 서명 키 객체 생성
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // 토큰 발급 로직
    public String createToken(String userId, String role) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + accessTokenValidityInMilliseconds);

        return Jwts.builder()
                .subject(userId)
                .claim("role", role) // 관리자, 사용자 권한 정보 삽입
                .issuedAt(now)
                .expiration(validity)
                .signWith(secretKey) // 강력한 서명 추가
                .compact();
    }

    // 토큰 검증 로직 (CWE-345 방어를 위해 서명/알고리즘 엄격 확인)
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(secretKey) // 여기서 서명 값이 다르면 즉각 Exception이 발생함
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            // 서명 변조, 만료, 포맷 오류 시 모두 false 반환 (필요 시 세부 로깅)
            return false;
        }
    }

    // 토큰에서 아이디 추출
    public String getUserIdFromToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // 토큰에서 권한 추출
    public String getRoleFromToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }
}