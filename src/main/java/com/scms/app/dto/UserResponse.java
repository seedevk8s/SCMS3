package com.scms.app.dto;

import com.scms.app.model.User;
import com.scms.app.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 사용자 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Integer userId;
    private Integer studentNum;
    private String name;
    private String email;
    private String phone;
    private LocalDate birthDate;
    private String department;
    private Integer grade;
    private UserRole role;
    private Boolean locked;
    private Integer failCnt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Entity를 DTO로 변환
     */
    public static UserResponse from(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .studentNum(user.getStudentNum())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .birthDate(user.getBirthDate())
                .department(user.getDepartment())
                .grade(user.getGrade())
                .role(user.getRole())
                .locked(user.getLocked())
                .failCnt(user.getFailCnt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
