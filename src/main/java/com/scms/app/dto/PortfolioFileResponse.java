package com.scms.app.dto;

import com.scms.app.model.PortfolioFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 포트폴리오 파일 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioFileResponse {

    private Long fileId;
    private Long portfolioItemId;
    private String originalFileName;
    private String storedFileName;
    private String filePath;
    private Long fileSize;
    private String fileType;
    private LocalDateTime uploadedAt;

    /**
     * Entity를 DTO로 변환
     */
    public static PortfolioFileResponse from(PortfolioFile file) {
        return PortfolioFileResponse.builder()
                .fileId(file.getFileId())
                .portfolioItemId(file.getPortfolioItemId())
                .originalFileName(file.getOriginalFileName())
                .storedFileName(file.getStoredFileName())
                .filePath(file.getFilePath())
                .fileSize(file.getFileSize())
                .fileType(file.getFileType())
                .uploadedAt(file.getUploadedAt())
                .build();
    }

    /**
     * 파일 크기를 읽기 좋은 형식으로 변환
     */
    public String getFormattedFileSize() {
        if (fileSize == null) {
            return "0 B";
        }

        long size = fileSize;
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }
}
