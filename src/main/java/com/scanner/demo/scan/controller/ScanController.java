package com.scanner.demo.scan.controller;

import com.scanner.demo.scan.service.ScanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/scans")
@RequiredArgsConstructor
public class ScanController {

    private final ScanService scanService;

    @PostMapping("/run-upload")
    public ResponseEntity<?> runUploadScan(
            // 1. 프론트엔드에서 배열로 넘어오는 다중 파일
            @RequestParam("files") List<MultipartFile> files,

            // 2. 폼 데이터 (FastAPI의 Form(...)과 동일한 역할)
            @RequestParam(value = "llm_advisory", defaultValue = "false") boolean llmAdvisory,
            @RequestParam(value = "generate_sbom", defaultValue = "false") boolean generateSbom,
            @RequestParam(value = "profile", defaultValue = "security_core") String profile,

            // 3. 현재 로그인한 사용자의 ID (JwtFilter에서 SecurityContext에 넣어둔 값)
            @AuthenticationPrincipal String userId
    ) {
        try {
            // Service로 비즈니스 로직 위임
            Object result = scanService.processUploadScan(files, llmAdvisory, generateSbom, profile, userId);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            // 예외 발생 시 프론트엔드에 500 에러 전달
            return ResponseEntity.internalServerError()
                    .body("스캔 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}