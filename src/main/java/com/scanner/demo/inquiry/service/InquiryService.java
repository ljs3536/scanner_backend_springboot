package com.scanner.demo.inquiry.service;

import com.scanner.demo.file.entity.AttachedFile;
import com.scanner.demo.file.repository.AttachedFileRepository;
import com.scanner.demo.inquiry.dto.AdminInquiryAnswerRequest;
import com.scanner.demo.inquiry.dto.InquiryDetailResponse;
import com.scanner.demo.inquiry.dto.InquiryListResponse;
import com.scanner.demo.inquiry.entity.Inquiry;
import com.scanner.demo.inquiry.repository.InquiryRepository;
import com.scanner.demo.user.entity.User;
import com.scanner.demo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final AttachedFileRepository attachedFileRepository;
    private final UserRepository userRepository;

    @Value("${file.upload.dir:./uploads/inquiries}")
    private String uploadDir;

    /**
     * 1. 문의 목록 조회 (권한에 따른 분기 처리)
     */
    @Transactional(readOnly = true)
    public Page<InquiryListResponse> getInquiries(String userId, Pageable pageable) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Page<Inquiry> inquiryPage;

        // 관리자는 전체 목록 조회, 일반 유저는 본인 목록만 조회
        if ("ADMIN".equals(user.getRole())) {
            inquiryPage = inquiryRepository.findAll(pageable);
        } else {
            inquiryPage = inquiryRepository.findAllByUser(user, pageable);
        }

        // Entity Page -> DTO Page 변환
        return inquiryPage.map(InquiryListResponse::new);
    }

    /**
     * 2. 문의 상세 내용 조회
     */
    @Transactional(readOnly = true)
    public InquiryDetailResponse getInquiryDetail(Integer inquirySeq, String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Inquiry inquiry = inquiryRepository.findById(inquirySeq)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문의입니다."));

        // 권한 체크: 관리자가 아니면서 작성자 본인도 아니면 접근 차단
        if (!"ADMIN".equals(user.getRole()) && !inquiry.getUser().getUserSeq().equals(user.getUserSeq())) {
            throw new AccessDeniedException("해당 문의글을 열람할 권한이 없습니다.");
        }

        // 첨부파일 조회 (attachmentGroupId를 통해 files 테이블 조회)
        List<AttachedFile> files = null;
        if (inquiry.getAttachmentGroupId() != null && !inquiry.getAttachmentGroupId().isEmpty()) {
            files = attachedFileRepository.findByAttachmentGroupId(inquiry.getAttachmentGroupId());
        }

        return new InquiryDetailResponse(inquiry, files);
    }

    @Transactional
    public void createInquiry(String title, String content, String scanId, MultipartFile file, String userId) {

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        String attachmentGroupId = null;

        // 1. 파일이 첨부된 경우: 파일 로컬 저장 및 `files` 테이블 적재
        if (file != null && !file.isEmpty()) {
            try {
                // 그룹 식별자용 UUID 생성 (ATT_ 접두사를 붙여 식별하기 쉽게 구성)
                attachmentGroupId = "ATT_" + UUID.randomUUID().toString().substring(0, 8);

                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                String originalFileName = file.getOriginalFilename();
                String extension = originalFileName != null && originalFileName.contains(".")
                        ? originalFileName.substring(originalFileName.lastIndexOf(".")) : "";

                String savedFileName = UUID.randomUUID().toString() + extension;
                Path filePath = uploadPath.resolve(savedFileName);

                // 실제 파일 저장
                file.transferTo(filePath.toFile());

                // 💡 files 테이블에 데이터 Insert
                AttachedFile attachedFile = AttachedFile.builder()
                        .attachmentGroupId(attachmentGroupId)
                        .fileName(originalFileName)
                        .filePath(filePath.toString())
                        .build();

                attachedFileRepository.save(attachedFile);

            } catch (IOException e) {
                throw new RuntimeException("파일 저장 중 오류가 발생했습니다.", e);
            }
        }

        // 2. `inquiries` 테이블에 문의 사항 적재 (파일이 있다면 그룹 ID 포함)
        Inquiry inquiry = Inquiry.builder()
                .title(title)
                .content(content)
                .scanId(scanId)
                .user(user)
                .attachmentGroupId(attachmentGroupId) // 💡 연동 키 맵핑
                .status("PNDNG")
                .build();

        inquiryRepository.save(inquiry);
    }

    /**
     * [관리자 전용] 문의 답변 등록
     */
    @Transactional
    public void registerInquiryAnswer(Integer inquirySeq, AdminInquiryAnswerRequest request, String adminId) {

        // 1. 관리자 정보 조회 및 권한 체크
        User admin = userRepository.findByUserId(adminId)
                .orElseThrow(() -> new IllegalArgumentException("관리자를 찾을 수 없습니다."));

        if (!"ADMIN".equals(admin.getRole())) {
            throw new AccessDeniedException("답변을 등록할 권한이 없습니다.");
        }

        // 2. 문의 내역 조회
        Inquiry inquiry = inquiryRepository.findById(inquirySeq)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문의입니다."));

        // 3. 답변 등록 및 상태 변경 (엔티티 메서드 호출 -> 트랜잭션 종료 시 자동 UPDATE 쿼리 발생)
        inquiry.registerAnswer(request.getAnswerContent(), admin.getUserSeq());
    }

    /**
     *  사용자 답변 수정
     */
    @Transactional
    public void updateInquiry(Integer inquirySeq, String title, String content, String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Inquiry inquiry = inquiryRepository.findById(inquirySeq)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문의입니다."));

        // 1. 본인 작성 글인지 확인
        if (!inquiry.getUser().getUserSeq().equals(user.getUserSeq())) {
            throw new AccessDeniedException("수정 권한이 없습니다.");
        }

        // 2. 💡 답변 완료 상태인지 확인하여 방어
        if ("CMPLT".equals(inquiry.getStatus())) {
            throw new IllegalStateException("이미 답변이 완료된 문의는 수정할 수 없습니다.");
        }

        inquiry.update(title, content);
    }
}