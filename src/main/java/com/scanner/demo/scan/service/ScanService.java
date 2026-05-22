package com.scanner.demo.scan.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scanner.demo.scan.dto.RunCodeRequest;
import com.scanner.demo.scan.dto.SbomDetailResponse;
import com.scanner.demo.scan.entity.ScanHistory;
import com.scanner.demo.scan.entity.ScanIssue;
import com.scanner.demo.scan.entity.Sbom;
import com.scanner.demo.scan.entity.SbomThreat;
import com.scanner.demo.scan.repository.ScanHistoryRepository;
import com.scanner.demo.scan.repository.ScanIssueRepository;
import com.scanner.demo.scan.repository.SbomRepository;
import com.scanner.demo.scan.repository.SbomThreatRepository;
import com.scanner.demo.user.entity.User;
import com.scanner.demo.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ScanService {

    private final UserRepository userRepository;
    private final ScanHistoryRepository scanHistoryRepository;
    private final ScanIssueRepository scanIssueRepository;
    private final SbomRepository sbomRepository;
    private final SbomThreatRepository sbomThreatRepository; // 신규 추가
    private final RestClient restClient;

    public ScanService(
            UserRepository userRepository,
            ScanHistoryRepository scanHistoryRepository,
            ScanIssueRepository scanIssueRepository,
            SbomRepository sbomRepository,
            SbomThreatRepository sbomThreatRepository,
            @Value("${analyzer.base-url}") String analyzerBaseUrl) {

        this.userRepository = userRepository;
        this.scanHistoryRepository = scanHistoryRepository;
        this.scanIssueRepository = scanIssueRepository;
        this.sbomRepository = sbomRepository;
        this.sbomThreatRepository = sbomThreatRepository;
        this.restClient = RestClient.builder().baseUrl(analyzerBaseUrl).build();
    }

    @Transactional
    public Object processUploadScan(List<MultipartFile> files, boolean llmAdvisory,
                                    boolean generateSbom, String profile, String userId) {

        // 유저 조회 및 검증
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 분석기 엔진 폼 데이터 세팅
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("llm_advisory", String.valueOf(llmAdvisory).toLowerCase());
        body.add("generate_sbom", String.valueOf(generateSbom).toLowerCase());
        body.add("profile", profile);
        body.add("llm_max_issues", "20");
        body.add("llm_verify", "false");
        body.add("llm_filter_fp", "false");

        for (MultipartFile file : files) {
            body.add("files", file.getResource());
        }

        // 1차 스캔 실행 요청
        Map<String, Object> scanResult = restClient.post()
                .uri("/api/v1/scan/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});

        if (scanResult == null) {
            throw new RuntimeException("분석기 엔진으로부터 응답을 받지 못했습니다.");
        }

        // 4. 데이터베이스 분할 저장 시작
        String scanId = (String) scanResult.get("scan_id");
        String sbomId = (String) scanResult.get("sbom_id");
        Map<String, Object> summary = (Map<String, Object>) scanResult.get("summary");

        // 공통 메서드 호출
        saveScanData(scanResult, user, sbomId);

        // Sbom 메타데이터 저장 및 추가 위협(Threat) 데이터 연동
        if (sbomId != null) {
            Map<String, Object> sbomSummary = (Map<String, Object>) scanResult.get("sbom_summary");
            Integer finalRiskScore = 0; // 기본값
            List<SbomThreat> threatsToSave = null;

            // [STEP 1] Threat API부터 먼저 호출하여 데이터 확보
            try {
                // 이중 인코딩된 JSON을 방어하기 위해 일단 String으로 받아옵니다.
                String threatResultStr = restClient.get()
                        .uri("/api/v1/sbom/{sbom_id}/threats", sbomId)
                        .retrieve()
                        .body(String.class);
                System.out.println(threatResultStr);
                if (threatResultStr != null) {
                    // ObjectMapper로 문자열을 진짜 Map으로 파싱
                    ObjectMapper objectMapper = new ObjectMapper();
                    Map<String, Object> threatResult = objectMapper.readValue(
                            threatResultStr, new TypeReference<Map<String, Object>>() {}
                    );

                    // 1) Risk Score 추출
                    Map<String, Object> summaryObj = (Map<String, Object>) threatResult.get("summary");
                    if (summaryObj != null && summaryObj.get("risk_score") != null) {
                        finalRiskScore = ((Number) summaryObj.get("risk_score")).intValue();
                    }

                    // 2) Findings 추출 및 Entity 리스트로 변환 준비
                    List<Map<String, Object>> findingsData = (List<Map<String, Object>>) threatResult.get("findings");
                    if (findingsData != null && !findingsData.isEmpty()) {
                        threatsToSave = findingsData.stream().map(finding -> SbomThreat.builder()
                                .sbomId(sbomId)
                                .threatId((String) finding.get("id"))
                                .type((String) finding.get("type"))
                                .severity((String) finding.get("severity"))
                                .componentRef((String) finding.get("component_ref"))
                                .componentName((String) finding.get("component_name"))
                                .componentVersion((String) finding.get("component_version"))
                                .ecosystem((String) finding.get("ecosystem"))
                                .message((String) finding.get("message"))
                                .recommendation((String) finding.get("recommendation"))
                                .build()
                        ).toList();
                    }
                }
            } catch (Exception e) {
                System.err.println("Threat API 조회 및 파싱 실패 (스캔 이력은 유지됨): " + e.getMessage());
            }

            // [STEP 2] 스캔 결과와 Threat API에서 얻은 riskScore를 합쳐서 Sbom 저장
            Sbom sbom = Sbom.builder()
                    .sbomId(sbomId)
                    .status((String) scanResult.get("sbom_status"))
                    .format(sbomSummary != null ? (String) sbomSummary.get("format") : null)
                    .specVersion(sbomSummary != null ? (String) sbomSummary.get("spec_version") : null)
                    .componentCount(sbomSummary != null ? ((Number) sbomSummary.get("component_count")).intValue() : 0)
                    .licenseCount(sbomSummary != null ? ((Number) sbomSummary.get("license_count")).intValue() : 0)
                    .vulnerabilityCount(sbomSummary != null ? ((Number) sbomSummary.get("vulnerability_count")).intValue() : 0)
                    .riskScore(finalRiskScore) // 💡 Threat API에서 확보한 점수 주입!
                    .cyclonedxUrlOrData((String) scanResult.get("sbom_cyclonedx_json"))
                    .build();

            sbomRepository.save(sbom);

            // [STEP 3] 추출해둔 상세 위협(Findings) 데이터 일괄 저장
            if (threatsToSave != null && !threatsToSave.isEmpty()) {
                sbomThreatRepository.saveAll(threatsToSave);
            }
        }

        return scanResult;
    }

    @Transactional
    public Map<String, Object> processCodeScan(RunCodeRequest request, String userId) {
        // 1. 유저 검증
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 2. 분석기 엔진의 /code 엔드포인트에 맞춘 JSON 페이로드 구성
        Map<String, Object> analyzerPayload = new HashMap<>();
        analyzerPayload.put("code", request.getCode());
        analyzerPayload.put("filename", request.getFilename());
        analyzerPayload.put("profile", request.getProfile());

        // 3. 분석기 엔진 호출 (JSON 요청)
        Map<String, Object> scanResult = restClient.post()
                .uri("/api/v1/scan/code")
                .contentType(MediaType.APPLICATION_JSON)
                .body(analyzerPayload)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});

        if (scanResult == null) {
            throw new RuntimeException("분석기 엔진으로부터 응답을 받지 못했습니다.");
        }

        // 4. 공통 저장 로직 호출 (코드 스캔은 종속성이 없으므로 sbomId는 null 처리)
        saveScanData(scanResult, user, null);

        // 5. 프론트엔드 화면 렌더링을 위해 전체 데이터 반환
        return scanResult;
    }

    @Transactional(readOnly = true)
    public List<ScanHistory> getScanHistoryList(String userId) {

        // 1. JWT에 있던 userId(이메일)로 DB에서 User 객체(userSeq 포함)를 찾아옵니다.
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 2. startedAt 필드를 기준으로 내림차순 정렬 조건을 만듭니다.
        Sort sort = Sort.by(Sort.Direction.DESC, "startedAt");

        // 3. 찾아온 User 객체를 통째로 넘겨서 해당 사용자의 스캔 이력을 가져옵니다.
        return scanHistoryRepository.findByUser(user, sort);
    }

    /**
     * SBOM 상세 정보 및 위협 목록 단일 조회
     */
    @Transactional(readOnly = true)
    public SbomDetailResponse getSbomDetail(String sbomId) {
        // 1. SBOM 마스터 정보 조회
        Sbom sbom = sbomRepository.findById(sbomId)
                .orElseThrow(() -> new IllegalArgumentException("해당 SBOM 정보를 찾을 수 없습니다."));

        // 2. 해당 SBOM에 속한 상세 위협 내역 조회
        List<SbomThreat> threats = sbomThreatRepository.findBySbomId(sbomId);

        // 3. 하나의 응답 객체로 조립하여 반환
        return new SbomDetailResponse(sbom, threats);
    }


    // ScanHistory 및 ScanIssue 공통 저장 메서드
    private void saveScanData(Map<String, Object> scanResult, User user, String sbomId) {
        Map<String, Object> summary = (Map<String, Object>) scanResult.get("summary");

        // 1. ScanHistory 마스터 데이터 저장
        ScanHistory scanHistory = ScanHistory.builder()
                .scanId((String) scanResult.get("scan_id"))
                .user(user)
                .target((String) scanResult.get("target"))
                .policy((String) scanResult.get("profile"))
                .language((String) scanResult.get("language")) // 분석기가 감지한 언어 세팅
                .issuesCritical(summary != null && summary.get("CRITICAL") != null ? ((Number) summary.get("CRITICAL")).intValue() : 0)
                .issuesHigh(summary != null && summary.get("HIGH") != null ? ((Number) summary.get("HIGH")).intValue() : 0)
                .issuesMedium(summary != null && summary.get("MEDIUM") != null ? ((Number) summary.get("MEDIUM")).intValue() : 0)
                .issuesLow(summary != null && summary.get("LOW") != null ? ((Number) summary.get("LOW")).intValue() : 0)
                .filesScanned(scanResult.get("files_scanned") != null ? ((Number) scanResult.get("files_scanned")).intValue() : 1) // 코드 스캔 시 기본값 1
                .durationMs(scanResult.get("duration_ms") != null ? ((Number) scanResult.get("duration_ms")).doubleValue() : 0.0)
                .startedAt(LocalDateTime.now())
                .sbomId(sbomId) // 코드 스캔일 경우 null 삽입
                .build();

        scanHistoryRepository.save(scanHistory);

        // 2. ScanIssue 세부 취약점 리스트 저장
        List<Map<String, Object>> issuesData = (List<Map<String, Object>>) scanResult.get("issues");
        if (issuesData != null && !issuesData.isEmpty()) {
            List<ScanIssue> issues = issuesData.stream().map(issueData -> ScanIssue.builder()
                    .scanHistory(scanHistory)
                    .issueId((String) issueData.get("id"))
                    .type((String) issueData.get("type"))
                    .typeKo((String) issueData.get("type_ko"))
                    .severity((String) issueData.get("severity"))
                    .severityKo((String) issueData.get("severity_ko"))
                    .confidence(issueData.get("confidence") != null ? ((Number) issueData.get("confidence")).doubleValue() : null)
                    .filePath((String) issueData.get("file"))
                    .lineNumber(issueData.get("line") != null ? ((Number) issueData.get("line")).intValue() : null)
                    .columnNumber(issueData.get("column") != null ? ((Number) issueData.get("column")).intValue() : null)
                    .message((String) issueData.get("message"))
                    .ruleId((String) issueData.get("rule_id"))
                    .cweId((String) issueData.get("cwe"))
                    .owaspId((String) issueData.get("owasp"))
                    .analyzer((String) issueData.get("analyzer"))
                    .codeSnippet((String) issueData.get("code_snippet"))
                    .detectionReasonKo((String) issueData.get("detection_reason_ko"))
                    .fixDescriptionKo((String) issueData.get("fix_description_ko"))
                    .fixCode((String) issueData.get("fix_code"))
                    .build()
            ).toList();

            // JPA saveAll을 이용한 배치 처리로 성능 최적화
            scanIssueRepository.saveAll(issues);
        }
    }
}