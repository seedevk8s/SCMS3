package com.scms.app.controller;

import com.scms.app.dto.ExternalUserResponse;
import com.scms.app.model.AccountStatus;
import com.scms.app.model.ExternalUser;
import com.scms.app.service.ExternalUserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 관리자용 외부회원 관리 Controller
 */
@Controller
@RequestMapping("/admin/external-users")
@RequiredArgsConstructor
@Slf4j
public class ExternalUserAdminController {

    private final ExternalUserService externalUserService;

    /**
     * 관리자 권한 체크
     */
    private boolean checkAdminRole(HttpSession session) {
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        return isAdmin != null && isAdmin;
    }

    /**
     * 외부회원 목록 페이지
     */
    @GetMapping
    public String listExternalUsers(Model model, HttpSession session) {
        if (!checkAdminRole(session)) {
            return "redirect:/login?error=관리자 권한이 필요합니다";
        }

        // 통계 정보 조회
        Map<String, Object> statistics = externalUserService.getExternalUserStatistics();
        model.addAttribute("statistics", statistics);

        return "admin/external-user-list";
    }

    /**
     * 외부회원 목록 API (페이징)
     */
    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getExternalUsersApi(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String provider,
            @RequestParam(required = false) Boolean emailVerified,
            HttpSession session) {

        if (!checkAdminRole(session)) {
            return ResponseEntity.status(403).body(Map.of("error", "관리자 권한이 필요합니다"));
        }

        Page<ExternalUser> usersPage = externalUserService.getAllExternalUsers(
                page, size, search, provider, emailVerified);

        Map<String, Object> response = new HashMap<>();
        response.put("users", usersPage.getContent().stream()
                .map(ExternalUserResponse::from)
                .toList());
        response.put("currentPage", usersPage.getNumber());
        response.put("totalPages", usersPage.getTotalPages());
        response.put("totalElements", usersPage.getTotalElements());
        response.put("size", usersPage.getSize());

        return ResponseEntity.ok(response);
    }

    /**
     * 외부회원 상세 조회 API
     */
    @GetMapping("/{userId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getExternalUserDetail(
            @PathVariable Integer userId,
            HttpSession session) {

        if (!checkAdminRole(session)) {
            return ResponseEntity.status(403).body(Map.of("error", "관리자 권한이 필요합니다"));
        }

        try {
            ExternalUser user = externalUserService.findById(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("user", ExternalUserResponse.from(user));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * 계정 잠금/해제 토글 API
     */
    @PostMapping("/{userId}/toggle-lock")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleAccountLock(
            @PathVariable Integer userId,
            HttpSession session) {

        if (!checkAdminRole(session)) {
            return ResponseEntity.status(403).body(Map.of("error", "관리자 권한이 필요합니다"));
        }

        try {
            boolean isLocked = externalUserService.toggleAccountLock(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("locked", isLocked);
            response.put("message", isLocked ? "계정이 잠겼습니다" : "계정 잠금이 해제되었습니다");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("계정 잠금 토글 실패", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * 이메일 인증 수동 처리 API
     */
    @PostMapping("/{userId}/verify-email")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> verifyEmail(
            @PathVariable Integer userId,
            HttpSession session) {

        if (!checkAdminRole(session)) {
            return ResponseEntity.status(403).body(Map.of("error", "관리자 권한이 필요합니다"));
        }

        try {
            externalUserService.verifyEmailManually(userId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "이메일 인증이 완료되었습니다"
            ));
        } catch (Exception e) {
            log.error("이메일 인증 처리 실패", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * 계정 상태 변경 API
     */
    @PostMapping("/{userId}/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> changeAccountStatus(
            @PathVariable Integer userId,
            @RequestParam AccountStatus status,
            HttpSession session) {

        if (!checkAdminRole(session)) {
            return ResponseEntity.status(403).body(Map.of("error", "관리자 권한이 필요합니다"));
        }

        try {
            externalUserService.changeAccountStatus(userId, status);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "계정 상태가 변경되었습니다"
            ));
        } catch (Exception e) {
            log.error("계정 상태 변경 실패", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * 회원 삭제 API
     */
    @DeleteMapping("/{userId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteExternalUser(
            @PathVariable Integer userId,
            HttpSession session) {

        if (!checkAdminRole(session)) {
            return ResponseEntity.status(403).body(Map.of("error", "관리자 권한이 필요합니다"));
        }

        try {
            externalUserService.deleteExternalUserByAdmin(userId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "회원이 삭제되었습니다"
            ));
        } catch (Exception e) {
            log.error("회원 삭제 실패", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * 통계 조회 API
     */
    @GetMapping("/statistics")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getStatistics(HttpSession session) {
        if (!checkAdminRole(session)) {
            return ResponseEntity.status(403).body(Map.of("error", "관리자 권한이 필요합니다"));
        }

        Map<String, Object> statistics = externalUserService.getExternalUserStatistics();
        return ResponseEntity.ok(statistics);
    }
}
