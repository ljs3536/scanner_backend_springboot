package com.scanner.demo.notice.repository;

import com.scanner.demo.notice.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Integer> {
    List<Notice> findTop5ByOrderByCreatedAtDesc();
}
