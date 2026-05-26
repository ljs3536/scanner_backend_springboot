package com.scanner.demo.inquiry.entity;

import com.scanner.demo.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "inquiries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer inquirySeq;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_seq", nullable = false)
    private User user;

    @Column(length = 50)
    private String scanId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // 💡 첨부파일 그룹 ID 연동 컬럼
    @Column(length = 50)
    private String attachmentGroupId;

    @Builder.Default
    @Column(length = 20)
    private String status = "PNDNG"; // PNDNG: 접수, CMPLT: 답변완료

    @Column(columnDefinition = "TEXT")
    private String answerContent;

    // 답변 작성자 (관리자) 번호
    private Integer answererSeq;

    private LocalDateTime answeredAt;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public void registerAnswer(String answerContent, Integer answererSeq) {
        this.answerContent = answerContent;
        this.answererSeq = answererSeq;
        this.answeredAt = LocalDateTime.now();
        this.status = "CMPLT"; // 상태를 '답변완료'로 변경
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }
}