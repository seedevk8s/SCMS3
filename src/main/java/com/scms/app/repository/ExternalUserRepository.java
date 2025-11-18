package com.scms.app.repository;

import com.scms.app.model.ExternalUser;
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
}
