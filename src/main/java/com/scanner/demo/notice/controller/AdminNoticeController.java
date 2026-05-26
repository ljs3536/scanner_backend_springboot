package com.scanner.demo.notice.controller;

import com.scanner.demo.notice.dto.NoticeDto;
import com.scanner.demo.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/notices")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminNoticeController {

    private final NoticeService noticeService;

    @PostMapping
    public ResponseEntity<?> createNotice(
            @RequestBody NoticeDto.Request request,
            @AuthenticationPrincipal String adminId) {
        noticeService.createNotice(request, adminId);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "공지사항이 등록되었습니다."));
    }

    @PutMapping("/{noticeSeq}")
    public ResponseEntity<?> updateNotice(
            @PathVariable Integer noticeSeq,
            @RequestBody NoticeDto.Request request) {
        noticeService.updateNotice(noticeSeq, request);
        return ResponseEntity.ok(Map.of("message", "공지사항이 수정되었습니다."));
    }

    @DeleteMapping("/{noticeSeq}")
    public ResponseEntity<?> deleteNotice(@PathVariable Integer noticeSeq) {
        noticeService.deleteNotice(noticeSeq);
        return ResponseEntity.ok(Map.of("message", "공지사항이 삭제되었습니다."));
    }
}