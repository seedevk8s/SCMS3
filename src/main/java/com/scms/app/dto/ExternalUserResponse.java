package com.scms.app.dto;

import com.scms.app.model.AccountStatus;
import com.scms.app.model.ExternalUser;
import com.scms.app.model.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 외부회원 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalUserResponse {

    private Integer userId;
    private String email;
    private String name;
    private String phone;
    private LocalDate birthDate;
    private String address;
    private Gender gender;
    private AccountStatus status;
    private Boolean emailVerified;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    /**
     * Entity에서 DTO로 변환
     */
    public static ExternalUserResponse from(ExternalUser externalUser) {
        return ExternalUserResponse.builder()
                .userId(externalUser.getUserId())
                .email(externalUser.getEmail())
                .name(externalUser.getName())
                .phone(externalUser.getPhone())
                .birthDate(externalUser.getBirthDate())
                .address(externalUser.getAddress())
                .gender(externalUser.getGender())
                .status(externalUser.getStatus())
                .emailVerified(externalUser.getEmailVerified())
                .createdAt(externalUser.getCreatedAt())
                .lastLoginAt(externalUser.getLastLoginAt())
                .build();
    }
}
