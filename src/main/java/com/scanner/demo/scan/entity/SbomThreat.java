package com.scanner.demo.scan.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sbom_threat")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SbomThreat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "threat_seq")
    private Integer threatSeq;

    @Column(name = "sbom_id", length = 50, nullable = false)
    private String sbomId;

    @Column(name = "threat_id", length = 255)
    private String threatId;

    @Column(length = 100)
    private String type;

    @Column(length = 20)
    private String severity;

    @Column(name = "component_ref", length = 255)
    private String componentRef;

    @Column(name = "component_name", length = 100)
    private String componentName;

    @Column(name = "component_version", length = 50)
    private String componentVersion;

    @Column(length = 50)
    private String ecosystem;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(columnDefinition = "TEXT")
    private String recommendation;
}