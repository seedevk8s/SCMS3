package com.scms.app.repository;

import com.scms.app.model.Counselor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 상담사 Repository
 */
@Repository
public interface CounselorRepository extends JpaRepository<Counselor, Integer> {

    /**
     * 삭제되지 않은 상담사 조회
     */
    @Query("SELECT c FROM Counselor c WHERE c.counselorId = :counselorId AND c.deletedAt IS NULL")
    Optional<Counselor> findByIdAndNotDeleted(Integer counselorId);

    /**
     * 삭제되지 않은 모든 상담사 조회
     */
    @Query("SELECT c FROM Counselor c WHERE c.deletedAt IS NULL ORDER BY c.createdAt DESC")
    List<Counselor> findAllNotDeleted();

    /**
     * 전문분야로 상담사 조회 (삭제되지 않은 상담사만)
     */
    @Query("SELECT c FROM Counselor c WHERE c.specialty LIKE %:specialty% AND c.deletedAt IS NULL")
    List<Counselor> findBySpecialtyContainingAndNotDeleted(String specialty);
}
