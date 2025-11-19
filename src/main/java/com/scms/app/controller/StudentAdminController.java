package com.scms.app.controller;

import com.scms.app.dto.UserCreateRequest;
import com.scms.app.dto.UserResponse;
import com.scms.app.dto.UserUpdateRequest;
import com.scms.app.model.User;
import com.scms.app.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 관리자 학생 관리 컨트롤러
 */
@Slf4j
@Controller
@RequestMapping("/admin/students")
@RequiredArgsConstructor
public class StudentAdminController {

    private final UserService userService;

    /**
     * 학생 관리 페이지
     */
    @GetMapping
    public String studentListPage(Model model) {
        model.addAttribute("currentUri", "/admin/students");
        return "admin/student-list";
    }

    /**
     * 학생 목록 조회 API (페이징, 검색)
     */
    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) Integer grade) {

        try {
            Page<User> studentPage = userService.getStudents(page, size, search, department, grade);

            // User 엔티티를 UserResponse DTO로 변환
            List<UserResponse> content = studentPage.getContent().stream()
                    .map(UserResponse::from)
                    .collect(java.util.stream.Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("content", content);
            response.put("totalElements", studentPage.getTotalElements());
            response.put("totalPages", studentPage.getTotalPages());
            response.put("currentPage", studentPage.getNumber());
            response.put("size", studentPage.getSize());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("학생 목록 조회 실패", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 학생 통계 조회 API
     */
    @GetMapping("/statistics")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getStatistics() {
        try {
            Map<String, Object> stats = userService.getStudentStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("학생 통계 조회 실패", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 학생 상세 조회 API
     */
    @GetMapping("/{userId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getStudent(@PathVariable Integer userId) {
        try {
            UserResponse student = userService.getUser(userId);
            return ResponseEntity.ok(Map.of("student", student));
        } catch (Exception e) {
            log.error("학생 조회 실패: ID {}", userId, e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 학생 정보 수정 API
     */
    @PutMapping("/{userId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateStudent(
            @PathVariable Integer userId,
            @Valid @RequestBody UserUpdateRequest request) {

        try {
            UserResponse updatedStudent = userService.updateStudentByAdmin(userId, request);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "학생 정보가 수정되었습니다",
                    "student", updatedStudent
            ));
        } catch (Exception e) {
            log.error("학생 정보 수정 실패: ID {}", userId, e);
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        }
    }

    /**
     * 학생 계정 잠금/해제 API
     */
    @PostMapping("/{userId}/toggle-lock")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleLock(@PathVariable Integer userId) {
        try {
            boolean isLocked = userService.toggleStudentLock(userId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "locked", isLocked,
                    "message", isLocked ? "계정이 잠겼습니다" : "계정 잠금이 해제되었습니다"
            ));
        } catch (Exception e) {
            log.error("학생 계정 잠금/해제 실패: ID {}", userId, e);
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        }
    }

    /**
     * 학생 비밀번호 초기화 API
     */
    @PostMapping("/{userId}/reset-password")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> resetPassword(@PathVariable Integer userId) {
        try {
            userService.resetStudentPasswordByAdmin(userId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "비밀번호가 생년월일(YYMMDD)로 초기화되었습니다"
            ));
        } catch (Exception e) {
            log.error("학생 비밀번호 초기화 실패: ID {}", userId, e);
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        }
    }

    /**
     * 학생 로그인 실패 횟수 초기화 API
     */
    @PostMapping("/{userId}/reset-fail-count")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> resetFailCount(@PathVariable Integer userId) {
        try {
            userService.resetStudentFailCount(userId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "로그인 실패 횟수가 초기화되었습니다"
            ));
        } catch (Exception e) {
            log.error("학생 로그인 실패 횟수 초기화 실패: ID {}", userId, e);
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        }
    }

    /**
     * 학생 삭제 API
     */
    @DeleteMapping("/{userId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteStudent(@PathVariable Integer userId) {
        try {
            userService.deleteStudentByAdmin(userId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "학생이 삭제되었습니다"
            ));
        } catch (Exception e) {
            log.error("학생 삭제 실패: ID {}", userId, e);
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        }
    }

    /**
     * 학생 일괄 등록 API
     */
    @PostMapping("/bulk")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> bulkCreateStudents(
            @Valid @RequestBody List<UserCreateRequest> requests) {

        try {
            Map<String, Object> result = userService.bulkCreateStudents(requests);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("학생 일괄 등록 실패", e);
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        }
    }

    /**
     * 학생 단건 등록 API
     */
    @PostMapping
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createStudent(
            @Valid @RequestBody UserCreateRequest request) {

        try {
            UserResponse student = userService.createUser(request);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "학생이 등록되었습니다",
                    "student", student
            ));
        } catch (Exception e) {
            log.error("학생 등록 실패", e);
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        }
    }
}
