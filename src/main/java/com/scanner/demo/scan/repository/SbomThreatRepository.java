package com.scanner.demo.scan.repository;

import com.scanner.demo.scan.entity.SbomThreat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SbomThreatRepository extends JpaRepository<SbomThreat, Integer> {

    List<SbomThreat> findBySbomId(String sbomId);
}