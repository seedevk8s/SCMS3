package com.scms.app.service;

import com.scms.app.dto.*;
import com.scms.app.dto.AssessmentReportResponse.CategoryScoreDto;
import com.scms.app.dto.AssessmentReportResponse.CompetencyScoreDto;
import com.scms.app.model.Competency;
import com.scms.app.model.CompetencyCategory;
import com.scms.app.model.Student;
import com.scms.app.model.StudentCompetencyAssessment;
import com.scms.app.repository.CompetencyCategoryRepository;
import com.scms.app.repository.CompetencyRepository;
import com.scms.app.repository.StudentCompetencyAssessmentRepository;
import com.scms.app.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 학생 역량 평가 Service
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class CompetencyAssessmentService {

    private final StudentCompetencyAssessmentRepository assessmentRepository;
    private final CompetencyRepository competencyRepository;
    private final CompetencyCategoryRepository categoryRepository;
    private final StudentRepository studentRepository;

    /**
     * 평가 저장 (생성 또는 수정)
     */
    @Transactional
    public AssessmentResponse saveAssessment(AssessmentRequest request) {
        log.info("역량 평가 저장: 학생 ID {}, 역량 ID {}", request.getStudentId(), request.getCompetencyId());

        // 학생 확인
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다: ID " + request.getStudentId()));

        // 역량 확인
        Competency competency = competencyRepository.findByIdWithCategory(request.getCompetencyId())
                .orElseThrow(() -> new IllegalArgumentException("역량을 찾을 수 없습니다: ID " + request.getCompetencyId()));

        // 점수 유효성 검증
        if (request.getScore() < 0 || request.getScore() > 100) {
            throw new IllegalArgumentException("점수는 0~100 사이여야 합니다: " + request.getScore());
        }

        // 평가 생성
        StudentCompetencyAssessment assessment = StudentCompetencyAssessment.builder()
                .student(student)
                .competency(competency)
                .score(request.getScore())
                .assessmentDate(request.getAssessmentDate() != null ? request.getAssessmentDate() : LocalDate.now())
                .assessor(request.getAssessor())
                .notes(request.getNotes())
                .build();

        StudentCompetencyAssessment saved = assessmentRepository.save(assessment);
        log.info("역량 평가 저장 완료: ID {}", saved.getId());

        return AssessmentResponse.from(saved);
    }

    /**
     * 여러 역량 평가 일괄 저장
     */
    @Transactional
    public List<AssessmentResponse> saveMultipleAssessments(List<AssessmentRequest> requests) {
        log.info("역량 평가 일괄 저장: {} 건", requests.size());

        List<AssessmentResponse> responses = new ArrayList<>();
        for (AssessmentRequest request : requests) {
            responses.add(saveAssessment(request));
        }

        return responses;
    }

    /**
     * 학생별 평가 조회
     */
    public List<AssessmentResponse> getStudentAssessments(Long studentId) {
        log.debug("학생별 역량 평가 조회: 학생 ID {}", studentId);

        // 학생 존재 여부 확인
        if (!studentRepository.existsById(studentId)) {
            throw new IllegalArgumentException("학생을 찾을 수 없습니다: ID " + studentId);
        }

        return assessmentRepository.findByStudentIdWithDetails(studentId).stream()
                .map(AssessmentResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 학생별 최신 평가만 조회 (각 역량당 최신 1건)
     */
    public List<AssessmentResponse> getLatestAssessmentsByStudent(Long studentId) {
        log.debug("학생별 최신 역량 평가 조회: 학생 ID {}", studentId);

        // 학생 존재 여부 확인
        if (!studentRepository.existsById(studentId)) {
            throw new IllegalArgumentException("학생을 찾을 수 없습니다: ID " + studentId);
        }

        return assessmentRepository.findLatestAssessmentsByStudentId(studentId).stream()
                .map(AssessmentResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 평가 상세 조회
     */
    public AssessmentResponse getAssessmentById(Long id) {
        log.debug("역량 평가 상세 조회: ID {}", id);
        StudentCompetencyAssessment assessment = assessmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("평가를 찾을 수 없습니다: ID " + id));
        return AssessmentResponse.from(assessment);
    }

    /**
     * 평가 수정
     */
    @Transactional
    public AssessmentResponse updateAssessment(Long id, AssessmentRequest request) {
        log.info("역량 평가 수정: ID {}", id);

        StudentCompetencyAssessment assessment = assessmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("평가를 찾을 수 없습니다: ID " + id));

        // 점수 유효성 검증
        if (request.getScore() < 0 || request.getScore() > 100) {
            throw new IllegalArgumentException("점수는 0~100 사이여야 합니다: " + request.getScore());
        }

        // 역량 변경 시
        if (!assessment.getCompetency().getId().equals(request.getCompetencyId())) {
            Competency newCompetency = competencyRepository.findByIdWithCategory(request.getCompetencyId())
                    .orElseThrow(() -> new IllegalArgumentException("역량을 찾을 수 없습니다: ID " + request.getCompetencyId()));
            assessment.setCompetency(newCompetency);
        }

        assessment.setScore(request.getScore());
        if (request.getAssessmentDate() != null) {
            assessment.setAssessmentDate(request.getAssessmentDate());
        }
        assessment.setAssessor(request.getAssessor());
        assessment.setNotes(request.getNotes());

        StudentCompetencyAssessment updated = assessmentRepository.save(assessment);
        log.info("역량 평가 수정 완료: ID {}", updated.getId());

        return AssessmentResponse.from(updated);
    }

    /**
     * 평가 삭제
     */
    @Transactional
    public void deleteAssessment(Long id) {
        log.info("역량 평가 삭제: ID {}", id);

        StudentCompetencyAssessment assessment = assessmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("평가를 찾을 수 없습니다: ID " + id));

        assessmentRepository.delete(assessment);
        log.info("역량 평가 삭제 완료: ID {}", id);
    }

    /**
     * 학생 역량 평가 리포트 생성
     */
    public AssessmentReportResponse generateReport(Long studentId) {
        log.info("역량 평가 리포트 생성: 학생 ID {}", studentId);

        // 학생 정보 확인
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다: ID " + studentId));

        // 최신 평가 데이터 조회
        List<StudentCompetencyAssessment> assessments = assessmentRepository.findLatestAssessmentsByStudentId(studentId);

        if (assessments.isEmpty()) {
            throw new IllegalStateException("평가 데이터가 없습니다. 먼저 역량 평가를 실시해주세요.");
        }

        // 전체 평균 점수 계산
        Double totalScore = assessments.stream()
                .mapToInt(StudentCompetencyAssessment::getScore)
                .average()
                .orElse(0.0);

        // 전체 등급 계산
        String overallGrade = calculateGrade(totalScore);

        // 최신 평가일
        LocalDate latestAssessmentDate = assessments.stream()
                .map(StudentCompetencyAssessment::getAssessmentDate)
                .max(LocalDate::compareTo)
                .orElse(LocalDate.now());

        // 카테고리별 점수 집계
        Map<CompetencyCategory, List<StudentCompetencyAssessment>> categoryMap = assessments.stream()
                .collect(Collectors.groupingBy(a -> a.getCompetency().getCategory()));

        List<CategoryScoreDto> categoryScores = categoryMap.entrySet().stream()
                .map(entry -> {
                    CompetencyCategory category = entry.getKey();
                    List<StudentCompetencyAssessment> categoryAssessments = entry.getValue();

                    Double avgScore = categoryAssessments.stream()
                            .mapToInt(StudentCompetencyAssessment::getScore)
                            .average()
                            .orElse(0.0);

                    List<CompetencyScoreDto> competencies = categoryAssessments.stream()
                            .map(this::toCompetencyScoreDto)
                            .collect(Collectors.toList());

                    return CategoryScoreDto.builder()
                            .categoryId(category.getId())
                            .categoryName(category.getName())
                            .averageScore(avgScore)
                            .grade(calculateGrade(avgScore))
                            .competencies(competencies)
                            .build();
                })
                .sorted(Comparator.comparing(CategoryScoreDto::getCategoryId))
                .collect(Collectors.toList());

        // 강점 역량 추출 (Top 3)
        List<CompetencyScoreDto> strengths = assessments.stream()
                .sorted(Comparator.comparing(StudentCompetencyAssessment::getScore).reversed())
                .limit(3)
                .map(this::toCompetencyScoreDto)
                .collect(Collectors.toList());

        // 약점 역량 추출 (Bottom 3)
        List<CompetencyScoreDto> weaknesses = assessments.stream()
                .sorted(Comparator.comparing(StudentCompetencyAssessment::getScore))
                .limit(3)
                .map(this::toCompetencyScoreDto)
                .collect(Collectors.toList());

        // 리포트 생성
        return AssessmentReportResponse.builder()
                .studentId(student.getId())
                .studentName(student.getName())
                .studentNumber(student.getStudentId())
                .totalScore(totalScore)
                .overallGrade(overallGrade)
                .latestAssessmentDate(latestAssessmentDate)
                .assessmentCount(assessments.size())
                .categoryScores(categoryScores)
                .strengths(strengths)
                .weaknesses(weaknesses)
                .recommendations(new ArrayList<>())  // TODO: 프로그램 추천 로직 추가
                .build();
    }

    /**
     * 역량별 통계 조회
     */
    public CompetencyStatisticsResponse getCompetencyStatistics(Long competencyId) {
        log.debug("역량별 통계 조회: 역량 ID {}", competencyId);

        Competency competency = competencyRepository.findByIdWithCategory(competencyId)
                .orElseThrow(() -> new IllegalArgumentException("역량을 찾을 수 없습니다: ID " + competencyId));

        List<StudentCompetencyAssessment> assessments = assessmentRepository.findByCompetencyId(competencyId);

        if (assessments.isEmpty()) {
            return CompetencyStatisticsResponse.builder()
                    .competencyId(competency.getId())
                    .competencyName(competency.getName())
                    .categoryId(competency.getCategory().getId())
                    .categoryName(competency.getCategory().getName())
                    .totalAssessments(0L)
                    .averageScore(0.0)
                    .maxScore(0)
                    .minScore(0)
                    .excellentCount(0L)
                    .goodCount(0L)
                    .needsImprovementCount(0L)
                    .build();
        }

        List<Integer> scores = assessments.stream()
                .map(StudentCompetencyAssessment::getScore)
                .collect(Collectors.toList());

        Double avgScore = scores.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        Integer maxScore = scores.stream().max(Integer::compareTo).orElse(0);
        Integer minScore = scores.stream().min(Integer::compareTo).orElse(0);

        long excellentCount = scores.stream().filter(s -> s >= 80).count();
        long goodCount = scores.stream().filter(s -> s >= 60 && s < 80).count();
        long needsImprovementCount = scores.stream().filter(s -> s < 60).count();

        return CompetencyStatisticsResponse.builder()
                .competencyId(competency.getId())
                .competencyName(competency.getName())
                .categoryId(competency.getCategory().getId())
                .categoryName(competency.getCategory().getName())
                .totalAssessments((long) assessments.size())
                .averageScore(avgScore)
                .maxScore(maxScore)
                .minScore(minScore)
                .excellentCount(excellentCount)
                .goodCount(goodCount)
                .needsImprovementCount(needsImprovementCount)
                .build();
    }

    /**
     * 페이지네이션: 학생별 평가 목록
     */
    public Page<AssessmentResponse> getStudentAssessmentsWithPagination(Long studentId, Pageable pageable) {
        log.debug("학생별 역량 평가 조회 (페이지네이션): 학생 ID {}", studentId);

        if (!studentRepository.existsById(studentId)) {
            throw new IllegalArgumentException("학생을 찾을 수 없습니다: ID " + studentId);
        }

        return assessmentRepository.findByStudentId(studentId, pageable)
                .map(AssessmentResponse::from);
    }

    // ==================== Helper Methods ====================

    /**
     * 점수를 등급으로 변환
     */
    private String calculateGrade(Double score) {
        if (score >= 90) return "A";
        if (score >= 80) return "B";
        if (score >= 70) return "C";
        if (score >= 60) return "D";
        return "F";
    }

    /**
     * Assessment를 CompetencyScoreDto로 변환
     */
    private CompetencyScoreDto toCompetencyScoreDto(StudentCompetencyAssessment assessment) {
        return CompetencyScoreDto.builder()
                .competencyId(assessment.getCompetency().getId())
                .competencyName(assessment.getCompetency().getName())
                .categoryId(assessment.getCompetency().getCategory().getId())
                .categoryName(assessment.getCompetency().getCategory().getName())
                .score(assessment.getScore())
                .grade(assessment.getGrade())
                .scoreLevel(assessment.getScoreLevel())
                .build();
    }
}
