package com.scms.app.repository;

import com.scms.app.model.Program;
import com.scms.app.model.ProgramStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 프로그램 Repository
 */
@Repository
public interface ProgramRepository extends JpaRepository<Program, Integer> {

    /**
     * 삭제되지 않은 모든 프로그램 조회 (최신순)
     */
    @Query("SELECT p FROM Program p WHERE p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    List<Program> findAllNotDeleted();

    /**
     * 프로그램 ID로 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT p FROM Program p WHERE p.programId = :programId AND p.deletedAt IS NULL")
    Optional<Program> findByIdNotDeleted(@Param("programId") Integer programId);

    /**
     * 카테고리별 프로그램 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT p FROM Program p WHERE p.category = :category AND p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    List<Program> findByCategoryNotDeleted(@Param("category") String category);

    /**
     * 행정부서별 프로그램 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT p FROM Program p WHERE p.department = :department AND p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    List<Program> findByDepartmentNotDeleted(@Param("department") String department);

    /**
     * 단과대학별 프로그램 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT p FROM Program p WHERE p.college = :college AND p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    List<Program> findByCollegeNotDeleted(@Param("college") String college);

    /**
     * 상태별 프로그램 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT p FROM Program p WHERE p.status = :status AND p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    List<Program> findByStatusNotDeleted(@Param("status") ProgramStatus status);

    /**
     * 제목으로 프로그램 검색 (삭제되지 않은 것만)
     */
    @Query("SELECT p FROM Program p WHERE p.title LIKE %:keyword% AND p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    List<Program> searchByTitleNotDeleted(@Param("keyword") String keyword);

    /**
     * 신청 가능한 프로그램 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT p FROM Program p WHERE p.status = 'OPEN' " +
           "AND p.applicationStartDate <= :now " +
           "AND p.applicationEndDate > :now " +
           "AND p.deletedAt IS NULL " +
           "ORDER BY p.applicationEndDate ASC")
    List<Program> findAvailablePrograms(@Param("now") LocalDateTime now);

    /**
     * 인기 프로그램 조회 (조회수 기준, 삭제되지 않은 것만)
     */
    @Query("SELECT p FROM Program p WHERE p.deletedAt IS NULL ORDER BY p.hits DESC")
    List<Program> findPopularPrograms();

    /**
     * 복합 필터 검색 (모든 조건 optional)
     */
    @Query("SELECT p FROM Program p WHERE " +
           "(:department IS NULL OR p.department = :department) AND " +
           "(:college IS NULL OR p.college = :college) AND " +
           "(:category IS NULL OR p.category = :category) AND " +
           "p.deletedAt IS NULL " +
           "ORDER BY p.createdAt DESC")
    List<Program> findByFilters(
        @Param("department") String department,
        @Param("college") String college,
        @Param("category") String category
    );

    /**
     * 메인 페이지용 최신 프로그램 조회 (상위 8개, 삭제되지 않은 것만)
     */
    @Query(value = "SELECT p FROM Program p WHERE p.deletedAt IS NULL ORDER BY p.createdAt DESC",
           nativeQuery = false)
    List<Program> findTop8NotDeleted();
}
