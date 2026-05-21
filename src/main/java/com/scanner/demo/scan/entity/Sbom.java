package com.scanner.demo.scan.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sboms")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Sbom {

    @Id
    @Column(name = "sbom_id", length = 50)
    private String sbomId;

    @Column(length = 20)
    private String status;

    @Column(length = 50)
    private String format;

    @Column(name = "spec_version", length = 20)
    private String specVersion;

    @Column(length = 255)
    private String ecosystems;

    @Column(name = "component_count")
    private Integer componentCount;

    @Column(name = "license_count")
    private Integer licenseCount;

    @Column(name = "vulnerability_count")
    private Integer vulnerabilityCount;

    @Column(name = "risk_score")
    private Integer riskScore;

    @Column(name = "cyclonedx_url_or_data", columnDefinition = "TEXT")
    private String cyclonedxUrlOrData;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}