package com.scms.app.dto;

import com.scms.app.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 로그인 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private Integer userId;
    private Integer studentNum;
    private String name;
    private String email;
    private UserRole role;
    private String department;
    private Integer grade;

    /**
     * 최초 로그인 여부 (비밀번호 변경 필요)
     */
    private Boolean isFirstLogin;

    /**
     * JWT Access Token (추후 구현)
     */
    private String accessToken;

    /**
     * JWT Refresh Token (추후 구현)
     */
    private String refreshToken;
}
