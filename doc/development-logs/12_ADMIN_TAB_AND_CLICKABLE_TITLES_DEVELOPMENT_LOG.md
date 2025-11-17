# 관리자 탭 표시 및 프로그램 제목 클릭 기능 개발 로그

## 📅 작성일
2025-11-17

## 👤 작업자
Claude (세션 재개 - 새 PC 환경)

---

## 📋 작업 개요

이 문서는 관리자 탭 표시 문제 해결과 프로그램 제목 클릭 기능 추가에 대한 개발 로그입니다.
이전 Claude가 작업한 관리자 탭 기능의 버그를 수정하고, UX 개선을 위한 추가 작업을 진행했습니다.

---

## 🎯 주요 작업 내용

### 1. 관리자 탭 표시 문제 해결
### 2. 관리자 신청자 목록 조회 오류 수정
### 3. 프로그램 제목 클릭 기능 추가

---

## 🔍 문제 상황

### Issue 1: 관리자 탭이 표시되지 않음

**증상:**
- 관리자로 로그인해도 프로그램 상세 페이지에서 "신청 관리 (관리자)" 탭이 보이지 않음
- 세션에 `isAdmin` 속성이 제대로 설정되지 않음

**원인 분석:**
1. `AuthController.java`의 로그인 처리 시 `session.isAdmin` 설정 누락
2. Thymeleaf 템플릿에서 세션 속성 접근 방식 문제

### Issue 2: 관리자 신청자 목록 조회 실패

**증상:**
- 관리자 탭은 표시되지만 "신청 목록을 불러올 수 없습니다" 오류 발생
- JavaScript 콘솔에서 API 호출 실패 확인

**원인 분석:**
1. `ProgramApplicationResponse` DTO에 사용자 정보 필드 누락
2. 프론트엔드에서 `app.userName`, `app.studentNum` 등을 참조하지만 백엔드에서 제공하지 않음
3. 필드명 불일치 (`userStudentNum` vs `studentNum`)

### Issue 3: 프로그램 제목 클릭 불가

**증상:**
- 프로그램 목록/메인 페이지에서 제목을 클릭할 수 없음
- 카드 전체를 클릭해야만 상세 페이지로 이동 가능
- 사용자 입장에서 제목이 링크처럼 보이지 않아 혼란

---

## 🛠️ 해결 방법

### 1. 관리자 탭 표시 문제 해결

#### 1.1 세션에 isAdmin 속성 추가

**파일:** `src/main/java/com/scms/app/controller/AuthController.java`

**변경 전 (라인 54-58):**
```java
// 세션에 사용자 정보 저장
session.setAttribute("userId", response.getUserId());
session.setAttribute("studentNum", response.getStudentNum());
session.setAttribute("name", response.getName());
session.setAttribute("role", response.getRole());
session.setAttribute("isFirstLogin", response.getIsFirstLogin());
```

**변경 후 (라인 54-59):**
```java
// 세션에 사용자 정보 저장
session.setAttribute("userId", response.getUserId());
session.setAttribute("studentNum", response.getStudentNum());
session.setAttribute("name", response.getName());
session.setAttribute("role", response.getRole());
session.setAttribute("isFirstLogin", response.getIsFirstLogin());
session.setAttribute("isAdmin", response.getRole() == com.scms.app.model.UserRole.ADMIN);
```

**효과:**
- 로그인 시 세션에 `isAdmin` boolean 값 저장
- Thymeleaf 템플릿에서 간단하게 `${session.isAdmin}` 접근 가능

#### 1.2 Thymeleaf 템플릿 조건문 수정

**파일:** `src/main/resources/templates/program-detail.html`

**변경 전:**
```html
<button class="tab-button" th:if="${session.role == 'ADMIN'}" ...>
```

**변경 후 (라인 938):**
```html
<button class="tab-button" th:if="${session.isAdmin == true}" onclick="showTab(event, 'admin-applications')">
    <i class="fas fa-user-shield"></i> 신청 관리 (관리자)
</button>
```

