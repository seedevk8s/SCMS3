package com.scms.app.dto.external;

import com.scms.app.model.EmploymentType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

/**
 * 외부취업 활동 등록/수정 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalEmploymentRequest {

    @NotNull(message = "활동 유형은 필수입니다")
    private EmploymentType employmentType;

    @NotBlank(message = "회사/기관명은 필수입니다")
    @Size(max = 200, message = "회사/기관명은 200자 이내로 입력해주세요")
    private String companyName;

    @Size(max = 100, message = "직위/직책은 100자 이내로 입력해주세요")
    private String position;

    @Size(max = 100, message = "부서는 100자 이내로 입력해주세요")
    private String department;

    @NotNull(message = "시작일은 필수입니다")
    private LocalDate startDate;

    private LocalDate endDate;

    @Size(max = 2000, message = "활동 설명은 2000자 이내로 입력해주세요")
    private String description;

    @Size(max = 2000, message = "업무 내용은 2000자 이내로 입력해주세요")
    private String workContent;

    @Size(max = 1000, message = "습득한 기술은 1000자 이내로 입력해주세요")
    private String skillsLearned;

    @Size(max = 500, message = "증명서 URL은 500자 이내로 입력해주세요")
    private String certificateUrl;
}
