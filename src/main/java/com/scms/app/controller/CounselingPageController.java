package com.scms.app.controller;

import com.scms.app.model.UserRole;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 상담 페이지 Controller
 */
@Controller
@RequestMapping("/counseling")
@RequiredArgsConstructor
public class CounselingPageController {

    /**
     * 상담 신청 페이지
     */
    @GetMapping
    public String consultationPage(HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        model.addAttribute("userName", session.getAttribute("name"));
        UserRole role = (UserRole) session.getAttribute("role");
        model.addAttribute("userRole", role);

        // 관리자 권한 설정
        if (session.getAttribute("isAdmin") == null && role != null) {
            session.setAttribute("isAdmin", role == UserRole.ADMIN);
        }

        model.addAttribute("pageTitle", "통합상담 신청");
        return "consultation";
    }

    /**
     * 상담 내역 페이지
     */
    @GetMapping("/list")
    public String consultationList(HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        model.addAttribute("userName", session.getAttribute("name"));
        UserRole role = (UserRole) session.getAttribute("role");
        model.addAttribute("userRole", role);

        if (session.getAttribute("isAdmin") == null && role != null) {
            session.setAttribute("isAdmin", role == UserRole.ADMIN);
        }

        model.addAttribute("pageTitle", "내 상담 내역");
        return "consultation-list";
    }

    /**
     * 상담 상세 페이지
     */
    @GetMapping("/{sessionId}")
    public String consultationDetail(
            @PathVariable Long sessionId,
            HttpSession session,
            Model model) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        model.addAttribute("userName", session.getAttribute("name"));
        UserRole role = (UserRole) session.getAttribute("role");
        model.addAttribute("userRole", role);

        if (session.getAttribute("isAdmin") == null && role != null) {
            session.setAttribute("isAdmin", role == UserRole.ADMIN);
        }

        model.addAttribute("sessionId", sessionId);
        model.addAttribute("pageTitle", "상담 상세");
        return "consultation-detail";
    }

    /**
     * 상담사 관리 페이지 (상담사용)
     */
    @GetMapping("/manage")
    public String counselorManage(HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        UserRole role = (UserRole) session.getAttribute("role");
        if (role != UserRole.COUNSELOR && role != UserRole.ADMIN) {
            return "redirect:/";
        }

        model.addAttribute("userName", session.getAttribute("name"));
        model.addAttribute("userRole", role);

        if (session.getAttribute("isAdmin") == null && role != null) {
            session.setAttribute("isAdmin", role == UserRole.ADMIN);
        }

        model.addAttribute("pageTitle", "상담 관리");
        return "counselor/consultation-manage";
    }

    /**
     * 관리자 상담 관리 페이지
     */
    @GetMapping("/admin")
    public String adminConsultation(HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        UserRole role = (UserRole) session.getAttribute("role");
        if (role != UserRole.ADMIN) {
            return "redirect:/";
        }

        model.addAttribute("userName", session.getAttribute("name"));
        model.addAttribute("userRole", role);
        session.setAttribute("isAdmin", true);

        model.addAttribute("pageTitle", "상담 관리 (관리자)");
        return "admin/consultation-admin";
    }

    /**
     * 관리자 상담사 관리 페이지
     */
    @GetMapping("/admin/counselors")
    public String adminCounselors(HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        UserRole role = (UserRole) session.getAttribute("role");
        if (role != UserRole.ADMIN) {
            return "redirect:/";
        }

        model.addAttribute("userName", session.getAttribute("name"));
        model.addAttribute("userRole", role);
        session.setAttribute("isAdmin", true);

        model.addAttribute("pageTitle", "상담사 관리");
        return "admin/counselor-management";
    }
}
