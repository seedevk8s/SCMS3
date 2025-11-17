# 다음 구현 계획 (Next Implementation Plan)

## 📅 작성일
2025-11-14

---

## 📊 현재 구현 상태 정리

### ✅ 완료된 기능 (프로그램 상세 페이지)

#### 1. 프로그램 기본 정보 표시
- ✅ 썸네일 이미지 (picsum.photos)
- ✅ 프로그램 제목, 설명
- ✅ 행정부서, 단과대학, 카테고리
- ✅ 신청 기간, 정원, 현재 참여 인원
- ✅ 조회수 (HITS)

#### 2. 프로그램 신청/취소 기능
- ✅ 신청하기 버튼
- ✅ 취소하기 버튼
- ✅ 중복 신청 방지
- ✅ 정원 관리 (자동 증감)
- ✅ 신청 가능 기간 체크
- ✅ 로그인 상태 확인

**구현된 백엔드 컴포넌트**:
- `ProgramApplication` Entity (5가지 상태: PENDING, APPROVED, REJECTED, CANCELLED, COMPLETED)
- `ProgramApplicationRepository` (JOIN FETCH로 Lazy Loading 해결)
- `ProgramApplicationService` (신청/취소/승인/거부 로직)
- `ProgramApplicationController` (REST API)
- `ProgramApplicationResponse` DTO

#### 3. 나의 신청내역 조회
- ✅ 탭으로 구현 (나의 신청내역 / 세부내용)
- ✅ 신청 상태 표시 (대기/승인/거부/취소/참여완료)
- ✅ 신청일 표시
- ✅ 취소 가능 여부 확인
- ✅ LazyInitializationException 해결 (JOIN FETCH 적용)

#### 4. 상태 표시 개선
- ✅ D-day 숫자 표시 → 한글 라벨 변경
  - "접수예정" (파란색) - 신청 시작 전
  - "접수중" (초록색) - 마감 7일 초과
  - "마감임박" (보라색) - 마감 7일 이내
  - "마감" (회색) - 신청 종료
  - "정원마감" (회색) - 인원 초과
- ✅ 상태 범례 추가 (index.html, programs.html)
- ✅ 사용자 친화적 UI

#### 5. 데이터 관리
- ✅ DataLoader 스킵 로직 개선 (OPEN 상태 + 2025년 날짜 기준)
- ✅ 다양한 프로그램 상태 데이터 (25 OPEN / 15 SCHEDULED / 10 CLOSED)
- ✅ 자동 데이터 리로드

---

## 🎯 다음 구현 가능한 기능들

### 옵션 1: 관리자 기능 (프로그램 신청 관리) ⭐⭐⭐

**관련도**: 높음 - 프로그램 신청 관리와 직접 연관

#### 주요 기능
- 신청자 목록 조회 (관리자)
- 신청 승인/거부 (관리자)
- 신청자 정보 상세 조회
- 신청자 엑셀 다운로드 (관리자)
- 프로그램 수정/삭제 (관리자)
- 신청 통계 (승인/대기/거부 수)

#### 구현 위치
- **옵션 A**: 프로그램 상세 페이지에 관리자 전용 탭 추가
- **옵션 B**: 별도 관리자 페이지 생성 (`/admin/programs/{id}/applications`)

#### 필요한 작업

**Frontend**:
```html
<!-- 관리자 전용 탭 -->
<button class="tab-button" th:if="${isAdmin}" onclick="showTab(event, 'admin-applications')">
    신청 관리 (관리자)
</button>

<div id="admin-applications" class="tab-pane">
    <!-- 신청자 목록 테이블 -->
    <!-- 승인/거부 버튼 -->
    <!-- 엑셀 다운로드 버튼 -->
</div>
```

**Backend (이미 대부분 구현됨)**:
```java
// Service에 이미 구현된 메서드들
- approveApplication(applicationId)
- rejectApplication(applicationId, reason)
- findByProgramId(programId) - 프로그램별 신청 내역

// 추가 필요
- Excel 다운로드 로직 (Apache POI)
```

#### 예상 소요 시간
- Frontend: 2-3시간
- Backend (엑셀 다운로드): 1-2시간
- 테스트: 1시간
- **총 4-6시간**

---

### 옵션 2: 후기/리뷰 시스템 ⭐⭐⭐

**관련도**: 높음 - 프로그램 상세 페이지 기능 확장

