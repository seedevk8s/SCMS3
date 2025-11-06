package com.scms.app.repository;

import com.scms.app.model.LoginHistory;
import com.scms.app.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 로그인 이력 Repository
 */
@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Integer> {

    /**
     * 사용자별 로그인 이력 조회 (최신순)
     */
    List<LoginHistory> findByUserOrderByLoginAtDesc(User user);

    /**
     * 사용자별 로그인 이력 조회 (페이징)
     */
    Page<LoginHistory> findByUserOrderByLoginAtDesc(User user, Pageable pageable);

    /**
     * 성공한 로그인 이력 조회
     */
    @Query("SELECT lh FROM LoginHistory lh WHERE lh.isSuccess = true ORDER BY lh.loginAt DESC")
    Page<LoginHistory> findSuccessfulLogins(Pageable pageable);

    /**
     * 실패한 로그인 이력 조회
     */
    @Query("SELECT lh FROM LoginHistory lh WHERE lh.isSuccess = false ORDER BY lh.loginAt DESC")
    Page<LoginHistory> findFailedLogins(Pageable pageable);

    /**
     * 특정 기간 내 로그인 이력 조회
     */
    @Query("SELECT lh FROM LoginHistory lh WHERE lh.loginAt BETWEEN :startDate AND :endDate ORDER BY lh.loginAt DESC")
    List<LoginHistory> findByLoginAtBetween(@Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);

    /**
     * IP 주소로 로그인 이력 조회
     */
    List<LoginHistory> findByIpAddress(String ipAddress);

    /**
     * 특정 사용자의 최근 로그인 이력 조회
     */
    @Query("SELECT lh FROM LoginHistory lh WHERE lh.user = :user ORDER BY lh.loginAt DESC")
    Page<LoginHistory> findRecentLoginsByUser(@Param("user") User user, Pageable pageable);
}
