package com.scms.app.controller;

import com.scms.app.dto.RecommendedProgramDto;
import com.scms.app.model.Program;
import com.scms.app.model.Student;
import com.scms.app.model.User;
import com.scms.app.model.UserRole;
import com.scms.app.repository.StudentRepository;
import com.scms.app.repository.UserRepository;
import com.scms.app.service.ProgramRecommendationService;
import com.scms.app.service.ProgramService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
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
@Slf4j
public class HomeController {

    private final ProgramService programService;
    private final ProgramRecommendationService recommendationService;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    /**
     * 홈 페이지
     */
    @GetMapping("/")
    public String home(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String college,
            @RequestParam(required = false) String category,
            Model model,
            HttpSession session) {
        // 세션에서 사용자 정보 가져오기
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId != null) {
            model.addAttribute("userName", session.getAttribute("name"));
            UserRole role = (UserRole) session.getAttribute("role");
            model.addAttribute("userRole", role);
            // 기존 세션에 isAdmin이 없으면 추가
            if (session.getAttribute("isAdmin") == null && role != null) {
                session.setAttribute("isAdmin", role == UserRole.ADMIN);
            }
        }

        // 필터 파라미터가 있으면 필터링, 없으면 전체 조회
        List<Program> programs;
        if (department != null || college != null || category != null) {
            programs = programService.getProgramsByFilters(department, college, category);
        } else {
            programs = programService.getMainPagePrograms();
        }
        model.addAttribute("programs", programs);

        // 현재 선택된 필터 정보를 Model에 추가
        model.addAttribute("selectedDepartment", department);
        model.addAttribute("selectedCollege", college);
        model.addAttribute("selectedCategory", category);

        model.addAttribute("pageTitle", "푸름대학교 학생성장지원센터 CHAMP");
        return "index";
    }

    /**
     * 프로그램 상세 페이지
     */
    @GetMapping("/programs/{programId}")
    public String programDetail(
            @org.springframework.web.bind.annotation.PathVariable Integer programId,
            Model model,
            HttpSession session) {
        // 세션에서 사용자 정보 가져오기
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId != null) {
            model.addAttribute("userName", session.getAttribute("name"));
            UserRole role = (UserRole) session.getAttribute("role");
            model.addAttribute("userRole", role);

            // isAdmin 세션 및 Model 설정
            boolean isAdmin = role == UserRole.ADMIN;
            session.setAttribute("isAdmin", isAdmin);
            model.addAttribute("isAdmin", isAdmin);
        } else {
            model.addAttribute("isAdmin", false);
        }

        // 프로그램 조회 (조회수 증가)
        try {
            Program program = programService.getProgramWithHitIncrement(programId);
            model.addAttribute("program", program);
            model.addAttribute("pageTitle", program.getTitle() + " - 프로그램 상세");
            return "program-detail";
        } catch (IllegalArgumentException e) {
            // 프로그램이 존재하지 않는 경우
            model.addAttribute("error", "프로그램을 찾을 수 없습니다.");
            return "redirect:/programs";
        }
    }

    /**
     * 프로그램 전체보기 페이지
     */
    @GetMapping("/programs")
    public String programs(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String college,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean recommended,
            @RequestParam(required = false) Long studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model,
            HttpSession session) {
        // 세션에서 사용자 정보 가져오기
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId != null) {
            model.addAttribute("userName", session.getAttribute("name"));
            UserRole role = (UserRole) session.getAttribute("role");
            model.addAttribute("userRole", role);
            if (session.getAttribute("isAdmin") == null && role != null) {
                session.setAttribute("isAdmin", role == UserRole.ADMIN);
            }
        }

        // 추천 모드 처리
        if (Boolean.TRUE.equals(recommended) && studentId != null) {
            log.info("추천 프로그램 모드: studentId={}", studentId);

            // 추천 프로그램 가져오기
            List<RecommendedProgramDto> recommendedPrograms =
                recommendationService.getRecommendedPrograms(studentId, 50); // 최대 50개

            // RecommendedProgramDto를 Program으로 변환하여 표시
            List<Program> programs = recommendedPrograms.stream()
                .map(dto -> {
                    try {
                        return programService.getProgram(dto.getProgramId());
                    } catch (Exception e) {
                        log.error("프로그램 조회 실패: programId={}", dto.getProgramId(), e);
                        return null;
                    }
                })
                .filter(p -> p != null)
                .toList();

            model.addAttribute("programs", programs);
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 1);
            model.addAttribute("totalItems", programs.size());
            model.addAttribute("pageSize", programs.size());
            model.addAttribute("pageTitle", "맞춤형 추천 프로그램 (" + programs.size() + "개)");
            model.addAttribute("recommendedMode", true);
            model.addAttribute("studentId", studentId);

            return "programs";
        }

        // 일반 모드: 필터링 또는 검색 with 페이지네이션
        Page<Program> programPage;
        if (search != null && !search.trim().isEmpty()) {
            programPage = programService.searchProgramsByTitleWithPagination(search, page, size);
        } else if (department != null || college != null || category != null) {
            programPage = programService.getProgramsByFiltersWithPagination(department, college, category, page, size);
        } else {
            programPage = programService.getAllProgramsWithPagination(page, size);
        }

        model.addAttribute("programs", programPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", programPage.getTotalPages());
        model.addAttribute("totalItems", programPage.getTotalElements());
        model.addAttribute("pageSize", size);

        // 현재 선택된 필터 정보를 Model에 추가
        model.addAttribute("selectedDepartment", department);
        model.addAttribute("selectedCollege", college);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("searchKeyword", search);

        model.addAttribute("pageTitle", "프로그램 전체보기");
        model.addAttribute("recommendedMode", false);
        return "programs";
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

    /**
     * 알림 페이지
     */
    @GetMapping("/notifications")
    public String notifications(HttpSession session, Model model) {
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

        model.addAttribute("pageTitle", "알림");
        return "notifications";
    }
}
