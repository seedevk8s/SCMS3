package com.scms.app.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 관리자 페이지 컨트롤러
 * - 관리자 대시보드 및 기본 페이지 제공
 */
@Slf4j
@Controller
@RequestMapping("/admin")
public class AdminPageController {

    /**
     * 관리자 대시보드
     */
    @GetMapping
    public String adminDashboard(Model model) {
        log.info("관리자 대시보드 접근");
        model.addAttribute("currentUri", "/admin");
        return "admin/dashboard";
    }

    /**
     * 사용자 관리 (내부회원)
     */
    @GetMapping("/users")
    public String userManagement(Model model) {
        log.info("사용자 관리 페이지 접근");
        model.addAttribute("currentUri", "/admin/users");
        return "admin/user-list";
    }

    /**
     * 통계
     */
    @GetMapping("/statistics")
    public String statistics(Model model) {
        log.info("통계 페이지 접근");
        model.addAttribute("currentUri", "/admin/statistics");
        return "admin/statistics";
    }

    /**
     * 리포트
     */
    @GetMapping("/reports")
    public String reports(Model model) {
        log.info("리포트 페이지 접근");
        model.addAttribute("currentUri", "/admin/reports");
        return "admin/reports";
    }

    /**
     * 시스템 설정
     */
    @GetMapping("/settings")
    public String settings(Model model) {
        log.info("시스템 설정 페이지 접근");
        model.addAttribute("currentUri", "/admin/settings");
        return "admin/settings";
    }
}