**효과:**
- boolean 타입의 `isAdmin`을 직접 체크하여 더 명확한 조건 판단
- 관리자 권한이 있는 사용자만 탭 표시

#### 1.3 디버그 정보 추가

**파일:** `src/main/resources/templates/program-detail.html`

프로그램 상세 페이지 상단에 세션 정보를 표시하는 디버그 박스 추가:

```html
<!-- 디버그: 세션 정보 출력 (개발용, 배포 시 제거) -->
<div style="background: #fff3cd; padding: 10px; margin: 10px; border: 1px solid #ffc107; border-radius: 4px;"
     th:if="${session.userId != null}">
    <strong>🔍 디버그 정보:</strong><br/>
    userId: <span th:text="${session.userId}">N/A</span><br/>
    name: <span th:text="${session.name}">N/A</span><br/>
    role: <span th:text="${session.role}">N/A</span><br/>
    isAdmin: <span th:text="${session.isAdmin}">N/A</span><br/>
    isAdmin==true: <span th:text="${session.isAdmin == true}">N/A</span>
</div>
```

**효과:**
- 세션 값 실시간 확인 가능
- 관리자 탭 표시 문제 디버깅 용이

---

### 2. 관리자 신청자 목록 조회 오류 수정

#### 2.1 DTO에 사용자 정보 필드 추가

**파일:** `src/main/java/com/scms/app/dto/ProgramApplicationResponse.java`

**추가된 필드 (라인 29-36):**
```java
// 사용자 정보 (관리자 신청 목록에서 필요)
private Integer userId;
private Integer studentNum;
private String userName;
private String userEmail;
private String userPhone;
private String userDepartment;
private Integer userGrade;
```

**효과:**
- 관리자가 누가 신청했는지 확인 가능
- 이름, 학번, 학과, 학년 등 신청자 정보 제공

#### 2.2 DTO 변환 메서드 업데이트

**파일:** `src/main/java/com/scms/app/dto/ProgramApplicationResponse.java`

**변경 후 (라인 52-69):**
```java
public static ProgramApplicationResponse from(ProgramApplication application) {
    return ProgramApplicationResponse.builder()
            .applicationId(application.getApplicationId())
            .programId(application.getProgram().getProgramId())
            .programTitle(application.getProgram().getTitle())
            .programDepartment(application.getProgram().getDepartment())
            .programCollege(application.getProgram().getCollege())
            .programStartDate(application.getProgram().getApplicationStartDate())
            .programEndDate(application.getProgram().getApplicationEndDate())
            // 사용자 정보
            .userId(application.getUser().getUserId())
            .studentNum(application.getUser().getStudentNum())
            .userName(application.getUser().getName())
            .userEmail(application.getUser().getEmail())
            .userPhone(application.getUser().getPhone())
            .userDepartment(application.getUser().getDepartment())
            .userGrade(application.getUser().getGrade())
            // 신청 상태 정보
            .status(application.getStatus())
            .statusDescription(application.getStatus().getDescription())
            .appliedAt(application.getAppliedAt())
            .approvedAt(application.getApprovedAt())
            .rejectedAt(application.getRejectedAt())
            .cancelledAt(application.getCancelledAt())
            .completedAt(application.getCompletedAt())
            .rejectionReason(application.getRejectionReason())
            .notes(application.getNotes())
            .cancellable(application.isCancellable())
            .build();
}
```

**효과:**
- Entity → DTO 변환 시 사용자 정보 포함
- 관리자 API에서 완전한 데이터 제공

#### 2.3 프론트엔드 필드명 수정

**파일:** `src/main/resources/templates/program-detail.html`

**변경 전 (라인 1797):**
```javascript
<td>${escapeHtml(app.userStudentNum || '-')}</td>
```

**변경 후 (라인 1797):**
```javascript
<td>${escapeHtml(app.studentNum || '-')}</td>
```

**효과:**
- 백엔드 DTO 필드명과 프론트엔드 참조 일치
- 신청자 학번 정상 표시

#### 2.4 Repository 확인

**파일:** `src/main/java/com/scms/app/repository/ProgramApplicationRepository.java`

