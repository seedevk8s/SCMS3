package com.scms.app.repository;

import com.scms.app.model.Notification;
import com.scms.app.model.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 알림 Repository
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    /**
     * 사용자별 알림 목록 조회 (삭제되지 않은 것만, 최신순)
     * JOIN FETCH로 LazyInitializationException 방지
     */
    @Query("SELECT n FROM Notification n " +
           "LEFT JOIN FETCH n.user " +
           "WHERE n.user.userId = :userId " +
           "AND n.deletedAt IS NULL " +
           "ORDER BY n.createdAt DESC")
    List<Notification> findByUserIdAndDeletedAtIsNull(@Param("userId") Integer userId);

    /**
     * 사용자의 읽지 않은 알림 목록 조회 (최신순)
     */
    @Query("SELECT n FROM Notification n " +
           "LEFT JOIN FETCH n.user " +
           "WHERE n.user.userId = :userId " +
           "AND n.isRead = false " +
           "AND n.deletedAt IS NULL " +
           "ORDER BY n.createdAt DESC")
    List<Notification> findUnreadByUserId(@Param("userId") Integer userId);

    /**
     * 사용자의 읽지 않은 알림 개수
     */
    @Query("SELECT COUNT(n) FROM Notification n " +
           "WHERE n.user.userId = :userId " +
           "AND n.isRead = false " +
           "AND n.deletedAt IS NULL")
    Long countUnreadByUserId(@Param("userId") Integer userId);

    /**
     * 특정 알림 조회 (사용자 확인 포함)
     */
    @Query("SELECT n FROM Notification n " +
           "LEFT JOIN FETCH n.user " +
           "WHERE n.notificationId = :notificationId " +
           "AND n.user.userId = :userId " +
           "AND n.deletedAt IS NULL")
    Optional<Notification> findByIdAndUserId(
            @Param("notificationId") Integer notificationId,
            @Param("userId") Integer userId);

    /**
     * 사용자의 모든 알림 읽음 처리 (Bulk Update)
     */
    @Modifying
    @Query("UPDATE Notification n " +
           "SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP " +
           "WHERE n.user.userId = :userId " +
           "AND n.isRead = false " +
           "AND n.deletedAt IS NULL")
    int markAllAsReadByUserId(@Param("userId") Integer userId);

    /**
     * 특정 타입의 알림 조회
     */
    @Query("SELECT n FROM Notification n " +
           "LEFT JOIN FETCH n.user " +
           "WHERE n.user.userId = :userId " +
           "AND n.type = :type " +
           "AND n.deletedAt IS NULL " +
           "ORDER BY n.createdAt DESC")
    List<Notification> findByUserIdAndType(
            @Param("userId") Integer userId,
            @Param("type") NotificationType type);

    /**
     * 오래된 알림 삭제 (30일 이전 읽은 알림 또는 90일 이전 알림)
     */
    @Modifying
    @Query("UPDATE Notification n " +
           "SET n.deletedAt = CURRENT_TIMESTAMP " +
           "WHERE (n.isRead = true AND n.readAt < CURRENT_TIMESTAMP - 30 DAY) " +
           "OR (n.createdAt < CURRENT_TIMESTAMP - 90 DAY) " +
           "AND n.deletedAt IS NULL")
    int deleteOldNotifications();
}
