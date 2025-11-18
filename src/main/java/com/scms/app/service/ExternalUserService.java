package com.scms.app.service;

import com.scms.app.dto.ExternalSignupRequest;
import com.scms.app.model.AccountStatus;
import com.scms.app.model.ExternalUser;
import com.scms.app.repository.ExternalUserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 외부회원 Service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalUserService {

    private final ExternalUserRepository externalUserRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 외부회원 가입
     *
     * @param request 회원가입 요청
     * @return 생성된 외부회원
     */
    @Transactional
    public ExternalUser signup(ExternalSignupRequest request) {
        log.info("외부회원 가입 시도: {}", request.getEmail());

        // 1. 비밀번호 확인
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
        }

        // 2. 이메일 중복 체크
        if (externalUserRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다");
        }

        // 3. 이메일 인증 토큰 생성
        String verifyToken = UUID.randomUUID().toString();

        // 4. 외부회원 생성
        ExternalUser externalUser = ExternalUser.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phone(request.getPhone())
                .birthDate(request.getBirthDate())
                .address(request.getAddress())
                .gender(request.getGender())
                .agreeTerms(request.getAgreeTerms())
                .agreePrivacy(request.getAgreePrivacy())
                .agreeMarketing(request.getAgreeMarketing())
                .emailVerifyToken(verifyToken)
                .status(AccountStatus.ACTIVE)
                .build();

        ExternalUser savedUser = externalUserRepository.save(externalUser);

        // 5. 이메일 인증 메일 발송 (선택사항 - 추후 구현)
        // sendVerificationEmail(savedUser.getEmail(), verifyToken);

        log.info("외부회원 가입 완료: {} ({})", savedUser.getName(), savedUser.getEmail());

        return savedUser;
    }

    /**
     * 이메일 중복 체크
     *
     * @param email 확인할 이메일
     * @return 중복 여부
     */
    public boolean checkEmailDuplicate(String email) {
        return externalUserRepository.existsByEmail(email);
    }

    /**
     * 이메일 인증
     *
     * @param token 인증 토큰
     */
    @Transactional
    public void verifyEmail(String token) {
        ExternalUser user = externalUserRepository.findByEmailVerifyToken(token)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 인증 토큰입니다"));

        user.verifyEmail();
        log.info("이메일 인증 완료: {}", user.getEmail());
    }

    /**
     * 로그인
     *
     * @param email    이메일
     * @param password 비밀번호
     * @return 로그인한 외부회원
     */
    @Transactional
    public ExternalUser login(String email, String password) {
        ExternalUser user = externalUserRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new BadCredentialsException("이메일 또는 비밀번호가 일치하지 않습니다"));

        // 계정 잠금 확인
        if (user.getLocked()) {
            throw new AuthenticationException("계정이 잠겨있습니다. 관리자에게 문의하세요.") {
            };
        }

        // 비밀번호 확인
        if (!passwordEncoder.matches(password, user.getPassword())) {
            user.incrementFailCount();
            externalUserRepository.save(user);
            throw new BadCredentialsException("이메일 또는 비밀번호가 일치하지 않습니다");
        }

        // 로그인 성공
        user.resetFailCount();
        user.updateLastLogin();

        log.info("외부회원 로그인 성공: {} ({})", user.getName(), user.getEmail());

        return user;
    }

    /**
     * ID로 외부회원 조회
     *
     * @param userId 사용자 ID
     * @return 외부회원
     */
    public ExternalUser findById(Integer userId) {
        return externalUserRepository.findById(userId)
                .filter(user -> !user.isDeleted())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
    }

    /**
     * 이메일로 외부회원 조회
     *
     * @param email 이메일
     * @return 외부회원
     */
    public ExternalUser findByEmail(String email) {
        return externalUserRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
    }
}
