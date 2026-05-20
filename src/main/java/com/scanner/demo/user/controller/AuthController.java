package com.scanner.demo.user.controller;

import com.scanner.demo.user.dto.LoginRequest;
import com.scanner.demo.user.dto.TokenResponse;
import com.scanner.demo.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            TokenResponse tokenResponse = authService.login(request);
            return ResponseEntity.ok(tokenResponse);
        } catch (IllegalArgumentException e) {
            // 보안 가이드라인에 따라 아이디 존재 여부를 노출하지 않도록 공통 에러 메시지 반환
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }
}