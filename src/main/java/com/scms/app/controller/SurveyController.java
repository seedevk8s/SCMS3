package com.scms.app.controller;

import com.scms.app.dto.*;
import com.scms.app.service.SurveyResponseService;
import com.scms.app.service.SurveyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 설문조사 REST API 컨트롤러
 */
@RestController
@RequestMapping("/api/surveys")
@RequiredArgsConstructor
@Slf4j
public class SurveyController {

    private final SurveyService surveyService;
    private final SurveyResponseService surveyResponseService;

    /**
     * 설문 생성
     */
    @PostMapping
    public ResponseEntity<?> createSurvey(@Valid @RequestBody SurveyCreateRequest request,
                                          HttpSession session) {
        try {
            Integer userId = (Integer) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("로그인이 필요합니다"));
            }

            // 관리자 권한 확인
            Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
            if (isAdmin == null || !isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("관리자 권한이 필요합니다"));
            }

            SurveyResponse response = surveyService.createSurvey(request, userId);
            return ResponseEntity.ok(createSuccessResponse("설문이 생성되었습니다", response));
        } catch (Exception e) {
            log.error("설문 생성 실패", e);
            return ResponseEntity.badRequest()
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 설문 상세 조회
     */
    @GetMapping("/{surveyId}")
    public ResponseEntity<?> getSurveyDetail(@PathVariable Long surveyId,
                                             HttpSession session) {
        try {
            Integer userId = (Integer) session.getAttribute("userId");
            SurveyDetailResponse response = surveyService.getSurveyById(surveyId, userId);
            return ResponseEntity.ok(createSuccessResponse("설문 조회 성공", response));
        } catch (Exception e) {
            log.error("설문 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 활성 설문 목록 조회
     */
    @GetMapping
    public ResponseEntity<?> getActiveSurveys(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpSession session) {
        try {
            Integer userId = (Integer) session.getAttribute("userId");
            Page<SurveyResponse> surveys = surveyService.getActiveSurveys(page, size, userId);
            return ResponseEntity.ok(createSuccessResponse("설문 목록 조회 성공", surveys));
        } catch (Exception e) {
            log.error("설문 목록 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 진행 중인 설문 목록 조회
     */
    @GetMapping("/ongoing")
    public ResponseEntity<?> getOngoingSurveys(HttpSession session) {
        try {
            Integer userId = (Integer) session.getAttribute("userId");
            List<SurveyResponse> surveys = surveyService.getOngoingSurveys(userId);
            return ResponseEntity.ok(createSuccessResponse("진행 중인 설문 조회 성공", surveys));
        } catch (Exception e) {
            log.error("진행 중인 설문 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 사용자가 응답 가능한 설문 목록
     */
    @GetMapping("/available")
    public ResponseEntity<?> getAvailableSurveys(HttpSession session) {
        try {
            Integer userId = (Integer) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("로그인이 필요합니다"));
            }

            List<SurveyResponse> surveys = surveyService.getAvailableSurveysForUser(userId);
            return ResponseEntity.ok(createSuccessResponse("응답 가능한 설문 조회 성공", surveys));
        } catch (Exception e) {
            log.error("응답 가능한 설문 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 설문 수정
     */
    @PutMapping("/{surveyId}")
    public ResponseEntity<?> updateSurvey(@PathVariable Long surveyId,
                                          @Valid @RequestBody SurveyCreateRequest request,
                                          HttpSession session) {
        try {
            Integer userId = (Integer) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("로그인이 필요합니다"));
            }

            SurveyResponse response = surveyService.updateSurvey(surveyId, request, userId);
            return ResponseEntity.ok(createSuccessResponse("설문이 수정되었습니다", response));
        } catch (Exception e) {
            log.error("설문 수정 실패", e);
            return ResponseEntity.badRequest()
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 설문 삭제
     */
    @DeleteMapping("/{surveyId}")
    public ResponseEntity<?> deleteSurvey(@PathVariable Long surveyId,
                                          HttpSession session) {
        try {
            Integer userId = (Integer) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("로그인이 필요합니다"));
            }

            surveyService.deleteSurvey(surveyId, userId);
            return ResponseEntity.ok(createSuccessResponse("설문이 삭제되었습니다", null));
        } catch (Exception e) {
            log.error("설문 삭제 실패", e);
            return ResponseEntity.badRequest()
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 설문 활성화/비활성화
     */
    @PostMapping("/{surveyId}/toggle-active")
    public ResponseEntity<?> toggleSurveyActive(@PathVariable Long surveyId,
                                                HttpSession session) {
        try {
            Integer userId = (Integer) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("로그인이 필요합니다"));
            }

            surveyService.toggleSurveyActive(surveyId, userId);
            return ResponseEntity.ok(createSuccessResponse("설문 상태가 변경되었습니다", null));
        } catch (Exception e) {
            log.error("설문 상태 변경 실패", e);
            return ResponseEntity.badRequest()
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 설문 응답 제출
     */
    @PostMapping("/submit")
    public ResponseEntity<?> submitSurveyResponse(@Valid @RequestBody SurveySubmitRequest request,
                                                  HttpSession session,
                                                  HttpServletRequest httpRequest) {
        try {
            Integer userId = (Integer) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("로그인이 필요합니다"));
            }

            Long responseId = surveyResponseService.submitSurveyResponse(request, userId, httpRequest);
            return ResponseEntity.ok(createSuccessResponse("응답이 제출되었습니다", responseId));
        } catch (Exception e) {
            log.error("설문 응답 제출 실패", e);
            return ResponseEntity.badRequest()
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 설문 통계 조회
     */
    @GetMapping("/{surveyId}/statistics")
    public ResponseEntity<?> getSurveyStatistics(@PathVariable Long surveyId,
                                                 HttpSession session) {
        try {
            Integer userId = (Integer) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("로그인이 필요합니다"));
            }

            // 관리자만 통계 조회 가능
            Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
            if (isAdmin == null || !isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("통계 조회 권한이 없습니다"));
            }

            SurveyStatisticsResponse statistics = surveyResponseService.getSurveyStatistics(surveyId);
            return ResponseEntity.ok(createSuccessResponse("통계 조회 성공", statistics));
        } catch (Exception e) {
            log.error("통계 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 사용자의 응답 내역 조회
     */
    @GetMapping("/my-responses")
    public ResponseEntity<?> getMyResponses(HttpSession session) {
        try {
            Integer userId = (Integer) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("로그인이 필요합니다"));
            }

            List<SurveyResponse> responses = surveyResponseService.getUserResponses(userId);
            return ResponseEntity.ok(createSuccessResponse("응답 내역 조회 성공", responses));
        } catch (Exception e) {
            log.error("응답 내역 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    // Helper methods
    private Map<String, Object> createSuccessResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("data", data);
        return response;
    }

    private Map<String, Object> createErrorResponse(String error) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", error);
        return response;
    }
}
