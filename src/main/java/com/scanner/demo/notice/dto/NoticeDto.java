package com.scanner.demo.notice.dto;

import com.scanner.demo.notice.entity.Notice;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

public class NoticeDto {

    // 1. 등록/수정용 Request DTO
    @Getter @Setter
    public static class Request {
        private String title;
        private String content;
    }

    // 2. 목록용 Response DTO
    @Getter
    public static class ListResponse {
        private Integer noticeSeq;
        private String title;
        private String authorName;
        private Integer viewCount;
        private LocalDateTime createdAt;

        public ListResponse(Notice notice) {
            this.noticeSeq = notice.getNoticeSeq();
            this.title = notice.getTitle();
            this.authorName = notice.getAuthor().getName();
            this.viewCount = notice.getViewCount();
            this.createdAt = notice.getCreatedAt();
        }
    }

    // 3. 상세용 Response DTO
    @Getter
    public static class DetailResponse {
        private Integer noticeSeq;
        private String title;
        private String content;
        private String authorName;
        private Integer viewCount;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public DetailResponse(Notice notice) {
            this.noticeSeq = notice.getNoticeSeq();
            this.title = notice.getTitle();
            this.content = notice.getContent();
            this.authorName = notice.getAuthor().getName();
            this.viewCount = notice.getViewCount();
            this.createdAt = notice.getCreatedAt();
            this.updatedAt = notice.getUpdatedAt();
        }
    }
}