package com.scms.app.dto.external;

import com.scms.app.model.EmploymentType;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 외부취업 활동 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalEmploymentResponse {

    private Long employmentId;
    private Integer userId;
    private String userName; // 학생 이름 (조인 필요)
    private EmploymentType employmentType;
    private String employmentTypeName; // 활동 유형 한글명
    private String companyName;
    private String position;
    private String department;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer durationMonths;
    private String description;
    private String workContent;
    private String skillsLearned;
    private String certificateUrl;
    private Integer credits;
    private Boolean isVerified;
    private Integer verifiedBy;
    private String verifierName; // 승인자 이름
    private LocalDateTime verificationDate;
    private String rejectionReason;
    private Boolean isPortfolioLinked;
    private Long portfolioItemId;
    private Boolean isOngoing; // 진행 중 여부
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
