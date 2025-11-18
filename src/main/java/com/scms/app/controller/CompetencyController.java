package com.scms.app.controller;

import com.scms.app.dto.CompetencyCategoryRequest;
import com.scms.app.dto.CompetencyCategoryResponse;
import com.scms.app.dto.CompetencyRequest;
import com.scms.app.dto.CompetencyResponse;
import com.scms.app.service.CompetencyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 역량 관리 REST API Controller
 */
@RestController
@RequestMapping("/api/competencies")
@RequiredArgsConstructor
@Slf4j
public class CompetencyController {

    private final CompetencyService competencyService;

    // ==================== 역량 카테고리 API ====================

    /**
     * 모든 카테고리 조회
     */
    @GetMapping("/categories")
    public ResponseEntity<List<CompetencyCategoryResponse>> getAllCategories(
            @RequestParam(value = "includeCompetencies", required = false, defaultValue = "false") boolean includeCompetencies
    ) {
        log.debug("GET /api/competencies/categories - includeCompetencies: {}", includeCompetencies);

        List<CompetencyCategoryResponse> categories = includeCompetencies
                ? competencyService.getAllCategoriesWithCompetencies()
                : competencyService.getAllCategories();

        return ResponseEntity.ok(categories);
    }

    /**
     * 카테고리 상세 조회
     */
    @GetMapping("/categories/{categoryId}")
    public ResponseEntity<CompetencyCategoryResponse> getCategoryById(
            @PathVariable Long categoryId,
            @RequestParam(value = "includeCompetencies", required = false, defaultValue = "false") boolean includeCompetencies
    ) {
        log.debug("GET /api/competencies/categories/{} - includeCompetencies: {}", categoryId, includeCompetencies);

        CompetencyCategoryResponse category = includeCompetencies
                ? competencyService.getCategoryByIdWithCompetencies(categoryId)
                : competencyService.getCategoryById(categoryId);

        return ResponseEntity.ok(category);
    }

    /**
     * 카테고리 생성 (관리자)
     */
    @PostMapping("/categories")
    public ResponseEntity<CompetencyCategoryResponse> createCategory(
            @Valid @RequestBody CompetencyCategoryRequest request
    ) {
        log.info("POST /api/competencies/categories - 카테고리 생성: {}", request.getName());

        CompetencyCategoryResponse created = competencyService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * 카테고리 수정 (관리자)
     */
    @PutMapping("/categories/{categoryId}")
    public ResponseEntity<CompetencyCategoryResponse> updateCategory(
            @PathVariable Long categoryId,
            @Valid @RequestBody CompetencyCategoryRequest request
    ) {
        log.info("PUT /api/competencies/categories/{} - 카테고리 수정", categoryId);

        CompetencyCategoryResponse updated = competencyService.updateCategory(categoryId, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * 카테고리 삭제 (관리자)
     */
    @DeleteMapping("/categories/{categoryId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long categoryId) {
        log.info("DELETE /api/competencies/categories/{} - 카테고리 삭제", categoryId);

        competencyService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }

    // ==================== 역량 API ====================

    /**
     * 모든 역량 조회
     */
    @GetMapping
    public ResponseEntity<List<CompetencyResponse>> getAllCompetencies() {
        log.debug("GET /api/competencies - 모든 역량 조회");

        List<CompetencyResponse> competencies = competencyService.getAllCompetencies();
        return ResponseEntity.ok(competencies);
    }

    /**
     * 카테고리별 역량 조회
     */
    @GetMapping("/categories/{categoryId}/competencies")
    public ResponseEntity<List<CompetencyResponse>> getCompetenciesByCategory(
            @PathVariable Long categoryId
    ) {
        log.debug("GET /api/competencies/categories/{}/competencies - 카테고리별 역량 조회", categoryId);

        List<CompetencyResponse> competencies = competencyService.getCompetenciesByCategory(categoryId);
        return ResponseEntity.ok(competencies);
    }

    /**
     * 역량 상세 조회
     */
    @GetMapping("/{competencyId}")
    public ResponseEntity<CompetencyResponse> getCompetencyById(@PathVariable Long competencyId) {
        log.debug("GET /api/competencies/{} - 역량 상세 조회", competencyId);

        CompetencyResponse competency = competencyService.getCompetencyById(competencyId);
        return ResponseEntity.ok(competency);
    }

    /**
     * 역량 생성 (관리자)
     */
    @PostMapping
    public ResponseEntity<CompetencyResponse> createCompetency(
            @Valid @RequestBody CompetencyRequest request
    ) {
        log.info("POST /api/competencies - 역량 생성: {}", request.getName());

        CompetencyResponse created = competencyService.createCompetency(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * 역량 수정 (관리자)
     */
    @PutMapping("/{competencyId}")
    public ResponseEntity<CompetencyResponse> updateCompetency(
            @PathVariable Long competencyId,
            @Valid @RequestBody CompetencyRequest request
    ) {
        log.info("PUT /api/competencies/{} - 역량 수정", competencyId);

        CompetencyResponse updated = competencyService.updateCompetency(competencyId, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * 역량 삭제 (관리자)
     */
    @DeleteMapping("/{competencyId}")
    public ResponseEntity<Void> deleteCompetency(@PathVariable Long competencyId) {
        log.info("DELETE /api/competencies/{} - 역량 삭제", competencyId);

        competencyService.deleteCompetency(competencyId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 역량명으로 검색
     */
    @GetMapping("/search")
    public ResponseEntity<List<CompetencyResponse>> searchCompetencies(
            @RequestParam("keyword") String keyword
    ) {
        log.debug("GET /api/competencies/search?keyword={}", keyword);

        List<CompetencyResponse> competencies = competencyService.searchCompetenciesByName(keyword);
        return ResponseEntity.ok(competencies);
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