Repository는 이미 JOIN FETCH를 사용하여 User 정보를 함께 로드하고 있음을 확인:

```java
@Query("SELECT pa FROM ProgramApplication pa " +
       "JOIN FETCH pa.program " +
       "JOIN FETCH pa.user " +
       "WHERE pa.program.programId = :programId " +
       "AND pa.deletedAt IS NULL " +
       "ORDER BY pa.appliedAt DESC")
List<ProgramApplication> findByProgramId(@Param("programId") Integer programId);
```

**효과:**
- Lazy Loading 문제 방지
- N+1 쿼리 문제 없음

---

### 3. 프로그램 제목 클릭 기능 추가

#### 3.1 프로그램 목록 페이지 수정

**파일:** `src/main/resources/templates/programs.html`

**CSS 추가 (라인 342-351):**
```css
.program-title a {
    color: #333;
    text-decoration: none;
    transition: color 0.3s ease;
}

.program-title a:hover {
    color: #2C5F5D;
    text-decoration: underline;
}
```

**HTML 변경 전 (라인 642):**
```html
<div class="program-title" th:text="${program.title}"></div>
```

**HTML 변경 후 (라인 642-644):**
```html
<div class="program-title">
    <a th:href="@{/programs/{id}(id=${program.programId})}" th:text="${program.title}"></a>
</div>
```

**카드 onclick 제거:**
```html
<!-- 변경 전 -->
<div th:each="program, iterStat : ${programs}" class="program-card"
     th:onclick="'location.href=\'/programs/' + ${program.programId} + '\''">

<!-- 변경 후 -->
<div th:each="program, iterStat : ${programs}" class="program-card">
```

**CSS cursor 제거:**
```css
/* 변경 전 */
.program-card {
    cursor: pointer;
}

/* 변경 후 */
.program-card {
    /* cursor 제거 */
}
```

**효과:**
- 제목만 클릭 가능하도록 명확화
- 마우스 오버 시 녹색으로 변경 + 밑줄 표시
- 사용자 경험 개선

#### 3.2 메인 페이지 수정

**파일:** `src/main/resources/templates/index.html`

programs.html과 동일한 방식으로 수정:

**CSS 추가 (라인 439-448):**
```css
.program-title a {
    color: #172B4D;
    text-decoration: none;
    transition: color 0.3s ease;
}

.program-title a:hover {
    color: #2C5F5D;
    text-decoration: underline;
}
```

**HTML 변경 (라인 818-820):**
```html
<div class="program-title">
    <a th:href="@{/programs/{id}(id=${program.programId})}" th:text="${program.title}"></a>
</div>
```

**카드 onclick 제거:**
```html
<!-- 변경 전 -->
<div th:each="program, iterStat : ${programs}" class="program-card"
     th:attr="data-index=${iterStat.index}"
     th:onclick="'location.href=\'/programs/' + ${program.programId} + '\''">

<!-- 변경 후 -->
<div th:each="program, iterStat : ${programs}" class="program-card"
     th:attr="data-index=${iterStat.index}">
```

**효과:**
- 메인 페이지와 목록 페이지 일관된 UX
- 접근성 향상

---

## 📊 데이터 흐름

### 관리자 신청자 목록 조회 플로우

```
1. 사용자: 관리자 탭 클릭
   ↓
2. Frontend: loadAdminApplications() 호출
   ↓
3. API 요청: GET /api/programs/{programId}/applications
   ↓
4. Controller: ProgramApplicationController.getProgramApplications()
   - 세션에서 isAdmin 확인
   - 권한 체크 (403 Forbidden 반환 가능)
   ↓
5. Service: ProgramApplicationService.getProgramApplications()
   ↓
6. Repository: findByProgramId()
   - JOIN FETCH pa.program
   - JOIN FETCH pa.user  ← 사용자 정보 함께 로드
   ↓
7. DTO 변환: ProgramApplicationResponse.from()
   - 프로그램 정보
   - 사용자 정보 (userId, studentNum, userName, ...)  ← 추가됨
   - 신청 상태 정보
   ↓
8. Frontend: displayAdminApplications()
   - 테이블에 데이터 표시
   - app.userName, app.studentNum 사용 ← 이제 정상 작동
```

