package com.scms.app.repository;

import com.scms.app.model.PortfolioView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 포트폴리오 조회 통계 Repository
 */
@Repository
public interface PortfolioViewRepository extends JpaRepository<PortfolioView, Long> {

    /**
     * 포트폴리오의 총 조회 수
     */
    @Query("SELECT COUNT(v) FROM PortfolioView v WHERE v.portfolioId = :portfolioId")
    Long countByPortfolioId(@Param("portfolioId") Long portfolioId);

    /**
     * 포트폴리오의 조회 이력 조회 (최신순)
     */
    @Query("SELECT v FROM PortfolioView v WHERE v.portfolioId = :portfolioId ORDER BY v.viewedAt DESC")
    List<PortfolioView> findByPortfolioId(@Param("portfolioId") Long portfolioId);

    /**
     * 특정 기간 동안의 조회 수
     */
    @Query("SELECT COUNT(v) FROM PortfolioView v WHERE v.portfolioId = :portfolioId AND v.viewedAt BETWEEN :startDate AND :endDate")
    Long countByPortfolioIdAndDateRange(@Param("portfolioId") Long portfolioId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * 특정 사용자의 포트폴리오 조회 기록 존재 여부
     */
    @Query("SELECT COUNT(v) > 0 FROM PortfolioView v WHERE v.portfolioId = :portfolioId AND v.viewerUserId = :viewerUserId")
    boolean existsByPortfolioIdAndViewerUserId(@Param("portfolioId") Long portfolioId, @Param("viewerUserId") Integer viewerUserId);

    /**
     * 공유 토큰별 조회 수
     */
    @Query("SELECT COUNT(v) FROM PortfolioView v WHERE v.shareToken = :shareToken")
    Long countByShareToken(@Param("shareToken") String shareToken);
}
