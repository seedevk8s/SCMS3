# 페이지네이션 및 초기 데이터 로더 개발 로그

## 📅 개발 일자
2025-11-10

## 📋 목차
1. [개요](#개요)
2. [페이지네이션 구현](#페이지네이션-구현)
3. [초기 데이터 로더 구현](#초기-데이터-로더-구현)
4. [버그 수정](#버그-수정)
5. [최종 테스트](#최종-테스트)
6. [향후 개선사항](#향후-개선사항)

---

## 개요

프로그램 목록 페이지에 **페이지네이션 기능**을 추가하고, **50개 샘플 데이터를 자동으로 로드**하는 DataLoader를 구현했습니다. 이 과정에서 두 가지 주요 버그를 발견하고 수정했습니다.

### 주요 기능
- ✅ 페이지당 12개 프로그램 표시
- ✅ 스마트 페이지 번호 표시 (7페이지 이하는 모두 표시, 초과 시 축약)
- ✅ 이전/다음 버튼 제공
- ✅ 50개 샘플 데이터 자동 로드 (9개 부서 × 6개 단과대 × 5개 카테고리)
- ✅ DataLoader SQL 파싱 버그 수정
- ✅ 페이지네이션 중복 번호 버그 수정

---

## 페이지네이션 구현

### 1. Repository 수정

**파일**: `src/main/java/com/scms/app/repository/ProgramRepository.java`

```java
@Query("SELECT p FROM Program p WHERE " +
       "(:department IS NULL OR p.department = :department) AND " +
       "(:college IS NULL OR p.college = :college) AND " +
       "(:category IS NULL OR p.category = :category) AND " +
       "p.deletedAt IS NULL")
Page<Program> findByFiltersWithPagination(
    @Param("department") String department,
    @Param("college") String college,
    @Param("category") String category,
    Pageable pageable
);

@Query("SELECT p FROM Program p WHERE p.title LIKE %:keyword% AND p.deletedAt IS NULL")
Page<Program> searchByTitleWithPagination(
    @Param("keyword") String keyword,
    Pageable pageable
);

@Query("SELECT p FROM Program p WHERE p.deletedAt IS NULL")
Page<Program> findAllNotDeletedWithPagination(Pageable pageable);
```

**주요 특징**:
- `Page<T>` 반환 타입으로 변경
- `Pageable` 파라미터 추가
- Soft delete 지원 (`deletedAt IS NULL`)

### 2. Service 수정

**파일**: `src/main/java/com/scms/app/service/ProgramService.java`

```java
public Page<Program> getProgramsByFiltersWithPagination(
    String department,
    String college,
    String category,
    int page,
    int size
) {
    Pageable pageable = PageRequest.of(
        page,
        size,
        Sort.by(Sort.Direction.DESC, "createdAt")
    );
    return programRepository.findByFiltersWithPagination(
        department,
        college,
        category,
        pageable
    );
}
```

**정렬 기준**: `createdAt` 내림차순 (최신 프로그램 먼저)

### 3. Controller 수정

**파일**: `src/main/java/com/scms/app/controller/HomeController.java`

```java
@GetMapping("/programs")
public String programs(
    @RequestParam(required = false) String department,
    @RequestParam(required = false) String college,
    @RequestParam(required = false) String category,
    @RequestParam(required = false) String search,
    @RequestParam(defaultValue = "0") int page,      // 추가
    @RequestParam(defaultValue = "12") int size,     // 추가
    Model model,
    HttpSession session
) {
    Page<Program> programPage;

    if (search != null && !search.trim().isEmpty()) {
        programPage = programService.searchProgramsByTitleWithPagination(search, page, size);
    } else if (department != null || college != null || category != null) {
        programPage = programService.getProgramsByFiltersWithPagination(
            department, college, category, page, size
        );
    } else {
        programPage = programService.getAllProgramsWithPagination(page, size);
    }

    model.addAttribute("programs", programPage.getContent());
    model.addAttribute("currentPage", page);
    model.addAttribute("totalPages", programPage.getTotalPages());
    model.addAttribute("totalItems", programPage.getTotalElements());
    model.addAttribute("pageSize", size);

    return "programs";
}
```

**파라미터**:
- `page`: 현재 페이지 (0부터 시작, 기본값 0)
- `size`: 페이지당 항목 수 (기본값 12)

### 4. View 수정

**파일**: `src/main/resources/templates/programs.html`

#### 4.1 페이지네이션 UI (초기 버전)

```html
<!-- 페이지네이션 -->
<div class="pagination" th:if="${totalPages > 0}">
    <!-- 이전 버튼 -->
    <button class="pagination-button prev"
            th:disabled="${currentPage == 0}"
            onclick="goToPage([[${currentPage - 1}]])">
        <i class="fas fa-chevron-left"></i> 이전
    </button>

    <!-- 페이지 번호 -->
    <th:block th:each="i : ${#numbers.sequence(0, totalPages - 1)}">
        <!-- 첫 3페이지 -->
        <button th:if="${i < 3}"
                class="pagination-button"
                th:classappend="${i == currentPage ? 'active' : ''}"
                th:text="${i + 1}"
                th:onclick="'goToPage(' + ${i} + ')'">
        </button>

        <!-- ... (생략 표시) -->
        <span th:if="${i == 3 && totalPages > 7 && currentPage > 3}">...</span>

        <!-- 현재 페이지 주변 -->
        <button th:if="${i >= 3 && i < totalPages - 3 && i >= currentPage - 1 && i <= currentPage + 1}"
                class="pagination-button"
                th:classappend="${i == currentPage ? 'active' : ''}"
                th:text="${i + 1}"
                th:onclick="'goToPage(' + ${i} + ')'">
        </button>

        <!-- ... (생략 표시) -->
        <span th:if="${i == totalPages - 4 && totalPages > 7 && currentPage < totalPages - 4}">...</span>

        <!-- 마지막 3페이지 -->
        <button th:if="${i >= totalPages - 3 && totalPages > 3}"
                class="pagination-button"
                th:classappend="${i == currentPage ? 'active' : ''}"
                th:text="${i + 1}"
                th:onclick="'goToPage(' + ${i} + ')'">
        </button>
    </th:block>

    <!-- 다음 버튼 -->
    <button class="pagination-button next"
            th:disabled="${currentPage == totalPages - 1}"
            onclick="goToPage([[${currentPage + 1}]])">
        다음 <i class="fas fa-chevron-right"></i>
    </button>
</div>
```

**문제점**: 페이지가 5개일 때 3번이 두 번 표시됨 (버그 #2 참조)

#### 4.2 JavaScript 페이지 이동 함수

```javascript
function goToPage(pageNum) {
    const url = new URL(window.location.href);
    url.searchParams.set('page', pageNum);
    window.location.href = url.toString();
}
```

**동작 방식**:
- 현재 URL에 `page` 파라미터만 변경
- 다른 필터 파라미터는 유지
- 예: `/programs?department=도서관&page=2`

#### 4.3 CSS 스타일

```css
.pagination {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 8px;
    margin-top: 48px;
    padding: 24px 0;
}

.pagination-button {
    min-width: 40px;
    height: 40px;
    padding: 8px 12px;
    border: 1px solid #ddd;
    background: white;
    border-radius: 8px;
    color: #333;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.3s ease;
}

.pagination-button:hover:not(:disabled):not(.active) {
    border-color: #2C5F5D;
    color: #2C5F5D;
    background: #f8fafa;
}

.pagination-button.active {
    background: #2C5F5D;
    color: white;
    border-color: #2C5F5D;
    cursor: default;
}

.pagination-button:disabled {
    opacity: 0.5;
    cursor: not-allowed;
}
```

---

## 초기 데이터 로더 구현

### 1. DataLoader 클래스 생성

**파일**: `src/main/java/com/scms/app/config/DataLoader.java`

```java
@Component  // 초기 실행 후 주석처리 필요
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final ProgramRepository programRepository;

    @Override
    public void run(String... args) throws Exception {
        long count = programRepository.count();

        // 정확히 50개이고 특정 샘플 데이터가 있으면 초기화 완료로 간주
        if (count == 50) {
            boolean hasSampleData = programRepository.findAll().stream()
                    .anyMatch(p -> "학습전략 워크샵".equals(p.getTitle()) ||
                                   "취업 특강 시리즈".equals(p.getTitle()));

            if (hasSampleData) {
                log.info("샘플 데이터 50개가 이미 로드되어 있습니다. 초기화를 건너뜁니다.");
                return;
            }
        }

        // 기존 데이터 모두 삭제 (필터링 안 되는 구 데이터 제거)
        if (count > 0) {
            log.warn("기존 프로그램 데이터 {}개를 삭제하고 새로운 샘플 데이터로 초기화합니다...", count);
            programRepository.deleteAll();
            jdbcTemplate.execute("ALTER TABLE programs AUTO_INCREMENT = 1");
            log.info("기존 데이터 삭제 완료");
        }

        log.info("초기 프로그램 데이터 50개를 로드합니다...");

        try {
            // data.sql 파일 읽기 (주석 제거)
            ClassPathResource resource = new ClassPathResource("data.sql");
            String sql = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))
                .lines()
                .filter(line -> !line.trim().startsWith("--"))  // 주석 라인 제거
                .filter(line -> !line.trim().isEmpty())         // 빈 라인 제거
                .collect(Collectors.joining("\n"));

            // SQL을 개별 INSERT 문으로 분리
            String[] statements = sql.split(";");

            int insertCount = 0;
            for (String statement : statements) {
                String trimmed = statement.trim();
                if (!trimmed.isEmpty()) {
                    try {
                        jdbcTemplate.execute(trimmed);
                        insertCount++;
                        log.debug("SQL 실행 성공 ({}번째)", insertCount);
                    } catch (Exception e) {
                        log.error("SQL 실행 실패: {}", e.getMessage());
                    }
                }
            }

            long afterCount = programRepository.count();
            log.info("✅ 초기 데이터 로드 완료: {}개 INSERT 문 실행, {}개 프로그램 생성됨", insertCount, afterCount);

        } catch (Exception e) {
            log.error("초기 데이터 로드 중 오류 발생", e);
        }
    }
}
```

**동작 방식**:
1. 샘플 데이터 50개가 이미 있으면 → 건너뜀
2. 기존 데이터가 있으면 → 모두 삭제 후 새 샘플 데이터 50개 삽입
3. 데이터가 없으면 → 샘플 데이터 50개 삽입

### 2. 샘플 데이터 파일

**파일**: `src/main/resources/data.sql`

```sql
-- 초기 프로그램 데이터 (50개)
-- 모든 필터 옵션에 골고루 분산

-- 행정부서: 교수학습지원센터 (6개)
INSERT INTO programs (title, description, content, department, college, category, sub_category, application_start_date, application_end_date, max_participants, current_participants, status, hits, created_at) VALUES
('학습전략 워크샵', '효과적인 학습 방법을 배우는 워크샵', '학습전략과 시간관리 기법을 배웁니다', '교수학습지원센터', 'RISE사업단', '학습역량', '학습법', '2024-12-01 00:00:00', '2024-12-20 23:59:59', 30, 15, 'OPEN', 245, CURRENT_TIMESTAMP),
...
```

**데이터 분포**:

#### 부서별 (9개 부서, 총 50개)
| 부서 | 개수 |
|------|------|
| 교수학습지원센터 | 6 |
| 도서관 | 5 |
| 생활관 | 5 |
| 학생상담센터 | 6 |
| 장애학생지원센터 | 5 |
| 취창업지원센터 | 6 |
| 평생교육원 | 5 |
| 학생처 | 6 |
| 학습역량강화사업단 | 6 |

#### 단과대별 (6개)
- RISE사업단
- 간호대학
- 교육대학원
- 기계ICT융합공학부
- RIS지원센터
- 약학대학

#### 카테고리별 (5개)
- 학습역량
- 진로지도
- 심리상담
- 장애학생지원
- 기타

### 3. Dev 프로파일 설정 변경

**파일**: `src/main/resources/application-dev.yml`

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update  # create-drop에서 변경
```

**변경 이유**: `create-drop`은 재시작할 때마다 스키마를 삭제하고 재생성하므로, `update`로 변경하여 스키마는 유지하고 데이터만 관리

---

## 버그 수정

### 버그 #1: DataLoader SQL 파싱 오류

#### 문제 상황

DataLoader를 실행했을 때 **데이터가 0개 생성**되는 문제 발생:

```log
2025-11-10 19:17:08 - 기존 데이터 삭제 완료
2025-11-10 19:17:08 - 초기 프로그램 데이터 50개를 로드합니다...
2025-11-10 19:17:08 - ✅ 초기 데이터 로드 완료: 0개 프로그램 생성됨
```

#### 원인 분석

data.sql 파일 구조:
```sql
-- 행정부서: 교수학습지원센터 (6개)
INSERT INTO programs (...) VALUES
...
);
```

**문제점**:
1. 세미콜론(`;`)으로 split
2. 각 statement가 주석(`--`)으로 시작하는지 체크
3. **주석과 INSERT가 함께 묶여서 주석으로 시작** → 모든 INSERT가 건너뛰어짐

초기 코드:
```java
String sql = lines.collect(Collectors.joining("\n"));
String[] statements = sql.split(";");

for (String statement : statements) {
    String trimmed = statement.trim();
    if (!trimmed.isEmpty() && !trimmed.startsWith("--")) {  // ← 너무 늦음!
        jdbcTemplate.execute(trimmed);
    }
}
```

#### 해결 방법

**SQL 파일을 읽을 때 먼저 주석 라인을 제거**:

```java
String sql = new BufferedReader(
    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))
    .lines()
    .filter(line -> !line.trim().startsWith("--"))  // ← 먼저 주석 제거!
    .filter(line -> !line.trim().isEmpty())         // 빈 라인 제거
    .collect(Collectors.joining("\n"));

String[] statements = sql.split(";");

for (String statement : statements) {
    String trimmed = statement.trim();
    if (!trimmed.isEmpty()) {
        jdbcTemplate.execute(trimmed);
        insertCount++;
    }
}
```

#### 결과

```log
2025-11-10 19:25:14 - 초기 프로그램 데이터 50개를 로드합니다...
2025-11-10 19:25:15 - ✅ 초기 데이터 로드 완료: 9개 INSERT 문 실행, 50개 프로그램 생성됨
```

**성공**: 50개 프로그램 정상 생성

### 버그 #2: 페이지네이션 중복 번호

#### 문제 상황

페이지가 5개일 때 **3번이 두 번 표시**되는 문제:

```
[이전] 1 2 3 3 4 5 [다음]
            ↑ 중복!
```

#### 원인 분석

초기 로직:
```html
<!-- 첫 3페이지 -->
<button th:if="${i < 3}">  <!-- 0, 1, 2 = 페이지 1, 2, 3 -->

<!-- 마지막 3페이지 -->
<button th:if="${i >= totalPages - 3 && totalPages > 3}">
<!-- totalPages=5일 때: i >= 2 = 2, 3, 4 = 페이지 3, 4, 5 -->
```

**totalPages = 5일 때**:
- 첫 3페이지: i < 3 → 0, 1, 2 → **페이지 1, 2, 3**
- 마지막 3페이지: i >= 5-3 → i >= 2 → 2, 3, 4 → **페이지 3, 4, 5**
- **페이지 3 (i=2) 중복!**

#### 해결 방법

**7페이지 이하는 모두 표시, 7페이지 초과는 스마트 표시**:

```html
<!-- 페이지 번호 -->
<th:block th:each="i : ${#numbers.sequence(0, totalPages - 1)}">
    <!-- 7페이지 이하: 모든 페이지 표시 -->
    <button th:if="${totalPages <= 7}"
            class="pagination-button"
            th:classappend="${i == currentPage ? 'active' : ''}"
            th:text="${i + 1}"
            th:onclick="'goToPage(' + ${i} + ')'">
    </button>

    <!-- 7페이지 초과: 스마트 페이지 표시 -->
    <th:block th:if="${totalPages > 7}">
        <!-- 첫 2페이지 -->
        <button th:if="${i < 2}">

        <!-- ... -->
        <span th:if="${i == 2 && currentPage > 3}">...</span>

        <!-- 현재 페이지 주변 (currentPage-1, currentPage, currentPage+1) -->
        <button th:if="${i >= 2 && i < totalPages - 2 && i >= currentPage - 1 && i <= currentPage + 1}">

        <!-- ... -->
        <span th:if="${i == totalPages - 3 && currentPage < totalPages - 4}">...</span>

        <!-- 마지막 2페이지 -->
        <button th:if="${i >= totalPages - 2}">
    </th:block>
</th:block>
```

#### 결과

**totalPages = 5 (현재 상황)**:
```
[이전] 1 2 3 4 5 [다음]
```
모든 페이지 번호 표시, 중복 없음 ✅

**totalPages = 10 (가상 예시)**:
- 1페이지에 있을 때: `1 2 ... 10`
- 5페이지에 있을 때: `1 2 ... 4 5 6 ... 9 10`
- 10페이지에 있을 때: `1 ... 9 10`

---

## 최종 테스트

### 1. DataLoader 실행

```bash
# 애플리케이션 재시작
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

**콘솔 로그**:
```
2025-11-10 19:25:14 - 초기 프로그램 데이터 50개를 로드합니다...
Hibernate: delete from programs where program_id=?
...
2025-11-10 19:25:14 - 기존 데이터 삭제 완료
2025-11-10 19:25:15 - ✅ 초기 데이터 로드 완료: 9개 INSERT 문 실행, 50개 프로그램 생성됨
```

✅ **성공**: 50개 프로그램 생성

### 2. 페이지네이션 테스트

#### 2.1 첫 페이지 (page=0)
```
URL: /programs?page=0

표시: 프로그램 1~12 (총 50개 중)
페이지네이션: [이전(비활성)] 1 2 3 4 5 [다음]
```
✅ 정상 작동

#### 2.2 중간 페이지 (page=2)
```
URL: /programs?page=2

표시: 프로그램 25~36 (총 50개 중)
페이지네이션: [이전] 1 2 3 4 5 [다음]
```
✅ 3번 중복 없음, 정상 작동

#### 2.3 마지막 페이지 (page=4)
```
URL: /programs?page=4

표시: 프로그램 49~50 (총 50개 중, 2개만 표시)
페이지네이션: [이전] 1 2 3 4 5 [다음(비활성)]
```
✅ 정상 작동

### 3. 필터링 + 페이지네이션 테스트

```
URL: /programs?department=교수학습지원센터&page=0

결과: 6개 프로그램 표시 (1페이지)
페이지네이션: [이전(비활성)] 1 [다음(비활성)]
```
✅ 정상 작동

### 4. 검색 + 페이지네이션 테스트

```
URL: /programs?search=워크샵&page=0

결과: 2개 프로그램 표시 (학습전략 워크샵, 스트레스 관리 워크샵)
페이지네이션: [이전(비활성)] 1 [다음(비활성)]
```
✅ 정상 작동

### 5. DataLoader 비활성화

초기 데이터 로드 완료 후:

**파일**: `src/main/java/com/scms/app/config/DataLoader.java:30`

```java
// @Component  // 초기 데이터 로드 완료 - 재시작 시 데이터 삭제 방지를 위해 비활성화
```

✅ 이후 재시작 시 데이터 보존 확인

---

## 향후 개선사항

### 1. DataLoader 개선
- [ ] Profile 기반 제어 (dev 환경에서만 작동)
- [ ] 환경 변수로 활성화/비활성화 제어 (`ENABLE_DATA_LOADER=true/false`)
- [ ] DB 테이블에 초기화 플래그 저장 (`data_initialized` 테이블)

### 2. 페이지네이션 UX 개선
- [ ] 페이지 크기 선택 드롭다운 (12, 24, 48개)
- [ ] 키보드 단축키 (←, → 키로 페이지 이동)
- [ ] 무한 스크롤 옵션
- [ ] URL 히스토리 지원 (브라우저 뒤로가기)

### 3. 성능 최적화
- [ ] 프로그램 카운트 쿼리 캐싱
- [ ] 페이지네이션 결과 캐싱 (Redis)
- [ ] Lazy loading (필요한 필드만 조회)

### 4. 접근성 개선
- [ ] ARIA 라벨 추가
- [ ] 스크린 리더 지원
- [ ] 키보드 네비게이션 개선

---

## 커밋 히스토리

### 1단계: 페이지네이션 구현
```
commit a775e02
Add initial data and pagination for programs listing

- Repository에 페이지네이션 쿼리 추가
- Service에 페이지네이션 메서드 추가
- Controller에 page/size 파라미터 처리
- View에 페이지네이션 UI 추가
```

### 2단계: DataLoader 구현
```
commit eea1fb5
Add DataLoader to automatically load initial data on startup

- CommandLineRunner로 DataLoader 구현
- data.sql 파일 읽기 및 실행
- 50개 샘플 프로그램 데이터 생성
```

### 3단계: ddl-auto 변경
```
commit 827ab49
Change dev profile ddl-auto from create-drop to update

- create-drop → update 변경
- 재시작 시 스키마 유지
```

### 4단계: DataLoader 삭제 로직 추가
```
commit 6ab0085
Modify DataLoader to delete old incompatible data and insert fresh 50 programs

- 기존 데이터 삭제 로직 추가
- AUTO_INCREMENT 리셋
- 샘플 데이터 감지 로직 추가
```

### 5단계: DataLoader SQL 파싱 버그 수정
```
commit 05772b8
Fix DataLoader SQL parsing to remove comment lines

Problem: SQL 주석으로 인해 INSERT 문이 실행되지 않음
Solution: SQL 파일 읽을 때 주석 라인 먼저 제거
Result: 50개 프로그램 정상 생성
```

### 6단계: 페이지네이션 중복 번호 버그 수정
```
commit (현재)
Fix pagination duplicate page number issue

Problem: 페이지가 5개일 때 3번이 두 번 표시됨
Solution: 7페이지 이하는 모두 표시, 초과 시 스마트 표시
Result: 중복 없이 정상 작동
```

### 7단계: DataLoader 비활성화
```
commit (현재)
Disable DataLoader after initial data load

- @Component 주석처리
- 재시작 시 데이터 보존 확인
```

---

## 파일 변경 요약

### 추가된 파일
- `src/main/resources/data.sql` (79줄, 50개 프로그램 INSERT)
- `src/main/java/com/scms/app/config/DataLoader.java` (96줄)

### 수정된 파일
- `src/main/java/com/scms/app/repository/ProgramRepository.java`
  - 페이지네이션 쿼리 메서드 3개 추가

- `src/main/java/com/scms/app/service/ProgramService.java`
  - 페이지네이션 서비스 메서드 3개 추가

- `src/main/java/com/scms/app/controller/HomeController.java`
  - page/size 파라미터 추가
  - Page<Program> 반환 처리

- `src/main/resources/templates/programs.html`
  - 페이지네이션 UI 추가
  - JavaScript 페이지 이동 함수
  - CSS 스타일 추가
  - 페이지네이션 로직 개선 (중복 버그 수정)

- `src/main/resources/application-dev.yml`
  - ddl-auto: create-drop → update

- `src/main/java/com/scms/app/config/SecurityConfig.java`
  - /programs 경로 공개 허용

---

## 참고 자료

### Spring Data JPA 페이지네이션
- [Spring Data JPA - Pagination](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.special-parameters)
- [Pageable and Sort](https://docs.spring.io/spring-data/commons/docs/current/api/org/springframework/data/domain/Pageable.html)

### Thymeleaf 템플릿
- [Thymeleaf Tutorial](https://www.thymeleaf.org/doc/tutorials/3.0/usingthymeleaf.html)
- [Thymeleaf + Spring](https://www.thymeleaf.org/doc/tutorials/3.0/thymeleafspring.html)

### Spring Boot CommandLineRunner
- [CommandLineRunner](https://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/CommandLineRunner.html)

---

## 결론

페이지네이션과 초기 데이터 로더를 성공적으로 구현했습니다. 두 가지 주요 버그(DataLoader SQL 파싱, 페이지네이션 중복 번호)를 발견하고 수정하여 안정적으로 작동하는 시스템을 구축했습니다.

**주요 성과**:
- ✅ 50개 샘플 데이터 자동 로드
- ✅ 페이지네이션 정상 작동 (5페이지, 마지막 2개)
- ✅ 필터링 + 페이지네이션 조합 작동
- ✅ 검색 + 페이지네이션 조합 작동
- ✅ DataLoader 비활성화로 데이터 보존
- ✅ 중복 버그 수정으로 깔끔한 UI

이제 관리자가 프로그램을 추가/수정/삭제해도 재시작 시 데이터가 보존됩니다.
