package com.scanner.demo.scan.service;

import com.scanner.demo.scan.entity.Sbom;
import com.scanner.demo.scan.entity.ScanHistory;
import com.scanner.demo.scan.entity.ScanIssue;
import com.scanner.demo.scan.repository.SbomRepository;
import com.scanner.demo.scan.repository.ScanHistoryRepository;
import com.scanner.demo.scan.repository.ScanIssueRepository;
import com.scanner.demo.user.entity.User;
import com.scanner.demo.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
public class ScanService {

    private final UserRepository userRepository;
    private final RestClient restClient; // Spring Boot 3 최신 HTTP 클라이언트
    private final ScanHistoryRepository scanHistoryRepository;
    private final ScanIssueRepository scanIssueRepository;
    private final SbomRepository sbomRepository;

    // 생성자 주입을 통해 yml의 URL을 읽어오고 RestClient를 초기화합니다.
    public ScanService(
            UserRepository userRepository,
            ScanHistoryRepository scanHistoryRepository,
            ScanIssueRepository scanIssueRepository,
            SbomRepository sbomRepository,
            @Value("${analyzer.base-url}") String analyzerBaseUrl) {

        this.userRepository = userRepository;
        this.scanHistoryRepository = scanHistoryRepository;
        this.scanIssueRepository = scanIssueRepository;
        this.sbomRepository = sbomRepository;
        this.restClient = RestClient.builder().baseUrl(analyzerBaseUrl).build();
    }

    @Transactional
    public Object processUploadScan(List<MultipartFile> files, boolean llmAdvisory,
                                    boolean generateSbom, String profile, String userId) {

        // 1. 유저 검증
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 2. Python 엔진으로 보낼 Multipart 폼 데이터 셋업
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        // 텍스트 옵션들 추가 (FastAPI가 인식할 수 있게 String으로 변환)
        body.add("llm_advisory", String.valueOf(llmAdvisory).toLowerCase());
        body.add("generate_sbom", String.valueOf(generateSbom).toLowerCase());
        body.add("profile", profile);
        body.add("llm_max_issues", "20");
        body.add("llm_verify", "false");
        body.add("llm_filter_fp", "false");

        // 파일 리스트 추가 (Spring이 파일명, 바이너리, MIME 타입을 알아서 캡슐화해줌)
        for (MultipartFile file : files) {
            body.add("files", file.getResource());
        }

        // 3. RestClient를 이용한 동기식 통신
        Map<String, Object> scanResult = restClient.post()
                .uri("/api/v1/scan/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                // 분석기 엔진의 JSON 응답을 Map 형식으로 그대로 파싱하여 받음
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});

        String scanId = (String) scanResult.get("scan_id");
        Map<String, Object> summary = (Map<String, Object>) scanResult.get("summary");

        ScanHistory scanHistory = ScanHistory.builder()
                .scanId(scanId)
                .user(user) // 매핑된 User 엔티티
                .target((String) scanResult.get("target"))
                .policy((String) scanResult.get("profile"))
                .language("python") // 필요시 ecosystems 등에서 추출
                .issuesCritical(summary != null ? ((Number) summary.get("CRITICAL")).intValue() : 0)
                .issuesHigh(summary != null ? ((Number) summary.get("HIGH")).intValue() : 0)
                .issuesMedium(summary != null ? ((Number) summary.get("MEDIUM")).intValue() : 0)
                .issuesLow(summary != null ? ((Number) summary.get("LOW")).intValue() : 0)
                .filesScanned(scanResult.get("files_scanned") != null ? ((Number) scanResult.get("files_scanned")).intValue() : 0)
                .durationMs(scanResult.get("duration_ms") != null ? ((Number) scanResult.get("duration_ms")).doubleValue() : 0.0)
                // timestamp 문자열을 LocalDateTime으로 변환하는 로직 추가 필요
                .build();

        scanHistoryRepository.save(scanHistory);

        // 4-2. ScanIssue (개별 취약점 상세) 저장
        List<Map<String, Object>> issuesData = (List<Map<String, Object>>) scanResult.get("issues");
        if (issuesData != null && !issuesData.isEmpty()) {
            List<ScanIssue> issues = issuesData.stream().map(issue -> ScanIssue.builder()
                    .scanHistory(scanHistory)
                    .issueId((String) issue.get("id"))
                    .type((String) issue.get("type"))
                    .typeKo((String) issue.get("type_ko"))
                    .severity((String) issue.get("severity"))
                    .severityKo((String) issue.get("severity_ko"))
                    .confidence(issue.get("confidence") != null ? ((Number) issue.get("confidence")).doubleValue() : null)
                    .filePath((String) issue.get("file"))
                    .lineNumber(issue.get("line") != null ? ((Number) issue.get("line")).intValue() : null)
                    .columnNumber(issue.get("column") != null ? ((Number) issue.get("column")).intValue() : null)
                    .message((String) issue.get("message"))
                    .ruleId((String) issue.get("rule_id"))
                    .cweId((String) issue.get("cwe"))
                    .owaspId((String) issue.get("owasp"))
                    .analyzer((String) issue.get("analyzer"))
                    .codeSnippet((String) issue.get("code_snippet"))
                    .detectionReasonKo((String) issue.get("detection_reason_ko"))
                    .fixDescriptionKo((String) issue.get("fix_description_ko"))
                    .fixCode((String) issue.get("fix_code"))
                    .build()
            ).toList(); // Java 16+ 리스트 변환

            scanIssueRepository.saveAll(issues); // 한 번에 Batch Insert
        }

        // 4-3. SBOM 메타데이터 저장
        String sbomId = (String) scanResult.get("sbom_id");
        if (sbomId != null) {
            Map<String, Object> sbomSummary = (Map<String, Object>) scanResult.get("sbom_summary");

            Sbom sbom = Sbom.builder()
                    .sbomId(sbomId)
                    .scanHistory(scanHistory)
                    .status((String) scanResult.get("sbom_status"))
                    .format(sbomSummary != null ? (String) sbomSummary.get("format") : null)
                    .specVersion(sbomSummary != null ? (String) sbomSummary.get("spec_version") : null)
                    .componentCount(sbomSummary != null ? ((Number) sbomSummary.get("component_count")).intValue() : 0)
                    .licenseCount(sbomSummary != null ? ((Number) sbomSummary.get("license_count")).intValue() : 0)
                    .vulnerabilityCount(sbomSummary != null ? ((Number) sbomSummary.get("vulnerability_count")).intValue() : 0)
                    .cyclonedxUrlOrData((String) scanResult.get("sbom_cyclonedx_json"))
                    .build();

            sbomRepository.save(sbom);

            // (참고: sbom_threats 상세 파인딩 데이터는 FastAPI에서 백그라운드로 처리했었으므로,
            // 필요하다면 추후 비동기 호출이나 별도 API로 연동해야 합니다.)
        }
        // 5. 프론트엔드로 분석기 응답 그대로 토스!
        scanResult.forEach((key, value) -> System.out.println(key + " : " + value));
        return scanResult;
    }
}