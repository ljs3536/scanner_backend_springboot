package com.scanner.demo.user.dto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateRequest {
    private String userId;
    private String password;
    private String role; // "ADMIN" 또는 "USER"
}