package com.scanner.demo.notice.service;

import com.scanner.demo.notice.dto.NoticeDto;
import com.scanner.demo.notice.entity.Notice;
import com.scanner.demo.notice.repository.NoticeRepository;
import com.scanner.demo.user.entity.User;
import com.scanner.demo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;

    // --- [공통] 조회 로직 ---

    @Transactional(readOnly = true)
    public Page<NoticeDto.ListResponse> getNotices(Pageable pageable) {
        return noticeRepository.findAll(pageable).map(NoticeDto.ListResponse::new);
    }

    @Transactional // 조회수 증가를 위해 readOnly 해제
    public NoticeDto.DetailResponse getNoticeDetail(Integer noticeSeq) {
        Notice notice = noticeRepository.findById(noticeSeq)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공지사항입니다."));

        notice.increaseViewCount(); // 조회 시 조회수 1 증가
        return new NoticeDto.DetailResponse(notice);
    }

    // --- [관리자 전용] 등록/수정/삭제 로직 ---

    @Transactional
    public void createNotice(NoticeDto.Request request, String adminId) {
        User admin = userRepository.findByUserId(adminId)
                .orElseThrow(() -> new IllegalArgumentException("관리자를 찾을 수 없습니다."));

        Notice notice = Notice.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .author(admin)
                .build();
        noticeRepository.save(notice);
    }

    @Transactional
    public void updateNotice(Integer noticeSeq, NoticeDto.Request request) {
        Notice notice = noticeRepository.findById(noticeSeq)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공지사항입니다."));

        notice.update(request.getTitle(), request.getContent());
    }

    @Transactional
    public void deleteNotice(Integer noticeSeq) {
        noticeRepository.deleteById(noticeSeq);
    }
}