package com.scms.app.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 사용자 정보 수정 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    @Email(message = "올바른 이메일 형식이 아닙니다")
    @Size(max = 100, message = "이메일은 100자 이내로 입력해주세요")
    private String email;

    @Size(max = 100, message = "전화번호는 100자 이내로 입력해주세요")
    private String phone;

    @Size(max = 100, message = "학과는 100자 이내로 입력해주세요")
    private String department;

    @Min(value = 1, message = "학년은 1 이상이어야 합니다")
    @Max(value = 4, message = "학년은 4 이하이어야 합니다")
    private Integer grade;
}
