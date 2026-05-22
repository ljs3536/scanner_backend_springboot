package com.scanner.demo.user.controller;

import com.scanner.demo.user.dto.AdminUserRequest;
import com.scanner.demo.user.dto.AdminUserResponse;
import com.scanner.demo.user.service.AdminUserService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    /**
     * 전체 회원 목록 조회
     * 권한: ADMIN 만 가능
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminUserResponse>> getAllUsers() {
        List<AdminUserResponse> users = adminUserService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * 신규 계정 강제 생성 (관리자 발급)
     * 권한: ADMIN 만 가능
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUser(@RequestBody AdminUserRequest request) {
        try {
            adminUserService.createUser(request);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            // 중복 이메일 등 예외 발생 시 프론트엔드가 요구하는 JSON 에러 규격으로 반환
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // 단순 에러 응답용 내부 클래스
    @Getter
    private static class ErrorResponse {
        private final String detail;
        public ErrorResponse(String detail) { this.detail = detail; }
    }
}