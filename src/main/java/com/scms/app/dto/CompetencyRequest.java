package com.scms.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 역량 생성/수정 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompetencyRequest {

    @NotNull(message = "카테고리 ID는 필수입니다")
    private Long categoryId;

    @NotBlank(message = "역량명은 필수입니다")
    @Size(max = 100, message = "역량명은 100자 이내로 입력해주세요")
    private String name;

    @Size(max = 500, message = "설명은 500자 이내로 입력해주세요")
    private String description;
}
