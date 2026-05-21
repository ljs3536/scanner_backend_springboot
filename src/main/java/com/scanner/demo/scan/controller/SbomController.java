package com.scanner.demo.scan.controller;

import com.scanner.demo.scan.dto.SbomDetailResponse;
import com.scanner.demo.scan.service.ScanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sboms")
@RequiredArgsConstructor
public class SbomController {

    private final ScanService scanService;

    /**
     * SBOM 상세 및 파인딩 목록 통합 조회 API
     * 경로: GET /api/sboms/{sbomId}
     */
    @GetMapping("/{sbomId}")
    public ResponseEntity<SbomDetailResponse> getSbomDetail(@PathVariable("sbomId") String sbomId) {
        SbomDetailResponse response = scanService.getSbomDetail(sbomId);
        return ResponseEntity.ok(response);
    }
}