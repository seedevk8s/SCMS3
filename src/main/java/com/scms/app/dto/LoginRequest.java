package com.scms.app.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 로그인 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotNull(message = "학번을 입력해주세요")
    private Integer studentNum;

    @NotNull(message = "비밀번호를 입력해주세요")
    private String password;
}
