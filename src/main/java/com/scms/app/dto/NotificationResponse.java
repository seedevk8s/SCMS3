package com.scms.app.dto;

import com.scms.app.model.Notification;
import com.scms.app.model.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 알림 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    private Integer notificationId;
    private String title;
    private String content;
    private NotificationType type;
    private String typeTitle;
    private Boolean isRead;
    private String relatedUrl;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    /**
     * Entity를 DTO로 변환
     */
    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
                .notificationId(notification.getNotificationId())
                .title(notification.getTitle())
                .content(notification.getContent())
                .type(notification.getType())
                .typeTitle(notification.getType().getTitle())
                .isRead(notification.getIsRead())
                .relatedUrl(notification.getRelatedUrl())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .build();
    }
}
