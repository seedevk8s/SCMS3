package com.scms.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 역량 카테고리 생성/수정 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompetencyCategoryRequest {

    @NotBlank(message = "카테고리명은 필수입니다")
    @Size(max = 100, message = "카테고리명은 100자 이내로 입력해주세요")
    private String name;

    @Size(max = 500, message = "설명은 500자 이내로 입력해주세요")
    private String description;
}