#### 주요 기능
- 후기 작성 (참여 완료 학생만)
- 후기 조회 (모든 사용자)
- 후기 수정/삭제 (본인만)
- 별점 평가 (5점 척도)
- 사진 첨부 (선택)
- 후기 페이징 (무한 스크롤 또는 페이지네이션)

#### 구현 위치
프로그램 상세 페이지에 "후기" 탭 추가

#### 필요한 작업

**Entity**:
```java
@Entity
@Table(name = "program_reviews")
public class ProgramReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id")
    private Program program;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private Integer rating; // 1-5

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "image_url")
    private String imageUrl; // 선택

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt; // Soft Delete
}
```

**Repository**:
```java
public interface ProgramReviewRepository extends JpaRepository<ProgramReview, Integer> {
    List<ProgramReview> findByProgramIdAndDeletedAtIsNull(Integer programId);
    boolean existsByUserIdAndProgramIdAndDeletedAtIsNull(Integer userId, Integer programId);
    Optional<ProgramReview> findByReviewIdAndUserId(Integer reviewId, Integer userId);
}
```

**Service**:
```java
public class ProgramReviewService {
    // 후기 작성 (참여 완료 확인)
    public ProgramReview createReview(Integer userId, Integer programId, ReviewRequest request);

    // 후기 목록 조회
    public List<ProgramReview> getReviewsByProgram(Integer programId);

    // 후기 수정
    public ProgramReview updateReview(Integer userId, Integer reviewId, ReviewRequest request);

    // 후기 삭제
    public void deleteReview(Integer userId, Integer reviewId);

    // 평균 별점 계산
    public Double getAverageRating(Integer programId);
}
```

**Controller**:
```java
@RestController
@RequestMapping("/api/programs/{programId}/reviews")
public class ProgramReviewController {
    // POST / - 후기 작성
    // GET / - 후기 목록
    // PUT /{reviewId} - 후기 수정
    // DELETE /{reviewId} - 후기 삭제
}
```

**Frontend**:
```html
<!-- 후기 탭 -->
<button class="tab-button" onclick="showTab(event, 'reviews')">후기</button>

<div id="reviews" class="tab-pane">
    <!-- 평균 별점 표시 -->
    <div class="review-summary">
        <span class="average-rating">⭐ 4.5 / 5.0</span>
        <span class="review-count">(15개 후기)</span>
    </div>

    <!-- 후기 작성 버튼 (참여 완료자만) -->
    <button class="btn-write-review" th:if="${canWriteReview}">후기 작성하기</button>

    <!-- 후기 목록 -->
    <div class="review-list">
        <!-- 각 후기 카드 -->
    </div>
</div>
```

#### 예상 소요 시간
- Backend: 3-4시간
- Frontend: 3-4시간
- 테스트: 1-2시간
- **총 7-10시간**

---

### 옵션 3: 첨부파일 관리 ⭐⭐

**관련도**: 중간 - 프로그램 관련 자료 제공

#### 주요 기능
- 프로그램 자료 첨부 (관리자)
- 첨부파일 다운로드 (학생)
- 파일 목록 표시
- 파일 크기 제한 (예: 10MB)
- 허용 파일 형식 (PDF, DOCX, PPTX, ZIP)

#### 구현 위치
프로그램 상세 페이지에 "첨부파일" 섹션 추가

#### 필요한 작업

**Entity**:
```java
@Entity
@Table(name = "program_files")
public class ProgramFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer fileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id")
    private Program program;

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false)
    private String storedFileName; // UUID

    @Column(nullable = false)
    private String filePath;

    private Long fileSize; // bytes

    private String fileType; // MIME type

    private LocalDateTime uploadedAt;
    private LocalDateTime deletedAt;
}
```

**Configuration**:
```java
@Configuration
public class FileStorageConfig {
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Bean
    public String uploadDirectory() {
        return uploadDir;
    }
}
```

**Service**:
```java
public class ProgramFileService {
    // 파일 업로드
    public ProgramFile uploadFile(Integer programId, MultipartFile file);

    // 파일 다운로드
    public Resource downloadFile(Integer fileId);

    // 파일 목록 조회
    public List<ProgramFile> getFilesByProgram(Integer programId);

    // 파일 삭제
    public void deleteFile(Integer fileId);
}
```

**Controller**:
```java
@RestController
@RequestMapping("/api/programs/{programId}/files")
public class ProgramFileController {
    // POST / - 파일 업로드 (관리자)
    // GET / - 파일 목록
    // GET /{fileId}/download - 파일 다운로드
    // DELETE /{fileId} - 파일 삭제 (관리자)
}
```

