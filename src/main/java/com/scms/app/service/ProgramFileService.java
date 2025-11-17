package com.scms.app.service;

import com.scms.app.model.Program;
import com.scms.app.model.ProgramFile;
import com.scms.app.model.User;
import com.scms.app.repository.ProgramFileRepository;
import com.scms.app.repository.ProgramRepository;
import com.scms.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 프로그램 첨부파일 Service
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProgramFileService {

    private final ProgramFileRepository fileRepository;
    private final ProgramRepository programRepository;
    private final UserRepository userRepository;

    @Value("${file.upload-dir:${user.home}/scms-uploads}")
    private String uploadDir;

    @Value("${file.max-size:10485760}") // 10MB
    private Long maxFileSize;

    // 허용된 파일 확장자
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "pdf", "docx", "doc", "pptx", "ppt", "xlsx", "xls", "zip", "hwp", "txt", "jpg", "jpeg", "png"
    );

    /**
     * 프로그램별 첨부파일 목록 조회
     */
    public List<ProgramFile> getFilesByProgram(Integer programId) {
        return fileRepository.findByProgramIdAndDeletedAtIsNull(programId);
    }

    /**
     * 파일 업로드
     */
    @Transactional
    public ProgramFile uploadFile(Integer programId, Integer userId, MultipartFile file) {
        // 프로그램 존재 확인
        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로그램입니다."));

        // 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 파일 유효성 검사
        validateFile(file);

        try {
            // 업로드 디렉토리 생성
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("업로드 디렉토리 생성: {}", uploadPath);
            }

            // 원본 파일명
            String originalFileName = file.getOriginalFilename();

            // 저장할 파일명 생성 (UUID + 확장자)
            String fileExtension = getFileExtension(originalFileName);
            String storedFileName = UUID.randomUUID().toString() + "." + fileExtension;

            // 파일 저장 경로
            Path filePath = uploadPath.resolve(storedFileName);

            // 파일 저장
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // DB에 파일 정보 저장
            ProgramFile programFile = ProgramFile.builder()
                    .program(program)
                    .originalFileName(originalFileName)
                    .storedFileName(storedFileName)
                    .filePath(filePath.toString())
                    .fileSize(file.getSize())
                    .fileType(file.getContentType())
                    .uploadedBy(user)
                    .build();

            ProgramFile savedFile = fileRepository.save(programFile);

            log.info("파일 업로드 완료: programId={}, fileId={}, fileName={}",
                    programId, savedFile.getFileId(), originalFileName);

            return savedFile;

        } catch (IOException e) {
            log.error("파일 업로드 실패: programId={}, fileName={}", programId, file.getOriginalFilename(), e);
            throw new RuntimeException("파일 업로드에 실패했습니다.", e);
        }
    }

    /**
     * 파일 다운로드 (Resource 반환)
     */
    public Resource downloadFile(Integer fileId) {
        ProgramFile file = fileRepository.findByIdAndDeletedAtIsNull(fileId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 파일입니다."));

        try {
            Path filePath = Paths.get(file.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                log.info("파일 다운로드: fileId={}, fileName={}", fileId, file.getOriginalFileName());
                return resource;
            } else {
                throw new RuntimeException("파일을 읽을 수 없습니다: " + file.getOriginalFileName());
            }
        } catch (MalformedURLException e) {
            log.error("파일 다운로드 실패: fileId={}", fileId, e);
            throw new RuntimeException("파일 다운로드에 실패했습니다.", e);
        }
    }

    /**
     * 파일 정보 조회
     */
    public ProgramFile getFile(Integer fileId) {
        return fileRepository.findByIdAndDeletedAtIsNull(fileId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 파일입니다."));
    }

    /**
     * 파일 삭제 (Soft Delete)
     */
    @Transactional
    public void deleteFile(Integer fileId, Integer userId) {
        ProgramFile file = fileRepository.findByIdAndDeletedAtIsNull(fileId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 파일입니다."));

        // 관리자 권한 확인 (실제로는 isAdmin 확인 필요)
        // 간단하게 업로드한 사용자만 삭제 가능하도록 구현
        if (!file.getUploadedBy().getUserId().equals(userId)) {
            throw new IllegalStateException("파일을 삭제할 권한이 없습니다.");
        }

        file.delete();
        fileRepository.save(file);

        log.info("파일 삭제 완료: fileId={}, fileName={}", fileId, file.getOriginalFileName());
    }

    /**
     * 파일 유효성 검사
     */
    private void validateFile(MultipartFile file) {
        // 파일이 비어있는지 확인
        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        // 파일 크기 확인
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException(
                    String.format("파일 크기는 %dMB를 초과할 수 없습니다.", maxFileSize / (1024 * 1024)));
        }

        // 파일 확장자 확인
        String originalFileName = file.getOriginalFilename();
        String extension = getFileExtension(originalFileName);

        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException(
                    "허용되지 않는 파일 형식입니다. 허용 형식: " + String.join(", ", ALLOWED_EXTENSIONS));
        }
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    /**
     * 프로그램의 파일 개수 조회
     */
    public Long getFileCount(Integer programId) {
        return fileRepository.countByProgramId(programId);
    }

    /**
     * 프로그램의 총 파일 크기 조회
     */
    public Long getTotalFileSize(Integer programId) {
        Long totalSize = fileRepository.getTotalFileSizeByProgramId(programId);
        return totalSize != null ? totalSize : 0L;
    }
}
