# 04. 프로그램 관리 기능 개발 로그

> **작성일**: 2025-11-07
> **작업 내용**: Program 기능 수직적 슬라이스 구현 및 홈페이지 통합
> **브랜치**: `claude/implement-champ-homepage-011CUsko4woxjyHMhfELyeW2`

---

## 📋 목차

1. [개요](#1-개요)
2. [개발 방식](#2-개발-방식)
3. [Backend 구현](#3-backend-구현)
4. [Frontend 구현](#4-frontend-구현)
5. [테스트 데이터](#5-테스트-데이터)
6. [버그 수정](#6-버그-수정)
7. [커밋 히스토리](#7-커밋-히스토리)
8. [실행 및 확인](#8-실행-및-확인)
9. [다음 단계](#9-다음-단계)

---

## 1. 개요

### 1.1 목표

홈페이지에서 하드코딩된 프로그램 데이터를 DB 기반 동적 데이터로 전환하여, 실제 프로그램 관리가 가능하도록 전체 레이어를 구현합니다.

### 1.2 주요 성과

- ✅ Program Entity → Repository → Service → Controller → View 전체 레이어 구현
- ✅ 8개 샘플 프로그램 자동 생성 기능
- ✅ 홈페이지 동적 렌더링 (Thymeleaf)
- ✅ D-day 자동 계산 및 색상 분류
- ✅ 진행률 바 동적 표시
- ✅ Thymeleaf 파싱 에러 해결
- ✅ 로그 한글화 및 가독성 개선

---

## 2. 개발 방식

### 2.1 Vertical Slice Architecture

**특징**:
- 기능 단위로 전체 레이어를 관통하는 개발
- 각 기능이 독립적으로 완성되어 즉시 테스트/배포 가능
- 레이어별 개발(Horizontal)보다 빠른 피드백

**적용 순서**:
```
Entity (데이터 모델)
  ↓
Repository (데이터 접근)
  ↓
Service (비즈니스 로직)
  ↓
Controller (요청 처리)
  ↓
View (화면 표시)
```

### 2.2 개발 순서

1. **Entity 레이어**: Program, ProgramStatus 생성
2. **Repository 레이어**: ProgramRepository 생성 (JPQL 쿼리)
3. **Service 레이어**: ProgramService 생성 (비즈니스 로직)
4. **Controller 레이어**: HomeController 수정 (데이터 전달)
5. **View 레이어**: index.html 수정 (동적 렌더링)
6. **Data 레이어**: DataLoader 확장 (샘플 데이터)

---

## 3. Backend 구현

### 3.1 Entity Layer

#### 3.1.1 Program.java

**위치**: `src/main/java/com/scms/app/model/Program.java`

**주요 필드**:
```java
@Entity
@Table(name = "programs")
public class Program {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer programId;

    // 기본 정보
    private String title;                    // 프로그램 제목
    private String description;              // 간단 설명
    private String content;                  // 상세 내용

    // 분류
    private String department;               // 행정부서
    private String college;                  // 단과대학
    private String category;                 // 1차 분류
    private String subCategory;              // 2차 분류

    // 신청 정보
    private LocalDateTime applicationStartDate;  // 신청 시작일
    private LocalDateTime applicationEndDate;    // 신청 종료일
    private Integer maxParticipants;             // 최대 정원
    private Integer currentParticipants;         // 현재 참가자

    // 부가 정보
    private String thumbnailUrl;             // 썸네일 이미지
    private Integer hits;                    // 조회수
    private ProgramStatus status;            // 상태

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;         // Soft Delete
}
```

**비즈니스 메서드**:
```java
// D-day 계산
public Long getDDay() {
    LocalDateTime now = LocalDateTime.now();
    if (now.isAfter(applicationEndDate)) {
        return null;  // 마감된 경우 null
    }
    return Duration.between(now, applicationEndDate).toDays();
}

// 참가율 계산
public Integer getParticipationRate() {
    if (maxParticipants == null || maxParticipants == 0) {
        return 0;
    }
    return (currentParticipants * 100) / maxParticipants;
}

// 신청 가능 여부
public boolean isApplicationAvailable() {
    LocalDateTime now = LocalDateTime.now();
    return status == ProgramStatus.OPEN
        && now.isAfter(applicationStartDate)
        && now.isBefore(applicationEndDate)
        && (maxParticipants == null || currentParticipants < maxParticipants)
        && !isDeleted();
}
```

#### 3.1.2 ProgramStatus.java

**위치**: `src/main/java/com/scms/app/model/ProgramStatus.java`

```java
public enum ProgramStatus {
    SCHEDULED("접수예정"),
    OPEN("접수중"),
    FULL("접수완료"),
    CLOSED("마감");

    private final String description;

    ProgramStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
```

### 3.2 Repository Layer

#### 3.2.1 ProgramRepository.java

**위치**: `src/main/java/com/scms/app/repository/ProgramRepository.java`

**주요 메서드**:
```java
public interface ProgramRepository extends JpaRepository<Program, Integer> {

    // 기본 조회
    @Query("SELECT p FROM Program p WHERE p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    List<Program> findAllNotDeleted();

    @Query("SELECT p FROM Program p WHERE p.programId = :programId AND p.deletedAt IS NULL")
    Optional<Program> findByIdNotDeleted(@Param("programId") Integer programId);

    // 필터 조회
    @Query("SELECT p FROM Program p WHERE p.category = :category AND p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    List<Program> findByCategoryNotDeleted(@Param("category") String category);

    @Query("SELECT p FROM Program p WHERE p.department = :department AND p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    List<Program> findByDepartmentNotDeleted(@Param("department") String department);

    @Query("SELECT p FROM Program p WHERE p.college = :college AND p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    List<Program> findByCollegeNotDeleted(@Param("college") String college);

    // 복합 필터
    @Query("SELECT p FROM Program p WHERE " +
           "(:department IS NULL OR p.department = :department) AND " +
           "(:college IS NULL OR p.college = :college) AND " +
           "(:category IS NULL OR p.category = :category) AND " +
           "p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    List<Program> findByFilters(
        @Param("department") String department,
        @Param("college") String college,
        @Param("category") String category
    );

    // 특수 조회
    @Query("SELECT p FROM Program p WHERE p.status = 'OPEN' " +
           "AND p.applicationStartDate <= :now " +
           "AND p.applicationEndDate > :now " +
           "AND p.deletedAt IS NULL ORDER BY p.applicationEndDate ASC")
    List<Program> findAvailablePrograms(@Param("now") LocalDateTime now);

    @Query("SELECT p FROM Program p WHERE p.deletedAt IS NULL ORDER BY p.hits DESC")
    List<Program> findPopularPrograms();
}
```

**설계 특징**:
- Soft Delete 패턴 적용 (`deletedAt IS NULL`)
- 모든 조회에서 삭제되지 않은 데이터만 반환
- JPQL 사용으로 타입 안정성 보장

### 3.3 Service Layer

#### 3.3.1 ProgramService.java

**위치**: `src/main/java/com/scms/app/service/ProgramService.java`

**주요 메서드**:

```java
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ProgramService {

    private final ProgramRepository programRepository;

    // 조회
    public List<Program> getAllPrograms() {
        return programRepository.findAllNotDeleted();
    }

    public List<Program> getMainPagePrograms() {
        List<Program> programs = programRepository.findAllNotDeleted();
        return programs.stream().limit(8).collect(Collectors.toList());
    }

    public Program getProgram(Integer programId) {
        return programRepository.findByIdNotDeleted(programId)
            .orElseThrow(() -> new IllegalArgumentException(
                "프로그램을 찾을 수 없습니다: ID " + programId));
    }

    // CRUD
    @Transactional
    public Program createProgram(Program program) {
        Program savedProgram = programRepository.save(program);
        log.info("프로그램 생성 완료: {} (ID: {})",
            savedProgram.getTitle(), savedProgram.getProgramId());
        return savedProgram;
    }

    @Transactional
    public Program updateProgram(Integer programId, Program programData) {
        Program program = getProgram(programId);
        // 필드 업데이트 로직
        Program updatedProgram = programRepository.save(program);
        log.info("프로그램 수정 완료: {} (ID: {})",
            updatedProgram.getTitle(), updatedProgram.getProgramId());
        return updatedProgram;
    }

    @Transactional
    public void deleteProgram(Integer programId) {
        Program program = getProgram(programId);
        program.delete();  // Soft Delete
        programRepository.save(program);
        log.info("프로그램 삭제 완료: {} (ID: {})",
            program.getTitle(), program.getProgramId());
    }

    // 참가자 관리
    @Transactional
    public boolean incrementParticipants(Integer programId) {
        Program program = getProgram(programId);
        if (!program.isApplicationAvailable()) {
            log.warn("신청 불가능한 프로그램: {} (ID: {})",
                program.getTitle(), program.getProgramId());
            return false;
        }

        boolean success = program.incrementParticipants();
        if (success) {
            if (program.getMaxParticipants() != null
                && program.getCurrentParticipants() >= program.getMaxParticipants()) {
                program.setStatus(ProgramStatus.FULL);
            }
            programRepository.save(program);
        }
        return success;
    }
}
```

### 3.4 Controller Layer

#### 3.4.1 HomeController.java 수정

**위치**: `src/main/java/com/scms/app/controller/HomeController.java`

**변경 내용**:

```java
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProgramService programService;  // 추가

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        // 세션에서 사용자 정보 가져오기
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId != null) {
            model.addAttribute("userName", session.getAttribute("name"));
            model.addAttribute("userRole", session.getAttribute("role"));
        }

        // 메인 페이지용 프로그램 목록 조회 (최신 8개) - 추가
        List<Program> programs = programService.getMainPagePrograms();
        model.addAttribute("programs", programs);

        model.addAttribute("pageTitle", "푸름대학교 학생성장지원센터 CHAMP");
        return "index";
    }
}
```

---

## 4. Frontend 구현

### 4.1 index.html 동적 렌더링

**위치**: `src/main/resources/templates/index.html`

#### 4.1.1 Before (하드코딩)

```html
<!-- 프로그램 카드 1 -->
<div class="program-card">
    <div class="program-image" style="background: linear-gradient(...);">
        <div class="program-dday urgent">입박</div>
        <div class="program-hits">160 HITS</div>
        📋
    </div>
    <div class="program-content">
        <div class="program-title">[진전] 전공설계 포트폴리오 특강</div>
        ...
    </div>
</div>

<!-- 프로그램 카드 2 -->
<div class="program-card">
    ...
</div>

<!-- ... 8개 반복 ... -->
```

#### 4.1.2 After (동적 렌더링)

```html
<div th:each="program, iterStat : ${programs}" class="program-card">
    <!-- 배경색 설정 -->
    <div class="program-image" th:attr="data-bg-index=${iterStat.index % 8}">
        <!-- D-day 배지 -->
        <div th:if="${program.dDay != null}"
             th:class="${program.dDay <= 2 ? 'program-dday urgent' :
                        program.dDay <= 7 ? 'program-dday blue' :
                        'program-dday green'}">
            <span th:if="${program.dDay == 0}">D-Day</span>
            <span th:if="${program.dDay == 1}">입박</span>
            <span th:if="${program.dDay > 1}" th:text="'D-' + ${program.dDay}"></span>
        </div>
        <div th:if="${program.dDay == null}"
             class="program-dday closed"
             th:text="${program.status.description}"></div>

        <div class="program-favorite">⭐</div>
        <div class="program-hits"><span th:text="${program.hits}"></span> HITS</div>

        <!-- 아이콘 -->
        <span class="program-icon">
            <span th:if="${iterStat.index % 8 == 0}">📋</span>
            <span th:if="${iterStat.index % 8 == 1}">🎭</span>
            <span th:if="${iterStat.index % 8 == 2}">🎓</span>
            <span th:if="${iterStat.index % 8 == 3}">🎯</span>
            <span th:if="${iterStat.index % 8 == 4}">🌟</span>
            <span th:if="${iterStat.index % 8 == 5}">🌈</span>
            <span th:if="${iterStat.index % 8 == 6}">🎨</span>
            <span th:if="${iterStat.index % 8 == 7}">📖</span>
        </span>
    </div>

    <div class="program-content">
        <div class="program-department" th:text="${program.department}"></div>
        <div class="program-title" th:text="${program.title}"></div>
        <div class="program-category"
             th:text="${program.subCategory != null ? program.subCategory : program.category}"></div>
        <div class="program-dates">
            <span th:text="'📅 신청: ' +
                ${#temporals.format(program.applicationStartDate, 'yyyy.MM.dd(E)')} +
                ' ~ ' +
                ${#temporals.format(program.applicationEndDate, 'yyyy.MM.dd(E)')}"></span><br>
            <span th:if="${program.content != null}"
                  th:text="'⏰ 운영: ' +
                    ${#temporals.format(program.applicationStartDate, 'yyyy.MM.dd(E)')} +
                    ' ~ ' +
                    ${#temporals.format(program.applicationEndDate, 'yyyy.MM.dd(E)')}"></span>
        </div>
        <div class="program-progress">
            <div class="progress-bar-container">
                <div class="progress-bar"
                     th:style="'width: ' + ${program.participationRate} + '%;'"></div>
            </div>
            <div class="progress-text">
                <span th:text="${program.currentParticipants}"></span>/<span th:if="${program.maxParticipants != null}" th:text="${program.maxParticipants} + '명'"></span><span th:if="${program.maxParticipants == null}">무제한</span>
            </div>
        </div>
    </div>
</div>
```

### 4.2 CSS 배경색 설정

**추가된 CSS**:

```css
/* 프로그램 카드 배경색 (8가지) */
.program-image[data-bg-index="0"] {
    background: linear-gradient(135deg, #a8e063 0%, #56ab2f 100%);
}

.program-image[data-bg-index="1"] {
    background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.program-image[data-bg-index="2"] {
    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.program-image[data-bg-index="3"] {
    background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.program-image[data-bg-index="4"] {
    background: linear-gradient(135deg, #30cfd0 0%, #330867 100%);
}

.program-image[data-bg-index="5"] {
    background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
}

.program-image[data-bg-index="6"] {
    background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%);
}

.program-image[data-bg-index="7"] {
    background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
}

.program-icon {
    font-size: 48px;
}
```

---

## 5. 테스트 데이터

### 5.1 DataLoader 확장

**위치**: `src/main/java/com/scms/app/config/DataLoader.java`

**추가 내용**:

```java
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataLoader {

    private final UserRepository userRepository;
    private final ProgramRepository programRepository;  // 추가

    @Bean
    public CommandLineRunner loadInitialData() {
        return args -> {
            // 기존 User 데이터 로딩...

            // programs 테이블이 비어있으면 샘플 데이터 생성
            if (programRepository.count() == 0) {
                log.info("=================================================");
                log.info("샘플 프로그램 데이터를 생성합니다...");
                log.info("=================================================");
                createSamplePrograms();
                log.info("=================================================");
                log.info("샘플 프로그램 데이터 8개 생성 완료!");
                log.info("=================================================");
            } else {
                log.info("기존 프로그램 데이터가 있습니다. 샘플 데이터 생성을 건너뜁니다.");
            }
        };
    }
}
```

### 5.2 샘플 프로그램 8개

| # | 프로그램명 | D-day | 상태 | 정원 | 현재 | 조회수 |
|---|-----------|-------|------|------|------|--------|
| 1 | [진전] 전공설계 포트폴리오 특강 | D-1 (입박) | OPEN | 무제한 | 0 | 160 |
| 2 | 2025-2 나스너 대인관계능력 향상 | D-3 | OPEN | 무제한 | 6 | 172 |
| 3 | 면접스피치&이미지메이킹 과정 | D-4 | OPEN | 55명 | 20 | 198 |
| 4 | 동기역량강화 집단 학습컨설팅 | D-4 | OPEN | 8명 | 5 | 31 |
| 5 | 세대공감 '인생책 함께읽기' | 마감 | CLOSED | 무제한 | 15 | 94 |
| 6 | 인슐린 작용 이해 | - | FULL | 30명 | 30 | 90 |
| 7 | 우석챔프 시즌제 장학금 | D-24 | OPEN | 무제한 | 160 | 713 |
| 8 | Academic Advising | D-35 | OPEN | 무제한 | 432 | 398 |

---

## 6. 버그 수정

### 6.1 Thymeleaf 파싱 에러

#### 문제

**에러 메시지**:
```
Could not parse as expression: "'background: ' + ${#lists.contains({0,1,2,3,4,5,6,7},
iterStat.index % 8) ? (iterStat.index % 8 == 0 ? 'linear-gradient(...)' : ...) : ...};"
```

**원인**: Thymeleaf에서 복잡한 중첩 삼항 연산자를 파싱하지 못함

#### 해결 방법

**Before (인라인 조건문)**:
```html
<div class="program-image"
     th:style="'background: ' + ${복잡한 조건식};">
```

**After (CSS 속성 선택자)**:
```html
<!-- HTML -->
<div class="program-image" th:attr="data-bg-index=${iterStat.index % 8}">

<!-- CSS -->
.program-image[data-bg-index="0"] {
    background: linear-gradient(135deg, #a8e063 0%, #56ab2f 100%);
}
```

**장점**:
- Thymeleaf 표현식 단순화
- CSS에서 스타일 관리 (관심사 분리)
- 유지보수 용이

### 6.2 로그 한글화

#### Before

```
log.info("Creating sample program data...");
log.info("Program created: {} (ID: {})", title, id);
log.info("Sample program data loading completed!");
```

#### After

```
log.info("=================================================");
log.info("샘플 프로그램 데이터를 생성합니다...");
log.info("=================================================");
log.info("✅ 프로그램 생성: {} (신청 마감: {})", title, endDate);
log.info("=================================================");
log.info("샘플 프로그램 데이터 8개 생성 완료!");
log.info("=================================================");
```

**개선 사항**:
- 모든 로그 메시지 한글화
- 시각적 구분선(===) 추가
- 체크마크(✅) 추가
- 프로그램 개수 명시 (8개)
- 관리자 비밀번호 로그 출력 (편의성)

---

## 7. 커밋 히스토리

### Commit 1: Program Entity 및 통합

**커밋 해시**: `9c22ee3`
**메시지**: `Implement Program entity and integrate with homepage`

**변경 사항**:
- ✅ Program.java 엔티티 생성
- ✅ ProgramStatus.java enum 생성
- ✅ ProgramRepository.java 생성
- ✅ ProgramService.java 생성
- ✅ HomeController.java 수정 (ProgramService 주입)
- ✅ index.html 수정 (동적 렌더링)

**파일 변경**:
```
6 files changed, 580 insertions(+), 187 deletions(-)
 create mode 100644 src/main/java/com/scms/app/model/Program.java
 create mode 100644 src/main/java/com/scms/app/model/ProgramStatus.java
 create mode 100644 src/main/java/com/scms/app/repository/ProgramRepository.java
 create mode 100644 src/main/java/com/scms/app/service/ProgramService.java
```

### Commit 2: 샘플 데이터 로더

**커밋 해시**: `c1da9e5`
**메시지**: `Add sample program data loader`

**변경 사항**:
- ✅ DataLoader.java 확장
- ✅ 8개 샘플 프로그램 생성 로직 추가
- ✅ 다양한 D-day 및 상태 데이터

**파일 변경**:
```
1 file changed, 161 insertions(+)
```

### Commit 3: 버그 수정 및 개선

**커밋 해시**: `7f3af81`
**메시지**: `Fix Thymeleaf parsing error and improve logging`

**변경 사항**:
- ✅ Thymeleaf 파싱 에러 수정
- ✅ CSS 속성 선택자 사용
- ✅ 로그 한글화
- ✅ 로그 가독성 개선

**파일 변경**:
```
2 files changed, 76 insertions(+), 35 deletions(-)
```

---

## 8. 실행 및 확인

### 8.1 사전 준비

**MySQL 데이터베이스 생성**:
```sql
CREATE DATABASE scms2 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

**설정 파일 확인**: `src/main/resources/application.yml`
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/scms2?...
    username: root
    password: password
```

### 8.2 실행

```bash
./gradlew bootRun
```

### 8.3 로그 확인

**예상 로그**:
```
=================================================
초기 사용자 데이터를 생성합니다...
=================================================
✅ 학생 계정 생성: 김철수 (학번: 2024001, 초기 비밀번호: 030101)
✅ 학생 계정 생성: 이영희 (학번: 2024002, 초기 비밀번호: 040215)
...
✅ 관리자 계정 생성: 시스템관리자 (학번: 9999999, 비밀번호: admin123)
=================================================
초기 사용자 데이터 생성 완료!
=================================================

=================================================
샘플 프로그램 데이터를 생성합니다...
=================================================
✅ 프로그램 생성: [진전] 전공설계 포트폴리오 특강 (신청 마감: 2025-11-08)
✅ 프로그램 생성: 2025-2 나스너(WOW-더플 대인관계능력 향상 프로그램) (신청 마감: 2025-11-10)
✅ 프로그램 생성: 2025년 2학기 면접스피치&이미지메이킹 과정 (신청 마감: 2025-11-11)
✅ 프로그램 생성: 동기역량강화 집단 학습컨설팅(2025-2학기) (신청 마감: 2025-11-11)
✅ 프로그램 생성: 세대공감 '인생책 함께읽기' (신청 마감: 2025-11-06)
✅ 프로그램 생성: 우리 몸의 닮 조절을 위한 인슐린 작용 이해 (신청 마감: 2025-11-28)
✅ 프로그램 생성: 2025학년도 2학기 우석챔프 시즌제 장학금 (신청 마감: 2025-12-01)
✅ 프로그램 생성: [전주] 2025학년도 1학기 찾아가는 Academic Advising (신청 마감: 2025-12-12)
=================================================
샘플 프로그램 데이터 8개 생성 완료!
=================================================
```

### 8.4 브라우저 확인

**URL**: http://localhost:8080

**확인 포인트**:

1. **✅ 8개 프로그램 카드 표시**
   - DB에서 가져온 동적 데이터

2. **✅ D-day 배지 색상**
   - 🟣 보라색 (urgent): D-2 이하 (입박)
   - 🔵 파란색 (blue): D-3 ~ D-7
   - 🟢 초록색 (green): D-8 이상
   - ⚫ 회색 (closed): 마감/모집완료

3. **✅ HITS 조회수**
   - 160 HITS, 172 HITS, 398 HITS, 713 HITS 등

4. **✅ 진행률 바**
   - 0/무제한
   - 20/55명 (36% 진행률 바)
   - 5/8명 (62% 진행률 바)
   - 30/30명 (100% 진행률 바)

5. **✅ 8가지 배경색**
   - 그라데이션 배경이 각각 다른 색상

6. **✅ 프로그램 정보**
   - 제목, 부서명, 카테고리
   - 신청 기간 날짜 표시

### 8.5 데이터베이스 확인

```bash
mysql -u root -p scms2

# 프로그램 확인
SELECT program_id, title, status, hits, current_participants, max_participants
FROM programs;

# 8개 프로그램 개수 확인
SELECT COUNT(*) FROM programs;  -- 결과: 8
```

---

## 9. 다음 단계

### 9.1 우선순위 순서

1. ✅ **완료**: 프로그램 Entity 및 샘플 데이터
2. 🔜 **다음**: 프로그램 관리 기능 (관리자용 CRUD)
3. 🔜 **예정**: 프로그램 상세 페이지
4. 🔜 **예정**: 프로그램 신청 기능

### 9.2 상세 계획

#### Step 2: 프로그램 관리 기능

**목표**: 관리자가 프로그램을 등록/수정/삭제할 수 있는 기능

**구현 내용**:
- Admin용 프로그램 목록 페이지 (`/admin/programs`)
- 프로그램 등록 폼 (`/admin/programs/new`)
- 프로그램 수정 폼 (`/admin/programs/{id}/edit`)
- 프로그램 삭제 기능
- REST API 엔드포인트

**예상 파일**:
- `ProgramAdminController.java` (신규)
- `admin/program-list.html` (신규)
- `admin/program-form.html` (신규)
- `ProgramDTO.java` (신규)

#### Step 3: 프로그램 상세 페이지

**목표**: 프로그램 카드 클릭 시 상세 정보 표시

**구현 내용**:
- 프로그램 상세 페이지 (`/programs/{id}`)
- 조회수 자동 증가
- 신청 버튼 표시
- 참가자 정보 표시

**예상 파일**:
- `ProgramController.java` (신규)
- `program-detail.html` (신규)

#### Step 4: 프로그램 신청 기능

**목표**: 학생이 프로그램에 신청할 수 있는 기능

**구현 내용**:
- ProgramApplication Entity 생성
- 신청 API 엔드포인트
- 중복 신청 방지
- 정원 초과 방지
- 신청 내역 조회

**예상 파일**:
- `ProgramApplication.java` (신규)
- `ProgramApplicationRepository.java` (신규)
- `ProgramApplicationService.java` (신규)
- `ProgramApplicationController.java` (신규)

---

## 10. 배운 점 및 개선 사항

### 10.1 기술적 학습

#### Thymeleaf 복잡도 관리
- ❌ **문제**: 복잡한 인라인 조건문은 파싱 에러 유발
- ✅ **해결**: CSS 속성 선택자로 스타일 분리
- 💡 **교훈**: 관심사 분리(Separation of Concerns) 원칙 준수

#### Vertical Slice 아키텍처
- ✅ **장점**: 빠른 피드백, 독립적 배포 가능
- ✅ **효과**: 전체 기능을 한 번에 확인 가능
- 💡 **교훈**: 수평적 레이어 개발보다 효율적

### 10.2 개발자 경험 개선

#### 로그 가독성
- ✅ 한글 로그로 이해도 향상
- ✅ 시각적 구분선으로 가독성 향상
- ✅ 체크마크로 성공 표시
- 💡 **교훈**: 로그 품질이 디버깅 효율성에 큰 영향

#### 샘플 데이터 자동화
- ✅ 테스트 시간 단축
- ✅ 일관된 테스트 환경
- ✅ 신규 개발자 온보딩 용이
- 💡 **교훈**: 초기 설정 자동화의 중요성

### 10.3 코드 품질

#### 설계 패턴 적용
- ✅ Repository 패턴
- ✅ Service 레이어 분리
- ✅ DTO 사용 (향후 적용 예정)
- ✅ Soft Delete 패턴
- ✅ Builder 패턴

#### 예외 처리
- ✅ 명확한 예외 메시지
- ✅ 로그로 예외 추적
- ⚠️ **개선 필요**: 전역 예외 핸들러 적용

---

## 부록

### A. 관련 문서

- [01_PROJECT_OVERVIEW.md](./01_PROJECT_OVERVIEW.md) - 프로젝트 개요
- [02_DEVELOPMENT_LOG.md](./02_DEVELOPMENT_LOG.md) - 개발 로그
- [03_IMPLEMENTATION_SUMMARY.md](./03_IMPLEMENTATION_SUMMARY.md) - 구현 요약

### B. 참고 자료

- [Spring Data JPA 공식 문서](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Thymeleaf 공식 문서](https://www.thymeleaf.org/documentation.html)
- [Vertical Slice Architecture](https://www.jimmybogard.com/vertical-slice-architecture/)

### C. 주요 파일 경로

```
src/main/java/com/scms/app/
├── model/
│   ├── Program.java                 (172줄)
│   └── ProgramStatus.java           (21줄)
├── repository/
│   └── ProgramRepository.java       (99줄)
├── service/
│   └── ProgramService.java          (237줄)
├── controller/
│   └── HomeController.java          (수정, +12줄)
└── config/
    └── DataLoader.java              (수정, +173줄)

src/main/resources/templates/
└── index.html                       (수정, +68줄, -187줄)
```

---

**문서 작성**: Claude Code
**최종 수정**: 2025-11-07
**버전**: 1.0
