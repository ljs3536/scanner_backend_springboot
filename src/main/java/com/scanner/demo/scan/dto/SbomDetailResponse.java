package com.scanner.demo.scan.dto;

import com.scanner.demo.scan.entity.Sbom;
import com.scanner.demo.scan.entity.SbomThreat;
import lombok.Getter;

import java.util.List;

@Getter
public class SbomDetailResponse {
    // SBOM 마스터 정보
    private String sbomId;
    private String status;
    private String format;
    private String specVersion;
    private Integer componentCount;
    private Integer licenseCount;
    private Integer vulnerabilityCount;
    private Integer riskScore;

    // SBOM 상세 위협 목록 (Join 대신 별도 리스트로 포장)
    private List<SbomThreat> threats;

    public SbomDetailResponse(Sbom sbom, List<SbomThreat> threats) {
        this.sbomId = sbom.getSbomId();
        this.status = sbom.getStatus();
        this.format = sbom.getFormat();
        this.specVersion = sbom.getSpecVersion();
        this.componentCount = sbom.getComponentCount();
        this.licenseCount = sbom.getLicenseCount();
        this.vulnerabilityCount = sbom.getVulnerabilityCount();
        this.riskScore = sbom.getRiskScore();
        this.threats = threats;
    }
}