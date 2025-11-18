package com.scms.app.config;

import com.scms.app.model.*;
import com.scms.app.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 설문조사 테스트 데이터 초기화
 * - 애플리케이션 시작 시 테스트용 설문 데이터를 자동으로 생성
 */
@Component
@Order(2) // MileageDataInitializer 이후 실행
@RequiredArgsConstructor
@Slf4j
public class SurveyDataInitializer implements CommandLineRunner {

    private final SurveyRepository surveyRepository;
    private final SurveyQuestionRepository questionRepository;
    private final SurveyQuestionOptionRepository optionRepository;
    private final SurveyResponseRepository responseRepository;
    private final SurveyAnswerRepository answerRepository;

    @Override
    @Transactional
    public void run(String... args) {
        // 이미 데이터가 있으면 스킵
        if (surveyRepository.count() > 0) {
            log.info("설문조사 데이터가 이미 존재합니다. 초기화를 스킵합니다.");
            return;
        }

        log.info("=== 설문조사 테스트 데이터 삽입 시작 ===");

        // 1. 진행 중인 설문 - 학생복지 만족도 조사
        createWelfareSatisfactionSurvey();

        // 2. 진행 중인 설문 - 온라인 강의 개선 설문 (익명)
        createOnlineLectureSurvey();

        // 3. 종료된 설문 - 2024년 2학기 복지 만족도
        createPastSurvey();

        log.info("=== 설문조사 테스트 데이터 삽입 완료: {} 개 설문 생성됨 ===", surveyRepository.count());
    }

    /**
     * 1. 학생복지 만족도 조사 (진행 중)
     */
    private void createWelfareSatisfactionSurvey() {
        LocalDateTime now = LocalDateTime.now();

        Survey survey = Survey.builder()
                .title("2025년 학생복지 만족도 조사")
                .description("학생 여러분의 복지 서비스 이용 경험과 만족도를 조사하여 더 나은 서비스를 제공하고자 합니다. 솔직한 의견 부탁드립니다.")
                .startDate(now.minusDays(3))
                .endDate(now.plusDays(14))
                .isAnonymous(false)
                .isActive(true)
                .targetType(SurveyTargetType.ALL)
                .maxResponses(null)
                .allowMultipleResponses(false)
                .showResults(true)
                .createdBy(1) // 관리자 ID
                .build();

        survey = surveyRepository.save(survey);
        log.info("설문 생성: {}", survey.getTitle());

        // Q1: 복지 서비스 전반적 만족도 (척도형)
        SurveyQuestion q1 = SurveyQuestion.builder()
                .surveyId(survey.getSurveyId())
                .questionType(QuestionType.SCALE)
                .questionText("학생복지 서비스에 대한 전반적인 만족도를 평가해주세요.")
                .isRequired(true)
                .displayOrder(1)
                .scaleMin(1)
                .scaleMax(5)
                .scaleMinLabel("매우 불만족")
                .scaleMaxLabel("매우 만족")
                .build();
        questionRepository.save(q1);

        // Q2: 가장 유용한 복지 서비스 (객관식 단일선택)
        SurveyQuestion q2 = SurveyQuestion.builder()
                .surveyId(survey.getSurveyId())
                .questionType(QuestionType.SINGLE_CHOICE)
                .questionText("가장 유용하다고 생각하는 복지 서비스는 무엇인가요?")
                .isRequired(true)
                .displayOrder(2)
                .build();
        q2 = questionRepository.save(q2);

        // Q2 선택지
        optionRepository.save(SurveyQuestionOption.builder()
                .questionId(q2.getQuestionId())
                .optionText("심리상담 서비스")
                .displayOrder(1)
                .build());
        optionRepository.save(SurveyQuestionOption.builder()
                .questionId(q2.getQuestionId())
                .optionText("진로상담 서비스")
                .displayOrder(2)
                .build());
        optionRepository.save(SurveyQuestionOption.builder()
                .questionId(q2.getQuestionId())
                .optionText("장학금/재정지원")
                .displayOrder(3)
                .build());
        optionRepository.save(SurveyQuestionOption.builder()
                .questionId(q2.getQuestionId())
                .optionText("건강관리 서비스")
                .displayOrder(4)
                .build());
        optionRepository.save(SurveyQuestionOption.builder()
                .questionId(q2.getQuestionId())
                .optionText("기타")
                .displayOrder(5)
                .build());

        // Q3: 개선이 필요한 점 (주관식 서술형)
        SurveyQuestion q3 = SurveyQuestion.builder()
                .surveyId(survey.getSurveyId())
                .questionType(QuestionType.LONG_TEXT)
                .questionText("학생복지 서비스에서 개선이 필요하다고 생각하는 점을 자유롭게 작성해주세요.")
                .isRequired(false)
                .displayOrder(3)
                .build();
        questionRepository.save(q3);

        // Q4: 추가로 필요한 복지 서비스 (객관식 복수선택)
        SurveyQuestion q4 = SurveyQuestion.builder()
                .surveyId(survey.getSurveyId())
                .questionType(QuestionType.MULTIPLE_CHOICE)
                .questionText("추가로 필요하다고 생각하는 복지 서비스를 모두 선택해주세요.")
                .isRequired(false)
                .displayOrder(4)
                .build();
        q4 = questionRepository.save(q4);

        // Q4 선택지
        optionRepository.save(SurveyQuestionOption.builder()
                .questionId(q4.getQuestionId())
                .optionText("학업 멘토링")
                .displayOrder(1)
                .build());
        optionRepository.save(SurveyQuestionOption.builder()
                .questionId(q4.getQuestionId())
                .optionText("취업 준비 지원")
                .displayOrder(2)
                .build());
        optionRepository.save(SurveyQuestionOption.builder()
                .questionId(q4.getQuestionId())
                .optionText("법률 상담")
                .displayOrder(3)
                .build());
        optionRepository.save(SurveyQuestionOption.builder()
                .questionId(q4.getQuestionId())
                .optionText("외국어 교육 지원")
                .displayOrder(4)
                .build());
    }

