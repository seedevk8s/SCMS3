package com.scms.app.service;

import com.scms.app.dto.ExternalPasswordChangeRequest;
import com.scms.app.dto.ExternalProfileUpdateRequest;
import com.scms.app.dto.ExternalSignupRequest;
import com.scms.app.model.AccountStatus;
import com.scms.app.model.ExternalUser;
import com.scms.app.model.PasswordResetToken;
import com.scms.app.repository.ExternalUserRepository;
import com.scms.app.repository.PasswordResetTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final EmailService emailService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Value("${file.upload-dir:${user.home}/scms-uploads}")
    private String uploadDir;

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

        // 5. 이메일 인증 메일 발송
        try {
            emailService.sendVerificationEmail(savedUser.getEmail(), savedUser.getName(), verifyToken);
            log.info("이메일 인증 메일 발송 완료: {}", savedUser.getEmail());
        } catch (Exception e) {
            log.error("이메일 인증 메일 발송 실패: {}", savedUser.getEmail(), e);
            // 이메일 발송 실패 시 생성된 계정 삭제
            externalUserRepository.delete(savedUser);
            throw new RuntimeException("이메일 발송에 실패했습니다. 잠시 후 다시 시도해주세요.", e);
        }

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

        // 이메일 인증 확인
        if (!user.getEmailVerified()) {
            throw new AuthenticationException("이메일 인증이 완료되지 않았습니다. 이메일을 확인해주세요.") {
            };
        }

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

    /**
     * 인증 메일 재발송
     *
     * @param email 이메일
     */
    @Transactional
    public void resendVerificationEmail(String email) {
        ExternalUser user = externalUserRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        // 이미 인증된 경우
        if (user.getEmailVerified()) {
            throw new IllegalArgumentException("이미 인증된 이메일입니다");
        }

        // 새로운 토큰 생성
        String newToken = UUID.randomUUID().toString();
        user.updateEmailVerifyToken(newToken);

        // 이메일 발송
        emailService.sendVerificationEmail(user.getEmail(), user.getName(), newToken);

        log.info("인증 메일 재발송 완료: {}", user.getEmail());
    }

    /**
     * 비밀번호 재설정 요청 (이메일로 토큰 발송)
     *
     * @param email 이메일 주소
     */
    @Transactional
    public void requestPasswordResetByEmail(String email) {
        // 이메일로 외부 회원 조회
        ExternalUser user = externalUserRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new IllegalArgumentException("등록된 이메일이 없습니다"));

        // 기존 미사용 토큰 무효화
        passwordResetTokenRepository.invalidateAllExternalUserTokens(user, LocalDateTime.now());

        // 새 토큰 생성
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .tokenType(PasswordResetToken.TokenType.EXTERNAL)
                .externalUser(user)
                .email(email)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();

        passwordResetTokenRepository.save(resetToken);

        // 이메일 발송
        emailService.sendPasswordResetEmail(email, user.getName(), token);

        log.info("비밀번호 재설정 이메일 발송: {} ({})", user.getName(), email);
    }

    /**
     * 토큰을 이용한 비밀번호 재설정
     *
     * @param token 재설정 토큰
     * @param newPassword 새 비밀번호
     */
    @Transactional
    public void resetPasswordWithToken(String token, String newPassword) {
        // 토큰 조회 및 검증
        PasswordResetToken resetToken = passwordResetTokenRepository.findValidToken(token, LocalDateTime.now())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않거나 만료된 토큰입니다"));

        if (resetToken.getTokenType() != PasswordResetToken.TokenType.EXTERNAL) {
            throw new IllegalArgumentException("외부 회원용 토큰이 아닙니다");
        }

        ExternalUser user = resetToken.getExternalUser();
        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다");
        }

        // 비밀번호 변경
        user.setPassword(passwordEncoder.encode(newPassword));
        user.unlock(); // 계정 잠금 해제
        externalUserRepository.save(user);

        // 토큰 사용 처리
        resetToken.markAsUsed();
        passwordResetTokenRepository.save(resetToken);

        log.info("토큰을 이용한 비밀번호 재설정 완료: {} (ID: {})", user.getName(), user.getUserId());
    }

    /**
     * 프로필 수정
     *
     * @param userId 사용자 ID
     * @param request 프로필 수정 요청
     * @return 수정된 외부회원
     */
    @Transactional
    public ExternalUser updateProfile(Integer userId, ExternalProfileUpdateRequest request) {
        ExternalUser user = findById(userId);

        // 소셜 로그인 사용자는 일부 정보만 수정 가능
        if (user.isSocialUser()) {
            log.warn("소셜 로그인 사용자의 프로필 수정 제한: {} ({})", user.getName(), user.getProvider());
            user.setPhone(request.getPhone());
            user.setAddress(request.getAddress());
        } else {
            // 로컬 계정은 모든 정보 수정 가능
            user.setName(request.getName());
            user.setPhone(request.getPhone());
            user.setBirthDate(request.getBirthDate());
            user.setAddress(request.getAddress());
            user.setGender(request.getGender());
        }

        ExternalUser updatedUser = externalUserRepository.save(user);
        log.info("프로필 수정 완료: {} (ID: {})", updatedUser.getName(), updatedUser.getUserId());

        return updatedUser;
    }

    /**
     * 비밀번호 변경
     *
     * @param userId 사용자 ID
     * @param request 비밀번호 변경 요청
     */
    @Transactional
    public void changePassword(Integer userId, ExternalPasswordChangeRequest request) {
        ExternalUser user = findById(userId);

        // 소셜 로그인 사용자는 비밀번호 변경 불가
        if (user.isSocialUser()) {
            throw new IllegalArgumentException("소셜 로그인 사용자는 비밀번호를 변경할 수 없습니다");
        }

        // 새 비밀번호 확인
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("새 비밀번호가 일치하지 않습니다");
        }

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다");
        }

        // 비밀번호 변경
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        externalUserRepository.save(user);

        log.info("비밀번호 변경 완료: {} (ID: {})", user.getName(), user.getUserId());
    }

    /**
     * 프로필 이미지 업로드
     *
     * @param userId 사용자 ID
     * @param file 업로드할 이미지 파일
     * @return 저장된 이미지 URL
     */
    @Transactional
    public String updateProfileImage(Integer userId, MultipartFile file) {
        ExternalUser user = findById(userId);

        // 파일 검증
        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다");
        }

        // 파일 크기 검증 (5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("파일 크기는 5MB 이하여야 합니다");
        }

        // 파일 타입 검증
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다");
        }

        try {
            // 파일 저장 경로 생성
            Path uploadPath = Paths.get(uploadDir, "profiles");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 고유한 파일명 생성
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
            String filename = "profile_" + userId + "_" + System.currentTimeMillis() + extension;

            // 파일 저장
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // URL 생성 (실제 서버 도메인으로 변경 필요)
            String imageUrl = "/uploads/profiles/" + filename;

            // 기존 이미지 삭제 (로컬 저장소인 경우)
            if (user.getProfileImageUrl() != null && user.getProfileImageUrl().startsWith("/uploads/")) {
                try {
                    String oldFilename = user.getProfileImageUrl().substring(user.getProfileImageUrl().lastIndexOf("/") + 1);
                    Path oldFilePath = uploadPath.resolve(oldFilename);
                    Files.deleteIfExists(oldFilePath);
                } catch (Exception e) {
                    log.warn("기존 프로필 이미지 삭제 실패: {}", e.getMessage());
                }
            }

            // 사용자 프로필 이미지 URL 업데이트
            user.setProfileImageUrl(imageUrl);
            externalUserRepository.save(user);

            log.info("프로필 이미지 업로드 완료: {} (ID: {}, URL: {})", user.getName(), user.getUserId(), imageUrl);

            return imageUrl;
        } catch (IOException e) {
            log.error("프로필 이미지 업로드 실패: {}", e.getMessage(), e);
            throw new RuntimeException("파일 업로드 중 오류가 발생했습니다", e);
        }
    }

    /**
     * 회원 탈퇴 (Soft Delete)
     *
     * @param userId 사용자 ID
     * @param password 비밀번호 (로컬 계정인 경우 확인용)
     */
    @Transactional
    public void deleteAccount(Integer userId, String password) {
        ExternalUser user = findById(userId);

        // 로컬 계정인 경우 비밀번호 확인
        if (!user.isSocialUser()) {
            if (password == null || password.isEmpty()) {
                throw new IllegalArgumentException("비밀번호를 입력해주세요");
            }
            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
            }
        }

        // Soft Delete
        user.delete();

        // 개인정보 비식별화 (선택적)
        user.setName("탈퇴한 사용자");
        user.setPhone(null);
        user.setAddress(null);
        user.setProfileImageUrl(null);

        externalUserRepository.save(user);

        log.info("회원 탈퇴 완료: ID {}", userId);
    }

    // ==================== 관리자 전용 메서드 ====================

    /**
     * 전체 외부회원 목록 조회 (관리자용)
     *
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param search 검색어 (이름, 이메일)
     * @param provider 프로바이더 필터
     * @param emailVerified 이메일 인증 여부 필터
     * @return 페이징된 외부회원 목록
     */
    public Page<ExternalUser> getAllExternalUsers(
            int page, int size, String search,
            String provider, Boolean emailVerified) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // 검색어가 있으면 검색, 없으면 전체 조회
        if (search != null && !search.trim().isEmpty()) {
            return externalUserRepository.findByNameContainingOrEmailContainingAndDeletedAtIsNull(
                    search, search, pageable);
        }

        return externalUserRepository.findByDeletedAtIsNull(pageable);
    }

    /**
     * 외부회원 통계 조회 (관리자용)
     *
     * @return 통계 정보
     */
    public Map<String, Object> getExternalUserStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 전체 회원 수 (삭제되지 않은)
        long totalCount = externalUserRepository.countByDeletedAtIsNull();

        // 이메일 인증된 회원 수
        long verifiedCount = externalUserRepository.countByEmailVerifiedAndDeletedAtIsNull(true);

        // 잠긴 계정 수
        long lockedCount = externalUserRepository.countByLockedAndDeletedAtIsNull(true);

        // 프로바이더별 통계
        long localCount = externalUserRepository.countByProviderAndDeletedAtIsNull("LOCAL");
        long googleCount = externalUserRepository.countByProviderAndDeletedAtIsNull("GOOGLE");
        long kakaoCount = externalUserRepository.countByProviderAndDeletedAtIsNull("KAKAO");
        long naverCount = externalUserRepository.countByProviderAndDeletedAtIsNull("NAVER");

        stats.put("totalCount", totalCount);
        stats.put("verifiedCount", verifiedCount);
        stats.put("unverifiedCount", totalCount - verifiedCount);
        stats.put("lockedCount", lockedCount);
        stats.put("localCount", localCount);
        stats.put("googleCount", googleCount);
        stats.put("kakaoCount", kakaoCount);
        stats.put("naverCount", naverCount);

        return stats;
    }

    /**
     * 계정 잠금/해제 토글 (관리자용)
     *
     * @param userId 사용자 ID
     * @return 변경 후 잠금 상태
     */
    @Transactional
    public boolean toggleAccountLock(Integer userId) {
        ExternalUser user = findById(userId);

        if (user.getLocked()) {
            user.unlock();
            log.info("관리자가 계정 잠금 해제: {} (ID: {})", user.getName(), userId);
        } else {
            user.lock();
            log.info("관리자가 계정 잠금: {} (ID: {})", user.getName(), userId);
        }

        externalUserRepository.save(user);
        return user.getLocked();
    }

    /**
     * 이메일 인증 수동 처리 (관리자용)
     *
     * @param userId 사용자 ID
     */
    @Transactional
    public void verifyEmailManually(Integer userId) {
        ExternalUser user = findById(userId);

        if (user.getEmailVerified()) {
            throw new IllegalArgumentException("이미 인증된 이메일입니다");
        }

        user.verifyEmail();
        externalUserRepository.save(user);

        log.info("관리자가 이메일 인증 처리: {} (ID: {})", user.getName(), userId);
    }

    /**
     * 회원 강제 삭제 (관리자용)
     *
     * @param userId 사용자 ID
     */
    @Transactional
    public void deleteExternalUserByAdmin(Integer userId) {
        ExternalUser user = findById(userId);

        // Soft Delete
        user.delete();

        // 개인정보 비식별화
        user.setName("관리자삭제_" + userId);
        user.setPhone(null);
        user.setAddress(null);
        user.setProfileImageUrl(null);

        externalUserRepository.save(user);

        log.info("관리자가 회원 삭제: ID {}", userId);
    }

    /**
     * 계정 상태 변경 (관리자용)
     *
     * @param userId 사용자 ID
     * @param status 변경할 상태
     */
    @Transactional
    public void changeAccountStatus(Integer userId, AccountStatus status) {
        ExternalUser user = findById(userId);
        user.setStatus(status);
        externalUserRepository.save(user);

        log.info("관리자가 계정 상태 변경: {} (ID: {}) -> {}", user.getName(), userId, status);
    }
}
