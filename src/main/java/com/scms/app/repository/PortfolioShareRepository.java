package com.scms.app.repository;

import com.scms.app.model.PortfolioShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 포트폴리오 공유 Repository
 */
@Repository
public interface PortfolioShareRepository extends JpaRepository<PortfolioShare, Long> {

    /**
     * 공유 토큰으로 조회
     */
    @Query("SELECT s FROM PortfolioShare s WHERE s.shareToken = :shareToken")
    Optional<PortfolioShare> findByShareToken(@Param("shareToken") String shareToken);

    /**
     * 포트폴리오의 활성 공유 링크 조회 (취소되지 않은 것만)
     */
    @Query("SELECT s FROM PortfolioShare s WHERE s.portfolioId = :portfolioId AND s.revokedAt IS NULL ORDER BY s.createdAt DESC")
    List<PortfolioShare> findByPortfolioIdNotRevoked(@Param("portfolioId") Long portfolioId);

    /**
     * 포트폴리오의 모든 공유 링크 조회
     */
    @Query("SELECT s FROM PortfolioShare s WHERE s.portfolioId = :portfolioId ORDER BY s.createdAt DESC")
    List<PortfolioShare> findByPortfolioId(@Param("portfolioId") Long portfolioId);

    /**
     * 포트폴리오의 유효한 공유 링크 존재 여부 확인
     */
    @Query("SELECT COUNT(s) > 0 FROM PortfolioShare s WHERE s.portfolioId = :portfolioId AND s.revokedAt IS NULL AND (s.expiresAt IS NULL OR s.expiresAt > CURRENT_TIMESTAMP)")
    boolean existsValidShareByPortfolioId(@Param("portfolioId") Long portfolioId);
}
