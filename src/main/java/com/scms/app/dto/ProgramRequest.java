package com.scms.app.dto;

import com.scms.app.model.ProgramStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 프로그램 생성/수정 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProgramRequest {

    @NotBlank(message = "프로그램 제목은 필수입니다")
    @Size(max = 200, message = "제목은 200자 이내로 입력해주세요")
    private String title;

    @Size(max = 1000, message = "간단 설명은 1000자 이내로 입력해주세요")
    private String description;

    private String content;

    @Size(max = 100, message = "행정부서는 100자 이내로 입력해주세요")
    private String department;

    @Size(max = 100, message = "단과대학은 100자 이내로 입력해주세요")
    private String college;

    @NotBlank(message = "카테고리는 필수입니다")
    @Size(max = 50, message = "카테고리는 50자 이내로 입력해주세요")
    private String category;

    @Size(max = 50, message = "세부 카테고리는 50자 이내로 입력해주세요")
    private String subCategory;

    @NotNull(message = "신청 시작일은 필수입니다")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime applicationStartDate;

    @NotNull(message = "신청 종료일은 필수입니다")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime applicationEndDate;

    @Min(value = 1, message = "최대 정원은 1명 이상이어야 합니다")
    private Integer maxParticipants;

    @Size(max = 500, message = "썸네일 URL은 500자 이내로 입력해주세요")
    private String thumbnailUrl;

    @NotNull(message = "프로그램 상태는 필수입니다")
    private ProgramStatus status;
}
