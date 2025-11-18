package com.scms.app.dto;

import com.scms.app.model.ConsultationSession;
import com.scms.app.model.ConsultationStatus;
import com.scms.app.model.ConsultationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 상담 세션 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationResponse {

    private Long sessionId;

    // 학생 정보
    private Integer studentId;
    private Integer studentNum;
    private String studentName;
    private String studentEmail;
    private String studentPhone;
    private String studentDepartment;
    private Integer studentGrade;

    // 상담사 정보
    private Integer counselorId;
    private String counselorName;
    private String counselorSpecialty;

    // 상담 정보
    private ConsultationType consultationType;
    private String consultationTypeDescription;
    private ConsultationStatus status;
    private String statusDescription;
    private LocalDate requestedDate;
    private LocalTime requestedTime;
    private LocalDateTime scheduledDatetime;
    private String title;
    private String content;
    private String rejectionReason;

    // 메타 정보
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean cancellable;
    private boolean approvable;

    /**
     * Entity를 DTO로 변환
     */
    public static ConsultationResponse from(ConsultationSession session) {
        ConsultationResponseBuilder builder = ConsultationResponse.builder()
                .sessionId(session.getSessionId())
                // 학생 정보
                .studentId(session.getStudent().getUserId())
                .studentNum(session.getStudent().getStudentNum())
                .studentName(session.getStudent().getName())
                .studentEmail(session.getStudent().getEmail())
                .studentPhone(session.getStudent().getPhone())
                .studentDepartment(session.getStudent().getDepartment())
                .studentGrade(session.getStudent().getGrade())
                // 상담 정보
                .consultationType(session.getConsultationType())
                .consultationTypeDescription(session.getConsultationType().getDescription())
                .status(session.getStatus())
                .statusDescription(session.getStatus().getDescription())
                .requestedDate(session.getRequestedDate())
                .requestedTime(session.getRequestedTime())
                .scheduledDatetime(session.getScheduledDatetime())
                .title(session.getTitle())
                .content(session.getContent())
                .rejectionReason(session.getRejectionReason())
                // 메타 정보
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .cancellable(session.isCancellable())
                .approvable(session.isApprovable());

        // 상담사 정보 (없을 수 있음)
        if (session.getCounselor() != null) {
            builder.counselorId(session.getCounselor().getCounselorId())
                   .counselorName(session.getCounselor().getUser().getName())
                   .counselorSpecialty(session.getCounselor().getSpecialty());
        }

        return builder.build();
    }
}
