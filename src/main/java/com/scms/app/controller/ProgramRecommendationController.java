package com.scms.app.controller;

import com.scms.app.dto.RecommendedProgramDto;
import com.scms.app.service.ProgramRecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 프로그램 추천 REST API Controller
 */
@RestController
@RequestMapping("/api/programs")
@RequiredArgsConstructor
@Slf4j
public class ProgramRecommendationController {

    private final ProgramRecommendationService recommendationService;

    /**
     * 학생 맞춤형 프로그램 추천
     *
     * @param studentId 학생 ID (Student 테이블의 ID)
     * @param limit 추천 개수 (기본값: 10)
     * @return 추천 프로그램 목록
     */
    @GetMapping("/recommendations")
    public ResponseEntity<List<RecommendedProgramDto>> getRecommendedPrograms(
            @RequestParam Long studentId,
            @RequestParam(defaultValue = "10") int limit) {

        log.info("프로그램 추천 API 호출: studentId={}, limit={}", studentId, limit);

        try {
            List<RecommendedProgramDto> recommendations =
                recommendationService.getRecommendedPrograms(studentId, limit);

            if (recommendations.isEmpty()) {
                log.info("추천할 프로그램이 없습니다: studentId={}", studentId);
                return ResponseEntity.ok(recommendations);
            }

            log.info("추천 프로그램 {}개 반환", recommendations.size());
            return ResponseEntity.ok(recommendations);

        } catch (Exception e) {
            log.error("프로그램 추천 중 오류 발생: studentId={}", studentId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
