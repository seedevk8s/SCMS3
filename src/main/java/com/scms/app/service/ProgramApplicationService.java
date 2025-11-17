package com.scms.app.service;

import com.scms.app.model.ApplicationStatus;
import com.scms.app.model.NotificationType;
import com.scms.app.model.Program;
import com.scms.app.model.ProgramApplication;
import com.scms.app.model.User;
import com.scms.app.repository.ProgramApplicationRepository;
import com.scms.app.repository.ProgramRepository;
import com.scms.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 프로그램 신청 관리 Service
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ProgramApplicationService {

    private final ProgramApplicationRepository applicationRepository;
    private final ProgramRepository programRepository;
    private final UserRepository userRepository;
    private final ProgramService programService;
    private final NotificationService notificationService;

    /**
     * 프로그램 신청
     */
    @Transactional
    public ProgramApplication applyProgram(Integer userId, Integer programId) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: ID " + userId));

        // 프로그램 조회
        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new IllegalArgumentException("프로그램을 찾을 수 없습니다: ID " + programId));

        // 이미 신청한 이력이 있는지 확인 (활성 신청만)
        if (applicationRepository.existsActiveApplicationByUserAndProgram(userId, programId)) {
            throw new IllegalStateException("이미 신청한 프로그램입니다.");
        }

        // 신청 가능 여부 확인
        if (!program.isApplicationAvailable()) {
            throw new IllegalStateException("현재 신청할 수 없는 프로그램입니다.");
        }

        // 신청 정원 확인
        if (program.getMaxParticipants() != null
            && program.getCurrentParticipants() >= program.getMaxParticipants()) {
            throw new IllegalStateException("프로그램 정원이 마감되었습니다.");
        }

        // 신청 생성
        ProgramApplication application = ProgramApplication.builder()
                .program(program)
                .user(user)
                .status(ApplicationStatus.PENDING)
                .appliedAt(LocalDateTime.now())
                .build();

        ProgramApplication savedApplication = applicationRepository.save(application);

        // 프로그램 참가자 수 증가
        programService.incrementParticipants(programId);

        log.info("프로그램 신청 완료: 사용자 {} ({}), 프로그램 {} ({})",
                user.getName(), userId, program.getTitle(), programId);

        return savedApplication;
    }

    /**
     * 프로그램 신청 취소
     */
    @Transactional
    public void cancelApplication(Integer userId, Integer applicationId) {
        ProgramApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("신청 내역을 찾을 수 없습니다: ID " + applicationId));

        // 본인의 신청인지 확인
        if (!application.getUser().getUserId().equals(userId)) {
            throw new IllegalStateException("본인의 신청만 취소할 수 있습니다.");
        }

        // 취소 가능한 상태인지 확인
        if (!application.isCancellable()) {
            throw new IllegalStateException("취소할 수 없는 신청입니다.");
        }

        // 신청 취소 처리
        application.cancel();
        applicationRepository.save(application);

        // 프로그램 참가자 수 감소
        programService.decrementParticipants(application.getProgram().getProgramId());

        // 취소 알림 생성 (선택 사항 - 사용자가 직접 취소했으므로 알림이 필요없을 수 있음)
        // 하지만 기록을 위해 알림 생성
        try {
            String relatedUrl = "/programs/" + application.getProgram().getProgramId();
            notificationService.createNotificationByType(
                    userId,
                    NotificationType.APPLICATION_CANCELLED,
                    application.getProgram().getTitle(),
                    relatedUrl
            );
        } catch (Exception e) {
            log.error("취소 알림 생성 실패: applicationId={}, error={}", applicationId, e.getMessage());
        }

        log.info("프로그램 신청 취소 완료: 사용자 {}, 신청 ID {}",
                application.getUser().getName(), applicationId);
    }

    /**
     * 사용자의 모든 신청 내역 조회
     */
    public List<ProgramApplication> getUserApplications(Integer userId) {
        return applicationRepository.findByUserId(userId);
    }

    /**
     * 사용자의 특정 프로그램 신청 조회
     */
    public ProgramApplication getUserApplicationForProgram(Integer userId, Integer programId) {
        return applicationRepository.findByUserIdAndProgramId(userId, programId)
                .orElse(null);
    }

    /**
     * 프로그램별 신청 내역 조회 (관리자용)
     */
    public List<ProgramApplication> getProgramApplications(Integer programId) {
        return applicationRepository.findByProgramId(programId);
    }

    /**
     * 신청 승인 (관리자용)
     */
    @Transactional
    public void approveApplication(Integer applicationId) {
        ProgramApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("신청 내역을 찾을 수 없습니다: ID " + applicationId));

        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new IllegalStateException("대기 중인 신청만 승인할 수 있습니다.");
        }

        application.approve();
        applicationRepository.save(application);

        // 승인 알림 생성
        try {
            String relatedUrl = "/programs/" + application.getProgram().getProgramId();
            notificationService.createNotificationByType(
                    application.getUser().getUserId(),
                    NotificationType.APPLICATION_APPROVED,
                    application.getProgram().getTitle(),
                    relatedUrl
            );
        } catch (Exception e) {
            log.error("승인 알림 생성 실패: applicationId={}, error={}", applicationId, e.getMessage());
        }

        log.info("프로그램 신청 승인 완료: 신청 ID {}, 사용자 {}",
                applicationId, application.getUser().getName());
    }

    /**
     * 신청 거부 (관리자용)
     */
    @Transactional
    public void rejectApplication(Integer applicationId, String reason) {
        ProgramApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("신청 내역을 찾을 수 없습니다: ID " + applicationId));

        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new IllegalStateException("대기 중인 신청만 거부할 수 있습니다.");
        }

        application.reject(reason);
        applicationRepository.save(application);

        // 프로그램 참가자 수 감소
        programService.decrementParticipants(application.getProgram().getProgramId());

        // 거부 알림 생성
        try {
            String relatedUrl = "/programs/" + application.getProgram().getProgramId();
            String content = "'" + application.getProgram().getTitle() + "' 프로그램 신청이 거부되었습니다.";
            if (reason != null && !reason.isEmpty()) {
                content += "\n사유: " + reason;
            }
            notificationService.createNotification(
                    application.getUser().getUserId(),
                    NotificationType.APPLICATION_REJECTED.getTitle(),
                    content,
                    NotificationType.APPLICATION_REJECTED,
                    relatedUrl
            );
        } catch (Exception e) {
            log.error("거부 알림 생성 실패: applicationId={}, error={}", applicationId, e.getMessage());
        }

        log.info("프로그램 신청 거부 완료: 신청 ID {}, 사용자 {}, 사유: {}",
                applicationId, application.getUser().getName(), reason);
    }

    /**
     * 참여 완료 처리 (관리자용)
     */
    @Transactional
    public void completeApplication(Integer applicationId) {
        ProgramApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("신청 내역을 찾을 수 없습니다: ID " + applicationId));

        if (application.getStatus() != ApplicationStatus.APPROVED) {
            throw new IllegalStateException("승인된 신청만 완료 처리할 수 있습니다.");
        }

        application.complete();
        applicationRepository.save(application);

        log.info("프로그램 참여 완료 처리: 신청 ID {}, 사용자 {}",
                applicationId, application.getUser().getName());
    }

    /**
     * 신청 ID로 조회
     */
    public ProgramApplication getApplication(Integer applicationId) {
        return applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("신청 내역을 찾을 수 없습니다: ID " + applicationId));
    }

    /**
     * 사용자가 특정 프로그램을 신청했는지 확인
     */
    public boolean hasActiveApplication(Integer userId, Integer programId) {
        return applicationRepository.existsActiveApplicationByUserAndProgram(userId, programId);
    }
}
