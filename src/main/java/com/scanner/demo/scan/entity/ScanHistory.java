package com.scanner.demo.scan.entity;

import com.scanner.demo.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "scan_histories")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ScanHistory {

    @Id
    @Column(name = "scan_id", length = 50)
    private String scanId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_seq", nullable = false)
    private User user;

    @Column(length = 255)
    private String target;

    @Column(length = 50)
    private String policy;

    @Column(length = 50)
    private String language;

    @Column(name = "issues_critical")
    private Integer issuesCritical;

    @Column(name = "issues_high")
    private Integer issuesHigh;

    @Column(name = "issues_medium")
    private Integer issuesMedium;

    @Column(name = "issues_low")
    private Integer issuesLow;

    @Column(name = "max_severity", length = 20)
    private String maxSeverity;

    @Column(name = "files_scanned")
    private Integer filesScanned;

    @Column(name = "duration_ms")
    private Double durationMs;

    @Column(name = "started_at")
    private LocalDateTime startedAt;
}