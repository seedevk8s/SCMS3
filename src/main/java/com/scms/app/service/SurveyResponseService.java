package com.scms.app.service;

import com.scms.app.dto.*;
import com.scms.app.model.*;
import com.scms.app.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SurveyResponseService {

    private final SurveyRepository surveyRepository;
    private final SurveyQuestionRepository surveyQuestionRepository;
    private final SurveyQuestionOptionRepository surveyQuestionOptionRepository;
    private final SurveyResponseRepository surveyResponseRepository;
    private final SurveyAnswerRepository surveyAnswerRepository;
    private final SurveyTargetRepository surveyTargetRepository;

    /**
     * 설문 응답 제출
     */
    @Transactional
    public Long submitSurveyResponse(SurveySubmitRequest request, Integer userId,
                                     HttpServletRequest httpRequest) {
        log.info("설문 응답 제출 시작: surveyId={}, userId={}", request.getSurveyId(), userId);

        Survey survey = surveyRepository.findByIdAndNotDeleted(request.getSurveyId())
                .orElseThrow(() -> new IllegalArgumentException("설문을 찾을 수 없습니다"));

        // 응답 가능 여부 확인
        validateCanRespond(survey, userId);

        // 응답 저장
        SurveyResponse response = SurveyResponse.builder()
                .surveyId(survey.getSurveyId())
                .userId(survey.getIsAnonymous() ? null : userId)  // 익명이면 NULL
                .ipAddress(getClientIp(httpRequest))
                .userAgent(httpRequest.getHeader("User-Agent"))
                .build();

        response = surveyResponseRepository.save(response);
        log.info("응답 저장 완료: responseId={}", response.getResponseId());

        // 답변 저장
        for (SurveyAnswerRequest answerReq : request.getAnswers()) {
            saveAnswer(response.getResponseId(), answerReq);
        }

        // SPECIFIC 유형인 경우 응답 완료 표시
        if (survey.getTargetType() == SurveyTargetType.SPECIFIC) {
            surveyTargetRepository.markAsResponded(survey.getSurveyId(), userId);
        }

        log.info("설문 응답 제출 완료: responseId={}", response.getResponseId());
        return response.getResponseId();
    }

    /**
     * 답변 저장
     */
    private void saveAnswer(Long responseId, SurveyAnswerRequest answerReq) {
        // 복수선택인 경우 여러 개의 답변으로 저장
        if (answerReq.getOptionIds() != null && !answerReq.getOptionIds().isEmpty()) {
            for (Long optionId : answerReq.getOptionIds()) {
                SurveyAnswer answer = SurveyAnswer.builder()
                        .responseId(responseId)
                        .questionId(answerReq.getQuestionId())
                        .optionId(optionId)
                        .build();
                surveyAnswerRepository.save(answer);
            }
        } else {
            // 단일선택, 주관식, 척도형
            SurveyAnswer answer = SurveyAnswer.builder()
                    .responseId(responseId)
                    .questionId(answerReq.getQuestionId())
                    .optionId(answerReq.getOptionId())
                    .answerText(answerReq.getAnswerText())
                    .answerNumber(answerReq.getAnswerNumber())
                    .build();
            surveyAnswerRepository.save(answer);
        }
    }

    /**
     * 응답 가능 여부 확인
     */
    private void validateCanRespond(Survey survey, Integer userId) {
        // 진행 중인지 확인
        if (!survey.isAvailableForResponse()) {
            throw new IllegalArgumentException("현재 응답할 수 없는 설문입니다");
        }

        // 중복 응답 확인
        if (!survey.getAllowMultipleResponses()) {
            boolean hasResponded = surveyResponseRepository.existsBySurveyIdAndUserId(
                    survey.getSurveyId(), userId);
            if (hasResponded) {
                throw new IllegalArgumentException("이미 응답한 설문입니다");
            }
        }

        // SPECIFIC 유형인 경우 대상자 확인
        if (survey.getTargetType() == SurveyTargetType.SPECIFIC) {
            boolean isTarget = surveyTargetRepository.existsBySurveyIdAndUserId(
                    survey.getSurveyId(), userId);
            if (!isTarget) {
                throw new IllegalArgumentException("응답 권한이 없습니다");
            }
        }

        // 최대 응답 수 확인
        if (survey.getMaxResponses() != null) {
            long responseCount = surveyResponseRepository.countBySurveyId(survey.getSurveyId());
            if (responseCount >= survey.getMaxResponses()) {
                throw new IllegalArgumentException("설문 응답이 마감되었습니다");
            }
        }
    }

    /**
     * 설문 통계 조회
     */
    public SurveyStatisticsResponse getSurveyStatistics(Long surveyId) {
        log.info("설문 통계 조회: surveyId={}", surveyId);

        Survey survey = surveyRepository.findByIdAndNotDeleted(surveyId)
                .orElseThrow(() -> new IllegalArgumentException("설문을 찾을 수 없습니다"));

        long totalResponses = surveyResponseRepository.countBySurveyId(surveyId);
        long targetCount = 0;
        Double responseRate = null;

        if (survey.getTargetType() == SurveyTargetType.SPECIFIC) {
            targetCount = surveyTargetRepository.countBySurveyId(surveyId);
            if (targetCount > 0) {
                responseRate = (double) totalResponses / targetCount * 100;
            }
        }

        // 질문별 통계
        List<SurveyQuestion> questions = surveyQuestionRepository
                .findBySurveyIdOrderByDisplayOrder(surveyId);

        List<QuestionStatisticsResponse> questionStatistics = questions.stream()
                .map(question -> calculateQuestionStatistics(question, totalResponses))
                .collect(Collectors.toList());

        return SurveyStatisticsResponse.builder()
                .surveyId(surveyId)
                .title(survey.getTitle())
                .totalResponses(totalResponses)
                .targetCount(targetCount)
                .responseRate(responseRate)
                .questionStatistics(questionStatistics)
                .build();
    }

    /**
     * 질문별 통계 계산
     */
    private QuestionStatisticsResponse calculateQuestionStatistics(SurveyQuestion question,
                                                                     long totalResponses) {
        List<SurveyAnswer> answers = surveyAnswerRepository.findByQuestionId(question.getQuestionId());

        QuestionStatisticsResponse.QuestionStatisticsResponseBuilder builder =
                QuestionStatisticsResponse.builder()
                        .questionId(question.getQuestionId())
                        .questionText(question.getQuestionText())
                        .questionType(question.getQuestionType())
                        .totalAnswers((long) answers.size());

        // 객관식 통계
        if (question.isChoiceType()) {
            List<SurveyQuestionOption> options = surveyQuestionOptionRepository
                    .findByQuestionIdOrderByDisplayOrder(question.getQuestionId());

            List<QuestionStatisticsResponse.OptionStatistics> optionStats = options.stream()
                    .map(option -> {
                        long count = surveyAnswerRepository.countByQuestionIdAndOptionId(
                                question.getQuestionId(), option.getOptionId());
                        double percentage = totalResponses > 0 ?
                                (double) count / totalResponses * 100 : 0;

                        return QuestionStatisticsResponse.OptionStatistics.builder()
                                .optionId(option.getOptionId())
                                .optionText(option.getOptionText())
                                .count(count)
                                .percentage(percentage)
                                .build();
                    })
                    .collect(Collectors.toList());

            builder.optionStatistics(optionStats);
        }

        // 척도형 통계
        if (question.isScaleType()) {
            Double averageScale = surveyAnswerRepository.calculateAverageScale(question.getQuestionId());
            builder.averageScale(averageScale);

            // 척도별 분포
            Map<Integer, Long> scaleDistribution = answers.stream()
                    .filter(a -> a.getAnswerNumber() != null)
                    .collect(Collectors.groupingBy(
                            SurveyAnswer::getAnswerNumber,
                            Collectors.counting()
                    ));
            builder.scaleDistribution(scaleDistribution);
        }

        // 주관식 응답 목록
        if (question.isTextType()) {
            List<String> textAnswers = answers.stream()
                    .map(SurveyAnswer::getAnswerText)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            builder.textAnswers(textAnswers);
        }

        return builder.build();
    }

    /**
     * 사용자의 응답 내역 조회
     */
    public List<SurveyResponse> getUserResponses(Integer userId) {
        List<com.scms.app.model.SurveyResponse> responses = surveyResponseRepository
                .findByUserIdOrderBySubmittedAtDesc(userId);

        return responses.stream()
                .map(response -> {
                    Survey survey = surveyRepository.findById(response.getSurveyId())
                            .orElse(null);
                    if (survey == null) {
                        return null;
                    }

                    long questionCount = surveyQuestionRepository.countBySurveyId(survey.getSurveyId());
                    long responseCount = surveyResponseRepository.countBySurveyId(survey.getSurveyId());

                    return SurveyResponse.builder()
                            .surveyId(survey.getSurveyId())
                            .title(survey.getTitle())
                            .description(survey.getDescription())
                            .startDate(survey.getStartDate())
                            .endDate(survey.getEndDate())
                            .isAnonymous(survey.getIsAnonymous())
                            .createdAt(response.getSubmittedAt())
                            .questionCount(questionCount)
                            .responseCount(responseCount)
                            .hasResponded(true)
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 클라이언트 IP 주소 가져오기
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
