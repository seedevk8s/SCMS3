package com.scms.app.controller;

import com.scms.app.dto.NotificationResponse;
import com.scms.app.model.Notification;
import com.scms.app.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 알림 REST API Controller
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 알림 목록 조회
     */
    @GetMapping
    public ResponseEntity<?> getNotifications(HttpSession session) {
        try {
            Integer userId = (Integer) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "로그인이 필요합니다."));
            }

            List<Notification> notifications = notificationService.getNotificationsByUser(userId);
            List<NotificationResponse> response = notifications.stream()
                    .map(NotificationResponse::from)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("알림 목록 조회 실패: error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "알림 목록 조회에 실패했습니다."));
        }
    }

    /**
     * 읽지 않은 알림 목록 조회
     */
    @GetMapping("/unread")
    public ResponseEntity<?> getUnreadNotifications(HttpSession session) {
        try {
            Integer userId = (Integer) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "로그인이 필요합니다."));
            }

            List<Notification> notifications = notificationService.getUnreadNotifications(userId);
            List<NotificationResponse> response = notifications.stream()
                    .map(NotificationResponse::from)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("읽지 않은 알림 조회 실패: error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "읽지 않은 알림 조회에 실패했습니다."));
        }
    }

    /**
     * 읽지 않은 알림 개수 조회
     */
    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount(HttpSession session) {
        try {
            Integer userId = (Integer) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.ok(Map.of("count", 0));
            }

            Long count = notificationService.getUnreadCount(userId);
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            log.error("읽지 않은 알림 개수 조회 실패: error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "읽지 않은 알림 개수 조회에 실패했습니다."));
        }
    }

    /**
     * 알림 읽음 처리
     */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<?> markAsRead(
            @PathVariable Integer notificationId,
            HttpSession session) {
        try {
            Integer userId = (Integer) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "로그인이 필요합니다."));
            }

            notificationService.markAsRead(userId, notificationId);
            log.info("알림 읽음 처리 성공: notificationId={}, userId={}", notificationId, userId);

            return ResponseEntity.ok(Map.of("message", "알림이 읽음 처리되었습니다."));
        } catch (IllegalStateException e) {
            log.warn("알림 읽음 처리 실패: notificationId={}, error={}", notificationId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("알림 읽음 처리 실패: notificationId={}, error={}", notificationId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "알림 읽음 처리에 실패했습니다."));
        }
    }

    /**
     * 모든 알림 읽음 처리
     */
    @PutMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(HttpSession session) {
        try {
            Integer userId = (Integer) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "로그인이 필요합니다."));
            }

            int count = notificationService.markAllAsRead(userId);
            log.info("모든 알림 읽음 처리 성공: userId={}, count={}", userId, count);

            return ResponseEntity.ok(Map.of(
                    "message", "모든 알림이 읽음 처리되었습니다.",
                    "count", count
            ));
        } catch (Exception e) {
            log.error("모든 알림 읽음 처리 실패: error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "모든 알림 읽음 처리에 실패했습니다."));
        }
    }

    /**
     * 알림 삭제
     */
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<?> deleteNotification(
            @PathVariable Integer notificationId,
            HttpSession session) {
        try {
            Integer userId = (Integer) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "로그인이 필요합니다."));
            }

            notificationService.deleteNotification(userId, notificationId);
            log.info("알림 삭제 성공: notificationId={}, userId={}", notificationId, userId);

            return ResponseEntity.ok(Map.of("message", "알림이 삭제되었습니다."));
        } catch (IllegalStateException e) {
            log.warn("알림 삭제 실패: notificationId={}, error={}", notificationId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("알림 삭제 실패: notificationId={}, error={}", notificationId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "알림 삭제에 실패했습니다."));
        }
    }

    /**
     * 모든 알림 삭제
     */
    @DeleteMapping("/all")
    public ResponseEntity<?> deleteAllNotifications(HttpSession session) {
        try {
            Integer userId = (Integer) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "로그인이 필요합니다."));
            }

            notificationService.deleteAllNotifications(userId);
            log.info("모든 알림 삭제 성공: userId={}", userId);

            return ResponseEntity.ok(Map.of("message", "모든 알림이 삭제되었습니다."));
        } catch (Exception e) {
            log.error("모든 알림 삭제 실패: error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "모든 알림 삭제에 실패했습니다."));
        }
    }
}
