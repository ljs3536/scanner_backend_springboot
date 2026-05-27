package com.scanner.demo.dashboard.dto;

import com.scanner.demo.inquiry.dto.InquiryListResponse;
import com.scanner.demo.inquiry.entity.Inquiry;
import com.scanner.demo.notice.dto.NoticeDto;
import com.scanner.demo.notice.entity.Notice;
import com.scanner.demo.scan.dto.ScanListResponse;
import com.scanner.demo.scan.entity.ScanHistory;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class DashboardResponse {

    // 1. 상단 요약 카드용 데이터
    private SummaryStats summary;

    // 2. 스캔 언어 분포 (Pie 차트용)
    private List<ChartData> languageDistribution;

    // 3. 💡 SBOM 인사이트 데이터
    private SbomInsights sbomInsights;

    // 4. 최근 활동 내역 리스트
    private List<ScanHistory> recentScans;
    private List<Inquiry> recentInquiries;
    private List<Notice> recentNotices;

    // --- 내부 DTO 클래스들 ---

    @Getter @Builder
    public static class SummaryStats {
        private long totalScans;
        private long totalVulnerabilities; // Critical + High 합산
        private long pendingInquiries;     // 답변 대기 중인 문의 수
        private long totalSboms;           // 생성된 SBOM 수
    }

    @Getter @Builder
    public static class ChartData {
        private String name;  // 예: "python", "javascript"
        private long value;   // 예: 45, 30
    }

    @Getter @Builder
    public static class SbomInsights {
        private long totalComponents;
        private long totalLicenses;
        private double averageRiskScore;
        private List<ChartData> findingSeverityDistribution; // 심각도별 파인딩 통계
    }

    // (RecentScanDto, RecentInquiryDto 등은 목록 API에서 쓰던 DTO를 재사용하거나 간단히 정의합니다)
}