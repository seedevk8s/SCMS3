package com.scms.app.repository;

import com.scms.app.model.ProgramCompetency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProgramCompetencyRepository extends JpaRepository<ProgramCompetency, Long> {

    /**
     * 프로그램 ID로 역량 매핑 조회
     */
    List<ProgramCompetency> findByProgramProgramId(Integer programId);

    /**
     * 역량 ID로 프로그램 매핑 조회
     */
    @Query("SELECT pc FROM ProgramCompetency pc " +
           "JOIN FETCH pc.program p " +
           "WHERE pc.competency.id = :competencyId " +
           "AND p.deletedAt IS NULL " +
           "ORDER BY pc.weight DESC")
    List<ProgramCompetency> findByCompetencyId(@Param("competencyId") Long competencyId);

    /**
     * 여러 역량 ID로 프로그램 매핑 조회 (추천 알고리즘용)
     */
    @Query("SELECT pc FROM ProgramCompetency pc " +
           "JOIN FETCH pc.program p " +
           "WHERE pc.competency.id IN :competencyIds " +
           "AND p.deletedAt IS NULL " +
           "ORDER BY pc.weight DESC")
    List<ProgramCompetency> findByCompetencyIdIn(@Param("competencyIds") List<Long> competencyIds);
}
