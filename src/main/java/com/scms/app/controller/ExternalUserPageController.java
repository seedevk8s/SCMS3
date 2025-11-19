package com.scms.app.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 외부회원 페이지 Controller
 */
@Controller
@RequestMapping("/external")
@RequiredArgsConstructor
@Slf4j
public class ExternalUserPageController {

    /**
     * 회원가입 페이지
     */
    @GetMapping("/signup")
    public String signupPage() {
        return "external/signup";
    }

    /**
     * 이메일 인증 안내 페이지
     */
    @GetMapping("/verification-sent")
    public String verificationSentPage(@RequestParam String email, Model model) {
        model.addAttribute("email", email);
        return "external/verification-sent";
    }

    /**
     * 이메일 인증 완료 페이지
     */
    @GetMapping("/verify-success")
    public String verifySuccessPage() {
        return "external/verify-success";
    }

    /**
     * 프로필 관리 페이지
     */
    @GetMapping("/profile")
    public String profilePage() {
        return "external/profile";
    }
}