    /**
     * 2. 온라인 강의 개선 설문 (진행 중, 익명)
     */
    private void createOnlineLectureSurvey() {
        LocalDateTime now = LocalDateTime.now();

        Survey survey = Survey.builder()
                .title("온라인 강의 개선 설문")
                .description("온라인 강의 시스템 개선을 위한 여러분의 의견을 듣고자 합니다. 익명으로 진행되니 자유롭게 의견을 주세요.")
                .startDate(now.minusDays(1))
                .endDate(now.plusDays(7))
                .isAnonymous(true)
                .isActive(true)
                .targetType(SurveyTargetType.ALL)
                .maxResponses(null)
                .allowMultipleResponses(false)
                .showResults(false)
                .createdBy(1)
                .build();

        survey = surveyRepository.save(survey);
        log.info("설문 생성: {}", survey.getTitle());

        // Q1: 온라인 강의 만족도 (척도형)
        SurveyQuestion q1 = SurveyQuestion.builder()
                .surveyId(survey.getSurveyId())
                .questionType(QuestionType.SCALE)
                .questionText("온라인 강의 시스템에 대한 전반적인 만족도는?")
                .isRequired(true)
                .displayOrder(1)
                .scaleMin(1)
                .scaleMax(5)
                .scaleMinLabel("매우 불만족")
                .scaleMaxLabel("매우 만족")
                .build();
        questionRepository.save(q1);

        // Q2: 가장 불편한 점 (객관식 단일선택)
        SurveyQuestion q2 = SurveyQuestion.builder()
                .surveyId(survey.getSurveyId())
                .questionType(QuestionType.SINGLE_CHOICE)
                .questionText("온라인 강의 이용 시 가장 불편한 점은?")
                .isRequired(true)
                .displayOrder(2)
                .build();
        q2 = questionRepository.save(q2);

        optionRepository.save(SurveyQuestionOption.builder()
                .questionId(q2.getQuestionId())
                .optionText("화질/음질 문제")
                .displayOrder(1)
                .build());
        optionRepository.save(SurveyQuestionOption.builder()
                .questionId(q2.getQuestionId())
                .optionText("시스템 접속 불안정")
                .displayOrder(2)
                .build());
        optionRepository.save(SurveyQuestionOption.builder()
                .questionId(q2.getQuestionId())
                .optionText("UI/UX가 불편함")
                .displayOrder(3)
                .build());
        optionRepository.save(SurveyQuestionOption.builder()
                .questionId(q2.getQuestionId())
                .optionText("모바일 지원 부족")
                .displayOrder(4)
                .build());

        // Q3: 개선 의견 (주관식)
        SurveyQuestion q3 = SurveyQuestion.builder()
                .surveyId(survey.getSurveyId())
                .questionType(QuestionType.LONG_TEXT)
                .questionText("온라인 강의 시스템 개선을 위한 의견이 있다면 자유롭게 작성해주세요.")
                .isRequired(false)
                .displayOrder(3)
                .build();
        questionRepository.save(q3);
    }

