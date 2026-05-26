package com.scanner.demo.inquiry.controller;

import com.scanner.demo.inquiry.dto.InquiryDetailResponse;
import com.scanner.demo.inquiry.dto.InquiryListResponse;
import com.scanner.demo.inquiry.service.InquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/inquiries")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;

    /**
     * 문의 목록 조회 API
     * 경로: GET /api/inquiries
     */
    @GetMapping
    public ResponseEntity<Page<InquiryListResponse>> getInquiryList(
            @AuthenticationPrincipal String userId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<InquiryListResponse> result = inquiryService.getInquiries(userId, pageable);
        return ResponseEntity.ok(result);
    }

    /**
     * 특정 문의 상세 조회 API
     * 경로: GET /api/inquiries/{inquirySeq}
     */
    @GetMapping("/{inquirySeq}")
    public ResponseEntity<InquiryDetailResponse> getInquiryDetail(
            @PathVariable("inquirySeq") Integer inquirySeq,
            @AuthenticationPrincipal String userId) {

        InquiryDetailResponse response = inquiryService.getInquiryDetail(inquirySeq, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 문의사항 등록 (파일 첨부 포함 multipart/form-data)
     */
    @PostMapping
    public ResponseEntity<?> createInquiry(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "scanId", required = false) String scanId,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @AuthenticationPrincipal String userId) {

        try {
            inquiryService.createInquiry(title, content, scanId, file, userId);

            // 프론트엔드가 요구하는 성공 응답 규격
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "문의사항이 성공적으로 등록되었습니다."));

        } catch (Exception e) {
            log.error("문의 등록 중 에러 발생: ", e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "문의 등록 중 시스템 오류가 발생했습니다."));
        }
    }

    /**
     * 사용자 답변 수정
     */
    @PutMapping("/{inquirySeq}")
    public ResponseEntity<?> updateInquiry(
            @PathVariable("inquirySeq") Integer inquirySeq,
            @RequestBody Map<String, String> request, // title, content
            @AuthenticationPrincipal String userId) {
        try {
            inquiryService.updateInquiry(inquirySeq, request.get("title"), request.get("content"), userId);
            return ResponseEntity.ok(Map.of("message", "수정되었습니다."));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}