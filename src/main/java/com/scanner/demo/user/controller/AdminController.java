package com.scanner.demo.user.controller;

import com.scanner.demo.user.dto.UserCreateRequest;
import com.scanner.demo.user.entity.User;
import com.scanner.demo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 핵심: 이 API는 권한(Role)이 ADMIN인 사람만 호출할 수 있음!
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") 
    public ResponseEntity<?> createUser(@RequestBody UserCreateRequest request) {
        
        // 1. 이메일 중복 체크
        if (userRepository.findByUserId(request.getUserId()).isPresent()) {
            return ResponseEntity.badRequest().body("이미 존재하는 이메일입니다.");
        }

        // 2. 이메일을 아이디(userId)로 그대로 사용하고, 비밀번호는 BCrypt 암호화
        User newUser = User.builder()
                .userId(request.getUserId())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : "USER") // 기본값 USER
                .build();

        userRepository.save(newUser);
        
        return ResponseEntity.ok("사용자 생성이 완료되었습니다.");
    }
}