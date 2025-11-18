package com.scms.app.service;

import com.scms.app.dto.PortfolioFileResponse;
import com.scms.app.model.Portfolio;
import com.scms.app.model.PortfolioFile;
import com.scms.app.model.PortfolioItem;
import com.scms.app.repository.PortfolioFileRepository;
import com.scms.app.repository.PortfolioItemRepository;
import com.scms.app.repository.PortfolioRepository;
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
import java.util.stream.Collectors;

/**
 * 포트폴리오 파일 관리 Service
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PortfolioFileService {

    private final PortfolioFileRepository portfolioFileRepository;
    private final PortfolioItemRepository portfolioItemRepository;
    private final PortfolioRepository portfolioRepository;

    @Value("${file.upload-dir:${user.home}/scms-uploads/portfolio}")
    private String uploadDir;

    @Value("${file.max-size:10485760}") // 10MB
    private Long maxFileSize;

    // 허용된 파일 확장자
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "pdf", "docx", "doc", "pptx", "ppt", "xlsx", "xls", "zip", "hwp", "txt",
            "jpg", "jpeg", "png", "gif", "bmp", "svg"
    );

    /**
     * 포트폴리오 항목별 파일 목록 조회
     */
    public List<PortfolioFileResponse> getFilesByPortfolioItem(Long portfolioItemId) {
        List<PortfolioFile> files = portfolioFileRepository.findByPortfolioItemIdNotDeleted(portfolioItemId);
        return files.stream()
                .map(PortfolioFileResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 파일 업로드
     */
    @Transactional
    public PortfolioFile uploadFile(Long portfolioItemId, Integer userId, MultipartFile file) {
        // 포트폴리오 항목 조회
        PortfolioItem item = portfolioItemRepository.findByIdNotDeleted(portfolioItemId)
                .orElseThrow(() -> new IllegalArgumentException("포트폴리오 항목을 찾을 수 없습니다"));

        // 권한 확인
        Portfolio portfolio = portfolioRepository.findByIdAndUserIdNotDeleted(item.getPortfolioId(), userId)
                .orElseThrow(() -> new IllegalArgumentException("파일을 업로드할 권한이 없습니다"));

        // 파일 유효성 검사
        validateFile(file);

        try {
            // 업로드 디렉토리 생성
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("포트폴리오 파일 업로드 디렉토리 생성: {}", uploadPath);
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
            PortfolioFile portfolioFile = PortfolioFile.builder()
                    .portfolioItemId(portfolioItemId)
                    .originalFileName(originalFileName)
                    .storedFileName(storedFileName)
                    .filePath(filePath.toString())
                    .fileSize(file.getSize())
                    .fileType(file.getContentType())
                    .build();

            PortfolioFile savedFile = portfolioFileRepository.save(portfolioFile);

            log.info("포트폴리오 파일 업로드 완료: itemId={}, fileId={}, fileName={}",
                    portfolioItemId, savedFile.getFileId(), originalFileName);

            return savedFile;

        } catch (IOException e) {
            log.error("포트폴리오 파일 업로드 실패: itemId={}, fileName={}", portfolioItemId, file.getOriginalFilename(), e);
            throw new RuntimeException("파일 업로드에 실패했습니다.", e);
        }
    }

    /**
     * 파일 다운로드 (Resource 반환)
     */
    public Resource downloadFile(Long fileId) {
        PortfolioFile file = portfolioFileRepository.findByIdNotDeleted(fileId)
                .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다"));

        try {
            Path filePath = Paths.get(file.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                log.info("포트폴리오 파일 다운로드: fileId={}, fileName={}", fileId, file.getOriginalFileName());
                return resource;
            } else {
                throw new RuntimeException("파일을 읽을 수 없습니다: " + file.getOriginalFileName());
            }
        } catch (MalformedURLException e) {
            log.error("포트폴리오 파일 다운로드 실패: fileId={}", fileId, e);
            throw new RuntimeException("파일 다운로드에 실패했습니다.", e);
        }
    }

    /**
     * 파일 정보 조회
     */
    public PortfolioFile getFile(Long fileId) {
        return portfolioFileRepository.findByIdNotDeleted(fileId)
                .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다"));
    }

    /**
     * 파일 삭제 (Soft Delete)
     */
    @Transactional
    public void deleteFile(Long fileId, Integer userId) {
        PortfolioFile file = portfolioFileRepository.findByIdNotDeleted(fileId)
                .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다"));

        // 항목 조회
        PortfolioItem item = portfolioItemRepository.findByIdNotDeleted(file.getPortfolioItemId())
                .orElseThrow(() -> new IllegalArgumentException("포트폴리오 항목을 찾을 수 없습니다"));

        // 권한 확인
        Portfolio portfolio = portfolioRepository.findByIdAndUserIdNotDeleted(item.getPortfolioId(), userId)
                .orElseThrow(() -> new IllegalArgumentException("파일을 삭제할 권한이 없습니다"));

        file.delete();
        portfolioFileRepository.save(file);

        log.info("포트폴리오 파일 삭제 완료: fileId={}, fileName={}", fileId, file.getOriginalFileName());
    }

    /**
     * 파일 유효성 검사
     */
    private void validateFile(MultipartFile file) {
        // 파일이 비어있는지 확인
        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다");
        }

        // 파일 크기 확인
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException(
                    String.format("파일 크기는 %dMB를 초과할 수 없습니다", maxFileSize / (1024 * 1024)));
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
     * 포트폴리오 항목의 파일 개수 조회
     */
    public Long getFileCount(Long portfolioItemId) {
        return portfolioFileRepository.countByPortfolioItemIdNotDeleted(portfolioItemId);
    }
}
