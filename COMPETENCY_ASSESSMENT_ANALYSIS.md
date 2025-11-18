# 역량진단(Competency Assessment) 시스템 상세 분석 보고서

## 📋 Executive Summary

### 현황
- **현재 브랜치**: `claude/implement-competency-assessment-016oycC5yKv54U5RZQ41En7Y`
- **프로젝트**: SCMS3 (Student Competency Management System)
- **분석 날짜**: 2025-11-18

### 완성도
| 영역 | 상태 | 진행률 |
|------|------|--------|
| 데이터베이스 스키마 | ✅ 완료 | 100% |
| 백엔드 엔티티 | ❌ 미구현 | 0% |
| 백엔드 로직 | ❌ 미구현 | 0% |
| 프론트엔드 | ❌ 미구현 | 0% |
| **전체** | **⏳ 미구현** | **~5%** |

---

## 1️⃣ 데이터베이스 계층

### 1.1 현재 구현된 테이블 (스키마)

#### ✅ competency_categories (역량 카테고리)
**역할**: 역량의 상위 분류 (예: "전공역량", "일반역량", "리더십")

```sql
CREATE TABLE competency_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
)
```

**데이터 예시**:
```
id | name | description
1  | 전공역량 | 전공 관련 핵심 역량
2  | 일반역량 | 모든 학생이 갖춰야 할 기본 역량
3  | 리더십역량 | 리더십 및 대인관계 능력
```

---

#### ✅ competencies (역량)
**역할**: 세부 역량 (예: "프로그래밍 능력", "의사소통 능력")

```sql
CREATE TABLE competencies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES competency_categories(id) ON DELETE CASCADE,
    INDEX idx_category_id (category_id)
)
```

**데이터 예시**:
```
id | category_id | name | description
1  | 1 | 프로그래밍 능력 | Java, Python 등 프로그래밍 언어 활용 능력
2  | 1 | 데이터베이스 능력 | 관계형 데이터베이스 설계 및 구현 능력
3  | 2 | 의사소통 능력 | 효과적인 의사소통 능력
4  | 3 | 팀 리더십 | 팀을 이끌어가는 능력
```

---

#### ✅ student_competency_assessments (학생 역량 평가)
**역할**: 학생의 역량 평가 결과 저장

```sql
CREATE TABLE student_competency_assessments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,          -- FK: students
    competency_id BIGINT NOT NULL,       -- FK: competencies
    score INT NOT NULL,                  -- 점수 (0-100)
    assessment_date DATE NOT NULL,       -- 평가일
    assessor VARCHAR(100),               -- 평가자 (상담사명 등)
    notes TEXT,                          -- 비고
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (competency_id) REFERENCES competencies(id) ON DELETE CASCADE,
    INDEX idx_student_id (student_id),
    INDEX idx_competency_id (competency_id),
    INDEX idx_assessment_date (assessment_date)
)
```

**데이터 예시**:
```
id | student_id | competency_id | score | assessment_date | assessor | notes
1  | 1 | 1 | 85 | 2025-11-18 | 상담사A | 우수
2  | 1 | 2 | 70 | 2025-11-18 | 상담사A | 보강필요
3  | 1 | 3 | 80 | 2025-11-18 | 상담사A | 좋음
```

---

### 1.2 필요한 추가 테이블 (신규)

#### ❌ assessment_questions (진단 문항) - 필요함
**역할**: 역량 진단 설문의 개별 문항

```sql
CREATE TABLE IF NOT EXISTS assessment_questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    competency_id BIGINT NOT NULL,
    question_text TEXT NOT NULL,
    question_type ENUM('CHOICE', 'SCALE', 'TEXT') NOT NULL,
    options JSON,                        -- 선택지 JSON
    sequence INT NOT NULL,               -- 문항 순서
    is_active TINYINT(1) DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (competency_id) REFERENCES competencies(id) ON DELETE CASCADE,
    INDEX idx_competency_id (competency_id),
    INDEX idx_sequence (sequence)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
```

**용도**: 학생이 진단할 때 응답할 문항들을 저장

