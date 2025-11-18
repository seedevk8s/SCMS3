package com.scms.app.service;

import com.scms.app.dto.ConsultationRecordRequest;
import com.scms.app.dto.ConsultationRecordResponse;
import com.scms.app.dto.StudentFeedbackRequest;
import com.scms.app.model.Counselor;
import com.scms.app.model.ConsultationRecord;
import com.scms.app.model.ConsultationSession;
import com.scms.app.model.NotificationType;
import com.scms.app.repository.ConsultationRecordRepository;
import com.scms.app.repository.ConsultationSessionRepository;
import com.scms.app.repository.CounselorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 상담 기록 관리 Service
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ConsultationRecordService {

    private final ConsultationRecordRepository recordRepository;
    private final ConsultationSessionRepository sessionRepository;
    private final CounselorRepository counselorRepository;
    private final NotificationService notificationService;
    private final MileageService mileageService;

    /**
     * 상담 기록 작성 (상담사)
     */
    @Transactional
    public ConsultationRecord createRecord(Long sessionId, Integer counselorId, ConsultationRecordRequest request) {
        request.validate();

        // 세션 조회
        ConsultationSession session = sessionRepository.findByIdAndNotDeleted(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("상담 세션을 찾을 수 없습니다: ID " + sessionId));

        // 상담사 조회
        Counselor counselor = counselorRepository.findByIdAndNotDeleted(counselorId)
                .orElseThrow(() -> new IllegalArgumentException("상담사를 찾을 수 없습니다: ID " + counselorId));

        // 상담사 권한 확인
        if (session.getCounselor() == null ||
            !session.getCounselor().getCounselorId().equals(counselorId)) {
            throw new IllegalStateException("배정된 상담사만 기록을 작성할 수 있습니다.");
        }

        // 이미 기록이 있는지 확인
        if (recordRepository.findBySessionId(sessionId).isPresent()) {
            throw new IllegalStateException("이미 상담 기록이 작성되었습니다.");
        }

        // 상담 기록 생성
        ConsultationRecord record = ConsultationRecord.builder()
                .session(session)
                .counselor(counselor)
                .student(session.getStudent())
                .consultationDate(request.getConsultationDate())
                .durationMinutes(request.getDurationMinutes())
                .summary(request.getSummary())
                .counselorNotes(request.getCounselorNotes())
                .followUpRequired(request.getFollowUpRequired() != null ? request.getFollowUpRequired() : false)
                .build();

        ConsultationRecord saved = recordRepository.save(record);

        // 세션 완료 처리
        session.complete();
        sessionRepository.save(session);

        // 학생에게 알림 전송
        notificationService.createNotification(
                session.getStudent().getUserId(),
                "상담이 완료되었습니다",
                session.getTitle() + " 상담이 완료되었습니다. 피드백을 작성해주세요.",
                NotificationType.CONSULTATION
        );

        // 마일리지 적립
        try {
            mileageService.awardMileageForConsultation(
                    session.getStudent().getUserId(),
                    sessionId,
                    session.getTitle()
            );
            log.info("상담 완료 마일리지 적립: 학생 ID {}, 세션 ID {}", session.getStudent().getUserId(), sessionId);
        } catch (Exception e) {
            log.error("마일리지 적립 실패: " + e.getMessage(), e);
        }

        log.info("상담 기록 작성: 세션 ID {}, 상담사 ID {}", sessionId, counselorId);

        return saved;
    }

    /**
     * 상담 기록 수정 (상담사)
     */
    @Transactional
    public ConsultationRecord updateRecord(Long recordId, Integer counselorId, ConsultationRecordRequest request) {
        request.validate();

        ConsultationRecord record = recordRepository.findByIdAndNotDeleted(recordId)
                .orElseThrow(() -> new IllegalArgumentException("상담 기록을 찾을 수 없습니다: ID " + recordId));

        // 상담사 권한 확인
        if (!record.getCounselor().getCounselorId().equals(counselorId)) {
            throw new IllegalStateException("작성한 상담사만 수정할 수 있습니다.");
        }

        // 정보 업데이트
        if (request.getConsultationDate() != null) {
            record.setConsultationDate(request.getConsultationDate());
        }
        if (request.getDurationMinutes() != null) {
            record.setDurationMinutes(request.getDurationMinutes());
        }
        if (request.getSummary() != null) {
            record.setSummary(request.getSummary());
        }
        if (request.getCounselorNotes() != null) {
            record.setCounselorNotes(request.getCounselorNotes());
        }
        if (request.getFollowUpRequired() != null) {
            record.setFollowUpRequired(request.getFollowUpRequired());
        }

        ConsultationRecord updated = recordRepository.save(record);

        log.info("상담 기록 수정: 기록 ID {}, 상담사 ID {}", recordId, counselorId);

        return updated;
    }

    /**
     * 학생 피드백 작성
     */
    @Transactional
    public ConsultationRecord addStudentFeedback(Long sessionId, Integer studentId, StudentFeedbackRequest request) {
        request.validate();

        // 세션으로 기록 조회
        ConsultationRecord record = recordRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("상담 기록을 찾을 수 없습니다: 세션 ID " + sessionId));

        // 학생 권한 확인
        if (!record.getStudent().getUserId().equals(studentId)) {
            throw new IllegalStateException("본인의 상담 기록에만 피드백을 작성할 수 있습니다.");
        }

        // 피드백 업데이트
        record.setStudentFeedback(request.getStudentFeedback());
        record.setSatisfactionScore(request.getSatisfactionScore());

        ConsultationRecord updated = recordRepository.save(record);

        log.info("학생 피드백 작성: 기록 ID {}, 학생 ID {}, 만족도 {}",
                record.getRecordId(), studentId, request.getSatisfactionScore());

        return updated;
    }

    /**
     * 상담 기록 조회 (상담사용 - 모든 정보)
     */
    public ConsultationRecordResponse getRecordForCounselor(Long recordId, Integer counselorId) {
        ConsultationRecord record = recordRepository.findByIdAndNotDeleted(recordId)
                .orElseThrow(() -> new IllegalArgumentException("상담 기록을 찾을 수 없습니다: ID " + recordId));

        // 상담사 권한 확인
        if (!record.getCounselor().getCounselorId().equals(counselorId)) {
            throw new IllegalStateException("작성한 상담사만 조회할 수 있습니다.");
        }

        return ConsultationRecordResponse.fromForCounselor(record);
    }

    /**
     * 상담 기록 조회 (학생용 - 상담사 메모 제외)
     */
    public ConsultationRecordResponse getRecordForStudent(Long sessionId, Integer studentId) {
        ConsultationRecord record = recordRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("상담 기록을 찾을 수 없습니다: 세션 ID " + sessionId));

        // 학생 권한 확인
        if (!record.getStudent().getUserId().equals(studentId)) {
            throw new IllegalStateException("본인의 상담 기록만 조회할 수 있습니다.");
        }

        return ConsultationRecordResponse.fromForStudent(record);
    }

    /**
     * 학생의 모든 상담 기록 조회
     */
    public List<ConsultationRecordResponse> getStudentRecords(Integer studentId) {
        return recordRepository.findByStudentId(studentId).stream()
                .map(ConsultationRecordResponse::fromForStudent)
                .collect(Collectors.toList());
    }

    /**
     * 상담사의 모든 상담 기록 조회
     */
    public List<ConsultationRecordResponse> getCounselorRecords(Integer counselorId) {
        return recordRepository.findByCounselorId(counselorId).stream()
                .map(ConsultationRecordResponse::fromForCounselor)
                .collect(Collectors.toList());
    }

    /**
     * 추가 상담이 필요한 기록 조회 (관리자용)
     */
    public List<ConsultationRecordResponse> getFollowUpRequiredRecords() {
        return recordRepository.findByFollowUpRequired().stream()
                .map(ConsultationRecordResponse::fromForCounselor)
                .collect(Collectors.toList());
    }

    /**
     * 모든 상담 기록 조회 (관리자용)
     */
    public List<ConsultationRecordResponse> getAllRecords() {
        return recordRepository.findAllNotDeleted().stream()
                .map(ConsultationRecordResponse::fromForCounselor)
                .collect(Collectors.toList());
    }
}
