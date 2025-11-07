package com.scms.app.controller;

import com.scms.app.model.Program;
import com.scms.app.model.UserRole;
import com.scms.app.service.ProgramService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 페이지 컨트롤러
 */
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProgramService programService;

    /**
     * 홈 페이지
     */
    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        // 세션에서 사용자 정보 가져오기
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId != null) {
            model.addAttribute("userName", session.getAttribute("name"));
            model.addAttribute("userRole", session.getAttribute("role"));
        }

        // 메인 페이지용 프로그램 목록 조회 (최신 8개)
        List<Program> programs = programService.getMainPagePrograms();
        model.addAttribute("programs", programs);

        model.addAttribute("pageTitle", "푸름대학교 학생성장지원센터 CHAMP");
        return "index";
    }

    /**
     * 로그인 페이지
     */
    @GetMapping("/login")
    public String login(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String message,
            Model model,
            HttpSession session) {

        // 이미 로그인되어 있으면 홈으로 리다이렉트
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId != null) {
            return "redirect:/";
        }

        if (error != null) {
            model.addAttribute("error", error);
        }
        if (message != null) {
            model.addAttribute("message", message);
        }

        model.addAttribute("pageTitle", "로그인");
        return "login";
    }

    /**
     * 로그아웃
     */
    @GetMapping("/logout")
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        // Spring Security SecurityContext도 클리어
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        return "redirect:/";
    }

    /**
     * 비밀번호 변경 페이지
     */
    @GetMapping("/password/change")
    public String passwordChange(HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        model.addAttribute("isFirstLogin", session.getAttribute("isFirstLogin"));
        model.addAttribute("pageTitle", "비밀번호 변경");
        return "password-change";
    }

    /**
     * 비밀번호 찾기 페이지
     */
    @GetMapping("/password/reset")
    public String passwordReset(Model model) {
        model.addAttribute("pageTitle", "비밀번호 찾기");
        return "password-reset";
    }
}
