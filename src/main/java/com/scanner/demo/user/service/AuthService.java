package com.scanner.demo.user.service;

import com.scanner.demo.utils.security.JwtUtil;
import com.scanner.demo.user.dto.LoginRequest;
import com.scanner.demo.user.dto.TokenResponse;
import com.scanner.demo.user.entity.User;
import com.scanner.demo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public TokenResponse login(LoginRequest request) {
        // 1. 아이디로 사용자 조회
        User user = userRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다."));

        // 2. 비밀번호 검증 (BCrypt 암호화 대조)
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        // 3. 서명이 포함된 안전한 JWT 토큰 생성 및 반환
        String token = jwtUtil.createToken(user.getUserId(), user.getRole());
        return new TokenResponse(token, "Bearer");
    }
}