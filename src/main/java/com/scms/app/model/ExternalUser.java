package com.scms.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 외부 회원 엔티티
 * - 이메일로 로그인하는 외부 사용자
 */
@Entity
@Table(name = "external_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "address", length = 200)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private AccountStatus status = AccountStatus.ACTIVE;

    @Column(name = "locked", nullable = false)
    @Builder.Default
    private Boolean locked = false;

    @Column(name = "fail_cnt", nullable = false)
    @Builder.Default
    private Integer failCnt = 0;

    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(name = "email_verify_token", length = 255)
    private String emailVerifyToken;

    @Column(name = "email_verified_at")
    private LocalDateTime emailVerifiedAt;

    @Column(name = "agree_terms", nullable = false)
    @Builder.Default
    private Boolean agreeTerms = false;

    @Column(name = "agree_privacy", nullable = false)
    @Builder.Default
    private Boolean agreePrivacy = false;

    @Column(name = "agree_marketing")
    @Builder.Default
    private Boolean agreeMarketing = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 계정 잠금
     */
    public void lock() {
        this.locked = true;
    }

    /**
     * 계정 잠금 해제
     */
    public void unlock() {
        this.locked = false;
        this.failCnt = 0;
    }

    /**
     * 로그인 실패 횟수 증가
     * - 5회 이상 실패 시 자동 잠금
     */
    public void incrementFailCount() {
        this.failCnt++;
        if (this.failCnt >= 5) {
            this.lock();
        }
    }

    /**
     * 로그인 성공 시 실패 횟수 초기화
     */
    public void resetFailCount() {
        this.failCnt = 0;
    }

    /**
     * 이메일 인증 처리
     */
    public void verifyEmail() {
        this.emailVerified = true;
        this.emailVerifiedAt = LocalDateTime.now();
        this.emailVerifyToken = null;
    }

    /**
     * 마지막 로그인 시간 업데이트
     */
    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    /**
     * 삭제 여부 확인
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    /**
     * Soft Delete
     */
    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}
