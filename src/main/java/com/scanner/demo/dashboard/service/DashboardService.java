package com.scanner.demo.dashboard.service;

import com.scanner.demo.dashboard.dto.DashboardResponse;
import com.scanner.demo.inquiry.entity.Inquiry;
import com.scanner.demo.inquiry.repository.InquiryRepository;
import com.scanner.demo.notice.dto.NoticeDto;
import com.scanner.demo.notice.entity.Notice;
import com.scanner.demo.notice.repository.NoticeRepository;
import com.scanner.demo.scan.dto.ScanListResponse;
import com.scanner.demo.scan.entity.ScanHistory;
import com.scanner.demo.scan.repository.SbomRepository;
import com.scanner.demo.scan.repository.ScanHistoryRepository;
import com.scanner.demo.user.entity.User;
import com.scanner.demo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ScanHistoryRepository scanHistoryRepository;
    private final SbomRepository sbomRepository;
    private final InquiryRepository inquiryRepository;
    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboardData(String userId) {
        User user = userRepository.findByUserId(userId).orElseThrow();
        Integer userSeq = user.getUserSeq();

        // 1. 요약 카드 데이터 계산
        long totalScans = scanHistoryRepository.countByUser_UserSeq(userSeq);
        long totalSboms = scanHistoryRepository.countSbomsByUser_UserSeq(userSeq);
        long pendingInquiries = inquiryRepository.countByUser_UserSeqAndStatus(userSeq, "PNDNG");

        // 2. 언어 분포 차트 데이터 가공
        var languageData = scanHistoryRepository.countScansByLanguage(userSeq).stream()
                .map(p -> DashboardResponse.ChartData.builder()
                        .name(p.getName())
                        .value(p.getValue())
                        .build())
                .collect(Collectors.toList());

        // 3. SBOM 인사이트 데이터 가공 (null 처리 주의)
        Long totalComponents = sbomRepository.sumComponentCountByUser(userSeq);
        Long totalLicenses = sbomRepository.sumLicenseCountByUser(userSeq);
        Double avgRisk = sbomRepository.getAverageRiskScoreByUser(userSeq);

        DashboardResponse.SbomInsights sbomInsights = DashboardResponse.SbomInsights.builder()
                .totalComponents(totalComponents != null ? totalComponents : 0)
                .totalLicenses(totalLicenses != null ? totalLicenses : 0)
                .averageRiskScore(avgRisk != null ? Math.round(avgRisk * 10) / 10.0 : 0.0) // 소수점 1자리
                .build();

        // 최근 내역 리스트
        List<ScanHistory> recentScans = scanHistoryRepository.findTop5ByUser_UserSeqOrderByStartedAtDesc(userSeq);
        List<Notice> recentNotices = noticeRepository.findTop5ByOrderByCreatedAtDesc();
        List<Inquiry> recentInquiries = inquiryRepository.findTop5ByUser_UserSeqOrderByCreatedAtDesc(userSeq);

        return DashboardResponse.builder()
                .summary(DashboardResponse.SummaryStats.builder()
                        .totalScans(totalScans)
                        .pendingInquiries(pendingInquiries)
                        .totalSboms(totalSboms)
                        .build())
                .languageDistribution(languageData)
                .sbomInsights(sbomInsights)
                .recentScans(recentScans)
                .recentNotices(recentNotices)
                .recentInquiries(recentInquiries)
                .build();
    }
}