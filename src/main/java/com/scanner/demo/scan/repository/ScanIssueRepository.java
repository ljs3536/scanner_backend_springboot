package com.scanner.demo.scan.repository;

import com.scanner.demo.scan.entity.ScanIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScanIssueRepository extends JpaRepository<ScanIssue, Integer> {
}