**Frontend**:
```html
<!-- 첨부파일 섹션 -->
<div class="attachments-section">
    <h4>📎 첨부파일</h4>

    <!-- 파일 업로드 (관리자) -->
    <div th:if="${isAdmin}" class="file-upload">
        <input type="file" id="fileInput" multiple>
        <button onclick="uploadFiles()">파일 업로드</button>
    </div>

    <!-- 파일 목록 -->
    <ul class="file-list">
        <li th:each="file : ${files}">
            <i class="fas fa-file"></i>
            <a th:href="@{/api/programs/{programId}/files/{fileId}/download(programId=${program.programId}, fileId=${file.fileId})}"
               th:text="${file.originalFileName}"></a>
            <span th:text="${file.fileSize / 1024} + ' KB'"></span>
        </li>
    </ul>
</div>
```

#### application.yml 설정
```yaml
file:
  upload-dir: ${user.home}/scms-uploads
  max-size: 10485760 # 10MB

spring:
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 50MB
```

#### 예상 소요 시간
- Backend: 4-5시간
- Frontend: 2-3시간
- 테스트: 2시간
- **총 8-10시간**

---

### 옵션 4: 나의 프로그램 (마이페이지) ⭐⭐

**관련도**: 중간 - 신청 내역 확장

#### 주요 기능
- 신청한 프로그램 목록 (전체)
- 참여 완료한 프로그램
- 참여 예정 프로그램 (승인됨)
- 관심 프로그램 (북마크/즐겨찾기)
- 필터링 (상태별, 카테고리별)

#### 구현 위치
`/mypage` 또는 `/my-programs` 신규 페이지 생성

#### 필요한 작업

**Controller**:
```java
@Controller
public class MyPageController {
    @GetMapping("/mypage")
    public String myPage(HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");

        // 신청한 프로그램
        List<ProgramApplication> applications = applicationService.getUserApplications(userId);

        // 상태별 분류
        model.addAttribute("pending", applications.stream()
            .filter(a -> a.getStatus() == ApplicationStatus.PENDING).collect(Collectors.toList()));
        model.addAttribute("approved", applications.stream()
            .filter(a -> a.getStatus() == ApplicationStatus.APPROVED).collect(Collectors.toList()));
        model.addAttribute("completed", applications.stream()
            .filter(a -> a.getStatus() == ApplicationStatus.COMPLETED).collect(Collectors.toList()));

        return "mypage";
    }
}
```

**Frontend (mypage.html)**:
```html
<!-- 탭 메뉴 -->
<div class="mypage-tabs">
    <button class="tab active" onclick="showMyTab('all')">전체</button>
    <button class="tab" onclick="showMyTab('pending')">신청대기</button>
    <button class="tab" onclick="showMyTab('approved')">승인됨</button>
    <button class="tab" onclick="showMyTab('completed')">참여완료</button>
</div>

<!-- 프로그램 카드 목록 -->
<div class="mypage-programs">
    <!-- programs.html과 유사한 카드 레이아웃 -->
</div>
```

#### 예상 소요 시간
- Backend: 2-3시간
- Frontend: 4-5시간
- 테스트: 1-2시간
- **총 7-10시간**

---

### 옵션 5: 알림 시스템 ⭐

**관련도**: 낮음 - 독립적 기능

#### 주요 기능
- 신청 승인/거부 알림
- 프로그램 시작 알림 (D-1)
- 마감 임박 알림 (D-3)
- 알림 목록 조회
- 알림 읽음 처리
- 알림 삭제

#### 구현 위치
- 헤더에 알림 아이콘 (🔔)
- 별도 알림 페이지 (`/notifications`)

#### 필요한 작업

**Entity**:
```java
@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private NotificationType type; // APPLICATION_APPROVED, PROGRAM_STARTING, etc.

    @Column(nullable = false)
    private Boolean isRead = false;

    private String relatedUrl; // 관련 페이지 URL

    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
```

**Service**:
```java
public class NotificationService {
    // 알림 생성
    public void createNotification(Integer userId, String title, String content, NotificationType type);

    // 알림 목록 조회
    public List<Notification> getNotifications(Integer userId);

    // 읽지 않은 알림 개수
    public Long getUnreadCount(Integer userId);

    // 알림 읽음 처리
    public void markAsRead(Integer notificationId);

    // 모두 읽음 처리
    public void markAllAsRead(Integer userId);
}
```

