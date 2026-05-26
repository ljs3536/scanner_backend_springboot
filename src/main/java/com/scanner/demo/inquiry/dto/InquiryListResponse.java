package com.scanner.demo.inquiry.dto;

import com.scanner.demo.inquiry.entity.Inquiry;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class InquiryListResponse {
    private Integer inquirySeq;
    private String title;
    private String status;
    private String authorName; // 작성자 이름
    private boolean hasAttachment; // 첨부파일 존재 여부
    private LocalDateTime createdAt;

    public InquiryListResponse(Inquiry inquiry) {
        this.inquirySeq = inquiry.getInquirySeq();
        this.title = inquiry.getTitle();
        this.status = inquiry.getStatus();
        this.authorName = inquiry.getUser().getName(); // User 엔티티에서 이름 추출
        // 그룹 ID가 존재하면 첨부파일이 있는 것으로 간주
        this.hasAttachment = inquiry.getAttachmentGroupId() != null && !inquiry.getAttachmentGroupId().isEmpty();
        this.createdAt = inquiry.getCreatedAt();
    }
}