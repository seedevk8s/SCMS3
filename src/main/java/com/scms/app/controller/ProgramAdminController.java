package com.scms.app.controller;

import com.scms.app.dto.ProgramRequest;
import com.scms.app.dto.ProgramResponse;
import com.scms.app.model.Program;
import com.scms.app.model.ProgramStatus;
import com.scms.app.model.UserRole;
import com.scms.app.service.ProgramService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 관리자용 프로그램 관리 컨트롤러
 */
@Controller
@RequestMapping("/admin/programs")
@RequiredArgsConstructor
@Slf4j
public class ProgramAdminController {

    private final ProgramService programService;

    /**
     * 관리자 권한 체크
     */
    private boolean checkAdminRole(HttpSession session) {
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        log.info("=== 권한 체크: isAdmin = {}", isAdmin);
        return isAdmin != null && isAdmin;
    }

    /**
     * 프로그램 목록 조회
     */
    @GetMapping
    public String listPrograms(Model model, HttpSession session) {
        log.info("========================================");
        log.info("=== /admin/programs 호출됨");
        log.info("=== Session ID: {}", session.getId());
        log.info("=== Session userId: {}", session.getAttribute("userId"));
        log.info("=== Session name: {}", session.getAttribute("name"));
        log.info("=== Session isAdmin: {}", session.getAttribute("isAdmin"));

        // 관리자 권한 체크
        if (!checkAdminRole(session)) {
            log.warn("=== 관리자 권한 없음! 홈으로 리다이렉트");
            return "redirect:/";
        }

        log.info("=== 관리자 권한 확인 완료");
        List<Program> programs = programService.getAllPrograms();
        List<ProgramResponse> programResponses = programs.stream()
                .map(ProgramResponse::from)
                .collect(Collectors.toList());

        log.info("=== 프로그램 개수: {}", programResponses.size());
        model.addAttribute("programs", programResponses);
        model.addAttribute("pageTitle", "프로그램 관리");

        log.info("=== 반환할 템플릿: admin/program-list");
        log.info("=== 이 템플릿은 layout/admin-layout을 사용해야 함");
        log.info("========================================");
        return "admin/program-list";
    }

    /**
     * 프로그램 등록 폼
     */
    @GetMapping("/new")
    public String newProgramForm(Model model, HttpSession session) {
        // 관리자 권한 체크
        if (!checkAdminRole(session)) {
            return "redirect:/";
        }

        model.addAttribute("programRequest", new ProgramRequest());
        model.addAttribute("statuses", ProgramStatus.values());
        model.addAttribute("pageTitle", "프로그램 등록");
        model.addAttribute("isEdit", false);
        return "admin/program-form";
    }

    /**
     * 프로그램 등록 처리
     */
    @PostMapping("/new")
    public String createProgram(
            @Valid @ModelAttribute ProgramRequest request,
            BindingResult bindingResult,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        // 관리자 권한 체크
        if (!checkAdminRole(session)) {
            return "redirect:/";
        }

        // 유효성 검사 실패
        if (bindingResult.hasErrors()) {
            model.addAttribute("statuses", ProgramStatus.values());
            model.addAttribute("pageTitle", "프로그램 등록");
            model.addAttribute("isEdit", false);
            return "admin/program-form";
        }

        // 프로그램 생성
        Program program = Program.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .content(request.getContent())
                .department(request.getDepartment())
                .college(request.getCollege())
                .category(request.getCategory())
                .subCategory(request.getSubCategory())
                .applicationStartDate(request.getApplicationStartDate())
                .applicationEndDate(request.getApplicationEndDate())
                .maxParticipants(request.getMaxParticipants())
                .currentParticipants(0)
                .thumbnailUrl(request.getThumbnailUrl())
                .hits(0)
                .status(request.getStatus())
                .build();

        Program createdProgram = programService.createProgram(program);

        redirectAttributes.addFlashAttribute("message", "프로그램이 성공적으로 등록되었습니다.");
        redirectAttributes.addFlashAttribute("messageType", "success");

        return "redirect:/admin/programs";
    }

    /**
     * 프로그램 수정 폼
     */
    @GetMapping("/{id}/edit")
    public String editProgramForm(
            @PathVariable Integer id,
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // 관리자 권한 체크
        if (!checkAdminRole(session)) {
            return "redirect:/";
        }

        try {
            Program program = programService.getProgram(id);

            // Program을 ProgramRequest로 변환
            ProgramRequest request = new ProgramRequest();
            request.setTitle(program.getTitle());
            request.setDescription(program.getDescription());
            request.setContent(program.getContent());
            request.setDepartment(program.getDepartment());
            request.setCollege(program.getCollege());
            request.setCategory(program.getCategory());
            request.setSubCategory(program.getSubCategory());
            request.setApplicationStartDate(program.getApplicationStartDate());
            request.setApplicationEndDate(program.getApplicationEndDate());
            request.setMaxParticipants(program.getMaxParticipants());
            request.setThumbnailUrl(program.getThumbnailUrl());
            request.setStatus(program.getStatus());

            model.addAttribute("programRequest", request);
            model.addAttribute("programId", id);
            model.addAttribute("statuses", ProgramStatus.values());
            model.addAttribute("pageTitle", "프로그램 수정");
            model.addAttribute("isEdit", true);
            return "admin/program-form";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("message", "프로그램을 찾을 수 없습니다.");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/admin/programs";
        }
    }

    /**
     * 프로그램 수정 처리
     */
    @PostMapping("/{id}/edit")
    public String updateProgram(
            @PathVariable Integer id,
            @Valid @ModelAttribute ProgramRequest request,
            BindingResult bindingResult,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        // 관리자 권한 체크
        if (!checkAdminRole(session)) {
            return "redirect:/";
        }

        // 유효성 검사 실패
        if (bindingResult.hasErrors()) {
            model.addAttribute("programId", id);
            model.addAttribute("statuses", ProgramStatus.values());
            model.addAttribute("pageTitle", "프로그램 수정");
            model.addAttribute("isEdit", true);
            return "admin/program-form";
        }

        try {
            // 프로그램 데이터 생성
            Program programData = Program.builder()
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .content(request.getContent())
                    .department(request.getDepartment())
                    .college(request.getCollege())
                    .category(request.getCategory())
                    .subCategory(request.getSubCategory())
                    .applicationStartDate(request.getApplicationStartDate())
                    .applicationEndDate(request.getApplicationEndDate())
                    .maxParticipants(request.getMaxParticipants())
                    .thumbnailUrl(request.getThumbnailUrl())
                    .status(request.getStatus())
                    .build();

            programService.updateProgram(id, programData);

            redirectAttributes.addFlashAttribute("message", "프로그램이 성공적으로 수정되었습니다.");
            redirectAttributes.addFlashAttribute("messageType", "success");

            return "redirect:/admin/programs";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("message", "프로그램을 찾을 수 없습니다.");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/admin/programs";
        }
    }

    /**
     * 프로그램 삭제
     */
    @PostMapping("/{id}/delete")
    public String deleteProgram(
            @PathVariable Integer id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // 관리자 권한 체크
        if (!checkAdminRole(session)) {
            return "redirect:/";
        }

        try {
            programService.deleteProgram(id);

            redirectAttributes.addFlashAttribute("message", "프로그램이 성공적으로 삭제되었습니다.");
            redirectAttributes.addFlashAttribute("messageType", "success");

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("message", "프로그램을 찾을 수 없습니다.");
            redirectAttributes.addFlashAttribute("messageType", "error");
        }

        return "redirect:/admin/programs";
    }
}