**Scheduler (자동 알림)**:
```java
@Component
public class NotificationScheduler {
    @Scheduled(cron = "0 0 9 * * *") // 매일 오전 9시
    public void sendProgramStartingNotifications() {
        // D-1 프로그램 찾기
        // 승인된 신청자들에게 알림 발송
    }

    @Scheduled(cron = "0 0 9 * * *")
    public void sendDeadlineNotifications() {
        // D-3 마감 예정 프로그램 찾기
        // 미신청자들에게 알림 발송
    }
}
```

#### 예상 소요 시간
- Backend: 5-6시간
- Frontend: 3-4시간
- 스케줄러 설정: 2-3시간
- 테스트: 2시간
- **총 12-15시간**

---

## 💡 추천 구현 순서

### 1순위: 후기/리뷰 시스템 ⭐⭐⭐
**추천 이유**:
- 프로그램 상세 페이지와 자연스럽게 연결
- 사용자 경험 향상에 즉각적인 효과
- 구현 난이도 중간 (적절한 학습 곡선)
- 시각적으로 명확한 결과물
- 실제 프로그램 품질 향상에 기여

**구현 순서**:
1. Entity, Repository 생성
2. Service 로직 구현 (후기 작성 권한 체크)
3. Controller (REST API)
4. Frontend (후기 탭, 별점 UI, 후기 카드)
5. 테스트

---

### 2순위: 관리자 기능 (신청 관리) ⭐⭐⭐
**추천 이유**:
- 이미 구현된 신청 기능의 완성도를 높임
- 실제 운영에 필수적인 기능
- 백엔드는 대부분 구현됨 (Service에 approve/reject 이미 있음)
- 빠르게 완성 가능

**구현 순서**:
1. 관리자 권한 체크 로직
2. 프로그램 상세 페이지에 관리자 탭 추가
3. 신청자 목록 UI 구현
4. 승인/거부 버튼 및 모달
5. 엑셀 다운로드 기능 (Apache POI)
6. 테스트

---

### 3순위: 나의 프로그램 (마이페이지) ⭐⭐
**추천 이유**:
- 학생들이 자신의 활동을 한눈에 볼 수 있음
- 별도 페이지로 깔끔하게 분리
- 기존 기능 재사용 가능

---

### 4순위: 첨부파일 관리 ⭐⭐
**추천 이유**:
- 프로그램 자료 제공에 유용
- 파일 업로드/다운로드 학습 기회
- 다소 복잡한 구현 (파일 시스템, 보안)

---

### 5순위: 알림 시스템 ⭐
**추천 이유**:
- 사용자 경험 향상
- 독립적인 기능으로 나중에 추가 가능
- 구현 복잡도 높음 (스케줄러, 실시간 알림)

---

## 📋 구현 시 고려사항

### 공통 사항
1. **보안**: XSS, CSRF, SQL Injection 방지
2. **권한 체크**: 학생/관리자 권한 구분
3. **에러 핸들링**: 명확한 에러 메시지
4. **로깅**: 주요 액션 로그 기록
5. **트랜잭션**: @Transactional 적절히 사용
6. **Soft Delete**: 데이터 복구 가능하도록

### 성능 고려사항
1. **Lazy Loading**: JOIN FETCH로 N+1 문제 방지
2. **페이징**: 큰 데이터셋은 페이징 처리
3. **캐싱**: 자주 조회되는 데이터는 캐싱 고려
4. **인덱스**: 검색 조건 필드에 DB 인덱스

### 사용자 경험
1. **로딩 인디케이터**: 비동기 작업 시 스피너 표시
2. **에러 메시지**: 사용자 친화적인 메시지
3. **확인 모달**: 중요한 액션(삭제, 취소 등)은 확인 받기
4. **반응형 디자인**: 모바일 환경 고려

---

## 🔗 참고 문서

- [01_PROJECT_OVERVIEW.md](./01_PROJECT_OVERVIEW.md) - 프로젝트 전체 개요
- [10_PROGRAM_APPLICATION_FEATURE_DEVELOPMENT_LOG.md](./10_PROGRAM_APPLICATION_FEATURE_DEVELOPMENT_LOG.md) - 프로그램 신청 기능 개발 로그

---

## 📝 다음 액션

1. **우선순위 결정**: 위 옵션 중 구현할 기능 선택
2. **상세 설계**: 선택한 기능의 상세 설계서 작성
3. **개발 시작**: Entity → Repository → Service → Controller → Frontend 순서로 구현
4. **테스트**: 단위 테스트 및 통합 테스트
5. **문서 업데이트**: 개발 완료 후 문서 업데이트
