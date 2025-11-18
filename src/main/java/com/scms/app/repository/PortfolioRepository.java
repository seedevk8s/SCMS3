package com.scms.app.repository;

import com.scms.app.model.Portfolio;
import com.scms.app.model.PortfolioVisibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 포트폴리오 Repository
 */
@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    /**
     * 사용자의 모든 포트폴리오 조회 (삭제되지 않은 것만, 최신순)
     */
    @Query("SELECT p FROM Portfolio p WHERE p.userId = :userId AND p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    List<Portfolio> findByUserIdNotDeleted(@Param("userId") Integer userId);

    /**
     * 포트폴리오 ID로 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT p FROM Portfolio p WHERE p.portfolioId = :portfolioId AND p.deletedAt IS NULL")
    Optional<Portfolio> findByIdNotDeleted(@Param("portfolioId") Long portfolioId);

    /**
     * 사용자의 포트폴리오 수 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT COUNT(p) FROM Portfolio p WHERE p.userId = :userId AND p.deletedAt IS NULL")
    Long countByUserIdNotDeleted(@Param("userId") Integer userId);

    /**
     * 공개 포트폴리오 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT p FROM Portfolio p WHERE p.visibility = :visibility AND p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    List<Portfolio> findByVisibilityNotDeleted(@Param("visibility") PortfolioVisibility visibility);

    /**
     * 사용자의 포트폴리오 조회 with 페이지네이션
     */
    @Query("SELECT p FROM Portfolio p WHERE p.userId = :userId AND p.deletedAt IS NULL")
    Page<Portfolio> findByUserIdNotDeletedWithPagination(@Param("userId") Integer userId, Pageable pageable);

    /**
     * 포트폴리오 검색 (제목으로, 삭제되지 않은 것만)
     */
    @Query("SELECT p FROM Portfolio p WHERE p.userId = :userId AND p.title LIKE %:keyword% AND p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    List<Portfolio> searchByTitleNotDeleted(@Param("userId") Integer userId, @Param("keyword") String keyword);

    /**
     * 특정 사용자의 특정 ID 포트폴리오 조회 (권한 확인용)
     */
    @Query("SELECT p FROM Portfolio p WHERE p.portfolioId = :portfolioId AND p.userId = :userId AND p.deletedAt IS NULL")
    Optional<Portfolio> findByIdAndUserIdNotDeleted(@Param("portfolioId") Long portfolioId, @Param("userId") Integer userId);
}
