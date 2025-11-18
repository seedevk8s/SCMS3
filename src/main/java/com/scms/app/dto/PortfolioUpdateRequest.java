package com.scms.app.dto;

import com.scms.app.model.PortfolioVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 포트폴리오 수정 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioUpdateRequest {

    @NotBlank(message = "포트폴리오 제목은 필수입니다")
    @Size(max = 200, message = "제목은 200자 이내로 입력해주세요")
    private String title;

    @Size(max = 2000, message = "설명은 2000자 이내로 입력해주세요")
    private String description;

    private PortfolioVisibility visibility;

    @Size(max = 50, message = "템플릿 유형은 50자 이내로 입력해주세요")
    private String templateType;
}
