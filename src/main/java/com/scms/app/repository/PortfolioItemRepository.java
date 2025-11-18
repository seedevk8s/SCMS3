package com.scms.app.repository;

import com.scms.app.model.PortfolioItem;
import com.scms.app.model.PortfolioItemType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 포트폴리오 항목 Repository
 */
@Repository
public interface PortfolioItemRepository extends JpaRepository<PortfolioItem, Long> {

    /**
     * 포트폴리오의 모든 항목 조회 (삭제되지 않은 것만, 정렬 순서대로)
     */
    @Query("SELECT i FROM PortfolioItem i WHERE i.portfolioId = :portfolioId AND i.deletedAt IS NULL ORDER BY i.displayOrder ASC, i.createdAt DESC")
    List<PortfolioItem> findByPortfolioIdNotDeleted(@Param("portfolioId") Long portfolioId);

    /**
     * 항목 ID로 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT i FROM PortfolioItem i WHERE i.itemId = :itemId AND i.deletedAt IS NULL")
    Optional<PortfolioItem> findByIdNotDeleted(@Param("itemId") Long itemId);

    /**
     * 포트폴리오의 특정 유형 항목 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT i FROM PortfolioItem i WHERE i.portfolioId = :portfolioId AND i.itemType = :itemType AND i.deletedAt IS NULL ORDER BY i.displayOrder ASC, i.createdAt DESC")
    List<PortfolioItem> findByPortfolioIdAndTypeNotDeleted(@Param("portfolioId") Long portfolioId, @Param("itemType") PortfolioItemType itemType);

    /**
     * 포트폴리오의 항목 수 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT COUNT(i) FROM PortfolioItem i WHERE i.portfolioId = :portfolioId AND i.deletedAt IS NULL")
    Long countByPortfolioIdNotDeleted(@Param("portfolioId") Long portfolioId);

    /**
     * 프로그램 신청 ID로 항목 조회 (중복 방지용)
     */
    @Query("SELECT i FROM PortfolioItem i WHERE i.programApplicationId = :programApplicationId AND i.deletedAt IS NULL")
    Optional<PortfolioItem> findByProgramApplicationIdNotDeleted(@Param("programApplicationId") Long programApplicationId);

    /**
     * 포트폴리오와 항목 ID로 조회 (권한 확인용)
     */
    @Query("SELECT i FROM PortfolioItem i WHERE i.itemId = :itemId AND i.portfolioId = :portfolioId AND i.deletedAt IS NULL")
    Optional<PortfolioItem> findByIdAndPortfolioIdNotDeleted(@Param("itemId") Long itemId, @Param("portfolioId") Long portfolioId);

    /**
     * 포트폴리오의 최대 display_order 조회 (다음 순서 계산용)
     */
    @Query("SELECT COALESCE(MAX(i.displayOrder), 0) FROM PortfolioItem i WHERE i.portfolioId = :portfolioId AND i.deletedAt IS NULL")
    Integer findMaxDisplayOrderByPortfolioId(@Param("portfolioId") Long portfolioId);
}
