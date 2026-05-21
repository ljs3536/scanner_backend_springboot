package com.scanner.demo.scan.repository;

import com.scanner.demo.scan.entity.ScanHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScanHistoryRepository extends JpaRepository<ScanHistory, String> {
}