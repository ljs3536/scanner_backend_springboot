package com.scanner.demo.scan.dto;

import com.scanner.demo.scan.entity.ScanHistory;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class ScanListResponse {
    private String scanId;
    private String target;
    private Integer issuesHigh; // 예시: High 등급 개수만 노출
    private String sbomId;
    private LocalDateTime startedAt;

    // Entity -> DTO 변환 생성자
    public ScanListResponse(ScanHistory history) {
        this.scanId = history.getScanId();
        this.target = history.getTarget();
        this.issuesHigh = history.getIssuesHigh();
        this.sbomId = history.getSbomId();
        this.startedAt = history.getStartedAt();
    }
}