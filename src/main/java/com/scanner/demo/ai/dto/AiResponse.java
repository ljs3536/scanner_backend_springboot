package com.scanner.demo.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AiResponse {
    // 프론트엔드의 res.response 매핑을 위해 통일
    private String response;
}