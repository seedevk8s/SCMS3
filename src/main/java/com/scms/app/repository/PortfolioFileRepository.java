package com.scms.app.repository;

import com.scms.app.model.PortfolioFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 포트폴리오 파일 Repository
 */
@Repository
public interface PortfolioFileRepository extends JpaRepository<PortfolioFile, Long> {

    /**
     * 포트폴리오 항목의 모든 파일 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT f FROM PortfolioFile f WHERE f.portfolioItemId = :portfolioItemId AND f.deletedAt IS NULL ORDER BY f.uploadedAt DESC")
    List<PortfolioFile> findByPortfolioItemIdNotDeleted(@Param("portfolioItemId") Long portfolioItemId);

    /**
     * 파일 ID로 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT f FROM PortfolioFile f WHERE f.fileId = :fileId AND f.deletedAt IS NULL")
    Optional<PortfolioFile> findByIdNotDeleted(@Param("fileId") Long fileId);

    /**
     * 포트폴리오 항목의 파일 수 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT COUNT(f) FROM PortfolioFile f WHERE f.portfolioItemId = :portfolioItemId AND f.deletedAt IS NULL")
    Long countByPortfolioItemIdNotDeleted(@Param("portfolioItemId") Long portfolioItemId);

    /**
     * 저장된 파일명으로 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT f FROM PortfolioFile f WHERE f.storedFileName = :storedFileName AND f.deletedAt IS NULL")
    Optional<PortfolioFile> findByStoredFileNameNotDeleted(@Param("storedFileName") String storedFileName);
}