---

## 🧪 테스트

### 1. 관리자 탭 표시 테스트

**테스트 계정:**
- 관리자: 9999999 / admin123
- 학생: 2024001 / 030101

**테스트 절차:**
1. 관리자 계정으로 로그인
2. 아무 프로그램 상세 페이지 이동
3. 상단에 디버그 정보 박스 확인
   - `isAdmin: true` 표시 확인
4. 탭 메뉴에서 "🛡️ 신청 관리 (관리자)" 탭 표시 확인
5. 학생 계정으로 로그아웃 후 재로그인
6. 동일 프로그램 상세 페이지 이동
7. 관리자 탭이 표시되지 않음 확인

**결과:** ✅ 통과

### 2. 관리자 신청자 목록 조회 테스트

**사전 조건:**
- 최소 1명의 학생이 프로그램에 신청한 상태

**테스트 절차:**
1. 관리자 계정으로 로그인
2. 신청자가 있는 프로그램 상세 페이지 이동
3. "신청 관리 (관리자)" 탭 클릭
4. 신청자 목록 테이블 확인
   - 번호 표시
   - 이름 표시 (예: 김철수)
   - 학번 표시 (예: 2024001)
   - 상태 표시 (승인 대기/승인됨 등)
   - 신청일 표시
   - 액션 버튼 표시 (승인/거부)

**결과:** ✅ 통과

### 3. 프로그램 제목 클릭 테스트

