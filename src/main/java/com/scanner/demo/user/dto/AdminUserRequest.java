package com.scanner.demo.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserRequest {
    private String user_id;
    private String email;
    private String password;
    private String role;
}