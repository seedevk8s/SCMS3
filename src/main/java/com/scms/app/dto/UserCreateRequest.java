package com.scms.app.dto;

import com.scms.app.model.UserRole;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 사용자 생성 요청 DTO (관리자용)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequest {

    @NotNull(message = "학번은 필수입니다")
    private Integer studentNum;

    @NotBlank(message = "이름은 필수입니다")
    @Size(max = 50, message = "이름은 50자 이내로 입력해주세요")
    private String name;

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    @Size(max = 100, message = "이메일은 100자 이내로 입력해주세요")
    private String email;

    @Size(max = 100, message = "전화번호는 100자 이내로 입력해주세요")
    private String phone;

    @NotNull(message = "생년월일은 필수입니다")
    private LocalDate birthDate;

    @Size(max = 100, message = "학과는 100자 이내로 입력해주세요")
    private String department;

    @Min(value = 1, message = "학년은 1 이상이어야 합니다")
    @Max(value = 4, message = "학년은 4 이하이어야 합니다")
    private Integer grade;

    @NotNull(message = "역할은 필수입니다")
    private UserRole role;

    /**
     * 초기 비밀번호 (생년월일 6자리: YYMMDD)
     * 최초 로그인 시 변경 필수
     */
    private String initialPassword;
}
