package com.scms.app.repository;

import com.scms.app.model.Competency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 역량 Repository
 */
@Repository
public interface CompetencyRepository extends JpaRepository<Competency, Long> {

    /**
     * 카테고리별 역량 조회
     */
    List<Competency> findByCategoryId(Long categoryId);

    /**
     * 카테고리별 역량 조회 (생성일 순)
     */
    List<Competency> findByCategoryIdOrderByCreatedAtAsc(Long categoryId);

    /**
     * 역량명으로 검색
     */
    List<Competency> findByNameContaining(String name);

    /**
     * 역량명으로 조회
     */
    Optional<Competency> findByName(String name);

    /**
     * 역량 존재 여부 확인
     */
    boolean existsByName(String name);

    /**
     * 모든 역량 조회 (카테고리 정보 포함)
     */
    @Query("SELECT c FROM Competency c JOIN FETCH c.category ORDER BY c.category.id, c.createdAt")
    List<Competency> findAllWithCategory();

    /**
     * 역량 ID로 조회 (카테고리 정보 포함)
     */
    @Query("SELECT c FROM Competency c JOIN FETCH c.category WHERE c.id = :id")
    Optional<Competency> findByIdWithCategory(@Param("id") Long id);

    /**
     * 전체 역량 수
     */
    @Query("SELECT COUNT(c) FROM Competency c")
    long countAll();

    /**
     * 카테고리별 역량 수
     */
    long countByCategoryId(Long categoryId);
}
