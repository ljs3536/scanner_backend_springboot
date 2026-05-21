package com.scanner.demo.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenResponse {

    @JsonProperty("access_token") // JSON 변환 시 access_token으로 매핑
    private String accessToken;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("user_id")
    private String userId;

    private String role; // 변수명이 동일하므로 그대로 role로 나감
}