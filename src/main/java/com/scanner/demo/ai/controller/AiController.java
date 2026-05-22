package com.scanner.demo.ai.controller;

import com.scanner.demo.ai.dto.AiRequest;
import com.scanner.demo.ai.dto.AiResponse;
import com.scanner.demo.ai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai") // 또는 SecurityConfig 설정에 따라 /api/ai
@RequiredArgsConstructor
public class AiController {

    private final OpenAiService openAiService;

    @PostMapping("/explain")
    public ResponseEntity<AiResponse> explain(@RequestBody AiRequest request) {
        AiResponse response = openAiService.explainVulnerability(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/fix")
    public ResponseEntity<AiResponse> fix(@RequestBody AiRequest request) {
        AiResponse response = openAiService.fixVulnerability(request);
        return ResponseEntity.ok(response);
    }
}