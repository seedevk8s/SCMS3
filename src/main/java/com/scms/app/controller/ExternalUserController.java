package com.scms.app.controller;

import com.scms.app.dto.*;
import com.scms.app.model.ExternalUser;
import com.scms.app.service.ExternalUserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 외부회원 REST API Controller
 */
@RestController
@RequestMapping("/api/external")
@RequiredArgsConstructor
@Slf4j
public class ExternalUserController {

    private final ExternalUserService externalUserService;

    /**
     * 회원가입 API
     *
     * @param request 회원가입 요청
     * @return 성공 메시지 및 사용자 ID
     */
    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signup(@Valid @RequestBody ExternalSignupRequest request) {
        try {
            ExternalUser user = externalUserService.signup(request);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "회원가입이 완료되었습니다. 이메일을 확인해주세요.");
            response.put("userId", user.getUserId());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error("회원가입 중 오류 발생", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage() != null ? e.getMessage() : "회원가입 중 오류가 발생했습니다");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 이메일 중복 체크 API
     *
     * @param email 확인할 이메일
     * @return 중복 여부
     */
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmail(@RequestParam String email) {
        boolean exists = externalUserService.checkEmailDuplicate(email);

        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);

        return ResponseEntity.ok(response);
    }

    /**
     * 이메일 인증 API
     *
     * @param token 인증 토큰
     * @return 성공 메시지
     */
    @GetMapping("/verify-email")
    public ResponseEntity<Map<String, Object>> verifyEmail(@RequestParam String token) {
        try {
            externalUserService.verifyEmail(token);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "이메일 인증이 완료되었습니다");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 외부회원 로그인 API
     *
     * @param request 로그인 요청
     * @param session HTTP 세션
     * @return 로그인 응답
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @Valid @RequestBody ExternalLoginRequest request,
            HttpSession session) {
        try {
            ExternalUser user = externalUserService.login(request.getEmail(), request.getPassword());

            // 세션에 사용자 정보 저장
            session.setAttribute("externalUserId", user.getUserId());
            session.setAttribute("externalUserEmail", user.getEmail());
            session.setAttribute("externalUserName", user.getName());
            session.setAttribute("userType", "EXTERNAL");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "로그인 성공");
            response.put("user", ExternalUserResponse.from(user));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("로그인 실패: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 현재 로그인한 외부회원 정보 조회 API
     *
     * @param session HTTP 세션
     * @return 외부회원 정보
     */
    @GetMapping("/me")
    public ResponseEntity<ExternalUserResponse> getCurrentUser(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("externalUserId");

        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        ExternalUser user = externalUserService.findById(userId);
        return ResponseEntity.ok(ExternalUserResponse.from(user));
    }

    /**
     * 인증 메일 재발송 API
     *
     * @param email 이메일
     * @return 성공 메시지
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, Object>> resendVerificationEmail(@RequestParam String email) {
        try {
            externalUserService.resendVerificationEmail(email);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "인증 메일이 재발송되었습니다. 이메일을 확인해주세요.");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error("인증 메일 재발송 실패: {}", email, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "인증 메일 재발송 중 오류가 발생했습니다");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 프로필 수정 API
     *
     * @param request 프로필 수정 요청
     * @param session HTTP 세션
     * @return 수정된 프로필 정보
     */
    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @Valid @RequestBody ExternalProfileUpdateRequest request,
            HttpSession session) {
        try {
            Integer userId = (Integer) session.getAttribute("externalUserId");
            if (userId == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "로그인이 필요합니다");
                return ResponseEntity.status(401).body(errorResponse);
            }

            ExternalUser updatedUser = externalUserService.updateProfile(userId, request);

            // 세션 정보 업데이트
            session.setAttribute("externalUserName", updatedUser.getName());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "프로필이 수정되었습니다");
            response.put("user", ExternalUserResponse.from(updatedUser));

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error("프로필 수정 중 오류 발생", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "프로필 수정 중 오류가 발생했습니다");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 비밀번호 변경 API
     *
     * @param request 비밀번호 변경 요청
     * @param session HTTP 세션
     * @return 성공 메시지
     */
    @PutMapping("/password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @Valid @RequestBody ExternalPasswordChangeRequest request,
            HttpSession session) {
        try {
            Integer userId = (Integer) session.getAttribute("externalUserId");
            if (userId == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "로그인이 필요합니다");
                return ResponseEntity.status(401).body(errorResponse);
            }

            externalUserService.changePassword(userId, request);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "비밀번호가 변경되었습니다");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error("비밀번호 변경 중 오류 발생", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "비밀번호 변경 중 오류가 발생했습니다");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 프로필 이미지 업로드 API
     *
     * @param file 업로드할 이미지 파일
     * @param session HTTP 세션
     * @return 저장된 이미지 URL
     */
    @PostMapping("/profile-image")
    public ResponseEntity<Map<String, Object>> uploadProfileImage(
            @RequestParam("file") MultipartFile file,
            HttpSession session) {
        try {
            Integer userId = (Integer) session.getAttribute("externalUserId");
            if (userId == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "로그인이 필요합니다");
                return ResponseEntity.status(401).body(errorResponse);
            }

            String imageUrl = externalUserService.updateProfileImage(userId, file);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "프로필 이미지가 업로드되었습니다");
            response.put("imageUrl", imageUrl);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error("프로필 이미지 업로드 중 오류 발생", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "이미지 업로드 중 오류가 발생했습니다");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 회원 탈퇴 API
     *
     * @param password 비밀번호 (로컬 계정인 경우)
     * @param session HTTP 세션
     * @return 성공 메시지
     */
    @DeleteMapping("/account")
    public ResponseEntity<Map<String, Object>> deleteAccount(
            @RequestParam(required = false) String password,
            HttpSession session) {
        try {
            Integer userId = (Integer) session.getAttribute("externalUserId");
            if (userId == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "로그인이 필요합니다");
                return ResponseEntity.status(401).body(errorResponse);
            }

            externalUserService.deleteAccount(userId, password);

            // 세션 무효화
            session.invalidate();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "회원 탈퇴가 완료되었습니다");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error("회원 탈퇴 중 오류 발생", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "회원 탈퇴 중 오류가 발생했습니다");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 로그아웃 API
     *
     * @param session HTTP 세션
     * @return 성공 메시지
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpSession session) {
        session.invalidate();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "로그아웃 되었습니다");

        return ResponseEntity.ok(response);
    }
}
