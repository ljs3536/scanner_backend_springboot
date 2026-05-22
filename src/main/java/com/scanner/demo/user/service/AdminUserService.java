package com.scanner.demo.user.service;

import com.scanner.demo.user.dto.AdminUserRequest;
import com.scanner.demo.user.dto.AdminUserResponse;
import com.scanner.demo.user.entity.User;
import com.scanner.demo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // SecurityConfig에 등록된 빈 사용

    /**
     * 1. 등록된 모든 회원 목록 조회
     */
    @Transactional(readOnly = true)
    public List<AdminUserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(AdminUserResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * 2. 관리자에 의한 강제 계정 생성
     */
    @Transactional
    public void createUser(AdminUserRequest request) {
        // 이메일(DB의 userId 필드) 중복 검사
        if (userRepository.findByUserId(request.getUser_id()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 새 User 엔티티 생성 및 저장
        User newUser = User.builder()
                .userId(request.getUser_id())
                .password(passwordEncoder.encode(request.getPassword())) // 비밀번호 암호화 필수
                .name(request.getUser_id()) // 프론트의 user_id를 name으로 매핑
                .role(request.getRole())
                .status("ACT") // 관리자가 직접 만들었으므로 즉시 활성화
                // (필요 시 다른 필드 기본값 설정)
                .build();

        userRepository.save(newUser);
    }
}