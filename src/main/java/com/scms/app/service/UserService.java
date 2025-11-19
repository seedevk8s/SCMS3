package com.scms.app.service;

import com.scms.app.dto.*;
import com.scms.app.exception.AccountLockedException;
import com.scms.app.exception.DuplicateUserException;
import com.scms.app.exception.InvalidPasswordException;
import com.scms.app.exception.UserNotFoundException;
import com.scms.app.model.LoginHistory;
import com.scms.app.model.PasswordResetToken;
import com.scms.app.model.User;
import com.scms.app.model.UserRole;
import com.scms.app.repository.LoginHistoryRepository;
import com.scms.app.repository.PasswordResetTokenRepository;
import com.scms.app.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * 사용자 관리 Service
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final LoginHistoryRepository loginHistoryRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 로그인
     */
    @Transactional
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        // 학번으로 사용자 조회
        User user = userRepository.findByStudentNumAndNotDeleted(request.getStudentNum())
                .orElseThrow(() -> new UserNotFoundException("학번 또는 비밀번호가 일치하지 않습니다"));

        // 계정 잠금 확인
        if (user.getLocked()) {
            saveLoginHistory(user, httpRequest, false, "계정 잠금");
            throw new AccountLockedException();
        }

        // 비밀번호 확인
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            user.incrementFailCount();
            userRepository.save(user);
            saveLoginHistory(user, httpRequest, false, "비밀번호 불일치");
            throw new InvalidPasswordException("학번 또는 비밀번호가 일치하지 않습니다");
        }

        // 로그인 성공
        user.resetFailCount();
        userRepository.save(user);
        saveLoginHistory(user, httpRequest, true, null);

        // 최초 로그인 여부 확인 (초기 비밀번호: 생년월일 6자리)
        String initialPassword = user.getBirthDate().format(DateTimeFormatter.ofPattern("yyMMdd"));
        boolean isFirstLogin = request.getPassword().equals(initialPassword);

        return LoginResponse.builder()
                .userId(user.getUserId())
                .studentNum(user.getStudentNum())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .department(user.getDepartment())
                .grade(user.getGrade())
                .isFirstLogin(isFirstLogin)
                .build();
    }

    /**
     * 로그인 이력 저장
     */
    private void saveLoginHistory(User user, HttpServletRequest request, boolean success, String failReason) {
        String ipAddress = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        LoginHistory history = LoginHistory.builder()
                .user(user)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .isSuccess(success)
                .failReason(failReason)
                .build();

        loginHistoryRepository.save(history);
    }

    /**
     * 클라이언트 IP 주소 가져오기
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * 사용자 생성 (관리자용)
     */
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        // 학번 중복 확인
        if (userRepository.existsByStudentNum(request.getStudentNum())) {
            throw new DuplicateUserException("학번", request.getStudentNum().toString());
        }

        // 이메일 중복 확인
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateUserException("이메일", request.getEmail());
        }

        // 초기 비밀번호: 생년월일 6자리 (YYMMDD)
        String initialPassword = request.getBirthDate().format(DateTimeFormatter.ofPattern("yyMMdd"));
        String encodedPassword = passwordEncoder.encode(initialPassword);

        User user = User.builder()
                .studentNum(request.getStudentNum())
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(encodedPassword)
                .birthDate(request.getBirthDate())
                .department(request.getDepartment())
                .grade(request.getGrade())
                .role(request.getRole())
                .build();

        User savedUser = userRepository.save(user);
        log.info("사용자 생성 완료: {} (학번: {})", savedUser.getName(), savedUser.getStudentNum());

        return UserResponse.from(savedUser);
    }

    /**
     * 사용자 정보 수정
     */
    @Transactional
    public UserResponse updateUser(Integer userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: ID " + userId));

        // 이메일 변경 시 중복 확인
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateUserException("이메일", request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getDepartment() != null) {
            user.setDepartment(request.getDepartment());
        }
        if (request.getGrade() != null) {
            user.setGrade(request.getGrade());
        }

        User updatedUser = userRepository.save(user);
        log.info("사용자 정보 수정 완료: {} (ID: {})", updatedUser.getName(), updatedUser.getUserId());

        return UserResponse.from(updatedUser);
    }

    /**
     * 비밀번호 변경
     */
    @Transactional
    public void changePassword(Integer userId, PasswordChangeRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: ID " + userId));

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidPasswordException("현재 비밀번호가 일치하지 않습니다");
        }

        // 새 비밀번호 확인
        if (!request.getNewPassword().equals(request.getNewPasswordConfirm())) {
            throw new InvalidPasswordException("새 비밀번호가 일치하지 않습니다");
        }

        // 비밀번호 변경
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(encodedPassword);
        userRepository.save(user);

        log.info("비밀번호 변경 완료: {} (ID: {})", user.getName(), user.getUserId());
    }

    /**
     * 비밀번호 재설정 (비밀번호 찾기)
     */
    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        User user = userRepository.findByStudentNumAndNotDeleted(request.getStudentNum())
                .orElseThrow(() -> new UserNotFoundException("등록된 회원이 없습니다"));

        // 이름과 생년월일 확인
        if (!user.getName().equals(request.getName()) ||
                !user.getBirthDate().equals(request.getBirthDate())) {
            throw new UserNotFoundException("등록된 회원이 없습니다");
        }

        // 비밀번호를 생년월일 6자리로 초기화
        String initialPassword = request.getBirthDate().format(DateTimeFormatter.ofPattern("yyMMdd"));
        String encodedPassword = passwordEncoder.encode(initialPassword);
        user.setPassword(encodedPassword);
        user.unlock(); // 계정 잠금 해제

        userRepository.save(user);
        log.info("비밀번호 재설정 완료: {} (학번: {})", user.getName(), user.getStudentNum());
    }

    /**
     * 사용자 조회
     */
    public UserResponse getUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: ID " + userId));

        if (user.isDeleted()) {
            throw new UserNotFoundException("삭제된 사용자입니다");
        }

        return UserResponse.from(user);
    }

    /**
     * 학번으로 사용자 조회
     */
    public UserResponse getUserByStudentNum(Integer studentNum) {
        User user = userRepository.findByStudentNumAndNotDeleted(studentNum)
                .orElseThrow(() -> new UserNotFoundException(studentNum));

        return UserResponse.from(user);
    }

    /**
     * 모든 사용자 조회
     */
    public List<UserResponse> getAllUsers() {
        return userRepository.findAllNotDeleted().stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 역할별 사용자 조회
     */
    public List<UserResponse> getUsersByRole(UserRole role) {
        return userRepository.findByRoleAndNotDeleted(role).stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 사용자 삭제 (Soft Delete)
     */
    @Transactional
    public void deleteUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: ID " + userId));

        user.delete();
        userRepository.save(user);

        log.info("사용자 삭제 완료: {} (ID: {})", user.getName(), user.getUserId());
    }

    /**
     * 계정 잠금 해제
     */
    @Transactional
    public void unlockAccount(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: ID " + userId));

        user.unlock();
        userRepository.save(user);

        log.info("계정 잠금 해제: {} (ID: {})", user.getName(), user.getUserId());
    }

    /**
     * 비밀번호 재설정 요청 (이메일로 토큰 발송)
     *
     * @param email 이메일 주소
     */
    @Transactional
    public void requestPasswordResetByEmail(String email) {
        // 이메일로 사용자 조회
        User user = userRepository.findByEmailAndNotDeleted(email)
                .orElseThrow(() -> new UserNotFoundException("등록된 이메일이 없습니다"));

        // 기존 미사용 토큰 무효화
        passwordResetTokenRepository.invalidateAllUserTokens(user, LocalDateTime.now());

        // 새 토큰 생성
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .tokenType(PasswordResetToken.TokenType.INTERNAL)
                .user(user)
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

        if (resetToken.getTokenType() != PasswordResetToken.TokenType.INTERNAL) {
            throw new IllegalArgumentException("내부 회원용 토큰이 아닙니다");
        }

        User user = resetToken.getUser();
        if (user == null) {
            throw new UserNotFoundException("사용자를 찾을 수 없습니다");
        }

        // 비밀번호 변경
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        user.unlock(); // 계정 잠금 해제
        userRepository.save(user);

        // 토큰 사용 처리
        resetToken.markAsUsed();
        passwordResetTokenRepository.save(resetToken);

        log.info("토큰을 이용한 비밀번호 재설정 완료: {} (ID: {})", user.getName(), user.getUserId());
    }

    /**
     * 재설정 토큰 유효성 검증
     *
     * @param token 검증할 토큰
     * @return 유효 여부
     */
    public boolean validateResetToken(String token) {
        return passwordResetTokenRepository.findValidToken(token, LocalDateTime.now())
                .isPresent();
    }

    /**
     * 토큰으로 사용자 정보 조회
     *
     * @param token 재설정 토큰
     * @return 사용자 이메일 및 이름
     */
    public UserResponse getUserByResetToken(String token) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findValidToken(token, LocalDateTime.now())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않거나 만료된 토큰입니다"));

        if (resetToken.getTokenType() == PasswordResetToken.TokenType.INTERNAL && resetToken.getUser() != null) {
            return UserResponse.from(resetToken.getUser());
        } else if (resetToken.getTokenType() == PasswordResetToken.TokenType.EXTERNAL && resetToken.getExternalUser() != null) {
            // 외부 회원의 경우 (추후 구현)
            throw new IllegalArgumentException("외부 회원은 별도 처리가 필요합니다");
        }

        throw new IllegalArgumentException("토큰에 연결된 사용자를 찾을 수 없습니다");
    }

    /**
     * 만료된 토큰 정리 (스케줄러에서 호출)
     */
    @Transactional
    public void cleanupExpiredTokens() {
        passwordResetTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        passwordResetTokenRepository.deleteUsedTokensOlderThan(LocalDateTime.now().minusDays(7));
        log.info("만료된 비밀번호 재설정 토큰 정리 완료");
    }

    // ==================== 관리자 전용 메서드 ====================

    /**
     * 학생 목록 조회 (페이징, 검색)
     */
    public Page<User> getStudents(int page, int size, String search, String department, Integer grade) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // 검색어가 있는 경우
        if (search != null && !search.trim().isEmpty()) {
            return userRepository.searchStudents(search.trim(), pageable);
        }

        // 학과 필터
        if (department != null && !department.trim().isEmpty()) {
            return userRepository.findStudentsByDepartment(department, pageable);
        }

        // 학년 필터
        if (grade != null) {
            return userRepository.findStudentsByGrade(grade, pageable);
        }

        // 전체 학생 조회
        return userRepository.findStudents(pageable);
    }

    /**
     * 학생 통계 조회
     */
    public Map<String, Object> getStudentStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 전체 학생 수
        long totalStudents = userRepository.countStudents();
        stats.put("totalStudents", totalStudents);

        // 잠긴 학생 수
        long lockedStudents = userRepository.countLockedStudents();
        stats.put("lockedStudents", lockedStudents);

        // 활성 학생 수
        stats.put("activeStudents", totalStudents - lockedStudents);

        // 학년별 통계
        Map<Integer, Long> gradeStats = new HashMap<>();
        for (int i = 1; i <= 4; i++) {
            gradeStats.put(i, userRepository.countStudentsByGrade(i));
        }
        stats.put("gradeStats", gradeStats);

        // 학과별 통계 (상위 10개)
        List<String> departments = userRepository.findAllDepartments();
        Map<String, Long> departmentStats = new LinkedHashMap<>();
        for (String dept : departments) {
            if (dept != null && !dept.trim().isEmpty()) {
                departmentStats.put(dept, userRepository.countStudentsByDepartment(dept));
            }
        }
        stats.put("departmentStats", departmentStats);

        // 모든 학과 목록
        stats.put("departments", departments);

        log.info("학생 통계 조회 완료: 전체 {}명, 잠김 {}명", totalStudents, lockedStudents);
        return stats;
    }

    /**
     * 학생 계정 잠금/해제 토글
     */
    @Transactional
    public boolean toggleStudentLock(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("학생을 찾을 수 없습니다: ID " + userId));

        if (user.getRole() != UserRole.STUDENT) {
            throw new IllegalArgumentException("학생 계정이 아닙니다");
        }

        if (user.getLocked()) {
            user.unlock();
            log.info("학생 계정 잠금 해제: {} (학번: {})", user.getName(), user.getStudentNum());
        } else {
            user.lock();
            log.info("학생 계정 잠금: {} (학번: {})", user.getName(), user.getStudentNum());
        }

        userRepository.save(user);
        return user.getLocked();
    }

    /**
     * 학생 로그인 실패 횟수 초기화
     */
    @Transactional
    public void resetStudentFailCount(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("학생을 찾을 수 없습니다: ID " + userId));

        if (user.getRole() != UserRole.STUDENT) {
            throw new IllegalArgumentException("학생 계정이 아닙니다");
        }

        user.resetFailCount();
        userRepository.save(user);

        log.info("학생 로그인 실패 횟수 초기화: {} (학번: {})", user.getName(), user.getStudentNum());
    }

    /**
     * 관리자에 의한 학생 정보 수정
     */
    @Transactional
    public UserResponse updateStudentByAdmin(Integer userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("학생을 찾을 수 없습니다: ID " + userId));

        if (user.getRole() != UserRole.STUDENT) {
            throw new IllegalArgumentException("학생 계정이 아닙니다");
        }

        // 이메일 변경 시 중복 확인
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateUserException("이메일", request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        // 정보 업데이트
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getDepartment() != null) {
            user.setDepartment(request.getDepartment());
        }
        if (request.getGrade() != null) {
            user.setGrade(request.getGrade());
        }

        User updatedUser = userRepository.save(user);
        log.info("관리자에 의한 학생 정보 수정: {} (학번: {})", updatedUser.getName(), updatedUser.getStudentNum());

        return UserResponse.from(updatedUser);
    }

    /**
     * 관리자에 의한 학생 비밀번호 초기화
     */
    @Transactional
    public void resetStudentPasswordByAdmin(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("학생을 찾을 수 없습니다: ID " + userId));

        if (user.getRole() != UserRole.STUDENT) {
            throw new IllegalArgumentException("학생 계정이 아닙니다");
        }

        // 비밀번호를 생년월일 6자리로 초기화
        String initialPassword = user.getBirthDate().format(DateTimeFormatter.ofPattern("yyMMdd"));
        String encodedPassword = passwordEncoder.encode(initialPassword);
        user.setPassword(encodedPassword);
        user.unlock(); // 계정 잠금 해제

        userRepository.save(user);
        log.info("관리자에 의한 학생 비밀번호 초기화: {} (학번: {})", user.getName(), user.getStudentNum());
    }

    /**
     * 관리자에 의한 학생 삭제
     */
    @Transactional
    public void deleteStudentByAdmin(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("학생을 찾을 수 없습니다: ID " + userId));

        if (user.getRole() != UserRole.STUDENT) {
            throw new IllegalArgumentException("학생 계정이 아닙니다");
        }

        user.delete();
        userRepository.save(user);

        log.info("관리자에 의한 학생 삭제: {} (학번: {})", user.getName(), user.getStudentNum());
    }

    /**
     * 학생 일괄 등록
     */
    @Transactional
    public Map<String, Object> bulkCreateStudents(List<UserCreateRequest> requests) {
        int successCount = 0;
        int failCount = 0;
        List<String> errors = new ArrayList<>();

        for (UserCreateRequest request : requests) {
            try {
                // 학번 중복 확인
                if (userRepository.existsByStudentNum(request.getStudentNum())) {
                    errors.add(String.format("학번 %d: 이미 존재하는 학번입니다", request.getStudentNum()));
                    failCount++;
                    continue;
                }

                // 이메일 중복 확인
                if (userRepository.existsByEmail(request.getEmail())) {
                    errors.add(String.format("학번 %d: 이메일 %s가 이미 사용 중입니다", request.getStudentNum(), request.getEmail()));
                    failCount++;
                    continue;
                }

                // 초기 비밀번호: 생년월일 6자리 (YYMMDD)
                String initialPassword = request.getBirthDate().format(DateTimeFormatter.ofPattern("yyMMdd"));
                String encodedPassword = passwordEncoder.encode(initialPassword);

                User user = User.builder()
                        .studentNum(request.getStudentNum())
                        .name(request.getName())
                        .email(request.getEmail())
                        .phone(request.getPhone())
                        .password(encodedPassword)
                        .birthDate(request.getBirthDate())
                        .department(request.getDepartment())
                        .grade(request.getGrade())
                        .role(UserRole.STUDENT)
                        .locked(false)
                        .failCnt(0)
                        .build();

                userRepository.save(user);
                successCount++;

            } catch (Exception e) {
                errors.add(String.format("학번 %d: %s", request.getStudentNum(), e.getMessage()));
                failCount++;
                log.error("학생 일괄 등록 실패: 학번 {}", request.getStudentNum(), e);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", requests.size());
        result.put("success", successCount);
        result.put("fail", failCount);
        result.put("errors", errors);

        log.info("학생 일괄 등록 완료: 총 {}명, 성공 {}명, 실패 {}명", requests.size(), successCount, failCount);
        return result;
    }

    /**
     * 전체 사용자 목록 조회 (페이징, 검색, 필터링)
     */
    public Page<UserResponse> getAllUsers(int page, int size, String search, UserRole role, Boolean locked) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> usersPage;

        if (search != null && !search.trim().isEmpty()) {
            // 검색 조건이 있으면 검색
            if (role != null && locked != null) {
                usersPage = userRepository.findAllBySearchAndRoleAndLocked(search.trim(), role, locked, pageable);
            } else if (role != null) {
                usersPage = userRepository.findAllBySearchAndRole(search.trim(), role, pageable);
            } else if (locked != null) {
                usersPage = userRepository.findAllBySearchAndLocked(search.trim(), locked, pageable);
            } else {
                usersPage = userRepository.findAllBySearch(search.trim(), pageable);
            }
        } else {
            // 검색 조건이 없으면 필터만 적용
            if (role != null && locked != null) {
                usersPage = userRepository.findAllByRoleAndLocked(role, locked, pageable);
            } else if (role != null) {
                usersPage = userRepository.findAllByRole(role, pageable);
            } else if (locked != null) {
                usersPage = userRepository.findAllByLocked(locked, pageable);
            } else {
                usersPage = userRepository.findAllByNotDeleted(pageable);
            }
        }

        return usersPage.map(UserResponse::fromEntity);
    }

    /**
     * 전체 사용자 통계 조회
     */
    public Map<String, Object> getAllUserStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 전체 사용자 수
        long totalUsers = userRepository.countByDeletedFalse();
        stats.put("totalUsers", totalUsers);

        // 역할별 사용자 수
        long totalStudents = userRepository.countByRoleAndDeletedFalse(UserRole.STUDENT);
        long totalCounselors = userRepository.countByRoleAndDeletedFalse(UserRole.COUNSELOR);
        long totalAdmins = userRepository.countByRoleAndDeletedFalse(UserRole.ADMIN);

        stats.put("totalStudents", totalStudents);
        stats.put("totalCounselors", totalCounselors);
        stats.put("totalAdmins", totalAdmins);

        // 잠긴 계정 수
        long lockedUsers = userRepository.countByLockedTrueAndDeletedFalse();
        stats.put("lockedUsers", lockedUsers);

        return stats;
    }

    /**
     * 사용자 계정 잠금/해제 토글
     */
    @Transactional
    public void toggleUserLock(Long userId) {
        User user = userRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다"));

        user.setLocked(!user.getLocked());
        userRepository.save(user);

        log.info("사용자 계정 잠금 상태 변경: userId={}, locked={}", userId, user.getLocked());
    }

    /**
     * 사용자 비밀번호 초기화
     */
    @Transactional
    public String resetUserPassword(Long userId) {
        User user = userRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다"));

        String newPassword;
        String message;

        if (user.getRole() == UserRole.STUDENT) {
            // 학생: 생년월일 6자리 (YYMMDD)
            newPassword = user.getBirthDate().format(DateTimeFormatter.ofPattern("yyMMdd"));
            user.setPassword(passwordEncoder.encode(newPassword));
            message = "비밀번호가 생년월일(YYMMDD)로 초기화되었습니다";
        } else {
            // 상담사/관리자: 임시 비밀번호 생성 및 이메일 발송
            newPassword = generateTemporaryPassword();
            user.setPassword(passwordEncoder.encode(newPassword));

            // 이메일 발송
            try {
                emailService.sendPasswordResetEmail(user.getEmail(), user.getName(), newPassword);
                message = "임시 비밀번호가 이메일로 발송되었습니다";
            } catch (Exception e) {
                log.error("비밀번호 초기화 이메일 발송 실패: userId={}", userId, e);
                message = "비밀번호는 초기화되었으나 이메일 발송에 실패했습니다";
            }
        }

        user.resetFailCount();
        userRepository.save(user);

        log.info("사용자 비밀번호 초기화: userId={}, role={}", userId, user.getRole());
        return message;
    }

    /**
     * 임시 비밀번호 생성 (12자리: 영문 대소문자 + 숫자 + 특수문자)
     */
    private String generateTemporaryPassword() {
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "!@#$%^&*";
        String all = upper + lower + digits + special;

        Random random = new Random();
        StringBuilder password = new StringBuilder();

        // 각 종류에서 최소 1개씩
        password.append(upper.charAt(random.nextInt(upper.length())));
        password.append(lower.charAt(random.nextInt(lower.length())));
        password.append(digits.charAt(random.nextInt(digits.length())));
        password.append(special.charAt(random.nextInt(special.length())));

        // 나머지 8자리는 랜덤
        for (int i = 0; i < 8; i++) {
            password.append(all.charAt(random.nextInt(all.length())));
        }

        // 문자 섞기
        List<Character> chars = password.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.toList());
        Collections.shuffle(chars, random);

        return chars.stream()
                .map(String::valueOf)
                .collect(Collectors.joining());
    }
}
