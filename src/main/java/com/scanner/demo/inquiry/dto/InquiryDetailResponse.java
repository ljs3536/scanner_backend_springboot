package com.scanner.demo.inquiry.dto;

import com.scanner.demo.file.entity.AttachedFile;
import com.scanner.demo.inquiry.entity.Inquiry;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class InquiryDetailResponse {
    private Integer inquirySeq;
    private String title;
    private String content;
    private String scanId;
    private String status;
    private String answerContent;
    private LocalDateTime answeredAt;
    private LocalDateTime createdAt;

    // 첨부파일 리스트
    private List<FileDto> files;

    public InquiryDetailResponse(Inquiry inquiry, List<AttachedFile> attachedFiles) {
        this.inquirySeq = inquiry.getInquirySeq();
        this.title = inquiry.getTitle();
        this.content = inquiry.getContent();
        this.scanId = inquiry.getScanId();
        this.status = inquiry.getStatus();
        this.answerContent = inquiry.getAnswerContent();
        this.answeredAt = inquiry.getAnsweredAt();
        this.createdAt = inquiry.getCreatedAt();

        if (attachedFiles != null) {
            this.files = attachedFiles.stream()
                    .map(f -> new FileDto(f.getFileSeq(), f.getFileName()))
                    .collect(Collectors.toList());
        }
    }

    @Getter
    public static class FileDto {
        private Integer fileSeq;
        private String fileName;

        public FileDto(Integer fileSeq, String fileName) {
            this.fileSeq = fileSeq;
            this.fileName = fileName;
        }
    }
}