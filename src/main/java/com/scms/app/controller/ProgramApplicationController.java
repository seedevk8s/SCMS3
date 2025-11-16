package com.scms.app.controller;

import com.scms.app.dto.ProgramApplicationResponse;
import com.scms.app.model.Program;
import com.scms.app.model.ProgramApplication;
import com.scms.app.service.ExcelService;
import com.scms.app.service.ProgramApplicationService;
import com.scms.app.service.ProgramService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 프로그램 신청 API Controller
 */
@RestController
@RequestMapping("/api/programs")
@RequiredArgsConstructor
@Slf4j
public class ProgramApplicationController {

    private final ProgramApplicationService applicationService;
    private final ExcelService excelService;
    private final ProgramService programService;

    /**
     * 프로그램 신청
     */
    @PostMapping("/{programId}/apply")
    public ResponseEntity<?> applyProgram(
            @PathVariable Integer programId,
            HttpSession session) {

        // 로그인 확인
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인이 필요합니다."));
        }

        try {
            ProgramApplication application = applicationService.applyProgram(userId, programId);
            ProgramApplicationResponse response = ProgramApplicationResponse.from(application);

            log.info("프로그램 신청 성공: 사용자 ID {}, 프로그램 ID {}", userId, programId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "프로그램 신청이 완료되었습니다.",
                    "application", response
            ));

        } catch (IllegalArgumentException e) {
            log.error("프로그램 신청 실패 (잘못된 요청): {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));

        } catch (IllegalStateException e) {
            log.error("프로그램 신청 실패 (상태 오류): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            log.error("프로그램 신청 실패 (서버 오류): ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "서버 오류가 발생했습니다."));
        }
    }

    /**
     * 프로그램 신청 취소
     */
    @DeleteMapping("/applications/{applicationId}")
    public ResponseEntity<?> cancelApplication(
            @PathVariable Integer applicationId,
            HttpSession session) {

        // 로그인 확인
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인이 필요합니다."));
        }

        try {
            applicationService.cancelApplication(userId, applicationId);

            log.info("프로그램 신청 취소 성공: 사용자 ID {}, 신청 ID {}", userId, applicationId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "프로그램 신청이 취소되었습니다."
            ));

        } catch (IllegalArgumentException e) {
            log.error("프로그램 신청 취소 실패 (잘못된 요청): {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));

        } catch (IllegalStateException e) {
            log.error("프로그램 신청 취소 실패 (상태 오류): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            log.error("프로그램 신청 취소 실패 (서버 오류): ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "서버 오류가 발생했습니다."));
        }
    }

    /**
     * 나의 신청 내역 조회
     */
    @GetMapping("/applications/my")
    public ResponseEntity<?> getMyApplications(HttpSession session) {

        // 로그인 확인
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인이 필요합니다."));
        }

        try {
            List<ProgramApplication> applications = applicationService.getUserApplications(userId);
            List<ProgramApplicationResponse> responses = applications.stream()
                    .map(ProgramApplicationResponse::from)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);

        } catch (Exception e) {
            log.error("신청 내역 조회 실패: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "서버 오류가 발생했습니다."));
        }
    }

    /**
     * 특정 프로그램에 대한 나의 신청 상태 조회
     */
    @GetMapping("/{programId}/my-application")
    public ResponseEntity<?> getMyApplicationForProgram(
            @PathVariable Integer programId,
            HttpSession session) {

        // 로그인 확인
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.ok(Map.of("applied", false));
        }

        try {
            ProgramApplication application = applicationService.getUserApplicationForProgram(userId, programId);

            if (application == null) {
                return ResponseEntity.ok(Map.of("applied", false));
            }

            ProgramApplicationResponse response = ProgramApplicationResponse.from(application);
            return ResponseEntity.ok(Map.of(
                    "applied", true,
                    "application", response
            ));

        } catch (Exception e) {
            log.error("신청 상태 조회 실패: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "서버 오류가 발생했습니다."));
        }
    }

    /**
     * 프로그램별 신청 내역 조회 (관리자용)
     */
    @GetMapping("/{programId}/applications")
    public ResponseEntity<?> getProgramApplications(
            @PathVariable Integer programId,
            HttpSession session) {

        // 관리자 확인
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        if (isAdmin == null || !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "관리자 권한이 필요합니다."));
        }

        try {
            List<ProgramApplication> applications = applicationService.getProgramApplications(programId);
            List<ProgramApplicationResponse> responses = applications.stream()
                    .map(ProgramApplicationResponse::from)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);

        } catch (Exception e) {
            log.error("프로그램 신청 내역 조회 실패: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "서버 오류가 발생했습니다."));
        }
    }

    /**
     * 프로그램별 신청 내역 Excel 다운로드 (관리자용)
     */
    @GetMapping("/{programId}/applications/excel")
    public ResponseEntity<?> downloadApplicationsExcel(
            @PathVariable Integer programId,
            HttpSession session) {

        // 관리자 확인
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        if (isAdmin == null || !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "관리자 권한이 필요합니다."));
        }

        try {
            // 프로그램 정보 조회
            Program program = programService.getProgram(programId);

            // 신청 내역 조회
            List<ProgramApplication> applications = applicationService.getProgramApplications(programId);

            // Excel 파일 생성
            ByteArrayInputStream excelFile = excelService.generateApplicationsExcel(applications, program.getTitle());

            // 파일명 생성 (한글 인코딩 처리)
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = String.format("%s_신청자목록_%s.xlsx", program.getTitle(), timestamp);
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");

            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName);
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

            log.info("Excel 다운로드 성공: 프로그램 ID {}, 신청 수 {}", programId, applications.size());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new InputStreamResource(excelFile));

        } catch (Exception e) {
            log.error("Excel 다운로드 실패: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Excel 파일 생성에 실패했습니다."));
        }
    }

    /**
     * 신청 승인 (관리자용)
     */
    @PostMapping("/applications/{applicationId}/approve")
    public ResponseEntity<?> approveApplication(
            @PathVariable Integer applicationId,
            HttpSession session) {

        // 관리자 확인
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        if (isAdmin == null || !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "관리자 권한이 필요합니다."));
        }

        try {
            applicationService.approveApplication(applicationId);

            log.info("프로그램 신청 승인 성공: 신청 ID {}", applicationId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "신청이 승인되었습니다."
            ));

        } catch (IllegalArgumentException e) {
            log.error("신청 승인 실패 (잘못된 요청): {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));

        } catch (IllegalStateException e) {
            log.error("신청 승인 실패 (상태 오류): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            log.error("신청 승인 실패 (서버 오류): ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "서버 오류가 발생했습니다."));
        }
    }

    /**
     * 신청 거부 (관리자용)
     */
    @PostMapping("/applications/{applicationId}/reject")
    public ResponseEntity<?> rejectApplication(
            @PathVariable Integer applicationId,
            @RequestBody(required = false) Map<String, String> requestBody,
            HttpSession session) {

        // 관리자 확인
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        if (isAdmin == null || !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "관리자 권한이 필요합니다."));
        }

        try {
            String reason = (requestBody != null) ? requestBody.get("reason") : null;
            if (reason == null || reason.trim().isEmpty()) {
                reason = "관리자에 의해 거부됨";
            }

            applicationService.rejectApplication(applicationId, reason);

            log.info("프로그램 신청 거부 성공: 신청 ID {}, 사유: {}", applicationId, reason);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "신청이 거부되었습니다."
            ));

        } catch (IllegalArgumentException e) {
            log.error("신청 거부 실패 (잘못된 요청): {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));

        } catch (IllegalStateException e) {
            log.error("신청 거부 실패 (상태 오류): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            log.error("신청 거부 실패 (서버 오류): ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "서버 오류가 발생했습니다."));
        }
    }

    /**
     * 참여 완료 처리 (관리자용)
     */
    @PostMapping("/applications/{applicationId}/complete")
    public ResponseEntity<?> completeApplication(
            @PathVariable Integer applicationId,
            HttpSession session) {

        // 관리자 확인
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        if (isAdmin == null || !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "관리자 권한이 필요합니다."));
        }

        try {
            applicationService.completeApplication(applicationId);

            log.info("프로그램 참여 완료 처리 성공: 신청 ID {}", applicationId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "참여 완료 처리되었습니다."
            ));

        } catch (IllegalArgumentException e) {
            log.error("참여 완료 처리 실패 (잘못된 요청): {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));

        } catch (IllegalStateException e) {
            log.error("참여 완료 처리 실패 (상태 오류): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            log.error("참여 완료 처리 실패 (서버 오류): ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "서버 오류가 발생했습니다."));
        }
    }
}
