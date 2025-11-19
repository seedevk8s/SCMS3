package com.scms.app.repository;

import com.scms.app.model.ExternalUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 외부회원 Repository
 */
@Repository
public interface ExternalUserRepository extends JpaRepository<ExternalUser, Integer> {

    /**
     * 이메일로 외부회원 조회 (삭제되지 않은 회원만)
     */
    Optional<ExternalUser> findByEmailAndDeletedAtIsNull(String email);

    /**
     * 이메일 존재 여부 확인
     */
    boolean existsByEmail(String email);

    /**
     * 이메일 인증 토큰으로 조회
     */
    Optional<ExternalUser> findByEmailVerifyToken(String token);

    /**
     * OAuth2 제공자와 제공자 ID로 외부회원 조회 (삭제되지 않은 회원만)
     */
    Optional<ExternalUser> findByProviderAndProviderIdAndDeletedAtIsNull(String provider, String providerId);

    /**
     * OAuth2 제공자와 이메일로 외부회원 조회 (삭제되지 않은 회원만)
     */
    Optional<ExternalUser> findByProviderAndEmailAndDeletedAtIsNull(String provider, String email);

    // ==================== 관리자 전용 메서드 ====================

    /**
     * 삭제되지 않은 전체 외부회원 조회 (페이징)
     */
    Page<ExternalUser> findByDeletedAtIsNull(Pageable pageable);

    /**
     * 이름 또는 이메일로 검색 (삭제되지 않은 회원만, 페이징)
     */
    Page<ExternalUser> findByNameContainingOrEmailContainingAndDeletedAtIsNull(
            String name, String email, Pageable pageable);

    /**
     * 삭제되지 않은 회원 수
     */
    long countByDeletedAtIsNull();

    /**
     * 이메일 인증 여부로 카운트 (삭제되지 않은 회원만)
     */
    long countByEmailVerifiedAndDeletedAtIsNull(Boolean emailVerified);

    /**
     * 잠김 여부로 카운트 (삭제되지 않은 회원만)
     */
    long countByLockedAndDeletedAtIsNull(Boolean locked);

    /**
     * 프로바이더별 카운트 (삭제되지 않은 회원만)
     */
    long countByProviderAndDeletedAtIsNull(String provider);
}