**테스트 절차:**
1. 메인 페이지(/) 접속
2. 프로그램 카드의 제목에 마우스 오버
3. 제목 색상이 녹색(#2C5F5D)으로 변경되고 밑줄 표시 확인
4. 제목 클릭하여 프로그램 상세 페이지로 이동 확인
5. 프로그램 목록 페이지(/programs) 접속
6. 동일하게 제목 클릭 기능 작동 확인

**결과:** ✅ 통과

---

## 🐛 알려진 이슈 및 해결

### Issue: 디버그 정보 박스가 프로덕션에도 표시됨

**해결 방법:**
배포 전에 다음 코드 제거 필요:

```html
<!-- 디버그: 세션 정보 출력 (개발용, 배포 시 제거) -->
<div style="background: #fff3cd; ..." th:if="${session.userId != null}">
    ...
</div>
```

또는 프로파일별로 조건부 렌더링:
```html
<div th:if="${#profiles.active == 'dev' && session.userId != null}">
    ...
</div>
```

---

## 📈 성능 고려사항

### 1. JOIN FETCH 사용

**장점:**
- N+1 쿼리 문제 방지
- 한 번의 쿼리로 Program, User 정보 함께 로드

**SQL 예시:**
```sql
SELECT
    pa.*,
    p.*,
    u.*
FROM program_applications pa
JOIN FETCH programs p ON pa.program_id = p.program_id
JOIN FETCH users u ON pa.user_id = u.user_id
WHERE pa.program_id = ?
  AND pa.deleted_at IS NULL
ORDER BY pa.applied_at DESC
```

### 2. DTO 변환 비용

**현재:** Stream API 사용
```java
List<ProgramApplicationResponse> responses = applications.stream()
    .map(ProgramApplicationResponse::from)
    .collect(Collectors.toList());
```

**성능:**
- 소규모 데이터셋(<100건)에서는 문제없음
- 대규모 데이터셋의 경우 페이징 처리 권장

---

## 🔄 Git 커밋 히스토리

### 관리자 탭 표시 수정 관련 커밋

1. **c1778fc** - Fix admin tab visibility: Add isAdmin to Model
   - AuthController에서 세션에 isAdmin 설정 추가

2. **fe9f6e1** - Fix admin tab visibility: Use session.isAdmin in Thymeleaf template
   - Thymeleaf 조건문을 session.isAdmin으로 변경

3. **75ba6b7** - Add debug info to display session values for admin tab troubleshooting
   - 디버그 정보 박스 추가

### 프로그램 제목 클릭 기능 추가 커밋

4. **c6b248e** - Make program titles clickable links for better UX
   - index.html, programs.html에 제목 링크 추가
   - CSS 호버 효과 추가
   - 카드 onclick 제거

### 신청자 목록 조회 오류 수정 커밋

5. **0773c2f** - Fix admin applications list: Add user information to DTO
   - ProgramApplicationResponse에 사용자 정보 필드 추가
   - from() 메서드 업데이트
   - 프론트엔드 필드명 수정

---

## 📝 배운 점 & 개선 사항

### 배운 점

1. **세션 속성 설정의 중요성**
   - 로그인 시 필요한 모든 세션 속성을 명시적으로 설정해야 함
   - `isAdmin` 같은 파생 속성도 세션에 저장하면 템플릿에서 편리하게 사용 가능

2. **DTO 설계 시 고려사항**
   - API를 사용하는 클라이언트가 필요로 하는 모든 정보를 포함해야 함
   - 관리자용 API는 일반 사용자용 API보다 더 많은 정보 필요
   - 필드명 일관성 유지 중요 (userStudentNum vs studentNum)

3. **UX 개선의 중요성**
   - 클릭 가능한 요소는 명확하게 표시해야 함
   - 호버 효과로 상호작용 가능성 암시
   - 일관된 디자인 패턴 유지

### 개선 가능한 부분

1. **디버그 코드 분리**
   - 프로파일별로 디버그 코드 활성화/비활성화
   - 프로덕션 환경에서 자동 제거

2. **에러 처리 개선**
   - 관리자 권한 없을 때 더 친절한 메시지
   - API 오류 시 구체적인 오류 메시지

3. **타입 안전성**
   - TypeScript 도입 검토
   - DTO 필드명 오타 방지

---

## 🔜 다음 단계

### 관리자 기능 완성

1. **신청 승인/거부 기능 테스트**
   - 현재 버튼은 있지만 동작 확인 필요
   - 승인 시 이메일 알림 (선택)

2. **Excel 다운로드 기능 테스트**
   - 신청자 목록 Excel 내보내기
   - 한글 파일명 인코딩 확인

3. **통계 기능**
   - 승인률, 신청자 수 등 집계
   - 차트 시각화 (선택)

### UI/UX 개선

1. **디버그 정보 제거**
   - 프로덕션 배포 전 제거 또는 조건부 렌더링

2. **반응형 디자인**
   - 모바일 환경에서 관리자 탭 확인
   - 테이블 가로 스크롤 처리

3. **접근성 개선**
   - 스크린 리더 지원
   - 키보드 네비게이션

---

## 📚 참고 문서

- [이전 개발 로그] `doc/11_NEXT_IMPLEMENTATION_PLAN.md`
- [프로그램 신청 기능 개발 로그] `doc/10_PROGRAM_APPLICATION_FEATURE_DEVELOPMENT_LOG.md`
- [프로젝트 전체 개요] `doc/01_PROJECT_OVERVIEW.md`

---

## ✅ 체크리스트

- [x] 관리자 탭 표시 문제 해결
- [x] 세션에 isAdmin 속성 추가
- [x] Thymeleaf 조건문 수정
- [x] 디버그 정보 추가
- [x] 관리자 신청자 목록 조회 오류 수정
- [x] ProgramApplicationResponse DTO에 사용자 정보 추가
- [x] DTO 변환 메서드 업데이트
- [x] 프론트엔드 필드명 수정
- [x] 프로그램 제목 클릭 기능 추가
- [x] index.html, programs.html 수정
- [x] CSS 호버 효과 추가
- [x] 테스트 완료
- [x] Git 커밋 및 푸시
- [x] PR 생성 준비 완료
- [ ] 디버그 정보 제거 (배포 전)
- [ ] 승인/거부 기능 테스트
- [ ] Excel 다운로드 테스트

---

**작성 완료일:** 2025-11-17
**다음 리뷰 예정일:** PR 머지 후
