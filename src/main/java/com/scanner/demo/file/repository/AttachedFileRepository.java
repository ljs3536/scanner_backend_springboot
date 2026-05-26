package com.scanner.demo.file.repository;

import com.scanner.demo.file.entity.AttachedFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachedFileRepository extends JpaRepository<AttachedFile, Integer> {
    // 나중에 그룹 ID로 파일 목록을 찾을 때 유용합니다.
    List<AttachedFile> findByAttachmentGroupId(String attachmentGroupId);
}