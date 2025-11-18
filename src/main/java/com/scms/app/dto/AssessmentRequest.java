package com.scms.app.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 역량 평가 생성/수정 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentRequest {

    @NotNull(message = "학생 ID는 필수입니다")
    private Long studentId;

    @NotNull(message = "역량 ID는 필수입니다")
    private Long competencyId;

    @NotNull(message = "점수는 필수입니다")
    @Min(value = 0, message = "점수는 0점 이상이어야 합니다")
    @Max(value = 100, message = "점수는 100점 이하여야 합니다")
    private Integer score;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate assessmentDate;

    @Size(max = 100, message = "평가자는 100자 이내로 입력해주세요")
    private String assessor;

    @Size(max = 500, message = "비고는 500자 이내로 입력해주세요")
    private String notes;
}
