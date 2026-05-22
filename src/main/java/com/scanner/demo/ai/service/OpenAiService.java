package com.scanner.demo.ai.service;

import com.scanner.demo.ai.dto.AiRequest;
import com.scanner.demo.ai.dto.AiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class OpenAiService {

    private final RestClient restClient;

    public OpenAiService(@Value("${openai.api.key}") String apiKey) {
        // Spring Boot 3.2+ 에서 지원하는 모던 HTTP 클라이언트 세팅
        this.restClient = RestClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    /**
     * 1. 취약점 원인 심층 진단 (Explain)
     */
    public AiResponse explainVulnerability(AiRequest request) {
        String systemPrompt = "당신은 15년 차 이상의 전문적인 엔터프라이즈 시큐어 코딩 분석가입니다. " +
                "사용자가 제공한 코드와 취약점 정보를 분석하여 발생 원인과 잠재적 위험성을 상세히 마크다운으로 설명하세요.";

        String userPrompt = buildUserPrompt(request, "이 코드가 왜 취약한지, 공격 시나리오는 무엇인지 설명해주세요.");

        String responseContent = callOpenAiApi(systemPrompt, userPrompt);
        return new AiResponse(responseContent);
    }

    /**
     * 2. 시큐어 패치 코드 생성 (Fix)
     */
    public AiResponse fixVulnerability(AiRequest request) {
        String systemPrompt = "당신은 전문 시큐어 코딩 엔지니어입니다. " +
                "제공된 취약한 코드를 분석하여, 해당 언어와 프레임워크의 보안 표준을 준수하는 안전한 코드로 재작성하세요. " +
                "코드 블록과 함께 수정한 이유를 간략하게 마크다운으로 설명해야 합니다.";

        String userPrompt = buildUserPrompt(request, "이 코드를 안전하게 수정하고, 수정 사항의 핵심을 설명해주세요.");

        String responseContent = callOpenAiApi(systemPrompt, userPrompt);
        return new AiResponse(responseContent);
    }

    // --- 내부 헬퍼 메서드 ---

    /**
     * 프론트엔드 데이터를 바탕으로 명확한 분석 컨텍스트를 조립합니다.
     */
    private String buildUserPrompt(AiRequest req, String taskDescription) {
        return String.format(
                "취약점 유형: %s (%s)\n" +
                        "심각도: %s\n" +
                        "환경: %s / %s\n" +
                        "위치: %s (Line %d)\n\n" +
                        "코드 스니펫:\n```%s\n%s\n```\n\n" +
                        "요청 사항: %s",
                req.getVulnerabilityType(), req.getCweId(),
                req.getSeverity(), req.getLanguage(), req.getFramework(),
                req.getFilePath(), req.getLineNumber(),
                req.getLanguage(), req.getCodeSnippet(), taskDescription
        );
    }

    /**
     * OpenAI API 실제 호출 및 응답 파싱
     */
    private String callOpenAiApi(String systemPrompt, String userPrompt) {
        Map<String, Object> requestBody = Map.of(
                "model", "gpt-4o", // 프론트엔드 UI 스펙과 동일한 모델 사용
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                ),
                "temperature", 0.3 // 코드 생성 및 분석이므로 환각(Hallucination)을 줄이기 위해 낮게 설정
        );

        Map<String, Object> response = restClient.post()
                .uri("/chat/completions")
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        // OpenAI의 JSON 응답 구조: choices[0].message.content
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        return (String) message.get("content");
    }
}