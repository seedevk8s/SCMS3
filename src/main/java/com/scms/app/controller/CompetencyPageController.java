package com.scms.app.controller;

import com.scms.app.model.Student;
import com.scms.app.model.User;
import com.scms.app.repository.StudentRepository;
import com.scms.app.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 역량진단 페이지 Controller
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class CompetencyPageController {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    /**
     * 역량진단 결과 페이지
     */
    @GetMapping("/competency-result")
    public String competencyResult(HttpSession session, Model model) {
        // 로그인 확인
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            log.warn("역량진단 결과 페이지 접근 실패: 로그인 필요");
            return "redirect:/login";
        }

        // User 정보로 Student 찾기
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("역량진단 결과 페이지 접근 실패: 사용자를 찾을 수 없음 userId={}", userId);
            return "redirect:/login";
        }

        // studentNum(학번)으로 Student 찾기
        Student student = studentRepository.findByStudentId(String.valueOf(user.getStudentNum())).orElse(null);
        if (student == null) {
            log.warn("역량진단 결과 페이지 접근 실패: 학생 정보를 찾을 수 없음 studentNum={}", user.getStudentNum());
            model.addAttribute("error", "학생 정보를 찾을 수 없습니다. 관리자에게 문의하세요.");
            return "error";
        }

        // Student ID를 모델에 추가
        model.addAttribute("studentId", student.getId());

        log.info("역량진단 결과 페이지 접근: userId={}, studentId={}, studentNum={}",
                userId, student.getId(), user.getStudentNum());
        return "competency-result";
    }

    /**
     * 역량진단 페이지 (별칭)
     */
    @GetMapping("/assessment")
    public String assessment(HttpSession session) {
        // 로그인 확인
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            log.warn("역량진단 페이지 접근 실패: 로그인 필요");
            return "redirect:/login";
        }

        log.info("역량진단 페이지 접근: userId={} (redirect to result)", userId);
        // 현재는 결과 페이지로 리다이렉트
        return "redirect:/competency-result";
    }

    /**
     * 역량진단 입력 페이지 (미래 구현)
     */
    @GetMapping("/competency-assessment")
    public String competencyAssessment(HttpSession session, Model model) {
        // 로그인 확인
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            log.warn("역량진단 입력 페이지 접근 실패: 로그인 필요");
            return "redirect:/login";
        }

        log.info("역량진단 입력 페이지 접근: userId={}", userId);

        // TODO: 역량진단 입력 페이지 구현 시 활성화
        // 현재는 결과 페이지로 리다이렉트
        return "redirect:/competency-result";
    }
}
