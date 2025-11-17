package com.scms.app.scheduler;

import com.scms.app.model.ApplicationStatus;
import com.scms.app.model.NotificationType;
import com.scms.app.model.Program;
import com.scms.app.model.ProgramApplication;
import com.scms.app.repository.ProgramApplicationRepository;
import com.scms.app.repository.ProgramRepository;
import com.scms.app.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 알림 스케줄러
 * - 프로그램 시작 알림 (D-1)
 * - 마감 임박 알림 (D-3)
 * - 오래된 알림 정리
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final NotificationService notificationService;
    private final ProgramRepository programRepository;
    private final ProgramApplicationRepository applicationRepository;

    /**
     * 프로그램 시작 알림 (D-1)
     * 매일 오전 9시 실행
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void sendProgramStartingNotifications() {
        log.info("프로그램 시작 알림 스케줄러 시작");

        try {
            LocalDate tomorrow = LocalDate.now().plusDays(1);

            // 내일 시작하는 프로그램 조회
            List<Program> programs = programRepository.findProgramsStartingOn(tomorrow);

            int notificationCount = 0;
            for (Program program : programs) {
                // 승인된 신청자들에게 알림 발송
                List<ProgramApplication> approvedApplications =
                        applicationRepository.findByProgramIdAndStatus(
                                program.getProgramId(),
                                ApplicationStatus.APPROVED);

                for (ProgramApplication application : approvedApplications) {
                    try {
                        String relatedUrl = "/programs/" + program.getProgramId();
                        notificationService.createNotificationByType(
                                application.getUser().getUserId(),
                                NotificationType.PROGRAM_STARTING,
                                program.getTitle(),
                                relatedUrl
                        );
                        notificationCount++;
                    } catch (Exception e) {
                        log.error("프로그램 시작 알림 생성 실패: programId={}, userId={}, error={}",
                                program.getProgramId(), application.getUser().getUserId(), e.getMessage());
                    }
                }
            }

            log.info("프로그램 시작 알림 발송 완료: 프로그램 수={}, 알림 수={}",
                    programs.size(), notificationCount);
        } catch (Exception e) {
            log.error("프로그램 시작 알림 스케줄러 실패: error={}", e.getMessage(), e);
        }
    }

    /**
     * 마감 임박 알림 (D-3)
     * 매일 오전 9시 실행
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void sendDeadlineApproachingNotifications() {
        log.info("마감 임박 알림 스케줄러 시작");

        try {
            LocalDateTime threeDaysLater = LocalDateTime.now().plusDays(3);
            LocalDateTime threeDaysLaterEnd = threeDaysLater.plusDays(1);

            // 3일 후 마감되는 프로그램 조회
            List<Program> programs = programRepository.findProgramsEndingBetween(
                    threeDaysLater.toLocalDate(),
                    threeDaysLaterEnd.toLocalDate());

            int notificationCount = 0;
            for (Program program : programs) {
                // 미신청자들에게 알림 발송하는 로직은 복잡하므로,
                // 현재는 프로그램별로 관리자에게만 알림 (향후 확장 가능)
                // 또는 전체 사용자에게 알림을 보낼 수도 있음

                // 여기서는 신청한 사용자 중 PENDING 상태인 사람들에게만 알림
                List<ProgramApplication> pendingApplications =
                        applicationRepository.findByProgramIdAndStatus(
                                program.getProgramId(),
                                ApplicationStatus.PENDING);

                for (ProgramApplication application : pendingApplications) {
                    try {
                        String relatedUrl = "/programs/" + program.getProgramId();
                        notificationService.createNotificationByType(
                                application.getUser().getUserId(),
                                NotificationType.DEADLINE_APPROACHING,
                                program.getTitle(),
                                relatedUrl
                        );
                        notificationCount++;
                    } catch (Exception e) {
                        log.error("마감 임박 알림 생성 실패: programId={}, userId={}, error={}",
                                program.getProgramId(), application.getUser().getUserId(), e.getMessage());
                    }
                }
            }

            log.info("마감 임박 알림 발송 완료: 프로그램 수={}, 알림 수={}",
                    programs.size(), notificationCount);
        } catch (Exception e) {
            log.error("마감 임박 알림 스케줄러 실패: error={}", e.getMessage(), e);
        }
    }

    /**
     * 오래된 알림 정리
     * 매일 자정 실행
     * - 30일 이전 읽은 알림 삭제
     * - 90일 이전 모든 알림 삭제
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void cleanupOldNotifications() {
        log.info("오래된 알림 정리 스케줄러 시작");

        try {
            int count = notificationService.cleanupOldNotifications();
            log.info("오래된 알림 정리 완료: 삭제된 알림 수={}", count);
        } catch (Exception e) {
            log.error("오래된 알림 정리 스케줄러 실패: error={}", e.getMessage(), e);
        }
    }
}
