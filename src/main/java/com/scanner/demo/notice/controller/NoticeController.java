package com.scanner.demo.notice.controller;

import com.scanner.demo.notice.dto.NoticeDto;
import com.scanner.demo.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping
    public ResponseEntity<Page<NoticeDto.ListResponse>> getList(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(noticeService.getNotices(pageable));
    }

    @GetMapping("/{noticeSeq}")
    public ResponseEntity<NoticeDto.DetailResponse> getDetail(@PathVariable Integer noticeSeq) {
        return ResponseEntity.ok(noticeService.getNoticeDetail(noticeSeq));
    }
}