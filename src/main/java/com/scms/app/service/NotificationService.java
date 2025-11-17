package com.scms.app.service;

import com.scms.app.model.Notification;
import com.scms.app.model.NotificationType;
import com.scms.app.model.User;
import com.scms.app.repository.NotificationRepository;
import com.scms.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 알림 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * 알림 생성
     */
    @Transactional
    public Notification createNotification(Integer userId, String title, String content,
                                          NotificationType type, String relatedUrl) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다."));

        // 알림 생성
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .content(content)
                .type(type)
                .relatedUrl(relatedUrl)
                .isRead(false)
                .build();

        notification = notificationRepository.save(notification);
        log.info("알림 생성: notificationId={}, userId={}, type={}",
                notification.getNotificationId(), userId, type);

        return notification;
    }

    /**
     * 알림 생성 (간편 버전 - relatedUrl 없이)
     */
    @Transactional
    public Notification createNotification(Integer userId, String title, String content,
                                          NotificationType type) {
        return createNotification(userId, title, content, type, null);
    }

    /**
     * 타입별 기본 알림 생성
     */
    @Transactional
    public Notification createNotificationByType(Integer userId, NotificationType type,
                                                String programTitle, String relatedUrl) {
        String title = type.getTitle();
        String content = generateContentByType(type, programTitle);

        return createNotification(userId, title, content, type, relatedUrl);
    }

    /**
     * 타입에 따른 알림 내용 생성
     */
    private String generateContentByType(NotificationType type, String programTitle) {
        return switch (type) {
            case APPLICATION_APPROVED ->
                "'" + programTitle + "' 프로그램 신청이 승인되었습니다. 프로그램 일정을 확인해주세요.";
            case APPLICATION_REJECTED ->
                "'" + programTitle + "' 프로그램 신청이 거부되었습니다.";
            case APPLICATION_CANCELLED ->
                "'" + programTitle + "' 프로그램 신청이 취소되었습니다.";
            case PROGRAM_STARTING ->
                "'" + programTitle + "' 프로그램이 내일 시작됩니다. 준비해주세요!";
            case DEADLINE_APPROACHING ->
                "'" + programTitle + "' 프로그램 신청 마감이 3일 남았습니다. 서둘러 신청하세요!";
        };
    }

    /**
     * 사용자별 알림 목록 조회 (최신순)
     */
    public List<Notification> getNotificationsByUser(Integer userId) {
        return notificationRepository.findByUserIdAndDeletedAtIsNull(userId);
    }

    /**
     * 사용자의 읽지 않은 알림 목록 조회
     */
    public List<Notification> getUnreadNotifications(Integer userId) {
        return notificationRepository.findUnreadByUserId(userId);
    }

    /**
     * 사용자의 읽지 않은 알림 개수
     */
    public Long getUnreadCount(Integer userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    /**
     * 알림 읽음 처리
     */
    @Transactional
    public void markAsRead(Integer userId, Integer notificationId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new IllegalStateException("알림을 찾을 수 없습니다."));

        if (!notification.getIsRead()) {
            notification.markAsRead();
            notificationRepository.save(notification);
            log.info("알림 읽음 처리: notificationId={}, userId={}", notificationId, userId);
        }
    }

    /**
     * 모든 알림 읽음 처리
     */
    @Transactional
    public int markAllAsRead(Integer userId) {
        int count = notificationRepository.markAllAsReadByUserId(userId);
        log.info("모든 알림 읽음 처리: userId={}, count={}", userId, count);
        return count;
    }

    /**
     * 알림 삭제 (Soft Delete)
     */
    @Transactional
    public void deleteNotification(Integer userId, Integer notificationId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new IllegalStateException("알림을 찾을 수 없습니다."));

        notification.delete();
        notificationRepository.save(notification);
        log.info("알림 삭제: notificationId={}, userId={}", notificationId, userId);
    }

    /**
     * 모든 알림 삭제 (Soft Delete)
     */
    @Transactional
    public void deleteAllNotifications(Integer userId) {
        List<Notification> notifications = notificationRepository.findByUserIdAndDeletedAtIsNull(userId);

        for (Notification notification : notifications) {
            notification.delete();
        }

        notificationRepository.saveAll(notifications);
        log.info("모든 알림 삭제: userId={}, count={}", userId, notifications.size());
    }

    /**
     * 특정 타입의 알림 조회
     */
    public List<Notification> getNotificationsByType(Integer userId, NotificationType type) {
        return notificationRepository.findByUserIdAndType(userId, type);
    }

    /**
     * 오래된 알림 정리 (스케줄러에서 호출)
     */
    @Transactional
    public int cleanupOldNotifications() {
        int count = notificationRepository.deleteOldNotifications();
        log.info("오래된 알림 정리 완료: count={}", count);
        return count;
    }
}
