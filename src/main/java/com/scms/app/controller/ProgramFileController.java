package com.scms.app.controller;

import com.scms.app.model.ProgramFile;
import com.scms.app.service.ProgramFileService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 프로그램 첨부파일 REST API Controller
 */
@RestController
@RequestMapping("/api/programs/{programId}/files")
@RequiredArgsConstructor
@Slf4j
public class ProgramFileController {

    private final ProgramFileService fileService;

    /**
     * 첨부파일 목록 조회
     */
    @GetMapping
    public ResponseEntity<?> getFiles(@PathVariable Integer programId) {
        try {
            List<ProgramFile> files = fileService.getFilesByProgram(programId);

            // DTO로 변환 (필요한 정보만 반환)
            List<Map<String, Object>> fileList = files.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("files", fileList);
            response.put("totalCount", files.size());
            response.put("totalSize", fileService.getTotalFileSize(programId));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("파일 목록 조회 실패: programId={}", programId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "파일 목록 조회에 실패했습니다."));
        }
    }

    /**
     * 파일 업로드
     */
    @PostMapping
    public ResponseEntity<?> uploadFile(
            @PathVariable Integer programId,
            @RequestParam("file") MultipartFile file,
            HttpSession session) {

        try {
            // 로그인 확인
            Integer userId = (Integer) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "로그인이 필요합니다."));
            }

            // 관리자 권한 확인
            Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
            if (isAdmin == null || !isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "관리자 권한이 필요합니다."));
            }

            // 파일 업로드
            ProgramFile uploadedFile = fileService.uploadFile(programId, userId, file);

            log.info("파일 업로드 성공: programId={}, fileId={}, fileName={}",
                    programId, uploadedFile.getFileId(), uploadedFile.getOriginalFileName());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "파일이 업로드되었습니다.",
                    "file", convertToDTO(uploadedFile)
            ));

        } catch (IllegalArgumentException e) {
            log.warn("파일 업로드 실패 (유효성 검증): programId={}, error={}", programId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("파일 업로드 실패: programId={}", programId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "파일 업로드에 실패했습니다."));
        }
    }

    /**
     * 파일 다운로드
     */
    @GetMapping("/{fileId}/download")
    public ResponseEntity<?> downloadFile(
            @PathVariable Integer programId,
            @PathVariable Integer fileId) {

        try {
            ProgramFile file = fileService.getFile(fileId);
            Resource resource = fileService.downloadFile(fileId);

            // 한글 파일명 인코딩
            String encodedFileName = URLEncoder.encode(file.getOriginalFileName(), StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename*=UTF-8''" + encodedFileName)
                    .body(resource);

        } catch (IllegalArgumentException e) {
            log.warn("파일 다운로드 실패: fileId={}, error={}", fileId, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("파일 다운로드 실패: fileId={}", fileId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "파일 다운로드에 실패했습니다."));
        }
    }

    /**
     * 파일 삭제
     */
    @DeleteMapping("/{fileId}")
    public ResponseEntity<?> deleteFile(
            @PathVariable Integer programId,
            @PathVariable Integer fileId,
            HttpSession session) {

        try {
            // 로그인 확인
            Integer userId = (Integer) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "로그인이 필요합니다."));
            }

            // 관리자 권한 확인
            Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
            if (isAdmin == null || !isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "관리자 권한이 필요합니다."));
            }

            fileService.deleteFile(fileId, userId);

            log.info("파일 삭제 성공: programId={}, fileId={}, userId={}",
                    programId, fileId, userId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "파일이 삭제되었습니다."
            ));

        } catch (IllegalStateException e) {
            log.warn("파일 삭제 실패 (권한): fileId={}, error={}", fileId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.warn("파일 삭제 실패: fileId={}, error={}", fileId, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("파일 삭제 실패: fileId={}", fileId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "파일 삭제에 실패했습니다."));
        }
    }

    /**
     * Entity를 DTO로 변환
     */
    private Map<String, Object> convertToDTO(ProgramFile file) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("fileId", file.getFileId());
        dto.put("originalFileName", file.getOriginalFileName());
        dto.put("fileSize", file.getFileSize());
        dto.put("fileSizeInKB", file.getFileSizeInKB());
        dto.put("fileSizeInMB", file.getFileSizeInMB());
        dto.put("fileType", file.getFileType());
        dto.put("fileExtension", file.getFileExtension());
        dto.put("uploadedAt", file.getUploadedAt());
        dto.put("uploadedBy", file.getUploadedBy() != null ? file.getUploadedBy().getName() : null);
        return dto;
    }
}
