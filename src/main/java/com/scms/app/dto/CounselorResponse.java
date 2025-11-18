package com.scms.app.dto;

import com.scms.app.model.Counselor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 상담사 정보 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CounselorResponse {

    private Integer counselorId;
    private Integer userId;
    private String name;
    private String email;
    private String phone;
    private String specialty;
    private String introduction;
    private LocalDateTime createdAt;

    /**
     * Entity를 DTO로 변환
     */
    public static CounselorResponse from(Counselor counselor) {
        return CounselorResponse.builder()
                .counselorId(counselor.getCounselorId())
                .userId(counselor.getUser().getUserId())
                .name(counselor.getUser().getName())
                .email(counselor.getUser().getEmail())
                .phone(counselor.getUser().getPhone())
                .specialty(counselor.getSpecialty())
                .introduction(counselor.getIntroduction())
                .createdAt(counselor.getCreatedAt())
                .build();
    }
}
