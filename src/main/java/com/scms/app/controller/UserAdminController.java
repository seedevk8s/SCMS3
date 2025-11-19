package com.scms.app.controller;

import com.scms.app.dto.UserResponse;
import com.scms.app.model.UserRole;
import com.scms.app.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 관리자 전체 사용자 관리 API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserAdminController {

    private final UserService userService;

    /**
     * 전체 사용자 목록 조회 API (페이징, 검색)
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) Boolean locked
    ) {
        try {
            Page<UserResponse> usersPage = userService.getAllUsers(page, size, search, role, locked);

            Map<String, Object> response = new HashMap<>();
            response.put("content", usersPage.getContent());
            response.put("totalElements", usersPage.getTotalElements());
            response.put("totalPages", usersPage.getTotalPages());
            response.put("currentPage", usersPage.getNumber());
            response.put("size", usersPage.getSize());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("사용자 목록 조회 실패", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "사용자 목록을 불러오는데 실패했습니다");
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 사용자 통계 조회
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        try {
            Map<String, Object> stats = userService.getAllUserStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("사용자 통계 조회 실패", e);
            return ResponseEntity.internalServerError().body(new HashMap<>());
        }
    }

    /**
     * 사용자 계정 잠금/해제
     */
    @PostMapping("/{userId}/toggle-lock")
    public ResponseEntity<Map<String, Object>> toggleLock(@PathVariable Long userId) {
        try {
            userService.toggleUserLock(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "계정 상태가 변경되었습니다");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("계정 잠금/해제 실패: userId={}", userId, e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "작업 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 사용자 비밀번호 초기화
     */
    @PostMapping("/{userId}/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@PathVariable Long userId) {
        try {
            String message = userService.resetUserPassword(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", message);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("비밀번호 초기화 실패: userId={}", userId, e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "작업 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}
