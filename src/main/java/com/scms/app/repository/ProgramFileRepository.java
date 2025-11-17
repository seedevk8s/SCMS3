package com.scms.app.repository;

import com.scms.app.model.ProgramFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 프로그램 첨부파일 Repository
 */
@Repository
public interface ProgramFileRepository extends JpaRepository<ProgramFile, Integer> {

    /**
     * 프로그램별 첨부파일 조회 (삭제되지 않은 것만, 업로드 날짜 순)
     */
    @Query("SELECT pf FROM ProgramFile pf " +
           "LEFT JOIN FETCH pf.uploadedBy " +
           "WHERE pf.program.programId = :programId " +
           "AND pf.deletedAt IS NULL " +
           "ORDER BY pf.uploadedAt DESC")
    List<ProgramFile> findByProgramIdAndDeletedAtIsNull(@Param("programId") Integer programId);

    /**
     * 파일 ID로 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT pf FROM ProgramFile pf " +
           "LEFT JOIN FETCH pf.uploadedBy " +
           "WHERE pf.fileId = :fileId " +
           "AND pf.deletedAt IS NULL")
    Optional<ProgramFile> findByIdAndDeletedAtIsNull(@Param("fileId") Integer fileId);

    /**
     * 프로그램의 파일 개수 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT COUNT(pf) FROM ProgramFile pf " +
           "WHERE pf.program.programId = :programId " +
           "AND pf.deletedAt IS NULL")
    Long countByProgramId(@Param("programId") Integer programId);

    /**
     * 프로그램의 총 파일 크기 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT SUM(pf.fileSize) FROM ProgramFile pf " +
           "WHERE pf.program.programId = :programId " +
           "AND pf.deletedAt IS NULL")
    Long getTotalFileSizeByProgramId(@Param("programId") Integer programId);
}
