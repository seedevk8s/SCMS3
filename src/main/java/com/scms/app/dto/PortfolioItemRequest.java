package com.scms.app.dto;

import com.scms.app.model.PortfolioItemType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 포트폴리오 항목 생성/수정 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioItemRequest {

    @NotNull(message = "항목 유형은 필수입니다")
    private PortfolioItemType itemType;

    @NotBlank(message = "항목 제목은 필수입니다")
    @Size(max = 200, message = "제목은 200자 이내로 입력해주세요")
    private String title;

    @Size(max = 5000, message = "설명은 5000자 이내로 입력해주세요")
    private String description;

    @Size(max = 200, message = "기관/조직은 200자 이내로 입력해주세요")
    private String organization;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private Integer displayOrder;

    private Long programApplicationId;
}
