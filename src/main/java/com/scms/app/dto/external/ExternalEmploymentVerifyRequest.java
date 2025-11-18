package com.scms.app.dto.external;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * 외부취업 활동 승인/거절 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalEmploymentVerifyRequest {

    @NotNull(message = "승인 여부는 필수입니다")
    private Boolean approve; // true: 승인, false: 거절

    @Min(value = 0, message = "가점은 0 이상이어야 합니다")
    private Integer credits; // 승인 시 부여할 가점

    @Size(max = 1000, message = "거절 사유는 1000자 이내로 입력해주세요")
    private String rejectionReason; // 거절 시 사유
}