    /**
     * 3. 종료된 설문 - 2024년 2학기 복지 만족도
     */
    private void createPastSurvey() {
        LocalDateTime now = LocalDateTime.now();

        Survey survey = Survey.builder()
                .title("2024년 2학기 학생복지 만족도 조사")
                .description("2024년 2학기 학생복지 서비스 이용 경험을 조사합니다.")
                .startDate(now.minusDays(60))
                .endDate(now.minusDays(30))
                .isAnonymous(false)
                .isActive(false)
                .targetType(SurveyTargetType.ALL)
                .maxResponses(null)
                .allowMultipleResponses(false)
                .showResults(true)
                .createdBy(1)
                .build();

        survey = surveyRepository.save(survey);
        log.info("설문 생성: {} (종료됨)", survey.getTitle());

        // Q1: 전반적 만족도 (척도형)
        SurveyQuestion q1 = SurveyQuestion.builder()
                .surveyId(survey.getSurveyId())
                .questionType(QuestionType.SCALE)
                .questionText("2024년 2학기 학생복지 서비스 전반에 대한 만족도는?")
                .isRequired(true)
                .displayOrder(1)
                .scaleMin(1)
                .scaleMax(5)
                .scaleMinLabel("매우 불만족")
                .scaleMaxLabel("매우 만족")
                .build();
        q1 = questionRepository.save(q1);

        // Q2: 이용한 서비스 (객관식 복수선택)
        SurveyQuestion q2 = SurveyQuestion.builder()
                .surveyId(survey.getSurveyId())
                .questionType(QuestionType.MULTIPLE_CHOICE)
                .questionText("2학기 동안 이용한 복지 서비스를 모두 선택해주세요.")
                .isRequired(true)
                .displayOrder(2)
                .build();
        q2 = questionRepository.save(q2);

        Long option1 = optionRepository.save(SurveyQuestionOption.builder()
                .questionId(q2.getQuestionId())
                .optionText("심리상담")
                .displayOrder(1)
                .build()).getOptionId();

        Long option2 = optionRepository.save(SurveyQuestionOption.builder()
                .questionId(q2.getQuestionId())
                .optionText("진로상담")
                .displayOrder(2)
                .build()).getOptionId();

        optionRepository.save(SurveyQuestionOption.builder()
                .questionId(q2.getQuestionId())
                .optionText("장학금 신청")
                .displayOrder(3)
                .build());

        // 샘플 응답 데이터 추가 (이영희 학생이 응답했다고 가정 - userId = 2)
        SurveyResponse response = SurveyResponse.builder()
                .surveyId(survey.getSurveyId())
                .userId(2) // 이영희 student ID
                .submittedAt(now.minusDays(35))
                .ipAddress("127.0.0.1")
                .userAgent("Test Browser")
                .build();
        response = responseRepository.save(response);

        // Q1에 대한 답변: 만족도 4점
        answerRepository.save(SurveyAnswer.builder()
                .responseId(response.getResponseId())
                .questionId(q1.getQuestionId())
                .answerNumber(4)
                .build());

        // Q2에 대한 답변: 심리상담, 진로상담 선택
        answerRepository.save(SurveyAnswer.builder()
                .responseId(response.getResponseId())
                .questionId(q2.getQuestionId())
                .optionId(option1)
                .build());

        answerRepository.save(SurveyAnswer.builder()
                .responseId(response.getResponseId())
                .questionId(q2.getQuestionId())
                .optionId(option2)
                .build());

        log.info("샘플 응답 데이터 추가: 이영희 학생의 과거 설문 응답");
    }
}
