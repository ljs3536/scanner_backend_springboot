package com.scanner.demo.scan.repository;

import com.scanner.demo.scan.entity.ScanHistory;
import com.scanner.demo.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScanHistoryRepository extends JpaRepository<ScanHistory, String> {
    Page<ScanHistory> findAllByUser(User user, Pageable pageable);

    @Query("SELECT s.language as name, COUNT(s) as value FROM ScanHistory s WHERE s.user.userSeq = :userSeq GROUP BY s.language")
    List<ChartDataProjection> countScansByLanguage(@Param("userSeq") Integer userSeq);

    // 💡 2. 메서드명 규칙 변경: ByUserSeq -> ByUser_UserSeq (User 객체 안의 userSeq를 찾으라는 뜻)
    List<ScanHistory> findTop5ByUser_UserSeqOrderByStartedAtDesc(Integer userSeq);

    // 💡 3. 카운트 메서드도 동일하게 변경
    long countByUser_UserSeq(Integer userSeq);

    // 💡 4. SBOM 갯수 카운트
    @Query("SELECT COUNT(s) FROM ScanHistory s WHERE s.user.userSeq = :userSeq AND s.sbomId IS NOT NULL")
    long countSbomsByUser_UserSeq(@Param("userSeq") Integer userSeq);

    @Query("SELECT SUM(s.issuesCritical) + SUM(s.issuesHigh) as vulnearabilities FROM ScanHistory s WHERE s.user.userSeq = :userSeq GROUP BY s.user.userSeq")
    long countTotalVulnearabilitiesByUser_UserSeq(@Param("userSeq") Integer userSeq);

    public interface ChartDataProjection {

        // 쿼리에서 "s.language as name" 으로 지정한 값을 가져옵니다.
        String getName();

        // 쿼리에서 "COUNT(s) as value" 로 지정한 값을 가져옵니다.
        Long getValue();

    }
}