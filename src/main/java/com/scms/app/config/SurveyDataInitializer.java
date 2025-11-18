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
 * 설문조사 샘플 데이터 초기화
 * - 애플리케이션 시작 시 테스트용 설문을 자동으로 생성
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Order(100)  // 다른 initializer 이후에 실행
public class SurveyDataInitializer implements CommandLineRunner {

    private final SurveyRepository surveyRepository;
    private final SurveyQuestionRepository surveyQuestionRepository;
    private final SurveyQuestionOptionRepository surveyQuestionOptionRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void run(String... args) {
        // 이미 데이터가 있으면 스킵
        if (surveyRepository.count() > 0) {
            log.info("설문조사 데이터가 이미 존재합니다. 초기화를 스킵합니다.");
            return;
        }

        log.info("=== 설문조사 샘플 데이터 생성 시작 ===");

        // 관리자 사용자 찾기 (없으면 user_id=1 사용)
        Integer adminId = userRepository.findAll().stream()
                .filter(u -> u.getRole() != null && u.getRole().name().equals("ADMIN"))
                .findFirst()
                .map(User::getUserId)
                .orElse(1);

        // 1. 학생 만족도 조사
        createSatisfactionSurvey(adminId);

        // 2. 프로그램 선호도 조사
        createProgramPreferenceSurvey(adminId);

        // 3. 간단한 피드백 설문
        createFeedbackSurvey(adminId);

        log.info("=== 설문조사 샘플 데이터 생성 완료: {} 개 설문 생성됨 ===", surveyRepository.count());
    }

    private void createSatisfactionSurvey(Integer adminId) {
        Survey survey = Survey.builder()
                .title("2024년 학생 만족도 조사")
                .description("푸름대학교 학생 지원 서비스에 대한 만족도를 조사합니다. 여러분의 솔직한 의견을 부탁드립니다.")
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusMonths(1))
                .isAnonymous(false)
                .isActive(true)
                .targetType(SurveyTargetType.ALL)
                .allowMultipleResponses(false)
                .showResults(false)
                .createdBy(adminId)
                .build();
        survey = surveyRepository.save(survey);
        log.info("설문 생성: {}", survey.getTitle());

        // 질문 1: 객관식 단일선택
        SurveyQuestion q1 = SurveyQuestion.builder()
                .surveyId(survey.getSurveyId())
                .questionType(QuestionType.SINGLE_CHOICE)
                .questionText("학생 지원 서비스에 전반적으로 얼마나 만족하시나요?")
                .isRequired(true)
                .displayOrder(0)
                .build();
        q1 = surveyQuestionRepository.save(q1);

        createOption(q1.getQuestionId(), "매우 만족", 0);
        createOption(q1.getQuestionId(), "만족", 1);
        createOption(q1.getQuestionId(), "보통", 2);
        createOption(q1.getQuestionId(), "불만족", 3);
        createOption(q1.getQuestionId(), "매우 불만족", 4);

        // 질문 2: 척도형
        SurveyQuestion q2 = SurveyQuestion.builder()
                .surveyId(survey.getSurveyId())
                .questionType(QuestionType.SCALE)
                .questionText("비교과 프로그램의 질은 어떻다고 생각하시나요?")
                .isRequired(true)
                .displayOrder(1)
                .scaleMin(1)
                .scaleMax(5)
                .scaleMinLabel("매우 낮음")
                .scaleMaxLabel("매우 높음")
                .build();
        surveyQuestionRepository.save(q2);

