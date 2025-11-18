package com.scms.app.dto;

import com.scms.app.model.PortfolioVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 포트폴리오 생성 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioCreateRequest {

    @NotBlank(message = "포트폴리오 제목은 필수입니다")
    @Size(max = 200, message = "제목은 200자 이내로 입력해주세요")
    private String title;

    @Size(max = 2000, message = "설명은 2000자 이내로 입력해주세요")
    private String description;

    private PortfolioVisibility visibility = PortfolioVisibility.PRIVATE;

    @Size(max = 50, message = "템플릿 유형은 50자 이내로 입력해주세요")
    private String templateType;

    // 프로필 정보
    @Size(max = 500, message = "프로필 이미지 URL은 500자 이내로 입력해주세요")
    private String profileImageUrl;

    @Size(max = 5000, message = "자기소개는 5000자 이내로 입력해주세요")
    private String aboutMe;

    @Size(max = 500, message = "경력 목표는 500자 이내로 입력해주세요")
    private String careerGoal;

    // 연락처 정보
    @Size(max = 100, message = "이메일은 100자 이내로 입력해주세요")
    private String contactEmail;

    @Size(max = 20, message = "전화번호는 20자 이내로 입력해주세요")
    private String contactPhone;

    // SNS & 링크
    @Size(max = 200, message = "GitHub URL은 200자 이내로 입력해주세요")
    private String githubUrl;

    @Size(max = 200, message = "블로그 URL은 200자 이내로 입력해주세요")
    private String blogUrl;

    @Size(max = 200, message = "LinkedIn URL은 200자 이내로 입력해주세요")
    private String linkedinUrl;

    @Size(max = 200, message = "웹사이트 URL은 200자 이내로 입력해주세요")
    private String websiteUrl;

    // 역량 정보
    @Size(max = 2000, message = "기술 스택은 2000자 이내로 입력해주세요")
    private String skills;

    @Size(max = 2000, message = "관심 분야는 2000자 이내로 입력해주세요")
    private String interests;

    // 학력 정보
    @Size(max = 100, message = "전공은 100자 이내로 입력해주세요")
    private String major;

    private Integer grade;

    private Double gpa;
}
