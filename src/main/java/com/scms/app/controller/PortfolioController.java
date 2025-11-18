package com.scms.app.controller;

import com.scms.app.dto.*;
import com.scms.app.model.Portfolio;
import com.scms.app.model.PortfolioVisibility;
import com.scms.app.service.PortfolioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 포트폴리오 REST API Controller
 */
@RestController
@RequestMapping("/api/portfolios")
@RequiredArgsConstructor
@Slf4j
public class PortfolioController {

    private final PortfolioService portfolioService;

    /**
     * 사용자의 포트폴리오 목록 조회
     */
    @GetMapping
    public ResponseEntity<?> getMyPortfolios(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인이 필요합니다"));
        }

        try {
            List<PortfolioResponse> portfolios = portfolioService.getPortfoliosByUserId(userId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "portfolios", portfolios,
                    "count", portfolios.size()
            ));
        } catch (Exception e) {
            log.error("포트폴리오 목록 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "포트폴리오 목록 조회에 실패했습니다"));
        }
    }

    /**
     * 포트폴리오 상세 조회
     */
    @GetMapping("/{portfolioId}")
    public ResponseEntity<?> getPortfolio(
            @PathVariable Long portfolioId,
            HttpSession session,
            HttpServletRequest request) {

        try {
            PortfolioDetailResponse portfolio = portfolioService.getPortfolioDetail(portfolioId);

            // 조회 기록 저장 (본인 포트폴리오가 아닌 경우에만)
            Integer userId = (Integer) session.getAttribute("userId");
            if (userId == null || !portfolio.getUserId().equals(userId)) {
                String ipAddress = getClientIP(request);
                String userAgent = request.getHeader("User-Agent");
                portfolioService.recordView(portfolioId, userId, ipAddress, userAgent);
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "portfolio", portfolio
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("포트폴리오 조회 실패: portfolioId={}", portfolioId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "포트폴리오 조회에 실패했습니다"));
        }
    }

    /**
     * 포트폴리오 생성
     */
    @PostMapping
    public ResponseEntity<?> createPortfolio(
            @Valid @RequestBody PortfolioCreateRequest request,
            HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인이 필요합니다"));
        }

        try {
            Portfolio portfolio = portfolioService.createPortfolio(userId, request);
            PortfolioResponse response = PortfolioResponse.from(portfolio);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "success", true,
                            "message", "포트폴리오가 생성되었습니다",
                            "portfolio", response
                    ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("포트폴리오 생성 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "포트폴리오 생성에 실패했습니다"));
        }
    }

    /**
     * 포트폴리오 수정
     */
    @PutMapping("/{portfolioId}")
    public ResponseEntity<?> updatePortfolio(
            @PathVariable Long portfolioId,
            @Valid @RequestBody PortfolioUpdateRequest request,
            HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인이 필요합니다"));
        }

        try {
            Portfolio portfolio = portfolioService.updatePortfolio(portfolioId, userId, request);
            PortfolioResponse response = PortfolioResponse.from(portfolio);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "포트폴리오가 수정되었습니다",
                    "portfolio", response
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("포트폴리오 수정 실패: portfolioId={}", portfolioId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "포트폴리오 수정에 실패했습니다"));
        }
    }

    /**
     * 포트폴리오 삭제
     */
    @DeleteMapping("/{portfolioId}")
    public ResponseEntity<?> deletePortfolio(
            @PathVariable Long portfolioId,
            HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인이 필요합니다"));
        }

        try {
            portfolioService.deletePortfolio(portfolioId, userId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "포트폴리오가 삭제되었습니다"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("포트폴리오 삭제 실패: portfolioId={}", portfolioId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "포트폴리오 삭제에 실패했습니다"));
        }
    }

    /**
     * 포트폴리오 공개 범위 변경
     */
    @PutMapping("/{portfolioId}/visibility")
    public ResponseEntity<?> updateVisibility(
            @PathVariable Long portfolioId,
            @RequestParam PortfolioVisibility visibility,
            HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인이 필요합니다"));
        }

        try {
            Portfolio portfolio = portfolioService.updateVisibility(portfolioId, userId, visibility);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "공개 범위가 변경되었습니다",
                    "visibility", portfolio.getVisibility()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("공개 범위 변경 실패: portfolioId={}", portfolioId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "공개 범위 변경에 실패했습니다"));
        }
    }

    /**
     * 공유 링크 생성
     */
    @PostMapping("/{portfolioId}/share")
    public ResponseEntity<?> createShareLink(
            @PathVariable Long portfolioId,
            @RequestParam(required = false) Integer expirationDays,
            HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인이 필요합니다"));
        }

        try {
            PortfolioShareResponse shareResponse = portfolioService.createShareLink(portfolioId, userId, expirationDays);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "공유 링크가 생성되었습니다",
                    "share", shareResponse
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("공유 링크 생성 실패: portfolioId={}", portfolioId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "공유 링크 생성에 실패했습니다"));
        }
    }

    /**
     * 공유 링크로 포트폴리오 조회
     */
    @GetMapping("/shared/{shareToken}")
    public ResponseEntity<?> getPortfolioByShareToken(
            @PathVariable String shareToken,
            HttpServletRequest request) {

        try {
            String ipAddress = getClientIP(request);
            String userAgent = request.getHeader("User-Agent");

            PortfolioDetailResponse portfolio = portfolioService.getPortfolioByShareToken(
                    shareToken, ipAddress, userAgent);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "portfolio", portfolio
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("공유 포트폴리오 조회 실패: shareToken={}", shareToken, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "포트폴리오 조회에 실패했습니다"));
        }
    }

    /**
     * 포트폴리오 검색
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchPortfolios(
            @RequestParam String keyword,
            HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인이 필요합니다"));
        }

        try {
            List<PortfolioResponse> portfolios = portfolioService.searchPortfolios(userId, keyword);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "portfolios", portfolios,
                    "count", portfolios.size()
            ));
        } catch (Exception e) {
            log.error("포트폴리오 검색 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "포트폴리오 검색에 실패했습니다"));
        }
    }

    /**
     * 클라이언트 IP 주소 추출
     */
    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
