package com.scms.app.controller;

import com.scms.app.dto.PortfolioFileResponse;
import com.scms.app.model.PortfolioFile;
import com.scms.app.service.PortfolioFileService;
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
import java.util.List;
import java.util.Map;

/**
 * 포트폴리오 파일 REST API Controller
 */
@RestController
@RequestMapping("/api/portfolio-items/{itemId}/files")
@RequiredArgsConstructor
@Slf4j
public class PortfolioFileController {

    private final PortfolioFileService portfolioFileService;

    /**
     * 포트폴리오 항목의 파일 목록 조회
     */
    @GetMapping
    public ResponseEntity<?> getFiles(@PathVariable Long itemId) {
        try {
            List<PortfolioFileResponse> files = portfolioFileService.getFilesByPortfolioItem(itemId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "files", files,
                    "count", files.size()
            ));
        } catch (Exception e) {
            log.error("파일 목록 조회 실패: itemId={}", itemId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "파일 목록 조회에 실패했습니다"));
        }
    }

    /**
     * 파일 업로드
     */
    @PostMapping
    public ResponseEntity<?> uploadFile(
            @PathVariable Long itemId,
            @RequestParam("file") MultipartFile file,
            HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인이 필요합니다"));
        }

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "파일이 비어있습니다"));
        }

        try {
            PortfolioFile portfolioFile = portfolioFileService.uploadFile(itemId, userId, file);
            PortfolioFileResponse response = PortfolioFileResponse.from(portfolioFile);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "success", true,
                            "message", "파일이 업로드되었습니다",
                            "file", response
                    ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            log.error("파일 업로드 실패: itemId={}, fileName={}", itemId, file.getOriginalFilename(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("파일 업로드 실패: itemId={}", itemId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "파일 업로드에 실패했습니다"));
        }
    }

    /**
     * 파일 다운로드
     */
    @GetMapping("/{fileId}/download")
    public ResponseEntity<?> downloadFile(@PathVariable Long itemId, @PathVariable Long fileId) {
        try {
            PortfolioFile file = portfolioFileService.getFile(fileId);
            Resource resource = portfolioFileService.downloadFile(fileId);

            String encodedFileName = URLEncoder.encode(file.getOriginalFileName(), StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(file.getFileType() != null ? file.getFileType() : "application/octet-stream"))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + encodedFileName + "\"")
                    .body(resource);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            log.error("파일 다운로드 실패: fileId={}", fileId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("파일 다운로드 실패: fileId={}", fileId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "파일 다운로드에 실패했습니다"));
        }
    }

    /**
     * 파일 삭제
     */
    @DeleteMapping("/{fileId}")
    public ResponseEntity<?> deleteFile(
            @PathVariable Long itemId,
            @PathVariable Long fileId,
            HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인이 필요합니다"));
        }

        try {
            portfolioFileService.deleteFile(fileId, userId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "파일이 삭제되었습니다"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("파일 삭제 실패: fileId={}", fileId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "파일 삭제에 실패했습니다"));
        }
    }
}
