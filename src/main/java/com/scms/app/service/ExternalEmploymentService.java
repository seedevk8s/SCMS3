package com.scms.app.service;

import com.scms.app.dto.external.*;
import com.scms.app.model.*;
import com.scms.app.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 외부취업 활동 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ExternalEmploymentService {

    private final ExternalEmploymentRepository employmentRepository;
    private final ExternalEmploymentCreditRuleRepository creditRuleRepository;
    private final UserRepository userRepository;
    private final MileageService mileageService;

    /**
     * 외부취업 활동 등록
     */
    @Transactional
    public ExternalEmploymentResponse registerEmployment(Integer userId, ExternalEmploymentRequest request) {
        log.info("외부취업 활동 등록 시작 - userId: {}, type: {}", userId, request.getEmploymentType());

        ExternalEmployment employment = ExternalEmployment.builder()
                .userId(userId)
                .employmentType(request.getEmploymentType())
                .companyName(request.getCompanyName())
                .position(request.getPosition())
                .department(request.getDepartment())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .description(request.getDescription())
                .workContent(request.getWorkContent())
                .skillsLearned(request.getSkillsLearned())
                .certificateUrl(request.getCertificateUrl())
                .isVerified(false)
                .credits(0)
                .isPortfolioLinked(false)
                .build();

        employment = employmentRepository.save(employment);
        log.info("외부취업 활동 등록 완료 - employmentId: {}", employment.getEmploymentId());

        return convertToResponse(employment);
    }

    /**
     * 외부취업 활동 수정
     */
    @Transactional
    public ExternalEmploymentResponse updateEmployment(Long employmentId, Integer userId, ExternalEmploymentRequest request) {
        log.info("외부취업 활동 수정 시작 - employmentId: {}, userId: {}", employmentId, userId);

        ExternalEmployment employment = employmentRepository.findByIdAndNotDeleted(employmentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 활동을 찾을 수 없습니다: " + employmentId));

        // 본인 확인
        if (!employment.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 활동만 수정할 수 있습니다");
        }

        // 승인된 활동은 수정 불가
        if (employment.getIsVerified()) {
            throw new IllegalArgumentException("승인된 활동은 수정할 수 없습니다");
        }

        // 업데이트
        employment.setEmploymentType(request.getEmploymentType());
        employment.setCompanyName(request.getCompanyName());
        employment.setPosition(request.getPosition());
        employment.setDepartment(request.getDepartment());
        employment.setStartDate(request.getStartDate());
        employment.setEndDate(request.getEndDate());
        employment.setDescription(request.getDescription());
        employment.setWorkContent(request.getWorkContent());
        employment.setSkillsLearned(request.getSkillsLearned());
        employment.setCertificateUrl(request.getCertificateUrl());

        employment = employmentRepository.save(employment);
        log.info("외부취업 활동 수정 완료 - employmentId: {}", employmentId);

        return convertToResponse(employment);
    }

    /**
     * 외부취업 활동 삭제 (Soft Delete)
     */
    @Transactional
    public void deleteEmployment(Long employmentId, Integer userId) {
        log.info("외부취업 활동 삭제 시작 - employmentId: {}, userId: {}", employmentId, userId);

        ExternalEmployment employment = employmentRepository.findByIdAndNotDeleted(employmentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 활동을 찾을 수 없습니다: " + employmentId));

        // 본인 확인
        if (!employment.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 활동만 삭제할 수 있습니다");
        }

        employment.delete();
        employmentRepository.save(employment);
        log.info("외부취업 활동 삭제 완료 - employmentId: {}", employmentId);
    }

    /**
     * 사용자의 외부취업 활동 목록 조회
     */
    public List<ExternalEmploymentResponse> getEmploymentsByUserId(Integer userId) {
        List<ExternalEmployment> employments = employmentRepository.findByUserId(userId);
        return employments.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 외부취업 활동 상세 조회
     */
    public ExternalEmploymentResponse getEmploymentById(Long employmentId) {
        ExternalEmployment employment = employmentRepository.findByIdAndNotDeleted(employmentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 활동을 찾을 수 없습니다: " + employmentId));

        return convertToResponse(employment);
    }

    /**
     * 사용자의 총 획득 가점 조회
     */
    public Integer getTotalCreditsByUserId(Integer userId) {
        return employmentRepository.getTotalCreditsByUserId(userId);
    }

    /**
     * 관리자: 승인 대기 중인 활동 목록 조회
     */
    public Page<ExternalEmploymentResponse> getPendingEmployments(Pageable pageable) {
        Page<ExternalEmployment> employments = employmentRepository.findPendingEmployments(pageable);
        return employments.map(this::convertToResponse);
    }

    /**
     * 관리자: 승인된 활동 목록 조회
     */
    public Page<ExternalEmploymentResponse> getVerifiedEmployments(Pageable pageable) {
        Page<ExternalEmployment> employments = employmentRepository.findVerifiedEmployments(pageable);
        return employments.map(this::convertToResponse);
    }

    /**
     * 관리자: 활동 승인 또는 거절
     */
    @Transactional
    public ExternalEmploymentResponse verifyEmployment(Long employmentId, Integer adminId, ExternalEmploymentVerifyRequest request) {
        log.info("외부취업 활동 검토 시작 - employmentId: {}, adminId: {}, approve: {}",
                employmentId, adminId, request.getApprove());

        ExternalEmployment employment = employmentRepository.findByIdAndNotDeleted(employmentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 활동을 찾을 수 없습니다: " + employmentId));

        if (request.getApprove()) {
            // 승인
            Integer credits = request.getCredits();
            if (credits == null || credits == 0) {
                // 가점이 지정되지 않은 경우 자동 계산
                credits = calculateCredits(employment.getEmploymentType(), employment.getDurationMonths());
            }

            employment.approve(adminId, credits);
            employmentRepository.save(employment);

            // 마일리지 지급
            try {
                mileageService.awardMileage(
                        employment.getUserId(),
                        "EXTERNAL_EMPLOYMENT",
                        employment.getEmploymentId(),
                        employment.getEmploymentType().getDisplayName() + " - " + employment.getCompanyName(),
                        credits,
                        employment.getDescription()
                );
                log.info("마일리지 지급 완료 - userId: {}, credits: {}", employment.getUserId(), credits);
            } catch (Exception e) {
                log.error("마일리지 지급 실패", e);
                // 마일리지 지급 실패해도 승인은 유지
            }

            log.info("외부취업 활동 승인 완료 - employmentId: {}, credits: {}", employmentId, credits);
        } else {
            // 거절
            employment.reject(adminId, request.getRejectionReason());
            employmentRepository.save(employment);
            log.info("외부취업 활동 거절 완료 - employmentId: {}, reason: {}", employmentId, request.getRejectionReason());
        }

        return convertToResponse(employment);
    }

    /**
     * 가점 자동 계산
     */
    public Integer calculateCredits(EmploymentType employmentType, Integer durationMonths) {
        if (durationMonths == null || durationMonths < 0) {
            log.warn("유효하지 않은 기간: {} 개월", durationMonths);
            return 0;
        }

        List<ExternalEmploymentCreditRule> matchingRules = creditRuleRepository.findMatchingRules(employmentType, durationMonths);

        if (matchingRules.isEmpty()) {
            log.warn("매칭되는 가점 규칙이 없습니다 - type: {}, duration: {} 개월", employmentType, durationMonths);
            return 0;
        }

        // 가장 높은 가점을 반환
        Integer credits = matchingRules.get(0).getBaseCredits();
        log.info("가점 계산 완료 - type: {}, duration: {} 개월, credits: {}", employmentType, durationMonths, credits);
        return credits;
    }

    /**
     * 활동 유형별 활성화된 가점 규칙 조회
     */
    public List<ExternalEmploymentCreditRule> getCreditRulesByType(EmploymentType employmentType) {
        return creditRuleRepository.findByEmploymentTypeAndActive(employmentType);
    }

    /**
     * 통계 조회 (관리자용)
     */
    public ExternalEmploymentStatisticsResponse getStatistics() {
        Long totalCount = employmentRepository.countAllActive();
        Long pendingCount = employmentRepository.countPending();
        Long verifiedCount = totalCount - pendingCount;

        // 총 가점 계산 (모든 승인된 활동의 가점 합계)
        Integer totalCredits = employmentRepository.findAll().stream()
                .filter(e -> e.getIsVerified() && e.getDeletedAt() == null)
                .mapToInt(ExternalEmployment::getCredits)
                .sum();

        // 활동 유형별 통계
        List<Object[]> typeStats = employmentRepository.getStatisticsByType();
        Map<String, Long> countByType = new HashMap<>();
        for (Object[] stat : typeStats) {
            EmploymentType type = (EmploymentType) stat[0];
            Long count = (Long) stat[1];
            countByType.put(type.getDisplayName(), count);
        }

        // 월별 통계
        List<Object[]> monthlyStats = employmentRepository.getMonthlyStatistics();
        List<ExternalEmploymentStatisticsResponse.MonthlyStatistic> monthlyStatistics = monthlyStats.stream()
                .map(stat -> ExternalEmploymentStatisticsResponse.MonthlyStatistic.builder()
                        .month((String) stat[0])
                        .count(((Number) stat[1]).longValue())
                        .build())
                .collect(Collectors.toList());

        return ExternalEmploymentStatisticsResponse.builder()
                .totalCount(totalCount)
                .pendingCount(pendingCount)
                .verifiedCount(verifiedCount)
                .totalCredits(totalCredits)
                .countByType(countByType)
                .monthlyStatistics(monthlyStatistics)
                .build();
    }

    /**
     * Entity를 Response DTO로 변환
     */
    private ExternalEmploymentResponse convertToResponse(ExternalEmployment employment) {
        // 사용자 이름 조회
        String userName = null;
        try {
            Optional<User> user = userRepository.findById(employment.getUserId());
            if (user.isPresent()) {
                userName = user.get().getName();
            }
        } catch (Exception e) {
            log.warn("사용자 이름 조회 실패 - userId: {}", employment.getUserId());
        }

        // 승인자 이름 조회
        String verifierName = null;
        if (employment.getVerifiedBy() != null) {
            try {
                Optional<User> verifier = userRepository.findById(employment.getVerifiedBy());
                if (verifier.isPresent()) {
                    verifierName = verifier.get().getName();
                }
            } catch (Exception e) {
                log.warn("승인자 이름 조회 실패 - verifiedBy: {}", employment.getVerifiedBy());
            }
        }

        return ExternalEmploymentResponse.builder()
                .employmentId(employment.getEmploymentId())
                .userId(employment.getUserId())
                .userName(userName)
                .employmentType(employment.getEmploymentType())
                .employmentTypeName(employment.getEmploymentType().getDisplayName())
                .companyName(employment.getCompanyName())
                .position(employment.getPosition())
                .department(employment.getDepartment())
                .startDate(employment.getStartDate())
                .endDate(employment.getEndDate())
                .durationMonths(employment.getDurationMonths())
                .description(employment.getDescription())
                .workContent(employment.getWorkContent())
                .skillsLearned(employment.getSkillsLearned())
                .certificateUrl(employment.getCertificateUrl())
                .credits(employment.getCredits())
                .isVerified(employment.getIsVerified())
                .verifiedBy(employment.getVerifiedBy())
                .verifierName(verifierName)
                .verificationDate(employment.getVerificationDate())
                .rejectionReason(employment.getRejectionReason())
                .isPortfolioLinked(employment.getIsPortfolioLinked())
                .portfolioItemId(employment.getPortfolioItemId())
                .isOngoing(employment.isOngoing())
                .createdAt(employment.getCreatedAt())
                .updatedAt(employment.getUpdatedAt())
                .build();
    }
}
