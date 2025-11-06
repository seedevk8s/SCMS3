package com.scms.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 비밀번호 찾기/재설정 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetRequest {

    @NotNull(message = "학번을 입력해주세요")
    private Integer studentNum;

    @NotBlank(message = "이름을 입력해주세요")
    private String name;

    @NotNull(message = "생년월일을 입력해주세요")
    private LocalDate birthDate;
}
