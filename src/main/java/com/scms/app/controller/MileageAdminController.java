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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CHAMP 마일리지 관리자 Controller
 */
@Controller
@RequestMapping("/admin/mileage")
@RequiredArgsConstructor
@Slf4j
public class MileageAdminController {

    private final MileageService mileageService;

    /**
     * 관리자 권한 체크
     */
    private boolean checkAdminRole(HttpSession session) {
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        return isAdmin != null && isAdmin;
    }

    /**
     * 마일리지 관리 페이지 (관리자용)
     */
    @GetMapping
    public String mileageAdminPage(HttpSession session, Model model) {
        if (!checkAdminRole(session)) {
            return "redirect:/";
        }

        try {
            // 모든 규칙 조회
            List<MileageRule> rules = mileageService.getAllRules();
            model.addAttribute("rules", rules);

            // 전체 랭킹
            List<Map<String, Object>> rankings = mileageService.getMileageRanking();
            model.addAttribute("rankings", rankings);

            log.info("마일리지 관리 페이지 조회");
            return "admin/mileage-admin";
        } catch (Exception e) {
            log.error("마일리지 관리 페이지 조회 실패: error={}", e.getMessage(), e);
            model.addAttribute("error", "마일리지 관리 페이지를 불러오는데 실패했습니다.");
            return "error";
        }
    }

    /**
     * API: 모든 마일리지 규칙 조회 (관리자용)
     */
    @GetMapping("/api/rules")
    @ResponseBody
    public ResponseEntity<?> getAllRules(HttpSession session) {
        try {
            if (!checkAdminRole(session)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "관리자 권한이 필요합니다."));
            }

            List<MileageRule> rules = mileageService.getAllRules();

            List<Map<String, Object>> rulesData = rules.stream()
                    .map(r -> {
                        Map<String, Object> data = new HashMap<>();
                        data.put("ruleId", r.getRuleId());
                        data.put("activityType", r.getActivityType());
                        data.put("activityName", r.getActivityName());
                        data.put("points", r.getPoints());
                        data.put("description", r.getDescription());
                        data.put("isActive", r.getIsActive());
                        data.put("createdAt", r.getCreatedAt().toString());
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

    /**
     * API: 마일리지 규칙 생성
     */
    @PostMapping("/api/rules")
    @ResponseBody
    public ResponseEntity<?> createRule(
            @RequestBody Map<String, Object> request,
            HttpSession session) {
        try {
            if (!checkAdminRole(session)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "관리자 권한이 필요합니다."));
            }

            String activityType = (String) request.get("activityType");
            String activityName = (String) request.get("activityName");
            Integer points = (Integer) request.get("points");
            String description = (String) request.get("description");

            MileageRule rule = mileageService.createRule(
                    activityType, activityName, points, description);

            log.info("마일리지 규칙 생성: ruleId={}, activityType={}, points={}",
                    rule.getRuleId(), activityType, points);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "마일리지 규칙이 생성되었습니다.");
            response.put("ruleId", rule.getRuleId());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("마일리지 규칙 생성 실패: error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "마일리지 규칙 생성에 실패했습니다."));
        }
    }

    /**
     * API: 마일리지 규칙 수정
     */
    @PutMapping("/api/rules/{ruleId}")
    @ResponseBody
    public ResponseEntity<?> updateRule(
            @PathVariable Long ruleId,
            @RequestBody Map<String, Object> request,
            HttpSession session) {
        try {
            if (!checkAdminRole(session)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "관리자 권한이 필요합니다."));
            }

            String activityName = (String) request.get("activityName");
            Integer points = (Integer) request.get("points");
            String description = (String) request.get("description");
            Boolean isActive = (Boolean) request.get("isActive");

            MileageRule rule = mileageService.updateRule(
                    ruleId, activityName, points, description, isActive);

            log.info("마일리지 규칙 수정: ruleId={}, points={}, isActive={}",
                    ruleId, points, isActive);

            return ResponseEntity.ok(Map.of("message", "마일리지 규칙이 수정되었습니다."));
        } catch (IllegalStateException e) {
            log.warn("마일리지 규칙 수정 실패: ruleId={}, error={}", ruleId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("마일리지 규칙 수정 실패: ruleId={}, error={}", ruleId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "마일리지 규칙 수정에 실패했습니다."));
        }
    }

    /**
     * API: 마일리지 규칙 삭제
     */
    @DeleteMapping("/api/rules/{ruleId}")
    @ResponseBody
    public ResponseEntity<?> deleteRule(
            @PathVariable Long ruleId,
            HttpSession session) {
        try {
            if (!checkAdminRole(session)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "관리자 권한이 필요합니다."));
            }

            mileageService.deleteRule(ruleId);
            log.info("마일리지 규칙 삭제: ruleId={}", ruleId);

            return ResponseEntity.ok(Map.of("message", "마일리지 규칙이 삭제되었습니다."));
        } catch (IllegalStateException e) {
            log.warn("마일리지 규칙 삭제 실패: ruleId={}, error={}", ruleId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("마일리지 규칙 삭제 실패: ruleId={}, error={}", ruleId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "마일리지 규칙 삭제에 실패했습니다."));
        }
    }

    /**
     * API: 마일리지 수동 지급/차감
     */
    @PostMapping("/api/award")
    @ResponseBody
    public ResponseEntity<?> manualAwardMileage(
            @RequestBody Map<String, Object> request,
            HttpSession session) {
        try {
            if (!checkAdminRole(session)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "관리자 권한이 필요합니다."));
            }

            Integer userId = (Integer) request.get("userId");
            Integer points = (Integer) request.get("points");
            String activityName = (String) request.get("activityName");
            String description = (String) request.get("description");
            Integer adminId = (Integer) session.getAttribute("userId");

            MileageHistory history = mileageService.manualAwardMileage(
                    userId, points, activityName, description, adminId);

            log.info("마일리지 수동 지급: userId={}, points={}, adminId={}",
                    userId, points, adminId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "마일리지가 지급되었습니다.");
            response.put("historyId", history.getHistoryId());

            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            log.warn("마일리지 수동 지급 실패: error={}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("마일리지 수동 지급 실패: error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "마일리지 수동 지급에 실패했습니다."));
        }
    }

    /**
     * API: 전체 사용자 마일리지 랭킹 조회 (관리자용)
     */
    @GetMapping("/api/ranking")
    @ResponseBody
    public ResponseEntity<?> getAllRankings(HttpSession session) {
        try {
            if (!checkAdminRole(session)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "관리자 권한이 필요합니다."));
            }

            List<Map<String, Object>> rankings = mileageService.getMileageRanking();
            return ResponseEntity.ok(rankings);
        } catch (Exception e) {
            log.error("마일리지 랭킹 조회 실패: error={}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "마일리지 랭킹 조회에 실패했습니다."));
        }
    }
}
