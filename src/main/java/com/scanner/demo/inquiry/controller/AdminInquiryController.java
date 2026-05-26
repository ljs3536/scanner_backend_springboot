package com.scanner.demo.inquiry.controller;

import com.scanner.demo.inquiry.dto.AdminInquiryAnswerRequest;
import com.scanner.demo.inquiry.dto.InquiryDetailResponse;
import com.scanner.demo.inquiry.dto.InquiryListResponse;
import com.scanner.demo.inquiry.service.InquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/inquiries")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // 💡 이 컨트롤러의 모든 API는 ADMIN만 접근 가능!
public class AdminInquiryController {

    private final InquiryService inquiryService;

    /**
     * [관리자용] 전체 문의 목록 조회 (페이징)
     */
    @GetMapping
    public ResponseEntity<Page<InquiryListResponse>> getAllInquiries(
            @AuthenticationPrincipal String adminId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        // InquiryService의 getInquiries 내부에서 Role이 ADMIN이면 전체를 가져오도록 이미 구현되어 있음
        Page<InquiryListResponse> result = inquiryService.getInquiries(adminId, pageable);
        return ResponseEntity.ok(result);
    }

    /**
     * [관리자용] 특정 문의 상세 조회
     */
    @GetMapping("/{inquirySeq}")
    public ResponseEntity<InquiryDetailResponse> getInquiryDetail(
            @PathVariable("inquirySeq") Integer inquirySeq,
            @AuthenticationPrincipal String adminId) {

        InquiryDetailResponse response = inquiryService.getInquiryDetail(inquirySeq, adminId);
        return ResponseEntity.ok(response);
    }

    /**
     * 💡 [핵심] 문의 답변 등록
     */
    @PutMapping("/{inquirySeq}/answer")
    public ResponseEntity<?> answerInquiry(
            @PathVariable("inquirySeq") Integer inquirySeq,
            @RequestBody AdminInquiryAnswerRequest request,
            @AuthenticationPrincipal String adminId) {

        inquiryService.registerInquiryAnswer(inquirySeq, request, adminId);

        // 명세서에 맞춘 성공 응답
        return ResponseEntity.ok(Map.of("message", "답변 등록이 완료되었습니다. 사용자에게 알림이 전송됩니다."));
    }
}