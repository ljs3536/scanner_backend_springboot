package com.scanner.demo.scan.repository;

import com.scanner.demo.scan.entity.ScanHistory;
import com.scanner.demo.scan.entity.ScanIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScanIssueRepository extends JpaRepository<ScanIssue, Integer> {

    // ScanHistory 객체를 기준으로 해당 스캔의 모든 이슈를 조회
    List<ScanIssue> findByScanHistory(ScanHistory scanHistory);
}