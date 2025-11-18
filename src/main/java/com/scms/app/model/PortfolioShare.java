package com.scms.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 포트폴리오 공유 엔티티
 */
@Entity
@Table(name = "portfolio_shares")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "share_id")
    private Long shareId;

    @Column(name = "portfolio_id", nullable = false)
    private Long portfolioId;

    @Column(name = "share_token", nullable = false, unique = true, length = 100)
    private String shareToken;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * 공유 링크 취소
     */
    public void revoke() {
        this.revokedAt = LocalDateTime.now();
    }

    /**
     * 조회수 증가
     */
    public void incrementViewCount() {
        this.viewCount++;
    }

    /**
     * 공유 링크가 유효한지 확인
     */
    public boolean isValid() {
        // 취소되었는지 확인
        if (revokedAt != null) {
            return false;
        }
        // 만료 시간 확인
        if (expiresAt != null && LocalDateTime.now().isAfter(expiresAt)) {
            return false;
        }
        return true;
    }
}
