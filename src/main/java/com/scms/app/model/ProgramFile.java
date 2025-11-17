package com.scms.app.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 프로그램 첨부파일 Entity
 */
@Entity
@Table(name = "program_files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Integer fileId;

    /**
     * 프로그램 (Many-to-One)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private Program program;

    /**
     * 원본 파일명
     */
    @Column(name = "original_file_name", nullable = false, length = 255)
    private String originalFileName;

    /**
     * 저장된 파일명 (UUID)
     */
    @Column(name = "stored_file_name", nullable = false, length = 255)
    private String storedFileName;

    /**
     * 파일 경로
     */
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    /**
     * 파일 크기 (bytes)
     */
    @Column(name = "file_size")
    private Long fileSize;

    /**
     * 파일 타입 (MIME type)
     */
    @Column(name = "file_type", length = 100)
    private String fileType;

    /**
     * 업로드 일시
     */
    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    /**
     * 삭제 일시 (Soft Delete)
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * 업로드한 사용자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by")
    private User uploadedBy;

    /**
     * Soft Delete
     */
    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * 삭제 여부 확인
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    /**
     * 파일 크기를 KB 단위로 반환
     */
    public String getFileSizeInKB() {
        if (fileSize == null) return "0 KB";
        return String.format("%.1f KB", fileSize / 1024.0);
    }

    /**
     * 파일 크기를 MB 단위로 반환
     */
    public String getFileSizeInMB() {
        if (fileSize == null) return "0 MB";
        return String.format("%.2f MB", fileSize / (1024.0 * 1024.0));
    }

    /**
     * 파일 확장자 반환
     */
    public String getFileExtension() {
        if (originalFileName == null || !originalFileName.contains(".")) {
            return "";
        }
        return originalFileName.substring(originalFileName.lastIndexOf(".") + 1).toLowerCase();
    }

    @PrePersist
    protected void onCreate() {
        if (uploadedAt == null) {
            uploadedAt = LocalDateTime.now();
        }
    }
}
