package com.scanner.demo.scan.repository;

import com.scanner.demo.scan.entity.Sbom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SbomRepository extends JpaRepository<Sbom, String> {

    @Query("SELECT SUM(s.componentCount) FROM Sbom s JOIN ScanHistory sh ON s.sbomId = sh.sbomId WHERE sh.user.userSeq = :userSeq")
    Long sumComponentCountByUser(@Param("userSeq") Integer userSeq);

    @Query("SELECT SUM(s.licenseCount) FROM Sbom s JOIN ScanHistory sh ON s.sbomId = sh.sbomId WHERE sh.user.userSeq = :userSeq")
    Long sumLicenseCountByUser(@Param("userSeq") Integer userSeq);

    @Query("SELECT AVG(s.riskScore) FROM Sbom s JOIN ScanHistory sh ON s.sbomId = sh.sbomId WHERE sh.user.userSeq = :userSeq")
    Double getAverageRiskScoreByUser(@Param("userSeq") Integer userSeq);

}