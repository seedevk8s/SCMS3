package com.scms.app.repository;

import com.scms.app.model.CompetencyCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 역량 카테고리 Repository
 */
@Repository
public interface CompetencyCategoryRepository extends JpaRepository<CompetencyCategory, Long> {

    /**
     * 모든 카테고리 조회 (생성일 순)
     */
    List<CompetencyCategory> findAllByOrderByCreatedAtAsc();

    /**
     * 카테고리명으로 검색
     */
    List<CompetencyCategory> findByNameContaining(String name);

    /**
     * 카테고리명으로 조회
     */
    Optional<CompetencyCategory> findByName(String name);

    /**
     * 카테고리 존재 여부 확인
     */
    boolean existsByName(String name);

    /**
     * 역량 포함 카테고리 조회 (JOIN FETCH)
     */
    @Query("SELECT DISTINCT c FROM CompetencyCategory c LEFT JOIN FETCH c.competencies ORDER BY c.createdAt ASC")
    List<CompetencyCategory> findAllWithCompetencies();

    /**
     * 특정 카테고리와 역량 함께 조회
     */
    @Query("SELECT c FROM CompetencyCategory c LEFT JOIN FETCH c.competencies WHERE c.id = :id")
    Optional<CompetencyCategory> findByIdWithCompetencies(@Param("id") Long id);
}
