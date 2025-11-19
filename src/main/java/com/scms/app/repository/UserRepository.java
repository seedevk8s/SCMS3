package com.scms.app.repository;

import com.scms.app.model.User;
import com.scms.app.model.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 Repository
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    /**
     * 학번으로 사용자 조회
     */
    Optional<User> findByStudentNum(Integer studentNum);

    /**
     * 이메일로 사용자 조회
     */
    Optional<User> findByEmail(String email);

    /**
     * 학번으로 사용자 조회 (삭제되지 않은 사용자만)
     */
    @Query("SELECT u FROM User u WHERE u.studentNum = :studentNum AND u.deletedAt IS NULL")
    Optional<User> findByStudentNumAndNotDeleted(@Param("studentNum") Integer studentNum);

    /**
     * 이메일로 사용자 조회 (삭제되지 않은 사용자만)
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
    Optional<User> findByEmailAndNotDeleted(@Param("email") String email);

    /**
     * 역할로 사용자 목록 조회 (삭제되지 않은 사용자만)
     */
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.deletedAt IS NULL")
    List<User> findByRoleAndNotDeleted(@Param("role") UserRole role);

    /**
     * 학번 중복 확인
     */
    boolean existsByStudentNum(Integer studentNum);

    /**
     * 이메일 중복 확인
     */
    boolean existsByEmail(String email);

    /**
     * 삭제되지 않은 모든 사용자 조회
     */
    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL ORDER BY u.createdAt DESC")
    List<User> findAllNotDeleted();

    /**
     * 이름으로 사용자 검색 (삭제되지 않은 사용자만)
     */
    @Query("SELECT u FROM User u WHERE u.name LIKE %:name% AND u.deletedAt IS NULL")
    List<User> searchByName(@Param("name") String name);

    /**
     * 학과로 사용자 조회 (삭제되지 않은 사용자만)
     */
    @Query("SELECT u FROM User u WHERE u.department = :department AND u.deletedAt IS NULL")
    List<User> findByDepartmentAndNotDeleted(@Param("department") String department);

    // ==================== 관리자용 메서드 ====================

    /**
     * 학생 목록 조회 (페이징, 삭제되지 않은 학생만)
     */
    @Query("SELECT u FROM User u WHERE u.role = 'STUDENT' AND u.deletedAt IS NULL")
    Page<User> findStudents(Pageable pageable);

    /**
     * 학생 검색 (이름, 학번, 학과, 삭제되지 않은 학생만)
     */
    @Query("SELECT u FROM User u WHERE u.role = 'STUDENT' AND u.deletedAt IS NULL " +
           "AND (CAST(u.studentNum AS string) LIKE %:keyword% OR u.name LIKE %:keyword% " +
           "OR u.email LIKE %:keyword% OR u.department LIKE %:keyword%)")
    Page<User> searchStudents(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 학과별 학생 조회 (페이징)
     */
    @Query("SELECT u FROM User u WHERE u.role = 'STUDENT' AND u.department = :department AND u.deletedAt IS NULL")
    Page<User> findStudentsByDepartment(@Param("department") String department, Pageable pageable);

    /**
     * 학년별 학생 조회 (페이징)
     */
    @Query("SELECT u FROM User u WHERE u.role = 'STUDENT' AND u.grade = :grade AND u.deletedAt IS NULL")
    Page<User> findStudentsByGrade(@Param("grade") Integer grade, Pageable pageable);

    /**
     * 전체 학생 수
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'STUDENT' AND u.deletedAt IS NULL")
    long countStudents();

    /**
     * 잠긴 학생 수
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'STUDENT' AND u.locked = true AND u.deletedAt IS NULL")
    long countLockedStudents();

    /**
     * 학과별 학생 수
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'STUDENT' AND u.department = :department AND u.deletedAt IS NULL")
    long countStudentsByDepartment(@Param("department") String department);

    /**
     * 학년별 학생 수
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'STUDENT' AND u.grade = :grade AND u.deletedAt IS NULL")
    long countStudentsByGrade(@Param("grade") Integer grade);

    /**
     * 모든 학과 목록 조회 (중복 제거, 삭제되지 않은 학생만)
     */
    @Query("SELECT DISTINCT u.department FROM User u WHERE u.role = 'STUDENT' AND u.deletedAt IS NULL AND u.department IS NOT NULL ORDER BY u.department")
    List<String> findAllDepartments();
}
