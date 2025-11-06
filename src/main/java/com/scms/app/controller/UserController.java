package com.scms.app.controller;

import com.scms.app.dto.UserCreateRequest;
import com.scms.app.dto.UserResponse;
import com.scms.app.dto.UserUpdateRequest;
import com.scms.app.model.UserRole;
import com.scms.app.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 사용자 관리 REST API Controller (관리자용)
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * 사용자 생성 API (관리자용)
     *
     * @param request 사용자 생성 요청
     * @return 생성된 사용자 정보
     */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        log.info("사용자 생성 요청: 학번 {}, 이름 {}", request.getStudentNum(), request.getName());

        UserResponse response = userService.createUser(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 사용자 정보 수정 API
     *
     * @param userId 사용자 ID
     * @param request 수정 요청
     * @return 수정된 사용자 정보
     */
    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Integer userId,
            @Valid @RequestBody UserUpdateRequest request) {

        log.info("사용자 정보 수정 요청: userId {}", userId);

        UserResponse response = userService.updateUser(userId, request);

        return ResponseEntity.ok(response);
    }

    /**
     * 사용자 조회 API
     *
     * @param userId 사용자 ID
     * @return 사용자 정보
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Integer userId) {
        UserResponse response = userService.getUser(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 학번으로 사용자 조회 API
     *
     * @param studentNum 학번
     * @return 사용자 정보
     */
    @GetMapping("/student-num/{studentNum}")
    public ResponseEntity<UserResponse> getUserByStudentNum(@PathVariable Integer studentNum) {
        UserResponse response = userService.getUserByStudentNum(studentNum);
        return ResponseEntity.ok(response);
    }

    /**
     * 모든 사용자 조회 API
     *
     * @return 사용자 목록
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * 역할별 사용자 조회 API
     *
     * @param role 사용자 역할 (STUDENT, COUNSELOR, ADMIN)
     * @return 사용자 목록
     */
    @GetMapping("/role/{role}")
    public ResponseEntity<List<UserResponse>> getUsersByRole(@PathVariable UserRole role) {
        List<UserResponse> users = userService.getUsersByRole(role);
        return ResponseEntity.ok(users);
    }

    /**
     * 사용자 삭제 API (Soft Delete)
     *
     * @param userId 사용자 ID
     * @return 성공 메시지
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Integer userId) {
        log.info("사용자 삭제 요청: userId {}", userId);

        userService.deleteUser(userId);

        return ResponseEntity.ok("사용자가 삭제되었습니다");
    }

    /**
     * 계정 잠금 해제 API (관리자용)
     *
     * @param userId 사용자 ID
     * @return 성공 메시지
     */
    @PostMapping("/{userId}/unlock")
    public ResponseEntity<String> unlockAccount(@PathVariable Integer userId) {
        log.info("계정 잠금 해제 요청: userId {}", userId);

        userService.unlockAccount(userId);

        return ResponseEntity.ok("계정 잠금이 해제되었습니다");
    }
}
