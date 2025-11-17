package com.scms.app.config;

import com.scms.app.model.MileageRule;
import com.scms.app.repository.MileageRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 마일리지 초기 데이터 삽입
 * - 애플리케이션 시작 시 기본 마일리지 규칙을 자동으로 생성
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MileageDataInitializer implements CommandLineRunner {

    private final MileageRuleRepository mileageRuleRepository;

    @Override
    @Transactional
    public void run(String... args) {
        // 이미 데이터가 있으면 스킵
        if (mileageRuleRepository.count() > 0) {
            log.info("마일리지 규칙 데이터가 이미 존재합니다. 초기화를 스킵합니다.");
            return;
        }

        log.info("=== 마일리지 초기 데이터 삽입 시작 ===");

        // 프로그램 관련 마일리지
        createRule("PROGRAM", "프로그램 참여 완료", 100, "프로그램 참여를 완료한 경우");
        createRule("PROGRAM", "장기 프로그램 참여 완료", 200, "4주 이상 장기 프로그램 참여 완료");
        createRule("PROGRAM", "우수 참여자", 150, "프로그램 우수 참여자로 선정된 경우");

        // 상담 관련 마일리지 (추후 구현)
        createRule("COUNSELING", "개인 상담 완료", 50, "개인 상담 1회 완료");
        createRule("COUNSELING", "집단 상담 참여", 30, "집단 상담 1회 참여");

        // 설문 관련 마일리지 (추후 구현)
        createRule("SURVEY", "설문조사 완료", 20, "설문조사 응답 완료");
        createRule("SURVEY", "만족도 조사 완료", 30, "프로그램 만족도 조사 완료");

        // 기타 활동
        createRule("MANUAL", "관리자 수동 지급", 0, "관리자가 수동으로 지급하는 마일리지 (포인트는 변동)");

        log.info("=== 마일리지 초기 데이터 삽입 완료: {} 개 규칙 생성됨 ===", mileageRuleRepository.count());
    }

    private void createRule(String activityType, String activityName, Integer points, String description) {
        MileageRule rule = MileageRule.builder()
                .activityType(activityType)
                .activityName(activityName)
                .points(points)
                .description(description)
                .isActive(true)
                .build();

        mileageRuleRepository.save(rule);
        log.info("마일리지 규칙 생성: {} - {}P", activityName, points);
    }
}
