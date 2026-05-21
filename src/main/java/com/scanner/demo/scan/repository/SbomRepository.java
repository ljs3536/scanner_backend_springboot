package com.scanner.demo.scan.repository;

import com.scanner.demo.scan.entity.Sbom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SbomRepository extends JpaRepository<Sbom, String> {
}