package com.scanner.demo.user.dto;

import com.scanner.demo.user.entity.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AdminUserResponse {
    private Integer user_seq;
    private String user_id; // 이름
    private String email;
    private String role;
    private LocalDateTime created_at; // 프론트의 snake_case 매핑

    public AdminUserResponse(User user) {
        this.user_seq = user.getUserSeq();
        this.user_id = user.getUserId(); // DB의 name 필드를 프론트의 user_id로 매핑
        this.email = user.getUserId(); // DB의 userId(이메일) 필드를 매핑
        this.role = user.getRole();
        this.created_at = user.getCreatedAt();
    }
}