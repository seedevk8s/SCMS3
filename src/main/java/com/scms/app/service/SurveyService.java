package com.scms.app.service;

import com.scms.app.dto.*;
import com.scms.app.model.*;
import com.scms.app.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SurveyService {

    private final SurveyRepository surveyRepository;
    private final SurveyQuestionRepository surveyQuestionRepository;
    private final SurveyQuestionOptionRepository surveyQuestionOptionRepository;
    private final SurveyTargetRepository surveyTargetRepository;
    private final SurveyResponseRepository surveyResponseRepository;
    private final UserRepository userRepository;

    /**
     * 설문 생성
     */
    @Transactional
    public SurveyResponse createSurvey(SurveyCreateRequest request, Integer createdBy) {
        log.info("설문 생성 시작: title={}, createdBy={}", request.getTitle(), createdBy);

        // 설문 생성
        Survey survey = Survey.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isAnonymous(request.getIsAnonymous())
                .isActive(true)
                .targetType(request.getTargetType())
                .maxResponses(request.getMaxResponses())
                .allowMultipleResponses(request.getAllowMultipleResponses())
                .showResults(request.getShowResults())
                .createdBy(createdBy)
                .build();

        survey = surveyRepository.save(survey);
        log.info("설문 저장 완료: surveyId={}", survey.getSurveyId());

        // 질문 및 선택지 생성
        for (SurveyQuestionRequest questionReq : request.getQuestions()) {
            SurveyQuestion question = SurveyQuestion.builder()
                    .surveyId(survey.getSurveyId())
                    .questionType(questionReq.getQuestionType())
                    .questionText(questionReq.getQuestionText())
                    .isRequired(questionReq.getIsRequired())
                    .displayOrder(questionReq.getDisplayOrder())
                    .scaleMin(questionReq.getScaleMin())
                    .scaleMax(questionReq.getScaleMax())
                    .scaleMinLabel(questionReq.getScaleMinLabel())
                    .scaleMaxLabel(questionReq.getScaleMaxLabel())
                    .build();

            question = surveyQuestionRepository.save(question);
            log.info("질문 저장 완료: questionId={}", question.getQuestionId());

            // 객관식인 경우 선택지 저장
            if (question.isChoiceType() && questionReq.getOptions() != null) {
                for (SurveyQuestionOptionRequest optionReq : questionReq.getOptions()) {
                    SurveyQuestionOption option = SurveyQuestionOption.builder()
                            .questionId(question.getQuestionId())
                            .optionText(optionReq.getOptionText())
                            .displayOrder(optionReq.getDisplayOrder())
                            .build();

                    surveyQuestionOptionRepository.save(option);
                }
                log.info("선택지 {} 개 저장 완료", questionReq.getOptions().size());
            }
        }

        // SPECIFIC 유형인 경우 대상자 저장
        if (request.getTargetType() == SurveyTargetType.SPECIFIC &&
            request.getTargetUserIds() != null && !request.getTargetUserIds().isEmpty()) {

            for (Integer userId : request.getTargetUserIds()) {
                SurveyTarget target = SurveyTarget.builder()
                        .surveyId(survey.getSurveyId())
                        .userId(userId)
                        .hasResponded(false)
                        .build();

                surveyTargetRepository.save(target);
            }
            log.info("대상자 {} 명 저장 완료", request.getTargetUserIds().size());
        }

        return convertToResponse(survey);
    }

    /**
     * 설문 조회 (ID)
     */
    public SurveyDetailResponse getSurveyById(Long surveyId, Integer currentUserId) {
        Survey survey = surveyRepository.findByIdAndNotDeleted(surveyId)
                .orElseThrow(() -> new IllegalArgumentException("설문을 찾을 수 없습니다"));

        return convertToDetailResponse(survey, currentUserId);
    }

    /**
     * 활성 설문 목록 조회 (페이징)
     */
    public Page<SurveyResponse> getActiveSurveys(int page, int size, Integer currentUserId) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Survey> surveys = surveyRepository.findActivesurveys(pageable);

        return surveys.map(survey -> convertToResponse(survey, currentUserId));
    }

    /**
     * 진행 중인 설문 목록 조회
     */
    public List<SurveyResponse> getOngoingSurveys(Integer currentUserId) {
        LocalDateTime now = LocalDateTime.now();
        List<Survey> surveys = surveyRepository.findOngoingSurveys(now);

        return surveys.stream()
                .map(survey -> convertToResponse(survey, currentUserId))
                .collect(Collectors.toList());
    }

    /**
     * 사용자가 응답 가능한 설문 목록
     */
    public List<SurveyResponse> getAvailableSurveysForUser(Integer userId) {
        LocalDateTime now = LocalDateTime.now();
        List<Survey> allOngoing = surveyRepository.findOngoingSurveys(now);

        return allOngoing.stream()
                .filter(survey -> canUserRespond(survey, userId))
                .map(survey -> convertToResponse(survey, userId))
                .collect(Collectors.toList());
    }

    /**
     * 사용자가 설문에 응답 가능한지 확인
     */
    private boolean canUserRespond(Survey survey, Integer userId) {
        // 이미 응답했는지 확인 (중복 응답 허용 안하는 경우)
        if (!survey.getAllowMultipleResponses()) {
            boolean hasResponded = surveyResponseRepository.existsBySurveyIdAndUserId(
                    survey.getSurveyId(), userId);
            if (hasResponded) {
                return false;
            }
        }

        // SPECIFIC 유형인 경우 대상자인지 확인
        if (survey.getTargetType() == SurveyTargetType.SPECIFIC) {
            return surveyTargetRepository.existsBySurveyIdAndUserId(
                    survey.getSurveyId(), userId);
        }

        return true;
    }

    /**
     * 설문 수정
     */
    @Transactional
    public SurveyResponse updateSurvey(Long surveyId, SurveyCreateRequest request, Integer userId) {
        Survey survey = surveyRepository.findByIdAndNotDeleted(surveyId)
                .orElseThrow(() -> new IllegalArgumentException("설문을 찾을 수 없습니다"));

        // 권한 확인
        if (!survey.getCreatedBy().equals(userId)) {
            throw new IllegalArgumentException("수정 권한이 없습니다");
        }

        // 이미 응답이 있는 경우 수정 제한
        long responseCount = surveyResponseRepository.countBySurveyId(surveyId);
        if (responseCount > 0) {
            throw new IllegalArgumentException("이미 응답이 있는 설문은 수정할 수 없습니다");
        }

        // 기본 정보 업데이트
        survey.setTitle(request.getTitle());
        survey.setDescription(request.getDescription());
        survey.setStartDate(request.getStartDate());
        survey.setEndDate(request.getEndDate());
        survey.setIsAnonymous(request.getIsAnonymous());
        survey.setTargetType(request.getTargetType());
        survey.setMaxResponses(request.getMaxResponses());
        survey.setAllowMultipleResponses(request.getAllowMultipleResponses());
        survey.setShowResults(request.getShowResults());

        survey = surveyRepository.save(survey);

        // 기존 질문 및 선택지 삭제
        List<SurveyQuestion> existingQuestions = surveyQuestionRepository
                .findBySurveyIdOrderByDisplayOrder(surveyId);
        for (SurveyQuestion q : existingQuestions) {
            surveyQuestionOptionRepository.deleteByQuestionId(q.getQuestionId());
        }
        surveyQuestionRepository.deleteBySurveyId(surveyId);

        // 새 질문 및 선택지 생성 (createSurvey와 동일한 로직)
        for (SurveyQuestionRequest questionReq : request.getQuestions()) {
            SurveyQuestion question = SurveyQuestion.builder()
                    .surveyId(survey.getSurveyId())
                    .questionType(questionReq.getQuestionType())
                    .questionText(questionReq.getQuestionText())
                    .isRequired(questionReq.getIsRequired())
                    .displayOrder(questionReq.getDisplayOrder())
                    .scaleMin(questionReq.getScaleMin())
                    .scaleMax(questionReq.getScaleMax())
                    .scaleMinLabel(questionReq.getScaleMinLabel())
                    .scaleMaxLabel(questionReq.getScaleMaxLabel())
                    .build();

            question = surveyQuestionRepository.save(question);

            if (question.isChoiceType() && questionReq.getOptions() != null) {
                for (SurveyQuestionOptionRequest optionReq : questionReq.getOptions()) {
                    SurveyQuestionOption option = SurveyQuestionOption.builder()
                            .questionId(question.getQuestionId())
                            .optionText(optionReq.getOptionText())
                            .displayOrder(optionReq.getDisplayOrder())
                            .build();

                    surveyQuestionOptionRepository.save(option);
                }
            }
        }

        // 대상자 업데이트
        surveyTargetRepository.deleteBySurveyId(surveyId);
        if (request.getTargetType() == SurveyTargetType.SPECIFIC &&
            request.getTargetUserIds() != null && !request.getTargetUserIds().isEmpty()) {

            for (Integer targetUserId : request.getTargetUserIds()) {
                SurveyTarget target = SurveyTarget.builder()
                        .surveyId(survey.getSurveyId())
                        .userId(targetUserId)
                        .hasResponded(false)
                        .build();

                surveyTargetRepository.save(target);
            }
        }

        log.info("설문 수정 완료: surveyId={}", surveyId);
        return convertToResponse(survey);
    }

    /**
     * 설문 삭제 (Soft Delete)
     */
    @Transactional
    public void deleteSurvey(Long surveyId, Integer userId) {
        Survey survey = surveyRepository.findByIdAndNotDeleted(surveyId)
                .orElseThrow(() -> new IllegalArgumentException("설문을 찾을 수 없습니다"));

        // 권한 확인
        if (!survey.getCreatedBy().equals(userId)) {
            throw new IllegalArgumentException("삭제 권한이 없습니다");
        }

        survey.delete();
        surveyRepository.save(survey);

        log.info("설문 삭제 완료: surveyId={}", surveyId);
    }

    /**
     * 설문 활성화/비활성화
     */
    @Transactional
    public void toggleSurveyActive(Long surveyId, Integer userId) {
        Survey survey = surveyRepository.findByIdAndNotDeleted(surveyId)
                .orElseThrow(() -> new IllegalArgumentException("설문을 찾을 수 없습니다"));

        if (!survey.getCreatedBy().equals(userId)) {
            throw new IllegalArgumentException("수정 권한이 없습니다");
        }

        survey.setIsActive(!survey.getIsActive());
        surveyRepository.save(survey);

        log.info("설문 활성화 상태 변경: surveyId={}, isActive={}", surveyId, survey.getIsActive());
    }

    /**
     * Survey -> SurveyResponse 변환
     */
    private SurveyResponse convertToResponse(Survey survey) {
        return convertToResponse(survey, null);
    }

    private SurveyResponse convertToResponse(Survey survey, Integer currentUserId) {
        long questionCount = surveyQuestionRepository.countBySurveyId(survey.getSurveyId());
        long responseCount = surveyResponseRepository.countBySurveyId(survey.getSurveyId());

        Boolean hasResponded = null;
        if (currentUserId != null) {
            hasResponded = surveyResponseRepository.existsBySurveyIdAndUserId(
                    survey.getSurveyId(), currentUserId);
        }

        String createdByName = userRepository.findById(survey.getCreatedBy())
                .map(User::getName)
                .orElse("알 수 없음");

        return SurveyResponse.builder()
                .surveyId(survey.getSurveyId())
                .title(survey.getTitle())
                .description(survey.getDescription())
                .startDate(survey.getStartDate())
                .endDate(survey.getEndDate())
                .isAnonymous(survey.getIsAnonymous())
                .isActive(survey.getIsActive())
                .targetType(survey.getTargetType())
                .maxResponses(survey.getMaxResponses())
                .allowMultipleResponses(survey.getAllowMultipleResponses())
                .showResults(survey.getShowResults())
                .createdBy(survey.getCreatedBy())
                .createdByName(createdByName)
                .createdAt(survey.getCreatedAt())
                .updatedAt(survey.getUpdatedAt())
                .questionCount(questionCount)
                .responseCount(responseCount)
                .isOngoing(survey.isOngoing())
                .isExpired(survey.isExpired())
                .hasResponded(hasResponded)
                .build();
    }

    /**
     * Survey -> SurveyDetailResponse 변환 (질문 포함)
     */
    private SurveyDetailResponse convertToDetailResponse(Survey survey, Integer currentUserId) {
        SurveyResponse baseResponse = convertToResponse(survey, currentUserId);

        // 질문 목록 조회
        List<SurveyQuestion> questions = surveyQuestionRepository
                .findBySurveyIdOrderByDisplayOrder(survey.getSurveyId());

        List<Long> questionIds = questions.stream()
                .map(SurveyQuestion::getQuestionId)
                .collect(Collectors.toList());

        // 모든 선택지 한번에 조회
        List<SurveyQuestionOption> allOptions = questionIds.isEmpty() ?
                new ArrayList<>() :
                surveyQuestionOptionRepository.findByQuestionIdIn(questionIds);

        // 질문별로 선택지 그룹화
        List<SurveyQuestionResponse> questionResponses = questions.stream()
                .map(question -> {
                    List<SurveyQuestionOptionResponse> options = allOptions.stream()
                            .filter(opt -> opt.getQuestionId().equals(question.getQuestionId()))
                            .map(opt -> SurveyQuestionOptionResponse.builder()
                                    .optionId(opt.getOptionId())
                                    .questionId(opt.getQuestionId())
                                    .optionText(opt.getOptionText())
                                    .displayOrder(opt.getDisplayOrder())
                                    .build())
                            .collect(Collectors.toList());

                    return SurveyQuestionResponse.builder()
                            .questionId(question.getQuestionId())
                            .surveyId(question.getSurveyId())
                            .questionType(question.getQuestionType())
                            .questionText(question.getQuestionText())
                            .isRequired(question.getIsRequired())
                            .displayOrder(question.getDisplayOrder())
                            .scaleMin(question.getScaleMin())
                            .scaleMax(question.getScaleMax())
                            .scaleMinLabel(question.getScaleMinLabel())
                            .scaleMaxLabel(question.getScaleMaxLabel())
                            .options(options)
                            .build();
                })
                .collect(Collectors.toList());

        return SurveyDetailResponse.builder()
                .surveyId(baseResponse.getSurveyId())
                .title(baseResponse.getTitle())
                .description(baseResponse.getDescription())
                .startDate(baseResponse.getStartDate())
                .endDate(baseResponse.getEndDate())
                .isAnonymous(baseResponse.getIsAnonymous())
                .isActive(baseResponse.getIsActive())
                .targetType(baseResponse.getTargetType())
                .maxResponses(baseResponse.getMaxResponses())
                .allowMultipleResponses(baseResponse.getAllowMultipleResponses())
                .showResults(baseResponse.getShowResults())
                .createdBy(baseResponse.getCreatedBy())
                .createdByName(baseResponse.getCreatedByName())
                .createdAt(baseResponse.getCreatedAt())
                .updatedAt(baseResponse.getUpdatedAt())
                .responseCount(baseResponse.getResponseCount())
                .isOngoing(baseResponse.getIsOngoing())
                .isExpired(baseResponse.getIsExpired())
                .hasResponded(baseResponse.getHasResponded())
                .questions(questionResponses)
                .build();
    }
}
