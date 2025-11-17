package com.scms.app.controller;

import com.scms.app.model.MileageHistory;
import com.scms.app.model.MileageRule;
import com.scms.app.service.MileageService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CHAMP 마일리지 Controller (학생용)
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class MileageController {

    private final MileageService mileageService;

    /**
     * 마일리지 메인 페이지
     */
    @GetMapping("/mileage")
    public String mileagePage(HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/auth/login";
        }

        try {
            // 총 마일리지
            Long totalMileage = mileageService.getTotalMileage(userId);
            model.addAttribute("totalMileage", totalMileage);

            // 최근 10개 내역
            List<MileageHistory> recentHistory = mileageService.getRecentMileageHistory(userId, 10);
            model.addAttribute("recentHistory", recentHistory);

            // 활동 타입별 통계
            Map<String, Map<String, Object>> statistics = mileageService.getMileageStatistics(userId);
            model.addAttribute("statistics", statistics);

            // 내 랭킹
            Map<String, Object> myRanking = mileageService.getUserMileageRanking(userId);
            model.addAttribute("myRanking", myRanking);

            // TOP 10 랭킹
            List<Map<String, Object>> topRankings = mileageService.getMileageRanking()
                    .stream().limit(10).toList();
            model.addAttribute("topRankings", topRankings);

            // 활성화된 규칙 (마일리지 획득 방법 안내용)
            List<MileageRule> activeRules = mileageService.getActiveRules();
            model.addAttribute("activeRules", activeRules);

            log.info("마일리지 페이지 조회: userId={}, totalMileage={}", userId, totalMileage);
            return "mileage";
        } catch (Exception e) {
            log.error("마일리지 페이지 조회 실패: userId={}, error={}", userId, e.getMessage(), e);
            model.addAttribute("error", "마일리지 정보를 불러오는데 실패했습니다.");
            return "error";
        }
    }

    /**
     * API: 내 총 마일리지 조회
     */
    @GetMapping("/api/mileage/my")
    @ResponseBody
    public ResponseEntity<?> getMyMileage(HttpSession session) {
        try {
            Integer userId = (Integer) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "로그인이 필요합니다."));
            }

            Long totalMileage = mileageService.getTotalMileage(userId);
            return ResponseEntity.ok(Map.of("totalMileage", totalMileage));
        } catch (Exception e) {
            log.error("마일리지 조회 실패: error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "마일리지 조회에 실패했습니다."));
        }
    }

    /**
     * API: 마일리지 적립 내역 조회
     */
    @GetMapping("/api/mileage/history")
    @ResponseBody
    public ResponseEntity<?> getMileageHistory(HttpSession session) {
        try {
            Integer userId = (Integer) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "로그인이 필요합니다."));
            }

            List<MileageHistory> history = mileageService.getMileageHistory(userId);

            // DTO 변환
            List<Map<String, Object>> historyData = history.stream()
                    .map(h -> {
                        Map<String, Object> data = new HashMap<>();
                        data.put("historyId", h.getHistoryId());
                        data.put("activityType", h.getActivityType());
                        data.put("activityName", h.getActivityName());
                        data.put("points", h.getPoints());
                        data.put("description", h.getDescription());
                        data.put("earnedAt", h.getEarnedAt().toString());
                        return data;
                    })
                    .toList();

            return ResponseEntity.ok(historyData);
        } catch (Exception e) {
            log.error("마일리지 내역 조회 실패: error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "마일리지 내역 조회에 실패했습니다."));
        }
    }

    /**
     * API: 활동 타입별 마일리지 통계 조회
     */
    @GetMapping("/api/mileage/statistics")
    @ResponseBody
    public ResponseEntity<?> getMileageStatistics(HttpSession session) {
        try {
            Integer userId = (Integer) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "로그인이 필요합니다."));
            }

            Map<String, Map<String, Object>> statistics = mileageService.getMileageStatistics(userId);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("마일리지 통계 조회 실패: error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "마일리지 통계 조회에 실패했습니다."));
        }
    }

    /**
     * API: 마일리지 랭킹 조회
     */
    @GetMapping("/api/mileage/ranking")
    @ResponseBody
    public ResponseEntity<?> getMileageRanking(HttpSession session) {
        try {
            Integer userId = (Integer) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "로그인이 필요합니다."));
            }

            // 전체 랭킹 (TOP 100)
            List<Map<String, Object>> rankings = mileageService.getMileageRanking();

            // 내 랭킹
            Map<String, Object> myRanking = mileageService.getUserMileageRanking(userId);

            Map<String, Object> result = new HashMap<>();
            result.put("rankings", rankings);
            result.put("myRanking", myRanking);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("마일리지 랭킹 조회 실패: error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "마일리지 랭킹 조회에 실패했습니다."));
        }
    }

    /**
     * API: 월별 마일리지 통계 조회
     */
    @GetMapping("/api/mileage/monthly-statistics")
    @ResponseBody
    public ResponseEntity<?> getMonthlyStatistics(HttpSession session) {
        try {
            Integer userId = (Integer) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "로그인이 필요합니다."));
            }

            List<Map<String, Object>> monthlyStats = mileageService.getMonthlyStatistics(userId);
            return ResponseEntity.ok(monthlyStats);
        } catch (Exception e) {
            log.error("월별 마일리지 통계 조회 실패: error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "월별 마일리지 통계 조회에 실패했습니다."));
        }
    }

    /**
     * API: 활성화된 마일리지 규칙 조회 (마일리지 획득 방법)
     */
    @GetMapping("/api/mileage/rules")
    @ResponseBody
    public ResponseEntity<?> getActiveRules() {
        try {
            List<MileageRule> rules = mileageService.getActiveRules();

            List<Map<String, Object>> rulesData = rules.stream()
                    .map(r -> {
                        Map<String, Object> data = new HashMap<>();
                        data.put("ruleId", r.getRuleId());
                        data.put("activityType", r.getActivityType());
                        data.put("activityName", r.getActivityName());
                        data.put("points", r.getPoints());
                        data.put("description", r.getDescription());
                        return data;
                    })
                    .toList();

            return ResponseEntity.ok(rulesData);
        } catch (Exception e) {
            log.error("마일리지 규칙 조회 실패: error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "마일리지 규칙 조회에 실패했습니다."));
        }
    }
}
