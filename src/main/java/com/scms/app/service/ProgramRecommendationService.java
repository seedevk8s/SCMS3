package com.scms.app.service;

import com.scms.app.dto.RecommendedProgramDto;
import com.scms.app.model.Competency;
import com.scms.app.model.Program;
import com.scms.app.model.ProgramCompetency;
import com.scms.app.model.StudentCompetencyAssessment;
import com.scms.app.repository.ProgramCompetencyRepository;
import com.scms.app.repository.StudentCompetencyAssessmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 프로그램 추천 서비스
 * 학생의 역량진단 결과를 기반으로 맞춤형 프로그램을 추천
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProgramRecommendationService {

    private final StudentCompetencyAssessmentRepository assessmentRepository;
    private final ProgramCompetencyRepository programCompetencyRepository;

    /**
     * 학생 ID 기반 프로그램 추천
     *
     * 알고리즘:
     * 1. 학생의 최신 역량진단 결과 조회
     * 2. 점수가 낮은 역량(약점) 파악 (70점 미만)
     * 3. 해당 역량을 향상시킬 수 있는 프로그램 찾기
     * 4. 가중치 기반 추천 점수 계산
     * 5. 추천 점수 순으로 정렬하여 반환
     */
    public List<RecommendedProgramDto> getRecommendedPrograms(Long studentId, int limit) {
        log.info("프로그램 추천 시작: studentId={}, limit={}", studentId, limit);

        // 1. 학생의 최신 역량진단 결과 조회
        List<StudentCompetencyAssessment> assessments =
            assessmentRepository.findLatestAssessmentsByStudentId(studentId);

        if (assessments.isEmpty()) {
            log.warn("역량진단 결과가 없습니다: studentId={}", studentId);
            return Collections.emptyList();
        }

        // 2. 약점 역량 파악 (점수가 낮은 순으로 정렬, 70점 미만 우선)
        List<StudentCompetencyAssessment> weaknesses = assessments.stream()
                .filter(a -> a.getScore() < 70)  // 70점 미만을 약점으로 간주
                .sorted(Comparator.comparing(StudentCompetencyAssessment::getScore))
                .collect(Collectors.toList());

        // 약점이 없으면 전체 역량 중 하위 3개 선택
        if (weaknesses.isEmpty()) {
            weaknesses = assessments.stream()
                    .sorted(Comparator.comparing(StudentCompetencyAssessment::getScore))
                    .limit(3)
                    .collect(Collectors.toList());
        }

        log.info("약점 역량 {}개 파악: {}", weaknesses.size(),
                weaknesses.stream()
                        .map(a -> a.getCompetency().getName() + "(" + a.getScore() + "점)")
                        .collect(Collectors.joining(", ")));

        // 3. 약점 역량을 향상시킬 수 있는 프로그램 찾기
        List<Long> weaknessCompetencyIds = weaknesses.stream()
                .map(a -> a.getCompetency().getId())
                .collect(Collectors.toList());

        List<ProgramCompetency> programCompetencies =
            programCompetencyRepository.findByCompetencyIdIn(weaknessCompetencyIds);

        // 4. 프로그램별 추천 점수 계산
        Map<Program, ProgramScore> programScores = new HashMap<>();

        for (ProgramCompetency pc : programCompetencies) {
            Program program = pc.getProgram();
            Competency competency = pc.getCompetency();

            // 해당 역량의 학생 점수 찾기
            Optional<StudentCompetencyAssessment> assessmentOpt = weaknesses.stream()
                    .filter(a -> a.getCompetency().getId().equals(competency.getId()))
                    .findFirst();

            if (assessmentOpt.isEmpty()) continue;

            int studentScore = assessmentOpt.get().getScore();

            // 추천 점수 계산: (100 - 학생점수) * 가중치 / 10
            // 점수가 낮을수록, 가중치가 높을수록 추천 점수 증가
            double score = (100.0 - studentScore) * pc.getWeight() / 10.0;

            ProgramScore ps = programScores.computeIfAbsent(program, p -> new ProgramScore());
            ps.addScore(score);
            ps.addReason(competency.getName() + " 향상");
        }

        // 5. 추천 점수 순으로 정렬하여 DTO 변환
        List<RecommendedProgramDto> recommendations = programScores.entrySet().stream()
                .sorted((e1, e2) -> Double.compare(e2.getValue().getTotalScore(), e1.getValue().getTotalScore()))
                .limit(limit)
                .map(entry -> {
                    Program program = entry.getKey();
                    ProgramScore score = entry.getValue();

                    // 점수 정규화 (0-100 범위)
                    double normalizedScore = Math.min(100.0, score.getTotalScore());

                    return RecommendedProgramDto.from(program, normalizedScore, score.getReasons());
                })
                .collect(Collectors.toList());

        log.info("추천 프로그램 {}개 생성 완료", recommendations.size());
        return recommendations;
    }

    /**
     * 프로그램별 추천 점수 계산용 내부 클래스
     */
    private static class ProgramScore {
        private double totalScore = 0.0;
        private List<String> reasons = new ArrayList<>();

        public void addScore(double score) {
            this.totalScore += score;
        }

        public void addReason(String reason) {
            if (!reasons.contains(reason)) {
                reasons.add(reason);
            }
        }

        public double getTotalScore() {
            return totalScore;
        }

        public List<String> getReasons() {
            return reasons;
        }
    }
}
