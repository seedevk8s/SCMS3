package com.scms.app.dto;

import com.scms.app.model.ConsultationRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 상담 기록 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationRecordResponse {

    private Long recordId;
    private Long sessionId;

    // 상담사 정보
    private Integer counselorId;
    private String counselorName;

    // 학생 정보
    private Integer studentId;
    private String studentName;

    // 상담 기록
    private LocalDateTime consultationDate;
    private Integer durationMinutes;
    private String summary;
    private String counselorNotes; // 상담사만 조회 가능
    private String studentFeedback;
    private Integer satisfactionScore;
    private Boolean followUpRequired;

    // 메타 정보
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Entity를 DTO로 변환 (상담사용 - 모든 정보)
     */
    public static ConsultationRecordResponse fromForCounselor(ConsultationRecord record) {
        return ConsultationRecordResponse.builder()
                .recordId(record.getRecordId())
                .sessionId(record.getSession().getSessionId())
                .counselorId(record.getCounselor().getCounselorId())
                .counselorName(record.getCounselor().getUser().getName())
                .studentId(record.getStudent().getUserId())
                .studentName(record.getStudent().getName())
                .consultationDate(record.getConsultationDate())
                .durationMinutes(record.getDurationMinutes())
                .summary(record.getSummary())
                .counselorNotes(record.getCounselorNotes()) // 상담사 전용
                .studentFeedback(record.getStudentFeedback())
                .satisfactionScore(record.getSatisfactionScore())
                .followUpRequired(record.getFollowUpRequired())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }

    /**
     * Entity를 DTO로 변환 (학생용 - 상담사 메모 제외)
     */
    public static ConsultationRecordResponse fromForStudent(ConsultationRecord record) {
        return ConsultationRecordResponse.builder()
                .recordId(record.getRecordId())
                .sessionId(record.getSession().getSessionId())
                .counselorId(record.getCounselor().getCounselorId())
                .counselorName(record.getCounselor().getUser().getName())
                .studentId(record.getStudent().getUserId())
                .studentName(record.getStudent().getName())
                .consultationDate(record.getConsultationDate())
                .durationMinutes(record.getDurationMinutes())
                .summary(record.getSummary())
                .counselorNotes(null) // 학생에게는 비공개
                .studentFeedback(record.getStudentFeedback())
                .satisfactionScore(record.getSatisfactionScore())
                .followUpRequired(record.getFollowUpRequired())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }
}
