package com.scms.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 포트폴리오 조회 통계 엔티티
 */
@Entity
@Table(name = "portfolio_views")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "view_id")
    private Long viewId;

    @Column(name = "portfolio_id", nullable = false)
    private Long portfolioId;

    @Column(name = "viewer_user_id")
    private Integer viewerUserId;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(name = "share_token", length = 100)
    private String shareToken;

    @Column(name = "viewed_at", nullable = false, updatable = false)
    private LocalDateTime viewedAt;

    @PrePersist
    protected void onCreate() {
        viewedAt = LocalDateTime.now();
    }
}
