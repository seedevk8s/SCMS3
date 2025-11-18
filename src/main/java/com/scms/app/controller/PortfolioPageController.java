package com.scms.app.controller;

import com.scms.app.dto.PortfolioDetailResponse;
import com.scms.app.dto.PortfolioResponse;
import com.scms.app.service.PortfolioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 포트폴리오 페이지 Controller
 */
@Controller
@RequestMapping("/portfolio")
@RequiredArgsConstructor
@Slf4j
public class PortfolioPageController {

    private final PortfolioService portfolioService;

    /**
     * 포트폴리오 목록 페이지
     */
    @GetMapping
    public String portfolioList(HttpSession session, Model model) {
        // 로그인 확인
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            log.warn("포트폴리오 목록 페이지 접근 실패: 로그인 필요");
            return "redirect:/login";
        }

        try {
            List<PortfolioResponse> portfolios = portfolioService.getPortfoliosByUserId(userId);
            model.addAttribute("portfolios", portfolios);
            model.addAttribute("portfolioCount", portfolios.size());

            log.info("포트폴리오 목록 페이지 접근: userId={}, count={}", userId, portfolios.size());
            return "portfolio/list";

        } catch (Exception e) {
            log.error("포트폴리오 목록 조회 실패: userId={}", userId, e);
            model.addAttribute("error", "포트폴리오 목록을 불러올 수 없습니다");
            return "error";
        }
    }

    /**
     * 포트폴리오 생성 페이지
     */
    @GetMapping("/create")
    public String createPortfolioPage(HttpSession session, Model model) {
        // 로그인 확인
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            log.warn("포트폴리오 생성 페이지 접근 실패: 로그인 필요");
            return "redirect:/login";
        }

        log.info("포트폴리오 생성 페이지 접근: userId={}", userId);
        return "portfolio/form";
    }

    /**
     * 포트폴리오 수정 페이지
     */
    @GetMapping("/{portfolioId}/edit")
    public String editPortfolioPage(
            @PathVariable Long portfolioId,
            HttpSession session,
            Model model) {

        // 로그인 확인
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            log.warn("포트폴리오 수정 페이지 접근 실패: 로그인 필요");
            return "redirect:/login";
        }

        try {
            PortfolioDetailResponse portfolio = portfolioService.getPortfolioDetail(portfolioId);

            // 권한 확인
            if (!portfolio.getUserId().equals(userId)) {
                log.warn("포트폴리오 수정 페이지 접근 실패: 권한 없음 userId={}, portfolioId={}", userId, portfolioId);
                model.addAttribute("error", "포트폴리오를 수정할 권한이 없습니다");
                return "error";
            }

            model.addAttribute("portfolio", portfolio);
            model.addAttribute("isEdit", true);

            log.info("포트폴리오 수정 페이지 접근: userId={}, portfolioId={}", userId, portfolioId);
            return "portfolio/form";

        } catch (IllegalArgumentException e) {
            log.warn("포트폴리오 수정 페이지 접근 실패: portfolioId={}", portfolioId);
            model.addAttribute("error", "포트폴리오를 찾을 수 없습니다");
            return "error";
        } catch (Exception e) {
            log.error("포트폴리오 수정 페이지 접근 실패: portfolioId={}", portfolioId, e);
            model.addAttribute("error", "포트폴리오를 불러올 수 없습니다");
            return "error";
        }
    }

    /**
     * 포트폴리오 상세 페이지
     */
    @GetMapping("/{portfolioId}")
    public String portfolioView(
            @PathVariable Long portfolioId,
            HttpSession session,
            HttpServletRequest request,
            Model model) {

        try {
            PortfolioDetailResponse portfolio = portfolioService.getPortfolioDetail(portfolioId);
            Integer userId = (Integer) session.getAttribute("userId");

            // 조회 권한 확인
            boolean isOwner = userId != null && portfolio.getUserId().equals(userId);
            boolean isPublic = portfolio.getVisibility() == com.scms.app.model.PortfolioVisibility.PUBLIC;

            if (!isOwner && !isPublic) {
                log.warn("포트폴리오 접근 권한 없음: userId={}, portfolioId={}", userId, portfolioId);
                model.addAttribute("error", "비공개 포트폴리오입니다");
                return "error";
            }

            // 조회 기록 (본인이 아닌 경우에만)
            if (!isOwner) {
                String ipAddress = getClientIP(request);
                String userAgent = request.getHeader("User-Agent");
                portfolioService.recordView(portfolioId, userId, ipAddress, userAgent);
            }

            model.addAttribute("portfolio", portfolio);
            model.addAttribute("isOwner", isOwner);

            log.info("포트폴리오 상세 페이지 접근: portfolioId={}, userId={}", portfolioId, userId);
            return "portfolio/view";

        } catch (IllegalArgumentException e) {
            log.warn("포트폴리오 상세 페이지 접근 실패: portfolioId={}", portfolioId);
            model.addAttribute("error", "포트폴리오를 찾을 수 없습니다");
            return "error";
        } catch (Exception e) {
            log.error("포트폴리오 상세 페이지 접근 실패: portfolioId={}", portfolioId, e);
            model.addAttribute("error", "포트폴리오를 불러올 수 없습니다");
            return "error";
        }
    }

    /**
     * 공유 포트폴리오 공개 페이지
     */
    @GetMapping("/shared/{shareToken}")
    public String sharedPortfolio(
            @PathVariable String shareToken,
            HttpServletRequest request,
            Model model) {

        try {
            String ipAddress = getClientIP(request);
            String userAgent = request.getHeader("User-Agent");

            PortfolioDetailResponse portfolio = portfolioService.getPortfolioByShareToken(
                    shareToken, ipAddress, userAgent);

            model.addAttribute("portfolio", portfolio);
            model.addAttribute("isShared", true);

            log.info("공유 포트폴리오 접근: shareToken={}", shareToken);
            return "portfolio/share";

        } catch (IllegalArgumentException e) {
            log.warn("공유 포트폴리오 접근 실패: shareToken={}", shareToken);
            model.addAttribute("error", "유효하지 않거나 만료된 공유 링크입니다");
            return "error";
        } catch (Exception e) {
            log.error("공유 포트폴리오 접근 실패: shareToken={}", shareToken, e);
            model.addAttribute("error", "포트폴리오를 불러올 수 없습니다");
            return "error";
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
