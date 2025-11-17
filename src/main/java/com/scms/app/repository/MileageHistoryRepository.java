package com.scms.app.repository;

import com.scms.app.model.MileageHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 마일리지 적립 내역 Repository
 */
@Repository
public interface MileageHistoryRepository extends JpaRepository<MileageHistory, Long> {

    /**
     * 사용자별 마일리지 내역 조회 (최신순, JOIN FETCH로 N+1 방지)
     */
    @Query("SELECT h FROM MileageHistory h " +
           "LEFT JOIN FETCH h.user " +
           "LEFT JOIN FETCH h.awardedBy " +
           "WHERE h.user.userId = :userId " +
           "ORDER BY h.earnedAt DESC")
    List<MileageHistory> findByUserId(@Param("userId") Integer userId);

    /**
     * 사용자의 총 마일리지 계산
     */
    @Query("SELECT COALESCE(SUM(h.points), 0) FROM MileageHistory h " +
           "WHERE h.user.userId = :userId")
    Long getTotalMileageByUserId(@Param("userId") Integer userId);

    /**
     * 사용자별 활동 타입별 마일리지 통계
     */
    @Query("SELECT h.activityType, COALESCE(SUM(h.points), 0), COUNT(h) " +
           "FROM MileageHistory h " +
           "WHERE h.user.userId = :userId " +
           "GROUP BY h.activityType " +
           "ORDER BY SUM(h.points) DESC")
    List<Object[]> getStatisticsByUserId(@Param("userId") Integer userId);

    /**
     * 전체 사용자 마일리지 랭킹 (TOP N)
     */
    @Query("SELECT h.user.userId, h.user.name, h.user.department, COALESCE(SUM(h.points), 0) as totalPoints " +
           "FROM MileageHistory h " +
           "WHERE h.user.deletedAt IS NULL " +
           "GROUP BY h.user.userId, h.user.name, h.user.department " +
           "ORDER BY totalPoints DESC")
    List<Object[]> getMileageRanking();

    /**
     * 특정 기간 내 마일리지 내역 조회
     */
    @Query("SELECT h FROM MileageHistory h " +
           "LEFT JOIN FETCH h.user " +
           "WHERE h.user.userId = :userId " +
           "AND h.earnedAt BETWEEN :startDate AND :endDate " +
           "ORDER BY h.earnedAt DESC")
    List<MileageHistory> findByUserIdAndDateRange(
            @Param("userId") Integer userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 특정 활동 타입의 마일리지 내역 조회
     */
    @Query("SELECT h FROM MileageHistory h " +
           "LEFT JOIN FETCH h.user " +
           "WHERE h.user.userId = :userId " +
           "AND h.activityType = :activityType " +
           "ORDER BY h.earnedAt DESC")
    List<MileageHistory> findByUserIdAndActivityType(
            @Param("userId") Integer userId,
            @Param("activityType") String activityType);

    /**
     * 최근 N개 마일리지 내역 조회
     */
    @Query("SELECT h FROM MileageHistory h " +
           "LEFT JOIN FETCH h.user " +
           "WHERE h.user.userId = :userId " +
           "ORDER BY h.earnedAt DESC")
    List<MileageHistory> findRecentByUserId(@Param("userId") Integer userId);

    /**
     * 특정 활동에 대한 마일리지 지급 여부 확인
     */
    @Query("SELECT COUNT(h) > 0 FROM MileageHistory h " +
           "WHERE h.user.userId = :userId " +
           "AND h.activityType = :activityType " +
           "AND h.activityId = :activityId")
    boolean existsByUserIdAndActivity(
            @Param("userId") Integer userId,
            @Param("activityType") String activityType,
            @Param("activityId") Long activityId);

    /**
     * 월별 마일리지 적립 통계
     */
    @Query("SELECT YEAR(h.earnedAt), MONTH(h.earnedAt), COALESCE(SUM(h.points), 0) " +
           "FROM MileageHistory h " +
           "WHERE h.user.userId = :userId " +
           "GROUP BY YEAR(h.earnedAt), MONTH(h.earnedAt) " +
           "ORDER BY YEAR(h.earnedAt) DESC, MONTH(h.earnedAt) DESC")
    List<Object[]> getMonthlyStatistics(@Param("userId") Integer userId);

}
