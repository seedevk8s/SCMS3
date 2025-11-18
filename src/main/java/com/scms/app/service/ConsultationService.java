package com.scms.app.service;

import com.scms.app.dto.ConsultationRequest;
import com.scms.app.dto.ConsultationResponse;
import com.scms.app.dto.ConsultationStatusUpdateRequest;
import com.scms.app.model.*;
import com.scms.app.repository.ConsultationSessionRepository;
import com.scms.app.repository.CounselorRepository;
import com.scms.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 상담 신청 및 관리 Service
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ConsultationService {

    private final ConsultationSessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final CounselorRepository counselorRepository;
    private final NotificationService notificationService;

    /**
     * 상담 신청
     */
    @Transactional
    public ConsultationSession createConsultation(Integer userId, ConsultationRequest request) {
        request.validate();

        // 학생 조회
        User student = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: ID " + userId));

        // 학생 역할 확인
        if (student.getRole() != UserRole.STUDENT) {
            throw new IllegalStateException("학생만 상담을 신청할 수 있습니다.");
        }

        // 상담사 조회 (지정된 경우)
        Counselor counselor = null;
        if (request.getCounselorId() != null) {
            counselor = counselorRepository.findByIdAndNotDeleted(request.getCounselorId())
                    .orElseThrow(() -> new IllegalArgumentException("상담사를 찾을 수 없습니다: ID " + request.getCounselorId()));
        }

        // 상담 세션 생성
        ConsultationSession session = ConsultationSession.builder()
                .student(student)
                .counselor(counselor)
                .consultationType(request.getConsultationType())
                .status(ConsultationStatus.PENDING)
                .requestedDate(request.getRequestedDate())
                .requestedTime(request.getRequestedTime())
                .title(request.getTitle())
                .content(request.getContent())
                .build();

        ConsultationSession saved = sessionRepository.save(session);

        // 상담사가 지정된 경우 알림 전송
        if (counselor != null) {
            notificationService.createNotification(
                    counselor.getUser().getUserId(),
                    NotificationType.CONSULTATION,
                    "새로운 상담 신청",
                    student.getName() + "님이 상담을 신청했습니다: " + request.getTitle()
            );
        }

        log.info("상담 신청 완료: 학생 {} ({}), 상담사 {}, 유형 {}",
                student.getName(), userId,
                counselor != null ? counselor.getUser().getName() : "미배정",
                request.getConsultationType().getDescription());

        return saved;
    }

    /**
     * 상담 세션 조회
     */
    public ConsultationSession getSessionById(Long sessionId) {
        return sessionRepository.findByIdAndNotDeleted(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("상담 세션을 찾을 수 없습니다: ID " + sessionId));
    }

    /**
     * 학생의 상담 내역 조회
     */
    public List<ConsultationResponse> getMyConsultations(Integer studentId) {
        return sessionRepository.findByStudentId(studentId).stream()
                .map(ConsultationResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 학생의 특정 상태 상담 내역 조회
     */
    public List<ConsultationResponse> getMyConsultationsByStatus(Integer studentId, ConsultationStatus status) {
        return sessionRepository.findByStudentIdAndStatus(studentId, status).stream()
                .map(ConsultationResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 상담사의 상담 세션 조회
     */
    public List<ConsultationResponse> getCounselorConsultations(Integer counselorId) {
        return sessionRepository.findByCounselorId(counselorId).stream()
                .map(ConsultationResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 상담사의 특정 상태 세션 조회
     */
    public List<ConsultationResponse> getCounselorConsultationsByStatus(Integer counselorId, ConsultationStatus status) {
        return sessionRepository.findByCounselorIdAndStatus(counselorId, status).stream()
                .map(ConsultationResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 관리자: 모든 상담 세션 조회
     */
    public List<ConsultationResponse> getAllConsultations() {
        return sessionRepository.findAllNotDeleted().stream()
                .map(ConsultationResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 관리자: 특정 상태의 모든 상담 세션 조회
     */
    public List<ConsultationResponse> getAllConsultationsByStatus(ConsultationStatus status) {
        return sessionRepository.findByStatus(status).stream()
                .map(ConsultationResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 상담 승인 (상담사 또는 관리자)
     */
    @Transactional
    public ConsultationSession approveConsultation(Long sessionId, Integer counselorId, ConsultationStatusUpdateRequest request) {
        request.validateForApproval();

        ConsultationSession session = getSessionById(sessionId);

        // 승인 가능한 상태인지 확인
        if (!session.isApprovable()) {
            throw new IllegalStateException("승인할 수 없는 상담 세션입니다.");
        }

        // 상담사 권한 확인 (상담사가 지정된 경우)
        if (session.getCounselor() != null &&
            !session.getCounselor().getCounselorId().equals(counselorId)) {
            throw new IllegalStateException("배정된 상담사만 승인할 수 있습니다.");
        }

        // 상담사 배정 (미배정인 경우)
        if (session.getCounselor() == null) {
            Counselor counselor = counselorRepository.findByIdAndNotDeleted(counselorId)
                    .orElseThrow(() -> new IllegalArgumentException("상담사를 찾을 수 없습니다: ID " + counselorId));
            session.setCounselor(counselor);
        }

        // 상담 승인
        session.approve(request.getScheduledDatetime());
        ConsultationSession updated = sessionRepository.save(session);

        // 학생에게 알림 전송
        notificationService.createNotification(
                session.getStudent().getUserId(),
                NotificationType.CONSULTATION,
                "상담 신청이 승인되었습니다",
                session.getTitle() + " 상담이 승인되었습니다. 일정: " + request.getScheduledDatetime()
        );

        log.info("상담 승인: 세션 ID {}, 상담사 {}, 확정일시 {}",
                sessionId, counselorId, request.getScheduledDatetime());

        return updated;
    }

    /**
     * 상담 거부 (상담사 또는 관리자)
     */
    @Transactional
    public ConsultationSession rejectConsultation(Long sessionId, Integer counselorId, ConsultationStatusUpdateRequest request) {
        request.validateForRejection();

        ConsultationSession session = getSessionById(sessionId);

        // 승인 가능한 상태인지 확인
        if (!session.isApprovable()) {
            throw new IllegalStateException("거부할 수 없는 상담 세션입니다.");
        }

        // 상담사 권한 확인 (상담사가 지정된 경우)
        if (session.getCounselor() != null &&
            !session.getCounselor().getCounselorId().equals(counselorId)) {
            throw new IllegalStateException("배정된 상담사만 거부할 수 있습니다.");
        }

        // 상담 거부
        session.reject(request.getRejectionReason());
        ConsultationSession updated = sessionRepository.save(session);

        // 학생에게 알림 전송
        notificationService.createNotification(
                session.getStudent().getUserId(),
                NotificationType.CONSULTATION,
                "상담 신청이 거부되었습니다",
                session.getTitle() + " 상담이 거부되었습니다. 사유: " + request.getRejectionReason()
        );

        log.info("상담 거부: 세션 ID {}, 상담사 {}, 사유: {}",
                sessionId, counselorId, request.getRejectionReason());

        return updated;
    }

    /**
     * 상담 취소 (학생)
     */
    @Transactional
    public void cancelConsultation(Integer userId, Long sessionId) {
        ConsultationSession session = getSessionById(sessionId);

        // 본인의 상담인지 확인
        if (!session.getStudent().getUserId().equals(userId)) {
            throw new IllegalStateException("본인의 상담만 취소할 수 있습니다.");
        }

        // 취소 가능한 상태인지 확인
        if (!session.isCancellable()) {
            throw new IllegalStateException("취소할 수 없는 상담 세션입니다.");
        }

        session.cancel();
        sessionRepository.save(session);

        // 상담사에게 알림 전송 (상담사가 있는 경우)
        if (session.getCounselor() != null) {
            notificationService.createNotification(
                    session.getCounselor().getUser().getUserId(),
                    NotificationType.CONSULTATION,
                    "상담이 취소되었습니다",
                    session.getStudent().getName() + "님이 상담을 취소했습니다: " + session.getTitle()
            );
        }

        log.info("상담 취소: 세션 ID {}, 학생 {}", sessionId, userId);
    }

    /**
     * 상담 완료 처리 (상담사)
     */
    @Transactional
    public void completeConsultation(Long sessionId, Integer counselorId) {
        ConsultationSession session = getSessionById(sessionId);

        // 상담사 권한 확인
        if (session.getCounselor() == null ||
            !session.getCounselor().getCounselorId().equals(counselorId)) {
            throw new IllegalStateException("배정된 상담사만 완료 처리할 수 있습니다.");
        }

        // 완료 가능한 상태인지 확인
        if (session.getStatus() != ConsultationStatus.APPROVED) {
            throw new IllegalStateException("승인된 상담만 완료 처리할 수 있습니다.");
        }

        session.complete();
        sessionRepository.save(session);

        log.info("상담 완료: 세션 ID {}, 상담사 {}", sessionId, counselorId);
    }

    /**
     * 상담사의 특정 날짜 승인된 세션 조회
     */
    public List<ConsultationResponse> getCounselorScheduleByDate(Integer counselorId, LocalDate date) {
        return sessionRepository.findByCounselorIdAndDate(counselorId, date).stream()
                .map(ConsultationResponse::from)
                .collect(Collectors.toList());
    }
}
