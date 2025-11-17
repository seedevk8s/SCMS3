package com.scms.app.service;

import com.scms.app.model.MileageHistory;
import com.scms.app.model.MileageRule;
import com.scms.app.model.User;
import com.scms.app.repository.MileageHistoryRepository;
import com.scms.app.repository.MileageRuleRepository;
import com.scms.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CHAMP 마일리지 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MileageService {

    private final MileageHistoryRepository mileageHistoryRepository;
    private final MileageRuleRepository mileageRuleRepository;
    private final UserRepository userRepository;

    /**
     * 마일리지 지급 (자동)
     *
     * @param userId 사용자 ID
     * @param activityType 활동 타입 (PROGRAM, COUNSELING, SURVEY)
     * @param activityId 활동 ID
     * @param activityName 활동명
     * @param points 지급 포인트
     * @param description 설명
     * @return 생성된 마일리지 내역
     */
    @Transactional
    public MileageHistory awardMileage(Integer userId, String activityType, Long activityId,
                                      String activityName, Integer points, String description) {
        // 중복 지급 방지 (같은 활동에 대해 이미 지급되었는지 확인)
        boolean alreadyAwarded = mileageHistoryRepository.existsByUserIdAndActivity(
                userId, activityType, activityId);

        if (alreadyAwarded) {
            log.warn("이미 지급된 마일리지: userId={}, activityType={}, activityId={}",
                    userId, activityType, activityId);
            throw new IllegalStateException("이미 마일리지가 지급된 활동입니다.");
        }

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다."));

        // 마일리지 내역 생성
        MileageHistory history = MileageHistory.builder()
                .user(user)
                .activityType(activityType)
                .activityId(activityId)
                .activityName(activityName)
                .points(points)
                .description(description)
                .earnedAt(LocalDateTime.now())
                .build();

        history = mileageHistoryRepository.save(history);
        log.info("마일리지 지급: userId={}, activityType={}, points={}, activityName={}",
                userId, activityType, points, activityName);

        return history;
    }

    /**
     * 마일리지 수동 지급/차감 (관리자용)
     *
     * @param userId 대상 사용자 ID
     * @param points 지급/차감 포인트 (양수: 지급, 음수: 차감)
     * @param activityName 활동명
     * @param description 사유
     * @param awardedBy 지급자 ID
     * @return 생성된 마일리지 내역
     */
    @Transactional
    public MileageHistory manualAwardMileage(Integer userId, Integer points,
                                            String activityName, String description,
                                            Integer awardedBy) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다."));

        // 지급자 조회
        User awarder = userRepository.findById(awardedBy)
                .orElseThrow(() -> new IllegalStateException("지급자를 찾을 수 없습니다."));

        // 마일리지 내역 생성
        MileageHistory history = MileageHistory.builder()
                .user(user)
                .activityType("MANUAL")
                .activityName(activityName)
                .points(points)
                .description(description)
                .awardedBy(awarder)
                .earnedAt(LocalDateTime.now())
                .build();

        history = mileageHistoryRepository.save(history);
        log.info("관리자 수동 마일리지 지급: userId={}, points={}, awardedBy={}, reason={}",
                userId, points, awardedBy, activityName);

        return history;
    }

    /**
     * 사용자의 총 마일리지 조회
     */
    public Long getTotalMileage(Integer userId) {
        return mileageHistoryRepository.getTotalMileageByUserId(userId);
    }

    /**
     * 사용자의 마일리지 내역 조회 (전체, 최신순)
     */
    public List<MileageHistory> getMileageHistory(Integer userId) {
        return mileageHistoryRepository.findByUserId(userId);
    }

    /**
     * 사용자의 최근 N개 마일리지 내역 조회
     */
    public List<MileageHistory> getRecentMileageHistory(Integer userId, int limit) {
        List<MileageHistory> allHistory = mileageHistoryRepository.findRecentByUserId(userId);
        return allHistory.stream().limit(limit).toList();
    }

    /**
     * 사용자의 활동 타입별 마일리지 통계
     *
     * @return Map<활동타입, Map<"totalPoints"|"count", 값>>
     */
    public Map<String, Map<String, Object>> getMileageStatistics(Integer userId) {
        List<Object[]> statistics = mileageHistoryRepository.getStatisticsByUserId(userId);

        Map<String, Map<String, Object>> result = new HashMap<>();
        for (Object[] stat : statistics) {
            String activityType = (String) stat[0];
            Long totalPoints = (Long) stat[1];
            Long count = (Long) stat[2];

            Map<String, Object> typeStats = new HashMap<>();
            typeStats.put("totalPoints", totalPoints);
            typeStats.put("count", count);
            typeStats.put("activityType", activityType);
            typeStats.put("activityTypeName", getActivityTypeName(activityType));

            result.put(activityType, typeStats);
        }

        return result;
    }

    /**
     * 마일리지 랭킹 조회 (전체 사용자)
     *
     * @return List<Map<userId, name, department, totalPoints, rank>>
     */
    public List<Map<String, Object>> getMileageRanking() {
        List<Object[]> rankings = mileageHistoryRepository.getMileageRanking();

        return rankings.stream()
                .limit(100) // TOP 100
                .map((Object[] ranking) -> {
                    int rank = rankings.indexOf(ranking) + 1;
                    Map<String, Object> rankData = new HashMap<>();
                    rankData.put("rank", rank);
                    rankData.put("userId", ranking[0]);
                    rankData.put("name", ranking[1]);
                    rankData.put("department", ranking[2]);
                    rankData.put("totalPoints", ranking[3]);
                    return rankData;
                })
                .toList();
    }

    /**
     * 특정 사용자의 랭킹 조회
     */
    public Map<String, Object> getUserMileageRanking(Integer userId) {
        Long totalMileage = getTotalMileage(userId);
        List<Map<String, Object>> allRankings = getMileageRanking();

        // 전체 랭킹에서 사용자 찾기
        for (Map<String, Object> ranking : allRankings) {
            if (ranking.get("userId").equals(userId)) {
                return ranking;
            }
        }

        // 랭킹에 없는 경우 (마일리지 0 또는 아직 없음)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다."));

        Map<String, Object> myRanking = new HashMap<>();
        myRanking.put("rank", allRankings.size() + 1);
        myRanking.put("userId", userId);
        myRanking.put("name", user.getName());
        myRanking.put("department", user.getDepartment());
        myRanking.put("totalPoints", totalMileage);

        return myRanking;
    }

    /**
     * 특정 기간의 마일리지 내역 조회
     */
    public List<MileageHistory> getMileageHistoryByDateRange(Integer userId,
                                                             LocalDateTime startDate,
                                                             LocalDateTime endDate) {
        return mileageHistoryRepository.findByUserIdAndDateRange(userId, startDate, endDate);
    }

    /**
     * 특정 활동 타입의 마일리지 내역 조회
     */
    public List<MileageHistory> getMileageHistoryByActivityType(Integer userId, String activityType) {
        return mileageHistoryRepository.findByUserIdAndActivityType(userId, activityType);
    }

    /**
     * 월별 마일리지 적립 통계
     */
    public List<Map<String, Object>> getMonthlyStatistics(Integer userId) {
        List<Object[]> statistics = mileageHistoryRepository.getMonthlyStatistics(userId);

        return statistics.stream()
                .map(stat -> {
                    Map<String, Object> monthData = new HashMap<>();
                    monthData.put("year", stat[0]);
                    monthData.put("month", stat[1]);
                    monthData.put("totalPoints", stat[2]);
                    return monthData;
                })
                .toList();
    }

    // ==================== 마일리지 규칙 관리 (관리자용) ====================

    /**
     * 모든 마일리지 규칙 조회 (관리자용)
     */
    public List<MileageRule> getAllRules() {
        return mileageRuleRepository.findAllNotDeleted();
    }

    /**
     * 활성화된 마일리지 규칙 조회
     */
    public List<MileageRule> getActiveRules() {
        return mileageRuleRepository.findActiveRules();
    }

    /**
     * 특정 활동 타입의 규칙 조회
     */
    public List<MileageRule> getRulesByActivityType(String activityType) {
        return mileageRuleRepository.findByActivityTypeAndActive(activityType);
    }

    /**
     * 마일리지 규칙 생성 (관리자용)
     */
    @Transactional
    public MileageRule createRule(String activityType, String activityName,
                                  Integer points, String description) {
        MileageRule rule = MileageRule.builder()
                .activityType(activityType)
                .activityName(activityName)
                .points(points)
                .description(description)
                .isActive(true)
                .build();

        rule = mileageRuleRepository.save(rule);
        log.info("마일리지 규칙 생성: ruleId={}, activityType={}, points={}",
                rule.getRuleId(), activityType, points);

        return rule;
    }

    /**
     * 마일리지 규칙 수정 (관리자용)
     */
    @Transactional
    public MileageRule updateRule(Long ruleId, String activityName,
                                  Integer points, String description, Boolean isActive) {
        MileageRule rule = mileageRuleRepository.findByIdNotDeleted(ruleId)
                .orElseThrow(() -> new IllegalStateException("규칙을 찾을 수 없습니다."));

        if (activityName != null) rule.setActivityName(activityName);
        if (points != null) rule.setPoints(points);
        if (description != null) rule.setDescription(description);
        if (isActive != null) rule.setIsActive(isActive);

        rule = mileageRuleRepository.save(rule);
        log.info("마일리지 규칙 수정: ruleId={}, points={}, isActive={}",
                ruleId, points, isActive);

        return rule;
    }

    /**
     * 마일리지 규칙 삭제 (Soft Delete, 관리자용)
     */
    @Transactional
    public void deleteRule(Long ruleId) {
        MileageRule rule = mileageRuleRepository.findByIdNotDeleted(ruleId)
                .orElseThrow(() -> new IllegalStateException("규칙을 찾을 수 없습니다."));

        rule.delete();
        mileageRuleRepository.save(rule);
        log.info("마일리지 규칙 삭제: ruleId={}", ruleId);
    }

    /**
     * 활동 타입 한글명 변환
     */
    private String getActivityTypeName(String activityType) {
        return switch (activityType) {
            case "PROGRAM" -> "프로그램 참여";
            case "COUNSELING" -> "상담";
            case "SURVEY" -> "설문조사";
            case "MANUAL" -> "관리자 지급";
            default -> activityType;
        };
    }
}
