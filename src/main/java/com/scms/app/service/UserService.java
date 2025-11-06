package com.scms.app.service;

import com.scms.app.dto.*;
import com.scms.app.exception.AccountLockedException;
import com.scms.app.exception.DuplicateUserException;
import com.scms.app.exception.InvalidPasswordException;
import com.scms.app.exception.UserNotFoundException;
import com.scms.app.model.LoginHistory;
import com.scms.app.model.User;
import com.scms.app.model.UserRole;
import com.scms.app.repository.LoginHistoryRepository;
import com.scms.app.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

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
}
