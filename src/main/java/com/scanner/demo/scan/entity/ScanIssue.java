package com.scanner.demo.scan.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "scan_issues")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ScanIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "issue_seq")
    private Integer issueSeq;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scan_id", nullable = false)
    private ScanHistory scanHistory;

    @Column(name = "issue_id", length = 100)
    private String issueId;

    @Column(length = 100)
    private String type;

    @Column(name = "type_ko", length = 100)
    private String typeKo;

    @Column(length = 20)
    private String severity;

    @Column(name = "severity_ko", length = 20)
    private String severityKo;

    private Double confidence;

    @Column(name = "file_path", length = 255)
    private String filePath;

    @Column(name = "line_number")
    private Integer lineNumber;

    @Column(name = "column_number")
    private Integer columnNumber;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "rule_id", length = 100)
    private String ruleId;

    @Column(name = "cwe_id", length = 50)
    private String cweId;

    @Column(name = "owasp_id", length = 50)
    private String owaspId;

    @Column(length = 50)
    private String analyzer;

    @Column(name = "code_snippet", columnDefinition = "TEXT")
    private String codeSnippet;

    @Column(name = "detection_reason_ko", columnDefinition = "TEXT")
    private String detectionReasonKo;

    @Column(name = "fix_description_ko", columnDefinition = "TEXT")
    private String fixDescriptionKo;

    @Column(name = "fix_code", columnDefinition = "TEXT")
    private String fixCode;
}