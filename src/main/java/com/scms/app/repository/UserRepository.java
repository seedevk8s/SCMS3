package com.scms.app.repository;

import com.scms.app.model.User;
import com.scms.app.model.UserRole;
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
}
