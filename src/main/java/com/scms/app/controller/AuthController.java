package com.scms.app.controller;

import com.scms.app.dto.LoginRequest;
import com.scms.app.dto.LoginResponse;
import com.scms.app.dto.PasswordChangeRequest;
import com.scms.app.dto.PasswordResetRequest;
import com.scms.app.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

/**
 * 인증 관련 REST API Controller
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final SecurityContextRepository securityContextRepository;

    /**
     * 로그인 API
     *
     * @param request 로그인 요청 (학번, 비밀번호)
     * @param httpRequest HTTP 요청 (IP, User-Agent 추출용)
     * @param session HTTP 세션
     * @return 로그인 응답 (사용자 정보, 최초 로그인 여부)
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpSession session) {

        log.info("로그인 시도: 학번 {}", request.getStudentNum());

        LoginResponse response = userService.login(request, httpRequest);

        // 세션에 사용자 정보 저장
        session.setAttribute("userId", response.getUserId());
        session.setAttribute("studentNum", response.getStudentNum());
        session.setAttribute("name", response.getName());
        session.setAttribute("role", response.getRole());
        session.setAttribute("isFirstLogin", response.getIsFirstLogin());
        session.setAttribute("isAdmin", response.getRole() == com.scms.app.model.UserRole.ADMIN);

        // Spring Security SecurityContext에 인증 정보 설정
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                response.getStudentNum(), // principal
                null, // credentials (비밀번호는 저장하지 않음)
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + response.getRole().name()))
        );

        // SecurityContext에 인증 정보 저장
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        // SecurityContext를 HttpSession에 명시적으로 저장 (REST API 로그인이므로 필수!)
        securityContextRepository.saveContext(securityContext, httpRequest, null);

        log.info("로그인 성공: {} ({}) - Role: {}", response.getName(), response.getStudentNum(), response.getRole());
        log.info("Spring Security 인증 정보 저장 완료: Principal={}, Authorities={}",
                authentication.getPrincipal(), authentication.getAuthorities());

        return ResponseEntity.ok(response);
    }

    /**
     * 로그아웃 API
     *
     * @param session HTTP 세션
     * @return 성공 메시지
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");

        if (userId != null) {
            log.info("로그아웃: userId {}", userId);
        }

        // 세션 무효화
        session.invalidate();

        // Spring Security SecurityContext 클리어
        SecurityContextHolder.clearContext();

        return ResponseEntity.ok("로그아웃 완료");
    }

    /**
     * 비밀번호 변경 API
     *
     * @param request 비밀번호 변경 요청
     * @param session HTTP 세션
     * @return 성공 메시지
     */
    @PostMapping("/password/change")
    public ResponseEntity<String> changePassword(
            @Valid @RequestBody PasswordChangeRequest request,
            HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다");
        }

        userService.changePassword(userId, request);

        // 최초 로그인 플래그 제거
        session.setAttribute("isFirstLogin", false);

        log.info("비밀번호 변경 완료: userId {}", userId);

        return ResponseEntity.ok("비밀번호가 변경되었습니다");
    }

    /**
     * 비밀번호 찾기/재설정 API
     *
     * @param request 비밀번호 재설정 요청 (학번, 이름, 생년월일)
     * @return 성공 메시지
     */
    @PostMapping("/password/reset")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody PasswordResetRequest request) {

        log.info("비밀번호 재설정 요청: 학번 {}", request.getStudentNum());

        userService.resetPassword(request);

        return ResponseEntity.ok("비밀번호가 초기화되었습니다. 생년월일(6자리)로 로그인해주세요.");
    }

    /**
     * 현재 로그인 사용자 정보 조회 API
     *
     * @param session HTTP 세션
     * @return 로그인 사용자 정보
     */
    @GetMapping("/me")
    public ResponseEntity<LoginResponse> getCurrentUser(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        LoginResponse response = LoginResponse.builder()
                .userId(userId)
                .studentNum((Integer) session.getAttribute("studentNum"))
                .name((String) session.getAttribute("name"))
                .role((com.scms.app.model.UserRole) session.getAttribute("role"))
                .isFirstLogin((Boolean) session.getAttribute("isFirstLogin"))
                .build();

        return ResponseEntity.ok(response);
    }
}
