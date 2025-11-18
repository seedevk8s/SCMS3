package com.scms.app.dto;

import com.scms.app.model.PortfolioShare;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 포트폴리오 공유 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioShareResponse {

    private Long shareId;
    private Long portfolioId;
    private String shareToken;
    private LocalDateTime expiresAt;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime revokedAt;
    private Boolean isValid;
    private String shareUrl;

    /**
     * Entity를 DTO로 변환
     */
    public static PortfolioShareResponse from(PortfolioShare share) {
        return PortfolioShareResponse.builder()
                .shareId(share.getShareId())
                .portfolioId(share.getPortfolioId())
                .shareToken(share.getShareToken())
                .expiresAt(share.getExpiresAt())
                .viewCount(share.getViewCount())
                .createdAt(share.getCreatedAt())
                .revokedAt(share.getRevokedAt())
                .isValid(share.isValid())
                .build();
    }

    /**
     * Entity를 DTO로 변환 (공유 URL 포함)
     */
    public static PortfolioShareResponse from(PortfolioShare share, String baseUrl) {
        PortfolioShareResponse response = from(share);
        response.setShareUrl(baseUrl + "/portfolio/shared/" + share.getShareToken());
        return response;
    }
}
