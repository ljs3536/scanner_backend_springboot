package com.scanner.demo.scan.repository;

import com.scanner.demo.scan.entity.ScanHistory;
import com.scanner.demo.user.entity.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScanHistoryRepository extends JpaRepository<ScanHistory, String> {

    List<ScanHistory> findByUser(User user, Sort sort);
}