        // 질문 3: 주관식 서술형
        SurveyQuestion q3 = SurveyQuestion.builder()
                .surveyId(survey.getSurveyId())
                .questionType(QuestionType.LONG_TEXT)
                .questionText("개선이 필요한 부분이나 건의사항이 있다면 자유롭게 작성해주세요.")
                .isRequired(false)
                .displayOrder(2)
                .build();
        surveyQuestionRepository.save(q3);
    }

    private void createProgramPreferenceSurvey(Integer adminId) {
        Survey survey = Survey.builder()
                .title("비교과 프로그램 선호도 조사")
                .description("학생들이 선호하는 프로그램 유형을 파악하여 더 나은 프로그램을 기획하고자 합니다.")
                .startDate(LocalDateTime.now().minusDays(2))
                .endDate(LocalDateTime.now().plusWeeks(2))
                .isAnonymous(true)
                .isActive(true)
                .targetType(SurveyTargetType.STUDENT)
                .allowMultipleResponses(false)
                .showResults(true)
                .createdBy(adminId)
                .build();
        survey = surveyRepository.save(survey);
        log.info("설문 생성: {}", survey.getTitle());

        // 질문 1: 객관식 복수선택
        SurveyQuestion q1 = SurveyQuestion.builder()
                .surveyId(survey.getSurveyId())
                .questionType(QuestionType.MULTIPLE_CHOICE)
                .questionText("관심있는 프로그램 분야를 모두 선택해주세요. (복수선택 가능)")
                .isRequired(true)
                .displayOrder(0)
                .build();
        q1 = surveyQuestionRepository.save(q1);

        createOption(q1.getQuestionId(), "진로/취업", 0);
        createOption(q1.getQuestionId(), "외국어", 1);
        createOption(q1.getQuestionId(), "자격증", 2);
        createOption(q1.getQuestionId(), "봉사활동", 3);
        createOption(q1.getQuestionId(), "문화/예술", 4);
        createOption(q1.getQuestionId(), "리더십", 5);
        createOption(q1.getQuestionId(), "창업", 6);

        // 질문 2: 객관식 단일선택
        SurveyQuestion q2 = SurveyQuestion.builder()
                .surveyId(survey.getSurveyId())
                .questionType(QuestionType.SINGLE_CHOICE)
                .questionText("선호하는 프로그램 운영 방식은?")
                .isRequired(true)
                .displayOrder(1)
                .build();
        q2 = surveyQuestionRepository.save(q2);

        createOption(q2.getQuestionId(), "대면 진행", 0);
        createOption(q2.getQuestionId(), "비대면(온라인) 진행", 1);
        createOption(q2.getQuestionId(), "혼합형(대면+비대면)", 2);

        // 질문 3: 척도형
        SurveyQuestion q3 = SurveyQuestion.builder()
                .surveyId(survey.getSurveyId())
                .questionType(QuestionType.SCALE)
                .questionText("단기 집중 프로그램(1-2주)에 대한 선호도는?")
                .isRequired(true)
                .displayOrder(2)
                .scaleMin(1)
                .scaleMax(5)
                .scaleMinLabel("선호하지 않음")
                .scaleMaxLabel("매우 선호함")
                .build();
        surveyQuestionRepository.save(q3);
    }

    private void createFeedbackSurvey(Integer adminId) {
        Survey survey = Survey.builder()
                .title("신규 서비스 이용 피드백")
                .description("새롭게 도입된 포트폴리오 및 설문조사 기능에 대한 여러분의 의견을 들려주세요!")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(7))
                .isAnonymous(false)
                .isActive(true)
                .targetType(SurveyTargetType.ALL)
                .allowMultipleResponses(false)
                .showResults(false)
                .createdBy(adminId)
                .build();
        survey = surveyRepository.save(survey);
        log.info("설문 생성: {}", survey.getTitle());

        // 질문 1: 척도형
        SurveyQuestion q1 = SurveyQuestion.builder()
                .surveyId(survey.getSurveyId())
                .questionType(QuestionType.SCALE)
                .questionText("새로운 포트폴리오 기능이 유용하다고 생각하시나요?")
                .isRequired(true)
                .displayOrder(0)
                .scaleMin(1)
                .scaleMax(5)
                .scaleMinLabel("전혀 유용하지 않음")
                .scaleMaxLabel("매우 유용함")
                .build();
        surveyQuestionRepository.save(q1);

        // 질문 2: 주관식 단답형
        SurveyQuestion q2 = SurveyQuestion.builder()
                .surveyId(survey.getSurveyId())
                .questionType(QuestionType.SHORT_TEXT)
                .questionText("가장 마음에 드는 기능은 무엇인가요?")
                .isRequired(false)
                .displayOrder(1)
                .build();
        surveyQuestionRepository.save(q2);

        // 질문 3: 주관식 서술형
        SurveyQuestion q3 = SurveyQuestion.builder()
                .surveyId(survey.getSurveyId())
                .questionType(QuestionType.LONG_TEXT)
                .questionText("추가로 원하시는 기능이나 개선 아이디어가 있다면 자유롭게 작성해주세요.")
                .isRequired(false)
                .displayOrder(2)
                .build();
        surveyQuestionRepository.save(q3);
    }

    private void createOption(Long questionId, String optionText, int displayOrder) {
        SurveyQuestionOption option = SurveyQuestionOption.builder()
                .questionId(questionId)
                .optionText(optionText)
                .displayOrder(displayOrder)
                .build();
        surveyQuestionOptionRepository.save(option);
    }
}
