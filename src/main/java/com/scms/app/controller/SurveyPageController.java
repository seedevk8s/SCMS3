package com.scms.app.controller;

import com.scms.app.dto.SurveyDetailResponse;
import com.scms.app.dto.SurveyDTO;
import com.scms.app.service.SurveyResponseService;
import com.scms.app.service.SurveyService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 설문조사 페이지 컨트롤러
 */
@Controller
@RequestMapping("/survey")
@RequiredArgsConstructor
@Slf4j
public class SurveyPageController {

    private final SurveyService surveyService;
    private final SurveyResponseService surveyResponseService;

    /**
     * 설문 목록 페이지 (사용자용)
     */
    @GetMapping
    public String surveyList(HttpSession session, Model model,
                            @RequestParam(defaultValue = "0") int page) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            // 응답 가능한 설문 목록
            List<SurveyDTO> availableSurveys = surveyService.getAvailableSurveysForUser(userId);
            model.addAttribute("availableSurveys", availableSurveys);

            // 사용자의 응답 내역
            List<SurveyDTO> myResponses = surveyResponseService.getUserResponses(userId);
            model.addAttribute("myResponses", myResponses);

            log.info("설문 목록 페이지: userId={}, 응답 가능={}, 응답 완료={}",
                    userId, availableSurveys.size(), myResponses.size());
        } catch (Exception e) {
            log.error("설문 목록 조회 실패", e);
            model.addAttribute("error", "설문 목록을 불러올 수 없습니다");
        }

        return "survey/list";
    }

    /**
     * 설문 응답 페이지
     */
    @GetMapping("/{surveyId}/respond")
    public String surveyRespond(@PathVariable Long surveyId,
                               HttpSession session,
                               Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            SurveyDetailResponse survey = surveyService.getSurveyById(surveyId, userId);
            model.addAttribute("survey", survey);

            log.info("설문 응답 페이지: surveyId={}, userId={}", surveyId, userId);
        } catch (Exception e) {
            log.error("설문 조회 실패", e);
            model.addAttribute("error", e.getMessage());
            return "survey/list";
        }

        return "survey/respond";
    }

    /**
     * 응답 완료 페이지
     */
    @GetMapping("/complete")
    public String surveyComplete() {
        return "survey/complete";
    }

    /**
     * 관리자 - 설문 관리 페이지
     */
    @GetMapping("/admin")
    public String surveyAdmin(HttpSession session, Model model,
                             @RequestParam(defaultValue = "0") int page) {
        Integer userId = (Integer) session.getAttribute("userId");
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");

        if (userId == null) {
            return "redirect:/login";
        }

        if (isAdmin == null || !isAdmin) {
            return "redirect:/survey";
        }

        try {
            Page<SurveyDTO> surveys = surveyService.getActiveSurveys(page, 10, userId);
            model.addAttribute("surveys", surveys);
            model.addAttribute("currentPage", page);

            log.info("설문 관리 페이지: userId={}, page={}", userId, page);
        } catch (Exception e) {
            log.error("설문 관리 목록 조회 실패", e);
            model.addAttribute("error", "설문 목록을 불러올 수 없습니다");
        }

        return "survey/admin/list";
    }

    /**
     * 관리자 - 설문 생성 페이지
     */
    @GetMapping("/admin/create")
    public String surveyCreateForm(HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");

        if (userId == null) {
            return "redirect:/login";
        }

        if (isAdmin == null || !isAdmin) {
            return "redirect:/survey";
        }

        model.addAttribute("isEdit", false);
        return "survey/admin/form";
    }

    /**
     * 관리자 - 설문 수정 페이지
     */
    @GetMapping("/admin/{surveyId}/edit")
    public String surveyEditForm(@PathVariable Long surveyId,
                                HttpSession session,
                                Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");

        if (userId == null) {
            return "redirect:/login";
        }

        if (isAdmin == null || !isAdmin) {
            return "redirect:/survey";
        }

        try {
            SurveyDetailResponse survey = surveyService.getSurveyById(surveyId, userId);
            model.addAttribute("survey", survey);
            model.addAttribute("isEdit", true);

            log.info("설문 수정 페이지: surveyId={}, userId={}", surveyId, userId);
        } catch (Exception e) {
            log.error("설문 조회 실패", e);
            return "redirect:/survey/admin";
        }

        return "survey/admin/form";
    }

    /**
     * 관리자 - 설문 통계 페이지
     */
    @GetMapping("/admin/{surveyId}/statistics")
    public String surveyStatistics(@PathVariable Long surveyId,
                                  HttpSession session,
                                  Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");

        if (userId == null) {
            return "redirect:/login";
        }

        if (isAdmin == null || !isAdmin) {
            return "redirect:/survey";
        }

        try {
            model.addAttribute("surveyId", surveyId);
            log.info("설문 통계 페이지: surveyId={}, userId={}", surveyId, userId);
        } catch (Exception e) {
            log.error("통계 페이지 로드 실패", e);
            return "redirect:/survey/admin";
        }

        return "survey/admin/statistics";
    }
}
