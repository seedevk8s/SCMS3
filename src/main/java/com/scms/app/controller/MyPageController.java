package com.scms.app.controller;

import com.scms.app.model.ApplicationStatus;
import com.scms.app.model.ProgramApplication;
import com.scms.app.service.ProgramApplicationService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 마이페이지 Controller
 */
@Controller
@RequestMapping("/mypage")
@RequiredArgsConstructor
@Slf4j
public class MyPageController {

    private final ProgramApplicationService applicationService;

    /**
     * 마이페이지 - 나의 프로그램 목록
     */
    @GetMapping
    public String myPage(
            @RequestParam(required = false) String status,
            HttpSession session,
            Model model) {

        // 로그인 확인
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            log.warn("마이페이지 접근 실패: 로그인 필요");
            return "redirect:/login";
        }

        String userName = (String) session.getAttribute("userName");

        try {
            // 사용자의 모든 신청 내역 조회
            List<ProgramApplication> allApplications = applicationService.getUserApplications(userId);

            // 상태별 분류
            List<ProgramApplication> pendingList = allApplications.stream()
                    .filter(a -> a.getStatus() == ApplicationStatus.PENDING)
                    .collect(Collectors.toList());

            List<ProgramApplication> approvedList = allApplications.stream()
                    .filter(a -> a.getStatus() == ApplicationStatus.APPROVED)
                    .collect(Collectors.toList());

            List<ProgramApplication> completedList = allApplications.stream()
                    .filter(a -> a.getStatus() == ApplicationStatus.COMPLETED)
                    .collect(Collectors.toList());

            List<ProgramApplication> rejectedList = allApplications.stream()
                    .filter(a -> a.getStatus() == ApplicationStatus.REJECTED)
                    .collect(Collectors.toList());

            List<ProgramApplication> cancelledList = allApplications.stream()
                    .filter(a -> a.getStatus() == ApplicationStatus.CANCELLED)
                    .collect(Collectors.toList());

            // 필터링된 목록 (쿼리 파라미터에 따라)
            List<ProgramApplication> filteredApplications;
            if (status != null && !status.isEmpty()) {
                switch (status.toUpperCase()) {
                    case "PENDING":
                        filteredApplications = pendingList;
                        break;
                    case "APPROVED":
                        filteredApplications = approvedList;
                        break;
                    case "COMPLETED":
                        filteredApplications = completedList;
                        break;
                    case "REJECTED":
                        filteredApplications = rejectedList;
                        break;
                    case "CANCELLED":
                        filteredApplications = cancelledList;
                        break;
                    default:
                        filteredApplications = allApplications;
                }
            } else {
                filteredApplications = allApplications;
            }

            // Model에 데이터 추가
            model.addAttribute("userName", userName);
            model.addAttribute("applications", filteredApplications);
            model.addAttribute("currentFilter", status != null ? status : "ALL");

            // 통계 정보
            model.addAttribute("totalCount", allApplications.size());
            model.addAttribute("pendingCount", pendingList.size());
            model.addAttribute("approvedCount", approvedList.size());
            model.addAttribute("completedCount", completedList.size());
            model.addAttribute("rejectedCount", rejectedList.size());
            model.addAttribute("cancelledCount", cancelledList.size());

            log.info("마이페이지 조회 성공: userId={}, 전체={}, 필터={}",
                    userId, allApplications.size(), status);

            return "mypage";

        } catch (Exception e) {
            log.error("마이페이지 조회 실패: userId={}", userId, e);
            model.addAttribute("error", "프로그램 목록을 불러올 수 없습니다.");
            return "mypage";
        }
    }
}
