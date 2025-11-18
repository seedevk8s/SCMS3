package com.scms.app.controller;

import com.scms.app.dto.AssessmentReportResponse;
import com.scms.app.dto.AssessmentRequest;
import com.scms.app.dto.AssessmentResponse;
import com.scms.app.dto.CompetencyStatisticsResponse;
import com.scms.app.service.CompetencyAssessmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 역량 평가 REST API Controller
 */
@RestController
@RequestMapping("/api/assessments")
@RequiredArgsConstructor
@Slf4j
public class CompetencyAssessmentController {

    private final CompetencyAssessmentService assessmentService;

    // ==================== 평가 기본 CRUD ====================

    /**
     * 평가 저장 (단건)
     */
    @PostMapping
    public ResponseEntity<AssessmentResponse> saveAssessment(
            @Valid @RequestBody AssessmentRequest request
    ) {
        log.info("POST /api/assessments - 역량 평가 저장: 학생 {}, 역량 {}",
                request.getStudentId(), request.getCompetencyId());

        AssessmentResponse created = assessmentService.saveAssessment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * 평가 일괄 저장 (여러 역량 동시 평가)
     */
    @PostMapping("/batch")
    public ResponseEntity<List<AssessmentResponse>> saveMultipleAssessments(
            @Valid @RequestBody List<AssessmentRequest> requests
    ) {
        log.info("POST /api/assessments/batch - 역량 평가 일괄 저장: {} 건", requests.size());

        List<AssessmentResponse> created = assessmentService.saveMultipleAssessments(requests);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * 평가 상세 조회
     */
    @GetMapping("/{assessmentId}")
    public ResponseEntity<AssessmentResponse> getAssessmentById(@PathVariable Long assessmentId) {
        log.debug("GET /api/assessments/{} - 평가 상세 조회", assessmentId);

        AssessmentResponse assessment = assessmentService.getAssessmentById(assessmentId);
        return ResponseEntity.ok(assessment);
    }

    /**
     * 평가 수정
     */
    @PutMapping("/{assessmentId}")
    public ResponseEntity<AssessmentResponse> updateAssessment(
            @PathVariable Long assessmentId,
            @Valid @RequestBody AssessmentRequest request
    ) {
        log.info("PUT /api/assessments/{} - 평가 수정", assessmentId);

        AssessmentResponse updated = assessmentService.updateAssessment(assessmentId, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * 평가 삭제
     */
    @DeleteMapping("/{assessmentId}")
    public ResponseEntity<Void> deleteAssessment(@PathVariable Long assessmentId) {
        log.info("DELETE /api/assessments/{} - 평가 삭제", assessmentId);

        assessmentService.deleteAssessment(assessmentId);
        return ResponseEntity.noContent().build();
    }

    // ==================== 학생별 평가 조회 ====================

    /**
     * 학생별 모든 평가 조회
     */
    @GetMapping("/students/{studentId}")
    public ResponseEntity<List<AssessmentResponse>> getStudentAssessments(
            @PathVariable Long studentId
    ) {
        log.debug("GET /api/assessments/students/{} - 학생별 평가 조회", studentId);

        List<AssessmentResponse> assessments = assessmentService.getStudentAssessments(studentId);
        return ResponseEntity.ok(assessments);
    }

    /**
     * 학생별 최신 평가만 조회 (각 역량당 최신 1건)
     */
    @GetMapping("/students/{studentId}/latest")
    public ResponseEntity<List<AssessmentResponse>> getLatestAssessmentsByStudent(
            @PathVariable Long studentId
    ) {
        log.debug("GET /api/assessments/students/{}/latest - 학생별 최신 평가 조회", studentId);

        List<AssessmentResponse> assessments = assessmentService.getLatestAssessmentsByStudent(studentId);
        return ResponseEntity.ok(assessments);
    }

    /**
     * 학생별 평가 조회 (페이지네이션)
     */
    @GetMapping("/students/{studentId}/page")
    public ResponseEntity<Page<AssessmentResponse>> getStudentAssessmentsWithPagination(
            @PathVariable Long studentId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "assessmentDate,desc") String sort
    ) {
        log.debug("GET /api/assessments/students/{}/page - 학생별 평가 조회 (페이징)", studentId);

        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));

        Page<AssessmentResponse> assessments = assessmentService.getStudentAssessmentsWithPagination(studentId, pageable);
        return ResponseEntity.ok(assessments);
    }

    // ==================== 평가 리포트 및 통계 ====================

    /**
     * 학생 역량 평가 리포트 생성
     */
    @GetMapping("/students/{studentId}/report")
    public ResponseEntity<AssessmentReportResponse> generateReport(@PathVariable Long studentId) {
        log.info("GET /api/assessments/students/{}/report - 평가 리포트 생성", studentId);

        AssessmentReportResponse report = assessmentService.generateReport(studentId);
        return ResponseEntity.ok(report);
    }

    /**
     * 역량별 통계 조회
     */
    @GetMapping("/competencies/{competencyId}/statistics")
    public ResponseEntity<CompetencyStatisticsResponse> getCompetencyStatistics(
            @PathVariable Long competencyId
    ) {
        log.debug("GET /api/assessments/competencies/{}/statistics - 역량별 통계 조회", competencyId);

        CompetencyStatisticsResponse statistics = assessmentService.getCompetencyStatistics(competencyId);
        return ResponseEntity.ok(statistics);
    }

    // ==================== 예외 처리 ====================

    /**
     * IllegalArgumentException 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("잘못된 요청: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse("BAD_REQUEST", ex.getMessage());
        return ResponseEntity.badRequest().body(error);
    }

    /**
     * IllegalStateException 처리
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex) {
        log.error("잘못된 상태: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse("CONFLICT", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * 일반 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        log.error("서버 오류 발생", ex);
        ErrorResponse error = new ErrorResponse("INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * 에러 응답 DTO
     */
    record ErrorResponse(String code, String message) {}
}
