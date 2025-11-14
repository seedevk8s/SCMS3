package com.scms.app.controller;

import com.scms.app.dto.ReviewRequest;
import com.scms.app.dto.ReviewResponse;
import com.scms.app.service.ProgramReviewService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 프로그램 후기 REST API Controller
 */
@RestController
@RequestMapping("/api/programs/{programId}/reviews")
@RequiredArgsConstructor
@Slf4j
public class ProgramReviewController {

    private final ProgramReviewService reviewService;

    /**
     * 프로그램 후기 목록 조회
     */
    @GetMapping
    public ResponseEntity<?> getReviews(
            @PathVariable Integer programId,
            HttpSession session) {
        try {
            Integer userId = (Integer) session.getAttribute("userId");

            List<ReviewResponse> reviews = reviewService.getReviewsByProgram(programId, userId);
            Double averageRating = reviewService.getAverageRating(programId);
            Long reviewCount = reviewService.getReviewCount(programId);
            Boolean canWriteReview = userId != null && reviewService.canWriteReview(userId, programId);

            Map<String, Object> response = new HashMap<>();
            response.put("reviews", reviews);
            response.put("averageRating", averageRating);
            response.put("reviewCount", reviewCount);
            response.put("canWriteReview", canWriteReview);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("후기 목록 조회 실패: programId={}, error={}", programId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "후기 목록 조회에 실패했습니다."));
        }
    }

    /**
     * 후기 작성
     */
    @PostMapping
    public ResponseEntity<?> createReview(
            @PathVariable Integer programId,
            @RequestBody ReviewRequest request,
            HttpSession session) {
        try {
            Integer userId = (Integer) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "로그인이 필요합니다."));
            }

            ReviewResponse review = reviewService.createReview(userId, programId, request);
            log.info("후기 작성 성공: programId={}, userId={}, reviewId={}",
                    programId, userId, review.getReviewId());

            return ResponseEntity.ok(review);
        } catch (IllegalArgumentException e) {
            log.warn("후기 작성 실패 (유효성 검증): programId={}, error={}", programId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            log.warn("후기 작성 실패 (비즈니스 로직): programId={}, error={}", programId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("후기 작성 실패: programId={}, error={}", programId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "후기 작성에 실패했습니다."));
        }
    }

    /**
     * 후기 수정
     */
    @PutMapping("/{reviewId}")
    public ResponseEntity<?> updateReview(
            @PathVariable Integer programId,
            @PathVariable Integer reviewId,
            @RequestBody ReviewRequest request,
            HttpSession session) {
        try {
            Integer userId = (Integer) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "로그인이 필요합니다."));
            }

            ReviewResponse review = reviewService.updateReview(userId, reviewId, request);
            log.info("후기 수정 성공: programId={}, userId={}, reviewId={}",
                    programId, userId, reviewId);

            return ResponseEntity.ok(review);
        } catch (IllegalArgumentException e) {
            log.warn("후기 수정 실패 (유효성 검증): reviewId={}, error={}", reviewId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            log.warn("후기 수정 실패 (권한): reviewId={}, error={}", reviewId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("후기 수정 실패: reviewId={}, error={}", reviewId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "후기 수정에 실패했습니다."));
        }
    }

    /**
     * 후기 삭제
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(
            @PathVariable Integer programId,
            @PathVariable Integer reviewId,
            HttpSession session) {
        try {
            Integer userId = (Integer) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "로그인이 필요합니다."));
            }

            reviewService.deleteReview(userId, reviewId);
            log.info("후기 삭제 성공: programId={}, userId={}, reviewId={}",
                    programId, userId, reviewId);

            return ResponseEntity.ok(Map.of("message", "후기가 삭제되었습니다."));
        } catch (IllegalStateException e) {
            log.warn("후기 삭제 실패 (권한): reviewId={}, error={}", reviewId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("후기 삭제 실패: reviewId={}, error={}", reviewId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "후기 삭제에 실패했습니다."));
        }
    }
}
