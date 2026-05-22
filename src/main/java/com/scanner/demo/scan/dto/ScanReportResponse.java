package com.scanner.demo.scan.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.scanner.demo.scan.entity.ScanHistory;
import com.scanner.demo.scan.entity.ScanIssue;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class ScanReportResponse {

    private Metadata metadata;

    @JsonProperty("severity_totals")
    private Map<String, Integer> severityTotals;

    private List<IssueDto> issues;

    @Getter
    @Builder
    public static class Metadata {
        @JsonProperty("scan_id")
        private String scanId;
        @JsonProperty("target_name")
        private String targetName;
        @JsonProperty("scan_date")
        private LocalDateTime scanDate;
        @JsonProperty("issues_count")
        private Integer issuesCount;
        @JsonProperty("framework_detected")
        private String frameworkDetected;
    }

    @Getter
    @Builder
    public static class IssueDto {
        @JsonProperty("issue_seq")
        private Integer issueSeq;
        @JsonProperty("issue_id")
        private String issueId;
        @JsonProperty("issue_title")
        private String issueTitle; // 기존 type 매핑
        @JsonProperty("type_ko")
        private String typeKo;
        private String severity;
        @JsonProperty("severity_ko")
        private String severityKo;
        @JsonProperty("file_path")
        private String filePath;
        @JsonProperty("line_number")
        private Integer lineNumber;
        @JsonProperty("cwe_id")
        private String cweId;
        @JsonProperty("owasp_id")
        private String owaspId;
        private Double confidence;
        private String description;
        @JsonProperty("code_snippet")
        private String codeSnippet;
        @JsonProperty("detection_reason_ko")
        private String detectionReasonKo;
        @JsonProperty("fix_description_ko")
        private String fixDescriptionKo;
        @JsonProperty("fix_code")
        private String fixCode;
        private String language;
    }
}