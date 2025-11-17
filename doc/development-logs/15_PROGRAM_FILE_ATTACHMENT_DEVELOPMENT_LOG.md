# 프로그램 첨부파일 관리 시스템 구현 개발 로그

**작성일**: 2025-11-17
**작성자**: Claude AI
**관련 이슈**: 프로그램 첨부파일 업로드/다운로드 기능 구현

## 📋 목차

1. [개요](#개요)
2. [구현 내용](#구현-내용)
3. [백엔드 구현](#백엔드-구현)
4. [프론트엔드 구현](#프론트엔드-구현)
5. [주요 기능](#주요-기능)
6. [API 명세](#api-명세)
7. [테스트 방법](#테스트-방법)
8. [보안 고려사항](#보안-고려사항)

---

## 개요

### 목적
관리자가 프로그램 상세 페이지에서 안내 자료, 참고 문서 등의 파일을 업로드하고, 모든 사용자가 해당 파일을 다운로드할 수 있는 첨부파일 관리 시스템 구현

### 주요 기능
- ✅ 파일 업로드 (관리자만 가능)
- ✅ 파일 목록 조회 (모든 사용자)
- ✅ 파일 다운로드 (모든 사용자)
- ✅ 파일 삭제 (Soft Delete, 관리자만 가능)
- ✅ 파일 유효성 검사 (확장자, 크기 제한)
- ✅ 파일 통계 표시 (파일 개수, 총 용량)

### 기술 스택
- **Backend**: Spring Boot 3.x, JPA/Hibernate, MultipartFile
- **Frontend**: Vanilla JavaScript, Thymeleaf
- **Database**: MySQL (program_files 테이블, JPA auto-DDL)
- **File Storage**: Local Filesystem (UUID 기반 파일명)
- **패턴**: DTO 패턴, Soft Delete 패턴

---

## 구현 내용

### 구현 일정
1. **백엔드 개발** (Entity, Repository, Service, Controller)
2. **파일 업로드 설정** (application.yml)
3. **프론트엔드 개발** (UI, JavaScript)
4. **문서화 및 커밋**

---

## 백엔드 구현

### 1. ProgramFile Entity

**파일**: `src/main/java/com/scms/app/model/ProgramFile.java`

#### 주요 필드
```java
@Entity
@Table(name = "program_files")
public class ProgramFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Integer fileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private Program program;

    @Column(name = "original_file_name", nullable = false, length = 255)
    private String originalFileName;

    @Column(name = "stored_file_name", nullable = false, length = 255)
    private String storedFileName;  // UUID 기반

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;  // bytes

    @Column(name = "file_type", length = 100)
    private String fileType;  // MIME type

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;  // Soft Delete

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by")
    private User uploadedBy;
}
```

#### 비즈니스 메서드
```java
// Soft Delete
public void delete() {
    this.deletedAt = LocalDateTime.now();
}

// 삭제 여부 확인
public boolean isDeleted() {
    return this.deletedAt != null;
}

// 파일 크기를 KB 단위로 반환
public String getFileSizeInKB() {
    if (fileSize == null) return "0 KB";
    return String.format("%.1f KB", fileSize / 1024.0);
}

// 파일 크기를 MB 단위로 반환
public String getFileSizeInMB() {
    if (fileSize == null) return "0 MB";
    return String.format("%.2f MB", fileSize / (1024.0 * 1024.0));
}

// 파일 확장자 반환
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
```

**설계 포인트**:
- **UUID 파일명**: 파일명 충돌 방지 및 보안
- **원본 파일명 보존**: 다운로드 시 사용자에게 원본 파일명 제공
- **Soft Delete**: 실수로 삭제한 파일 복구 가능
- **파일 크기 헬퍼 메서드**: UI에서 사용자 친화적인 형식으로 표시

### 2. ProgramFileRepository

**파일**: `src/main/java/com/scms/app/repository/ProgramFileRepository.java`

#### 주요 쿼리 메서드
```java
@Repository
public interface ProgramFileRepository extends JpaRepository<ProgramFile, Integer> {

    /**
     * 프로그램별 첨부파일 조회 (삭제되지 않은 것만, 업로드 날짜 순)
     */
    @Query("SELECT pf FROM ProgramFile pf " +
           "WHERE pf.program.programId = :programId " +
           "AND pf.deletedAt IS NULL " +
           "ORDER BY pf.uploadedAt DESC")
    List<ProgramFile> findByProgramIdAndDeletedAtIsNull(@Param("programId") Integer programId);

    /**
     * 파일 ID로 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT pf FROM ProgramFile pf " +
           "WHERE pf.fileId = :fileId " +
           "AND pf.deletedAt IS NULL")
    Optional<ProgramFile> findByIdAndDeletedAtIsNull(@Param("fileId") Integer fileId);

    /**
     * 프로그램의 파일 개수 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT COUNT(pf) FROM ProgramFile pf " +
           "WHERE pf.program.programId = :programId " +
           "AND pf.deletedAt IS NULL")
    Long countByProgramId(@Param("programId") Integer programId);

    /**
     * 프로그램의 총 파일 크기 조회 (삭제되지 않은 것만)
     */
    @Query("SELECT SUM(pf.fileSize) FROM ProgramFile pf " +
           "WHERE pf.program.programId = :programId " +
           "AND pf.deletedAt IS NULL")
    Long getTotalFileSizeByProgramId(@Param("programId") Integer programId);
}
```

**설계 포인트**:
- `deletedAt IS NULL`: Soft Delete 처리된 파일 제외
- `ORDER BY uploadedAt DESC`: 최신 파일 우선 표시
- 파일 통계 쿼리: 파일 개수 및 총 용량 계산

### 3. ProgramFileService

**파일**: `src/main/java/com/scms/app/service/ProgramFileService.java`

#### 설정 값
```java
@Value("${file.upload-dir:${user.home}/scms-uploads}")
private String uploadDir;

@Value("${file.max-size:10485760}") // 10MB
private Long maxFileSize;

// 허용된 파일 확장자
private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
    "pdf", "docx", "doc", "pptx", "ppt", "xlsx", "xls",
    "zip", "hwp", "txt", "jpg", "jpeg", "png"
);
```

#### 파일 업로드
```java
@Transactional
public ProgramFile uploadFile(Integer programId, Integer userId, MultipartFile file) {
    // 1. 프로그램 존재 확인
    Program program = programRepository.findById(programId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로그램입니다."));

    // 2. 사용자 존재 확인
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

    // 3. 파일 유효성 검사
    validateFile(file);

    // 4. 업로드 디렉토리 생성
    Path uploadPath = Paths.get(uploadDir);
    if (!Files.exists(uploadPath)) {
        Files.createDirectories(uploadPath);
    }

    // 5. UUID 기반 파일명 생성
    String originalFileName = file.getOriginalFilename();
    String fileExtension = getFileExtension(originalFileName);
    String storedFileName = UUID.randomUUID().toString() + "." + fileExtension;

    // 6. 파일 저장
    Path filePath = uploadPath.resolve(storedFileName);
    Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

    // 7. DB에 파일 정보 저장
    ProgramFile programFile = ProgramFile.builder()
        .program(program)
        .originalFileName(originalFileName)
        .storedFileName(storedFileName)
        .filePath(filePath.toString())
        .fileSize(file.getSize())
        .fileType(file.getContentType())
        .uploadedBy(user)
        .build();

    return fileRepository.save(programFile);
}
```

#### 파일 유효성 검사
```java
private void validateFile(MultipartFile file) {
    // 파일이 비어있는지 확인
    if (file.isEmpty()) {
        throw new IllegalArgumentException("파일이 비어있습니다.");
    }

    // 파일 크기 확인
    if (file.getSize() > maxFileSize) {
        throw new IllegalArgumentException(
            String.format("파일 크기는 %dMB를 초과할 수 없습니다.",
                         maxFileSize / (1024 * 1024)));
    }

    // 파일 확장자 확인
    String originalFileName = file.getOriginalFilename();
    String extension = getFileExtension(originalFileName);

    if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
        throw new IllegalArgumentException(
            "허용되지 않는 파일 형식입니다. 허용 형식: " +
            String.join(", ", ALLOWED_EXTENSIONS));
    }
}
```

#### 파일 다운로드
```java
public Resource downloadFile(Integer fileId) {
    ProgramFile file = fileRepository.findByIdAndDeletedAtIsNull(fileId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 파일입니다."));

    try {
        Path filePath = Paths.get(file.getFilePath());
        Resource resource = new UrlResource(filePath.toUri());

        if (resource.exists() && resource.isReadable()) {
            log.info("파일 다운로드: fileId={}, fileName={}",
                    fileId, file.getOriginalFileName());
            return resource;
        } else {
            throw new RuntimeException("파일을 읽을 수 없습니다: " +
                                     file.getOriginalFileName());
        }
    } catch (MalformedURLException e) {
        throw new RuntimeException("파일 다운로드에 실패했습니다.", e);
    }
}
```

#### 파일 삭제 (Soft Delete)
```java
@Transactional
public void deleteFile(Integer fileId, Integer userId) {
    ProgramFile file = fileRepository.findByIdAndDeletedAtIsNull(fileId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 파일입니다."));

    // 관리자 권한 확인 (간단하게 업로드한 사용자만 삭제 가능)
    if (!file.getUploadedBy().getUserId().equals(userId)) {
        throw new IllegalStateException("파일을 삭제할 권한이 없습니다.");
    }

    file.delete();
    fileRepository.save(file);

    log.info("파일 삭제 완료: fileId={}, fileName={}",
            fileId, file.getOriginalFileName());
}
```

### 4. ProgramFileController

**파일**: `src/main/java/com/scms/app/controller/ProgramFileController.java`

#### REST API 엔드포인트

| 메서드 | 엔드포인트 | 설명 | 권한 |
|--------|-----------|------|------|
| GET | `/api/programs/{programId}/files` | 파일 목록 조회 | 전체 |
| POST | `/api/programs/{programId}/files` | 파일 업로드 | 관리자 |
| GET | `/api/programs/{programId}/files/{fileId}/download` | 파일 다운로드 | 전체 |
| DELETE | `/api/programs/{programId}/files/{fileId}` | 파일 삭제 | 관리자 |

#### 파일 목록 조회
```java
@GetMapping
public ResponseEntity<?> getFiles(@PathVariable Integer programId) {
    try {
        List<ProgramFile> files = fileService.getFilesByProgram(programId);

        // DTO로 변환
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
```

#### 파일 업로드
```java
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

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "파일이 업로드되었습니다.",
            "file", convertToDTO(uploadedFile)
        ));

    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest()
            .body(Map.of("error", e.getMessage()));
    } catch (Exception e) {
        log.error("파일 업로드 실패: programId={}", programId, e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("error", "파일 업로드에 실패했습니다."));
    }
}
```

#### 파일 다운로드
```java
@GetMapping("/{fileId}/download")
public ResponseEntity<?> downloadFile(
        @PathVariable Integer programId,
        @PathVariable Integer fileId) {

    try {
        ProgramFile file = fileService.getFile(fileId);
        Resource resource = fileService.downloadFile(fileId);

        // 한글 파일명 인코딩
        String encodedFileName = URLEncoder.encode(file.getOriginalFileName(),
                                                  StandardCharsets.UTF_8)
            .replaceAll("\\+", "%20");

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION,
                   "attachment; filename*=UTF-8''" + encodedFileName)
            .body(resource);

    } catch (IllegalArgumentException e) {
        return ResponseEntity.notFound().build();
    } catch (Exception e) {
        log.error("파일 다운로드 실패: fileId={}", fileId, e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("error", "파일 다운로드에 실패했습니다."));
    }
}
```

### 5. application.yml 설정

**파일**: `src/main/resources/application.yml`

```yaml
spring:
  # File Upload Configuration
  servlet:
    multipart:
      enabled: true
      max-file-size: 10MB
      max-request-size: 50MB
      file-size-threshold: 2KB

# File Storage Configuration
file:
  upload-dir: ${user.home}/scms-uploads
  max-size: 10485760  # 10MB in bytes
```

**설정 설명**:
- `max-file-size`: 단일 파일 최대 크기 (10MB)
- `max-request-size`: 전체 요청 최대 크기 (50MB, 다중 파일 업로드 시)
- `file-size-threshold`: 메모리에 저장할 임계값 (2KB 이하는 메모리에 저장)
- `upload-dir`: 파일 저장 디렉토리 (사용자 홈 디렉토리/scms-uploads)

---

## 프론트엔드 구현

### 1. UI 구성

**파일**: `src/main/resources/templates/program-detail.html`

#### 첨부파일 탭 HTML
```html
<!-- 첨부파일 탭 -->
<div id="attachments" class="detail-section tab-pane">
    <div class="attachments-section">
        <!-- 파일 업로드 폼 (관리자만 표시) -->
        <div class="file-upload-container" th:if="${session.isAdmin}">
            <h3>파일 업로드</h3>
            <form id="fileUploadForm" enctype="multipart/form-data">
                <input type="file" id="fileInput" name="file"
                       accept=".pdf,.docx,.doc,.pptx,.ppt,.xlsx,.xls,.zip,.hwp,.txt,.jpg,.jpeg,.png">
                <button type="submit" class="btn-primary">
                    <i class="fas fa-upload"></i> 업로드
                </button>
            </form>
            <div class="file-type-notice">
                허용 형식: PDF, DOC/DOCX, PPT/PPTX, XLS/XLSX, ZIP, HWP, TXT, JPG, PNG (최대 10MB)
            </div>
        </div>

        <!-- 파일 목록 -->
        <div class="file-list-container">
            <div class="file-header">
                <h3>첨부파일</h3>
                <div id="fileStats">
                    총 <span id="fileCount">0</span>개 파일 (<span id="totalSize">0 MB</span>)
                </div>
            </div>

            <!-- 파일 목록 테이블 -->
            <div id="fileListContent">
                <div class="file-loading">
                    <i class="fas fa-spinner fa-spin"></i> 파일 목록을 불러오는 중...
                </div>
            </div>
        </div>
    </div>
</div>
```

### 2. JavaScript 구현

#### 첨부파일 목록 로드
```javascript
async function loadAttachments() {
    const programId = /*[[${program.programId}]]*/ 0;
    const fileListContent = document.getElementById('fileListContent');

    try {
        const response = await fetch(`/api/programs/${programId}/files`);
        const data = await response.json();

        if (data.files && data.files.length > 0) {
            // 파일 통계 업데이트
            document.getElementById('fileCount').textContent = data.totalCount || 0;
            document.getElementById('totalSize').textContent =
                (data.totalSize / (1024 * 1024)).toFixed(2) + ' MB';

            // 파일 목록 테이블 생성
            let html = `
                <table class="file-table">
                    <thead>
                        <tr>
                            <th>파일명</th>
                            <th>크기</th>
                            <th>업로드일</th>
                            <th>업로드자</th>
                            <th>다운로드</th>
            `;

            // 관리자인 경우 삭제 컬럼 추가
            const isAdmin = /*[[${session.isAdmin}]]*/ false;
            if (isAdmin) {
                html += `<th>삭제</th>`;
            }

            html += `
                        </tr>
                    </thead>
                    <tbody>
            `;

            data.files.forEach(file => {
                const uploadDate = new Date(file.uploadedAt)
                    .toLocaleDateString('ko-KR');

                html += `
                    <tr>
                        <td>
                            <i class="fas fa-file-${getFileIcon(file.fileExtension)}"></i>
                            ${file.originalFileName}
                        </td>
                        <td>${file.fileSizeInKB}</td>
                        <td>${uploadDate}</td>
                        <td>${file.uploadedBy || '-'}</td>
                        <td>
                            <button onclick="downloadFile(${file.fileId})"
                                    class="btn-icon" title="다운로드">
                                <i class="fas fa-download"></i>
                            </button>
                        </td>
                `;

                if (isAdmin) {
                    html += `
                        <td>
                            <button onclick="deleteFile(${file.fileId}, '${file.originalFileName}')"
                                    class="btn-icon btn-delete" title="삭제">
                                <i class="fas fa-trash"></i>
                            </button>
                        </td>
                    `;
                }

                html += `</tr>`;
            });

            html += `
                    </tbody>
                </table>
            `;

            fileListContent.innerHTML = html;
        } else {
            // 파일이 없을 때
            document.getElementById('fileCount').textContent = '0';
            document.getElementById('totalSize').textContent = '0 MB';
            fileListContent.innerHTML = `
                <div class="no-files">
                    <i class="fas fa-folder-open"></i>
                    <p>등록된 파일이 없습니다.</p>
                </div>
            `;
        }
    } catch (error) {
        console.error('파일 목록 로드 실패:', error);
        fileListContent.innerHTML = `
            <div class="error-message">
                <i class="fas fa-exclamation-circle"></i>
                파일 목록을 불러오는데 실패했습니다.
            </div>
        `;
    }
}
```

#### 파일 확장자 아이콘 매핑
```javascript
function getFileIcon(extension) {
    const iconMap = {
        'pdf': 'pdf',
        'doc': 'word', 'docx': 'word',
        'xls': 'excel', 'xlsx': 'excel',
        'ppt': 'powerpoint', 'pptx': 'powerpoint',
        'zip': 'archive',
        'jpg': 'image', 'jpeg': 'image', 'png': 'image',
        'txt': 'alt',
        'hwp': 'alt'
    };
    return iconMap[extension.toLowerCase()] || 'file';
}
```

#### 파일 업로드
```javascript
document.getElementById('fileUploadForm')?.addEventListener('submit', async function(e) {
    e.preventDefault();

    const programId = /*[[${program.programId}]]*/ 0;
    const fileInput = document.getElementById('fileInput');
    const file = fileInput.files[0];

    if (!file) {
        alert('파일을 선택해주세요.');
        return;
    }

    // 파일 크기 확인 (10MB)
    if (file.size > 10 * 1024 * 1024) {
        alert('파일 크기는 10MB를 초과할 수 없습니다.');
        return;
    }

    const formData = new FormData();
    formData.append('file', file);

    try {
        const response = await fetch(`/api/programs/${programId}/files`, {
            method: 'POST',
            body: formData
        });

        const data = await response.json();

        if (response.ok && data.success) {
            alert(data.message || '파일이 업로드되었습니다.');
            fileInput.value = ''; // 파일 입력 초기화
            loadAttachments(); // 목록 새로고침
        } else {
            alert(data.error || '파일 업로드에 실패했습니다.');
        }
    } catch (error) {
        console.error('파일 업로드 실패:', error);
        alert('파일 업로드 중 오류가 발생했습니다.');
    }
});
```

#### 파일 다운로드
```javascript
function downloadFile(fileId) {
    const programId = /*[[${program.programId}]]*/ 0;
    const downloadUrl = `/api/programs/${programId}/files/${fileId}/download`;

    const link = document.createElement('a');
    link.href = downloadUrl;
    link.download = '';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

    console.log('파일 다운로드 시작:', fileId);
}
```

#### 파일 삭제
```javascript
async function deleteFile(fileId, fileName) {
    if (!confirm(`'${fileName}' 파일을 삭제하시겠습니까?`)) {
        return;
    }

    const programId = /*[[${program.programId}]]*/ 0;

    try {
        const response = await fetch(`/api/programs/${programId}/files/${fileId}`, {
            method: 'DELETE'
        });

        const data = await response.json();

        if (response.ok && data.success) {
            alert(data.message || '파일이 삭제되었습니다.');
            loadAttachments(); // 목록 새로고침
        } else {
            alert(data.error || '파일 삭제에 실패했습니다.');
        }
    } catch (error) {
        console.error('파일 삭제 실패:', error);
        alert('파일 삭제 중 오류가 발생했습니다.');
    }
}
```

---

## 주요 기능

### 1. UUID 기반 파일명
```java
String storedFileName = UUID.randomUUID().toString() + "." + fileExtension;
```

**장점**:
- 파일명 충돌 방지
- 보안 향상 (원본 파일명 노출 방지)
- 추측 불가능한 파일명

### 2. 파일 유효성 검사
```java
// 1. 빈 파일 확인
if (file.isEmpty()) {
    throw new IllegalArgumentException("파일이 비어있습니다.");
}

// 2. 파일 크기 제한 (10MB)
if (file.getSize() > maxFileSize) {
    throw new IllegalArgumentException("파일 크기는 10MB를 초과할 수 없습니다.");
}

// 3. 확장자 화이트리스트
if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
    throw new IllegalArgumentException("허용되지 않는 파일 형식입니다.");
}
```

### 3. 한글 파일명 처리
```java
String encodedFileName = URLEncoder.encode(file.getOriginalFileName(),
                                          StandardCharsets.UTF_8)
    .replaceAll("\\+", "%20");

return ResponseEntity.ok()
    .header(HttpHeaders.CONTENT_DISPOSITION,
           "attachment; filename*=UTF-8''" + encodedFileName)
    .body(resource);
```

**처리 방법**:
- UTF-8 인코딩
- `+`를 `%20`으로 변환 (공백 처리)
- `filename*=UTF-8''` 헤더 사용 (RFC 2231)

### 4. 관리자 권한 확인
```java
Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
if (isAdmin == null || !isAdmin) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(Map.of("error", "관리자 권한이 필요합니다."));
}
```

---

## API 명세

### 1. 파일 목록 조회
```
GET /api/programs/{programId}/files
```

**Response**:
```json
{
  "files": [
    {
      "fileId": 1,
      "originalFileName": "프로그램_안내서.pdf",
      "fileSize": 2048576,
      "fileSizeInKB": "2000.0 KB",
      "fileSizeInMB": "1.95 MB",
      "fileType": "application/pdf",
      "fileExtension": "pdf",
      "uploadedAt": "2025-11-17T14:30:00",
      "uploadedBy": "시스템관리자"
    }
  ],
  "totalCount": 1,
  "totalSize": 2048576
}
```

### 2. 파일 업로드
```
POST /api/programs/{programId}/files
Content-Type: multipart/form-data

file: (binary)
```

**Response**:
```json
{
  "success": true,
  "message": "파일이 업로드되었습니다.",
  "file": {
    "fileId": 2,
    "originalFileName": "참고자료.docx",
    "fileSizeInKB": "500.0 KB",
    "uploadedAt": "2025-11-17T15:00:00"
  }
}
```

### 3. 파일 다운로드
```
GET /api/programs/{programId}/files/{fileId}/download
```

**Response**:
- Content-Type: `application/octet-stream`
- Content-Disposition: `attachment; filename*=UTF-8''프로그램_안내서.pdf`
- Body: (파일 바이너리)

### 4. 파일 삭제
```
DELETE /api/programs/{programId}/files/{fileId}
```

**Response**:
```json
{
  "success": true,
  "message": "파일이 삭제되었습니다."
}
```

---

## 테스트 방법

### 1. 파일 업로드 테스트 (관리자)
1. 관리자 계정으로 로그인
   - 학번: 9999999, 비밀번호: admin123
2. 프로그램 상세 페이지 접속
3. "첨부파일" 탭 클릭
4. 파일 선택 후 "업로드" 버튼 클릭
5. 확인 사항:
   - ✅ 업로드 성공 메시지
   - ✅ 파일 목록에 새 파일 표시
   - ✅ 파일 개수 및 총 용량 업데이트

### 2. 파일 다운로드 테스트 (모든 사용자)
1. 프로그램 상세 페이지 → 첨부파일 탭
2. 파일 목록에서 "다운로드" 버튼 클릭
3. 확인 사항:
   - ✅ 원본 파일명으로 다운로드
   - ✅ 한글 파일명 정상 처리
   - ✅ 파일 내용 정상

### 3. 파일 삭제 테스트 (관리자)
1. 관리자로 로그인
2. 첨부파일 탭에서 "삭제" 버튼 클릭
3. 확인 대화상자에서 "확인"
4. 확인 사항:
   - ✅ 삭제 성공 메시지
   - ✅ 파일 목록에서 제거
   - ✅ 통계 업데이트

### 4. 권한 검증 테스트

#### 일반 사용자
1. 일반 학생 계정으로 로그인
2. 첨부파일 탭 접속
3. 확인 사항:
   - ✅ 파일 업로드 폼 숨김
   - ✅ 삭제 버튼 숨김
   - ✅ 다운로드만 가능

### 5. 유효성 검사 테스트

#### 파일 크기 초과
1. 10MB 이상 파일 업로드 시도
2. 확인 사항:
   - ✅ "파일 크기는 10MB를 초과할 수 없습니다." 오류

#### 허용되지 않은 확장자
1. .exe, .sh 등 금지된 확장자 업로드 시도
2. 확인 사항:
   - ✅ "허용되지 않는 파일 형식입니다." 오류

---

## 보안 고려사항

### 1. 확장자 화이트리스트
```java
private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
    "pdf", "docx", "doc", "pptx", "ppt", "xlsx", "xls",
    "zip", "hwp", "txt", "jpg", "jpeg", "png"
);
```

**방지 대상**:
- 실행 파일 (.exe, .sh, .bat)
- 스크립트 파일 (.js, .php, .py)
- 위험한 압축 파일 (.rar with exploits)

### 2. UUID 기반 파일명
```java
String storedFileName = UUID.randomUUID().toString() + "." + fileExtension;
// 예: "a1b2c3d4-e5f6-7890-abcd-ef1234567890.pdf"
```

**보안 효과**:
- 파일 경로 추측 불가
- Directory Traversal 공격 방지
- 원본 파일명 노출 방지

### 3. 관리자 권한 확인
```java
Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
if (isAdmin == null || !isAdmin) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(Map.of("error", "관리자 권한이 필요합니다."));
}
```

### 4. 파일 크기 제한
```yaml
spring:
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 50MB
```

**방지 대상**:
- DoS 공격 (대용량 파일 업로드)
- 디스크 공간 고갈

---

## 다음 단계

### 개선 사항
1. ⏳ 파일 미리보기 기능 (PDF, 이미지)
2. ⏳ 파일 압축/최적화
3. ⏳ 클라우드 스토리지 연동 (AWS S3, Azure Blob)
4. ⏳ 파일 다운로드 통계 (조회수)
5. ⏳ 바이러스 스캔 연동

### 마무리
- ✅ 백엔드 구현 완료
- ✅ 프론트엔드 구현 완료
- ✅ 개발 로그 작성 완료
- ⏳ Git 커밋 및 푸시
- ⏳ PR 생성

---

## 정리

### 구현 완료 항목
- ✅ ProgramFile Entity
- ✅ ProgramFileRepository
- ✅ ProgramFileService (upload, download, delete)
- ✅ ProgramFileController (REST API)
- ✅ application.yml 파일 업로드 설정
- ✅ Frontend UI (업로드 폼, 파일 목록)
- ✅ Frontend JavaScript (CRUD)
- ✅ 파일 유효성 검사
- ✅ 관리자 권한 확인
- ✅ UUID 기반 파일명
- ✅ Soft Delete 패턴
- ✅ 한글 파일명 처리

### 핵심 성과
1. **완전한 파일 관리**: 업로드, 다운로드, 삭제
2. **보안 강화**: 확장자 검증, UUID 파일명, 권한 확인
3. **사용자 경험**: 직관적인 UI, 파일 아이콘, 통계 표시
4. **안정성**: 파일 크기 제한, 에러 처리

---

## 실제 구현 중 발생한 문제 및 해결

### 문제 1: LazyInitializationException 발생

**증상**:
```
org.hibernate.LazyInitializationException: could not initialize proxy [User#X] - no Session
```
파일 목록 조회 시 `uploadedBy` 정보에 접근할 때 LazyInitializationException 발생

**원인**:
ProgramFileRepository에서 ProgramFile 조회 시 연관된 User 엔티티를 LAZY 로딩으로 설정했으나, 세션이 종료된 후 접근 시도

**해결**:
```java
// ProgramFileRepository.java
@Query("SELECT pf FROM ProgramFile pf " +
       "LEFT JOIN FETCH pf.uploadedBy " +  // 추가
       "WHERE pf.program.programId = :programId " +
       "AND pf.deletedAt IS NULL " +
       "ORDER BY pf.uploadedAt DESC")
List<ProgramFile> findByProgramIdAndDeletedAtIsNull(@Param("programId") Integer programId);
```

`LEFT JOIN FETCH`를 추가하여 User 정보를 즉시 로딩하도록 수정

**커밋**: 326d459 - Fix LazyInitializationException in file upload by adding JOIN FETCH for uploadedBy

---

### 문제 2: 파일 업로드 후 목록에 표시되지 않음

**증상**:
- 파일 업로드 성공 메시지는 나오지만
- `loadAttachments()` 호출 후에도 파일 목록이 비어있음
- "등록된 파일이 없습니다" 메시지만 표시됨

**원인**:
각 JavaScript 함수 내부에서 `programId`를 로컬 변수로 재선언하여 전역 변수를 사용하지 않음

```javascript
// 문제 코드
async function loadAttachments() {
    const programId = /*[[${program.programId}]]*/ 0;  // 로컬 변수 재선언
    // ...
}
```

**해결**:
전역 `programId` 변수를 사용하도록 모든 함수 수정

```javascript
// 수정 코드
async function loadAttachments() {
    // const programId 선언 제거
    const fileListContent = document.getElementById('fileListContent');
    // programId는 전역 변수 사용
    const response = await fetch(`/api/programs/${programId}/files`);
}
```

**수정된 함수**:
- `loadAttachments()`
- 파일 업로드 이벤트 리스너
- `downloadFile()`
- `deleteFile()`

**커밋**: 12d52dc - Fix file attachment list by using global programId variable

---

### 문제 3: "첨부파일이 없습니다" 메시지 중복 표시

**증상**:
- 파일 목록이 정상적으로 표시되는데도
- 화면 하단에 "첨부파일이 없습니다" 메시지가 계속 표시됨
- 탭 시스템과 무관하게 항상 보임

**원인**:
구 버전의 첨부파일 섹션이 HTML에 하드코딩되어 있었음

```html
<!-- 문제 코드 (1224-1230번 라인) -->
<div class="attachment-section">
    <h3>첨부파일</h3>
    <div class="attachment-item" style="background: #f8f9fa; color: #6c757d; justify-content: center;">
        첨부파일이 없습니다.
    </div>
</div>
```

이 섹션이 메인 컨텐츠 영역에 항상 표시되어, 새로운 탭 시스템과 별도로 렌더링됨

**해결**:
구 버전 첨부파일 섹션 완전 제거

```html
<!-- 제거 완료 -->
```

새로운 탭 시스템의 `#attachments` 탭에서만 파일 목록 표시

**커밋**: db79685 - Remove hardcoded attachment section causing duplicate display

---

### 문제 4: 디버깅 로그 추가

**추가 사항**:
문제 진단을 위해 `loadAttachments()` 함수에 콘솔 로그 추가

```javascript
console.log('파일 목록 응답:', data);
console.log('files 배열:', data.files);
console.log('files 길이:', data.files ? data.files.length : 'undefined');
```

**커밋**: feb169b - Add debug logging for file attachment list loading

**참고**: 프로덕션 배포 전 디버그 로그 제거 권장

---

## 최종 테스트 결과

### 테스트 환경
- 브라우저: Chrome (시크릿 모드)
- 계정: 관리자 (9999999 / admin123)
- 테스트 파일: PDF, DOCX, JPG

### 테스트 결과
- ✅ 파일 업로드 성공
- ✅ 파일 목록 정상 표시 (파일명, 크기, 업로드일, 업로드자)
- ✅ 파일 다운로드 성공 (한글 파일명 정상 처리)
- ✅ 파일 삭제 성공 (Soft Delete)
- ✅ 관리자/일반 사용자 권한 분리 정상 작동
- ✅ 파일 크기 제한 (10MB) 정상 작동
- ✅ 확장자 검증 정상 작동

---

## 개발 완료 체크리스트

- ✅ Backend 구현 완료
- ✅ Frontend 구현 완료
- ✅ LazyInitializationException 해결
- ✅ 파일 목록 로드 문제 해결
- ✅ UI 중복 표시 문제 해결
- ✅ 전체 기능 테스트 완료
- ✅ 개발 문서 작성 완료
- ✅ Git 커밋 및 푸시 완료
- ⏳ PR 생성 (사용자가 직접 생성 예정)

---

**개발 로그 최종 업데이트**: 2025-11-17
**최종 커밋**: db79685
**브랜치**: claude/fix-admin-tab-display-01Tc8o3kAmdBzuhJ6mKaD15V
