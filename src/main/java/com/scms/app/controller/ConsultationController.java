package com.scms.app.controller;

import com.scms.app.dto.*;
import com.scms.app.model.ConsultationStatus;
import com.scms.app.model.User;
import com.scms.app.model.UserRole;
import com.scms.app.service.ConsultationRecordService;
import com.scms.app.service.ConsultationService;
import com.scms.app.service.CounselorService;
import com.scms.app.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 상담 API Controller
 */
@RestController
@RequestMapping("/api/consultations")
@RequiredArgsConstructor
@Slf4j
public class ConsultationController {

    private final ConsultationService consultationService;
    private final ConsultationRecordService recordService;
    private final CounselorService counselorService;
    private final UserService userService;

    /**
     * 상담 신청
     */
    @PostMapping
    public ResponseEntity<?> createConsultation(
            @RequestBody ConsultationRequest request,
            HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인이 필요합니다."));
        }

        try {
            request.validate();
            var consultation = consultationService.createConsultation(userId, request);
            var response = ConsultationResponse.from(consultation);

            log.info("상담 신청 성공: 학생 ID {}", userId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "상담 신청이 완료되었습니다.",
                    "consultation", response
            ));

        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("상담 신청 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            log.error("상담 신청 실패 (서버 오류): ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "상담 신청 중 오류가 발생했습니다."));
        }
    }

    /**
     * 내 상담 내역 조회
     */
    @GetMapping("/my")
    public ResponseEntity<?> getMyConsultations(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인이 필요합니다."));
        }

        try {
            List<ConsultationResponse> consultations = consultationService.getMyConsultations(userId);
            return ResponseEntity.ok(consultations);

        } catch (Exception e) {
            log.error("상담 내역 조회 실패: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "상담 내역 조회 중 오류가 발생했습니다."));
        }
    }

    /**
     * 상담 상세 조회
     */
    @GetMapping("/{sessionId}")
    public ResponseEntity<?> getConsultation(
            @PathVariable Long sessionId,
            HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인이 필요합니다."));
        }

        try {
            var consultation = consultationService.getSessionById(sessionId);
            var response = ConsultationResponse.from(consultation);

            // 권한 확인 (본인 또는 배정된 상담사만)
            if (!consultation.getStudent().getUserId().equals(userId) &&
                (consultation.getCounselor() == null ||
                 !consultation.getCounselor().getUser().getUserId().equals(userId))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "조회 권한이 없습니다."));
            }

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            log.error("상담 조회 실패: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "상담 조회 중 오류가 발생했습니다."));
        }
    }

    /**
     * 상담 취소
     */
    @PutMapping("/{sessionId}/cancel")
    public ResponseEntity<?> cancelConsultation(
            @PathVariable Long sessionId,
            HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인이 필요합니다."));
        }

        try {
            consultationService.cancelConsultation(userId, sessionId);

            log.info("상담 취소 성공: 세션 ID {}, 학생 ID {}", sessionId, userId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "상담이 취소되었습니다."
            ));

        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("상담 취소 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            log.error("상담 취소 실패 (서버 오류): ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "상담 취소 중 오류가 발생했습니다."));
        }
    }

    /**
     * 상담 승인 (상담사 또는 관리자)
     */
    @PutMapping("/{sessionId}/approve")
    public ResponseEntity<?> approveConsultation(
            @PathVariable Long sessionId,
            @RequestBody ConsultationStatusUpdateRequest request,
            HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인이 필요합니다."));
        }

        try {
            User user = userService.getUserById(userId);

            // 상담사 또는 관리자만 승인 가능
            if (user.getRole() != UserRole.COUNSELOR && user.getRole() != UserRole.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "상담사 또는 관리자만 승인할 수 있습니다."));
            }

            // Counselor 엔티티는 @MapsId를 사용하므로 counselorId == userId
            Integer counselorId = user.getUserId();
            var consultation = consultationService.approveConsultation(sessionId, counselorId, request);
            var response = ConsultationResponse.from(consultation);

            log.info("상담 승인 성공: 세션 ID {}, 상담사 ID {}", sessionId, counselorId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "상담이 승인되었습니다.",
                    "consultation", response
            ));

        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("상담 승인 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            log.error("상담 승인 실패 (서버 오류): ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "상담 승인 중 오류가 발생했습니다."));
        }
    }

    /**
     * 상담 거부 (상담사 또는 관리자)
     */
    @PutMapping("/{sessionId}/reject")
    public ResponseEntity<?> rejectConsultation(
            @PathVariable Long sessionId,
            @RequestBody ConsultationStatusUpdateRequest request,
            HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인이 필요합니다."));
        }

        try {
            User user = userService.getUserById(userId);

            // 상담사 또는 관리자만 거부 가능
            if (user.getRole() != UserRole.COUNSELOR && user.getRole() != UserRole.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "상담사 또는 관리자만 거부할 수 있습니다."));
            }

            // Counselor 엔티티는 @MapsId를 사용하므로 counselorId == userId
            Integer counselorId = user.getUserId();
            var consultation = consultationService.rejectConsultation(sessionId, counselorId, request);
            var response = ConsultationResponse.from(consultation);

            log.info("상담 거부 성공: 세션 ID {}, 상담사 ID {}", sessionId, counselorId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "상담이 거부되었습니다.",
                    "consultation", response
            ));

        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("상담 거부 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            log.error("상담 거부 실패 (서버 오류): ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "상담 거부 중 오류가 발생했습니다."));
        }
    }

    /**
     * 상담 기록 작성 (상담사)
     */
    @PostMapping("/{sessionId}/record")
    public ResponseEntity<?> createRecord(
            @PathVariable Long sessionId,
            @RequestBody ConsultationRecordRequest request,
            HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인이 필요합니다."));
        }

        try {
            User user = userService.getUserById(userId);

            if (user.getRole() != UserRole.COUNSELOR) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "상담사만 기록을 작성할 수 있습니다."));
            }

            // Counselor 엔티티는 @MapsId를 사용하므로 counselorId == userId
            Integer counselorId = user.getUserId();
            var record = recordService.createRecord(sessionId, counselorId, request);
            var response = ConsultationRecordResponse.fromForCounselor(record);

            log.info("상담 기록 작성 성공: 세션 ID {}, 상담사 ID {}", sessionId, counselorId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "상담 기록이 작성되었습니다.",
                    "record", response
            ));

        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("상담 기록 작성 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            log.error("상담 기록 작성 실패 (서버 오류): ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "상담 기록 작성 중 오류가 발생했습니다."));
        }
    }

    /**
     * 학생 피드백 작성
     */
    @PutMapping("/{sessionId}/feedback")
    public ResponseEntity<?> addFeedback(
            @PathVariable Long sessionId,
            @RequestBody StudentFeedbackRequest request,
            HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인이 필요합니다."));
        }

        try {
            var record = recordService.addStudentFeedback(sessionId, userId, request);
            var response = ConsultationRecordResponse.fromForStudent(record);

            log.info("학생 피드백 작성 성공: 세션 ID {}, 학생 ID {}", sessionId, userId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "피드백이 등록되었습니다.",
                    "record", response
            ));

        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("피드백 작성 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            log.error("피드백 작성 실패 (서버 오류): ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "피드백 등록 중 오류가 발생했습니다."));
        }
    }

    /**
     * 상담사 목록 조회
     */
    @GetMapping("/counselors")
    public ResponseEntity<?> getCounselors() {
        try {
            List<CounselorResponse> counselors = counselorService.getAllCounselors();
            return ResponseEntity.ok(counselors);

        } catch (Exception e) {
            log.error("상담사 목록 조회 실패: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "상담사 목록 조회 중 오류가 발생했습니다."));
        }
    }
}