---

#### ❌ assessment_answers (진단 응답) - 필요함
**역할**: 학생이 진단에 답한 내용 저장

```sql
CREATE TABLE IF NOT EXISTS assessment_answers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    assessment_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    answer_text TEXT,                   -- 텍스트 형식 답변
    answer_value INT,                   -- 수치형 답변 (1-5 등)
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (assessment_id) REFERENCES student_competency_assessments(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES assessment_questions(id) ON DELETE CASCADE,
    INDEX idx_assessment_id (assessment_id),
    INDEX idx_question_id (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
```

**용도**: 진단 시 학생이 제출한 개별 답변 기록

---

## 2️⃣ 백엔드 계층

### 2.1 필요한 엔티티 클래스

#### ❌ CompetencyCategory.java
**파일 경로**: `/src/main/java/com/scms/app/model/CompetencyCategory.java`

```java
@Entity
@Table(name = "competency_categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompetencyCategory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<Competency> competencies = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

---

#### ❌ Competency.java
**파일 경로**: `/src/main/java/com/scms/app/model/Competency.java`

```java
@Entity
@Table(name = "competencies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Competency {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private CompetencyCategory category;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "competency", cascade = CascadeType.ALL)
    private List<StudentCompetencyAssessment> assessments = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

---

#### ❌ StudentCompetencyAssessment.java
**파일 경로**: `/src/main/java/com/scms/app/model/StudentCompetencyAssessment.java`

```java
@Entity
@Table(name = "student_competency_assessments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentCompetencyAssessment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competency_id", nullable = false)
    private Competency competency;
    
    @Column(nullable = false)
    private Integer score;  // 0-100
    
    @Column(name = "assessment_date", nullable = false)
    private LocalDate assessmentDate;
    
    @Column(length = 100)
    private String assessor;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

---

### 2.2 필요한 Repository 인터페이스

#### ❌ CompetencyCategoryRepository.java
```java
@Repository
public interface CompetencyCategoryRepository extends JpaRepository<CompetencyCategory, Long> {
    List<CompetencyCategory> findAll();
    Optional<CompetencyCategory> findById(Long id);
    List<CompetencyCategory> findByNameContaining(String name);
}
```

---

#### ❌ CompetencyRepository.java
```java
@Repository
public interface CompetencyRepository extends JpaRepository<Competency, Long> {
    List<Competency> findByCategoryId(Long categoryId);
    List<Competency> findByNameContaining(String name);
    Optional<Competency> findById(Long id);
    List<Competency> findAll();
}
```

---

#### ❌ StudentCompetencyAssessmentRepository.java
```java
@Repository
public interface StudentCompetencyAssessmentRepository extends JpaRepository<StudentCompetencyAssessment, Long> {
    
    // 학생별 평가 조회
    List<StudentCompetencyAssessment> findByStudentId(Long studentId);
    
    // 역량별 평가 조회
    List<StudentCompetencyAssessment> findByCompetencyId(Long competencyId);
    
    // 학생-역량 평가 조회
    Optional<StudentCompetencyAssessment> findByStudentIdAndCompetencyId(
        Long studentId, Long competencyId
    );
    
    // 평가 날짜 범위 조회
    List<StudentCompetencyAssessment> findByAssessmentDateBetween(
        LocalDate startDate, LocalDate endDate
    );
    
    // 페이지네이션
    Page<StudentCompetencyAssessment> findByStudentId(
        Long studentId, Pageable pageable
    );
}
```

---

### 2.3 필요한 Service 클래스

#### ❌ CompetencyService.java
```java
@Service
@RequiredArgsConstructor
public class CompetencyService {
    
    private final CompetencyCategoryRepository categoryRepository;
    private final CompetencyRepository competencyRepository;
    
    // 역량 카테고리
    public List<CompetencyCategoryResponse> getAllCategories() { ... }
    public CompetencyCategoryResponse getCategoryById(Long id) { ... }
    public CompetencyCategoryResponse createCategory(CompetencyCategoryRequest req) { ... }
    public CompetencyCategoryResponse updateCategory(Long id, CompetencyCategoryRequest req) { ... }
    public void deleteCategory(Long id) { ... }
    
    // 역량
    public List<CompetencyResponse> getCompetenciesByCategory(Long categoryId) { ... }
    public CompetencyResponse getCompetencyById(Long id) { ... }
    public List<CompetencyResponse> getAllCompetencies() { ... }
    public CompetencyResponse createCompetency(CompetencyRequest req) { ... }
    public CompetencyResponse updateCompetency(Long id, CompetencyRequest req) { ... }
    public void deleteCompetency(Long id) { ... }
}
```

---

#### ❌ CompetencyAssessmentService.java
```java
@Service
@RequiredArgsConstructor
public class CompetencyAssessmentService {
    
    private final StudentCompetencyAssessmentRepository assessmentRepository;
    private final CompetencyRepository competencyRepository;
    private final StudentRepository studentRepository;
    
    // 평가 저장
    public AssessmentResponse saveAssessment(AssessmentRequest req) { ... }
    
    // 학생별 평가 조회
    public List<AssessmentResponse> getStudentAssessments(Long studentId) { ... }
    
    // 평가 리포트 생성
    public AssessmentReportResponse generateReport(Long studentId) { ... }
    
    // 역량별 평가 통계
    public CompetencyStatisticsResponse getCompetencyStatistics(Long competencyId) { ... }
    
    // 평가 수정/삭제
    public AssessmentResponse updateAssessment(Long id, AssessmentRequest req) { ... }
    public void deleteAssessment(Long id) { ... }
}
```

---

### 2.4 필요한 DTO 클래스

#### ❌ CompetencyCategoryResponse.java / CompetencyCategoryRequest.java
```java
@Data
public class CompetencyCategoryResponse {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private List<CompetencyResponse> competencies;
}

@Data
public class CompetencyCategoryRequest {
    @NotBlank
    private String name;
    private String description;
}
```

---

#### ❌ CompetencyResponse.java / CompetencyRequest.java
```java
@Data
public class CompetencyResponse {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String description;
    private LocalDateTime createdAt;
}

@Data
public class CompetencyRequest {
    @NotNull
    private Long categoryId;
    @NotBlank
    private String name;
    private String description;
}
```

---

#### ❌ AssessmentResponse.java / AssessmentRequest.java
```java
@Data
public class AssessmentResponse {
    private Long id;
    private Long studentId;
    private String studentName;
    private Long competencyId;
    private String competencyName;
    private String categoryName;
    private Integer score;
    private LocalDate assessmentDate;
    private String assessor;
    private String notes;
}

@Data
public class AssessmentRequest {
    @NotNull
    private Long studentId;
    @NotNull
    private Long competencyId;
    @NotNull
    @Min(0) @Max(100)
    private Integer score;
    @NotNull
    private LocalDate assessmentDate;
    private String assessor;
    private String notes;
}
```

---

#### ❌ AssessmentReportResponse.java
```java
@Data
public class AssessmentReportResponse {
    private Long studentId;
    private String studentName;
    private Double totalScore;
    private List<CategoryScoreDto> categoryScores;
    private List<String> strengths;
    private List<String> weaknesses;
    private List<RecommendedProgramDto> recommendations;
}

@Data
public class CategoryScoreDto {
    private Long categoryId;
    private String categoryName;
    private Double averageScore;
    private List<CompetencyScoreDto> competencies;
}

@Data
public class RecommendedProgramDto {
    private Long programId;
    private String programName;
    private String reason;
}
```

---

### 2.5 필요한 Controller 클래스

#### ❌ CompetencyController.java
```java
@RestController
@RequestMapping("/api/competencies")
@RequiredArgsConstructor
public class CompetencyController {
    
    private final CompetencyService competencyService;
    
    // 카테고리 관련 엔드포인트
    @GetMapping("/categories")
    public ResponseEntity<List<CompetencyCategoryResponse>> getAllCategories() { ... }
    
    @GetMapping("/categories/{categoryId}")
    public ResponseEntity<CompetencyCategoryResponse> getCategoryById(@PathVariable Long categoryId) { ... }
    
    @GetMapping("/categories/{categoryId}/competencies")
    public ResponseEntity<List<CompetencyResponse>> getCompetenciesByCategory(
        @PathVariable Long categoryId) { ... }
    
    @PostMapping("/categories")
    public ResponseEntity<CompetencyCategoryResponse> createCategory(
        @RequestBody CompetencyCategoryRequest req) { ... }
    
    @PutMapping("/categories/{categoryId}")
    public ResponseEntity<CompetencyCategoryResponse> updateCategory(
        @PathVariable Long categoryId,
        @RequestBody CompetencyCategoryRequest req) { ... }
    
    @DeleteMapping("/categories/{categoryId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long categoryId) { ... }
    
    // 역량 관련 엔드포인트
    @GetMapping
    public ResponseEntity<List<CompetencyResponse>> getAllCompetencies() { ... }
    
    @GetMapping("/{competencyId}")
    public ResponseEntity<CompetencyResponse> getCompetencyById(
        @PathVariable Long competencyId) { ... }
    
    @PostMapping
    public ResponseEntity<CompetencyResponse> createCompetency(
        @RequestBody CompetencyRequest req) { ... }
    
    @PutMapping("/{competencyId}")
    public ResponseEntity<CompetencyResponse> updateCompetency(
        @PathVariable Long competencyId,
        @RequestBody CompetencyRequest req) { ... }
    
    @DeleteMapping("/{competencyId}")
    public ResponseEntity<Void> deleteCompetency(@PathVariable Long competencyId) { ... }
}
```

---

#### ❌ CompetencyAssessmentController.java
```java
@RestController
@RequestMapping("/api/assessments")
@RequiredArgsConstructor
public class CompetencyAssessmentController {
    
    private final CompetencyAssessmentService assessmentService;
    
    @PostMapping
    public ResponseEntity<AssessmentResponse> saveAssessment(
        @RequestBody AssessmentRequest req) { ... }
    
    @GetMapping("/students/{studentId}")
    public ResponseEntity<List<AssessmentResponse>> getStudentAssessments(
        @PathVariable Long studentId) { ... }
    
    @GetMapping("/students/{studentId}/report")
    public ResponseEntity<AssessmentReportResponse> generateReport(
        @PathVariable Long studentId) { ... }
    
    @GetMapping("/competencies/{competencyId}/statistics")
    public ResponseEntity<CompetencyStatisticsResponse> getCompetencyStatistics(
        @PathVariable Long competencyId) { ... }
    
    @PutMapping("/{assessmentId}")
    public ResponseEntity<AssessmentResponse> updateAssessment(
        @PathVariable Long assessmentId,
        @RequestBody AssessmentRequest req) { ... }
    
    @DeleteMapping("/{assessmentId}")
    public ResponseEntity<Void> deleteAssessment(@PathVariable Long assessmentId) { ... }
}
```

---

## 3️⃣ 프론트엔드 계층

### 3.1 필요한 HTML 페이지

#### ❌ competency-assessment.html
**위치**: `/src/main/resources/templates/competency-assessment.html`

**기능**:
- 역량 목록 로드
- 각 역량별 진단 문항 표시
- 학생 응답 입력 (선택지, 점수, 텍스트)
- 진단 제출

**주요 요소**:
```html
- 진행률 표시 (Progress bar)
- 문항 순번 표시
- 다양한 입력 타입 (라디오, 슬라이더, 텍스트)
- 이전/다음 버튼
- 제출 버튼
```

---

#### ❌ competency-result.html
**위치**: `/src/main/resources/templates/competency-result.html`

**기능**:
- 역량별 점수 차트 (radar chart, bar chart)
- 카테고리별 평균 점수
- 강점/약점 분석 (Top 3, Bottom 3)
- 권장 프로그램 추천
- 상세 분석 리포트

**주요 요소**:
```html
- Chart.js를 사용한 시각화
- 색상 코딩 (녹색: 우수, 황색: 보통, 적색: 미흡)
- 프로그램 카드 (권장 프로그램)
- 인쇄 기능
- 공유 기능
```

---

#### ❌ competency-list.html
**위치**: `/src/main/resources/templates/competency-list.html`

**기능**:
- 역량 카테고리 목록
- 각 카테고리별 역량 상세 정보
- 역량 설명 및 중요도 표시

---

#### ❌ admin/competency-manage.html
**위치**: `/src/main/resources/templates/admin/competency-manage.html`

**기능**:
- 역량 카테고리 CRUD
- 역량 CRUD
- 진단 문항 CRUD
- 학생 평가 결과 조회 및 수정

---

### 3.2 필요한 JavaScript 파일

#### ❌ competency-assessment.js
**기능**:
- API 호출 (역량 목록 로드, 문항 로드)
- 폼 검증
- 진행 상황 관리 (페이지네이션)
- 답변 저장 (임시 저장)
- 제출 처리

---

#### ❌ competency-chart.js
**기능**:
- Chart.js를 사용한 차트 렌더링
- 레이더 차트 (Radar Chart): 역량 비교
- 막대 그래프 (Bar Chart): 카테고리별 점수
- 게이지 차트 (Gauge Chart): 전체 점수

---

#### ❌ competency-style.css
**기능**:
- 반응형 레이아웃
- 진단 페이지 스타일
- 결과 페이지 스타일
- 차트 스타일
- 모바일 최적화

---

## 4️⃣ API 엔드포인트 설계

### 4.1 RESTful API 엔드포인트

| 메서드 | 엔드포인트 | 설명 | 권한 |
|--------|-----------|------|------|
| **카테고리 관리** |
| GET | `/api/competencies/categories` | 모든 카테고리 조회 | 전체 |
| GET | `/api/competencies/categories/{id}` | 카테고리 상세 조회 | 전체 |
| POST | `/api/competencies/categories` | 카테고리 생성 | ADMIN |
| PUT | `/api/competencies/categories/{id}` | 카테고리 수정 | ADMIN |
| DELETE | `/api/competencies/categories/{id}` | 카테고리 삭제 | ADMIN |
| **역량 관리** |
| GET | `/api/competencies` | 모든 역량 조회 | 전체 |
| GET | `/api/competencies/{id}` | 역량 상세 조회 | 전체 |
| GET | `/api/competencies/categories/{categoryId}/competencies` | 카테고리별 역량 | 전체 |
| POST | `/api/competencies` | 역량 생성 | ADMIN |
| PUT | `/api/competencies/{id}` | 역량 수정 | ADMIN |
| DELETE | `/api/competencies/{id}` | 역량 삭제 | ADMIN |
| **평가 관리** |
| POST | `/api/assessments` | 평가 저장 | STUDENT, ADMIN |
| GET | `/api/assessments/students/{studentId}` | 학생별 평가 조회 | STUDENT, ADMIN |
| GET | `/api/assessments/students/{studentId}/report` | 평가 리포트 | STUDENT, ADMIN |
| GET | `/api/assessments/competencies/{competencyId}/statistics` | 역량별 통계 | ADMIN |
| PUT | `/api/assessments/{id}` | 평가 수정 | ADMIN |
| DELETE | `/api/assessments/{id}` | 평가 삭제 | ADMIN |

---

## 5️⃣ 구현 로드맵

### Phase 1: 백엔드 기초 (5-6시간)

1. **엔티티 생성** (1시간)
   - CompetencyCategory.java
   - Competency.java
   - StudentCompetencyAssessment.java

2. **Repository 생성** (30분)
   - CompetencyCategoryRepository.java
   - CompetencyRepository.java
   - StudentCompetencyAssessmentRepository.java

3. **Service 개발** (2시간)
   - CompetencyService.java
   - CompetencyAssessmentService.java

4. **DTO 생성** (45분)
   - 요청/응답 DTO 클래스들

5. **Controller 개발** (1.5시간)
   - CompetencyController.java
   - CompetencyAssessmentController.java

6. **초기 데이터 로더** (30분)
   - DataLoader에 샘플 데이터 추가

---

### Phase 2: 프론트엔드 기초 (8-10시간)

1. **진단 페이지** (3시간)
   - competency-assessment.html
   - competency-assessment.js
   - 스타일 적용

2. **결과 페이지** (3시간)
   - competency-result.html
   - 차트 라이브러리 통합
   - 결과 시각화

3. **리스트 페이지** (2시간)
   - competency-list.html
   - 헤더 네비게이션 추가

4. **관리자 페이지** (2시간)
   - admin/competency-manage.html
   - CRUD 기능

---

### Phase 3: 통합 및 최적화 (2-3시간)

1. **API 통합 테스트**
2. **버그 수정**
3. **성능 최적화**
4. **코드 리뷰**

---

## 6️⃣ 참고할 기존 구현

### 프로그램 시스템 구조 참고
- **엔티티**: `/src/main/java/com/scms/app/model/Program.java`
- **Repository**: `/src/main/java/com/scms/app/repository/ProgramRepository.java`
- **Service**: `/src/main/java/com/scms/app/service/ProgramService.java`
- **Controller**: `/src/main/java/com/scms/app/controller/ProgramAdminController.java`

### 기존 UI 패턴 참고
- **프로그램 목록**: `/src/main/resources/templates/programs.html`
- **프로그램 상세**: `/src/main/resources/templates/program-detail.html`
- **레이아웃**: `/src/main/resources/templates/layout/`

### DataLoader 참고
- **위치**: `/src/main/java/com/scms/app/config/DataLoader.java`
- **참고**: 샘플 데이터 추가 방법

---

## 7️⃣ 주요 고려사항

### 성능
- N+1 쿼리 방지: JOIN FETCH 사용
- 페이지네이션: 대량 데이터 조회 시 적용
- 캐싱: 자주 조회되는 데이터 캐싱

### 보안
- 권한 검증: @PreAuthorize 사용
- CSRF 토큰
- SQL Injection 방지 (JPA 사용)

### 사용성
- 반응형 디자인
- 로딩 상태 표시
- 오류 메시지 명확화
- 접근성 (Accessibility)

---

## 8️⃣ 질문 & 답변

### Q. 왜 assessment_questions 테이블이 필요한가?
A. 학생이 역량진단을 할 때 답할 문항들을 저장하기 위함. 각 역량마다 여러 개의 문항이 있을 수 있음.

### Q. score는 왜 0-100인가?
A. 일반적인 평가 점수 범위. 필요시 조정 가능.

### Q. student_id는 students 테이블의 id를 참조하는가?
A. 네, `/id` 컬럼을 참조. 학번이 아님.

### Q. 우선순위는?
A. 백엔드 → 프론트엔드 순. 백엔드가 완성되어야 프론트엔드 개발 가능.

---

## 9️⃣ 체크리스트

### 백엔드
- [ ] 엔티티 3개 생성
- [ ] Repository 3개 생성
- [ ] Service 2개 생성
- [ ] Controller 2개 생성
- [ ] DTO 7개 생성
- [ ] API 문서 작성
- [ ] 단위 테스트 작성
- [ ] 통합 테스트

### 프론트엔드
- [ ] HTML 4개 페이지 생성
- [ ] JavaScript 3개 파일 생성
- [ ] CSS 1개 파일 생성
- [ ] 헤더 네비게이션 추가
- [ ] 반응형 디자인 적용
- [ ] 브라우저 호환성 확인

### 데이터베이스
- [ ] assessment_questions 테이블 생성
- [ ] assessment_answers 테이블 생성
- [ ] 샘플 데이터 입력

---

## 🔟 다음 단계

1. 현재 분석 문서 검토
2. 데이터베이스 테이블 추가 생성 (assessment_questions, assessment_answers)
3. 백엔드 엔티티 구현 시작
4. 필요한 Repository/Service/Controller 구현
5. API 테스트 (Postman 등)
6. 프론트엔드 개발
7. 통합 테스트 및 버그 수정

---

**분석 완료**: 2025-11-18
**작성자**: AI Assistant
**버전**: 1.0
