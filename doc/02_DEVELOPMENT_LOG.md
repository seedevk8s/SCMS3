# 개발 로그 (Development Log)

푸름대학교 학생성장지원센터 CHAMP 시스템 (SCMS2) 개발 이력

---

## 프로젝트 개요

**프로젝트명**: 푸름대학교 학생성장지원센터 CHAMP (Student Competency Management System 2)
**목적**: 학생 역량 관리, 비교과 프로그램 운영, 마일리지 관리
**참고 시스템**: 우석대학교 CHAMP 웹사이트 (https://champ.woosuk.ac.kr)
**개발 기간**: 2025년 11월 6일 ~ 진행 중
**개발 브랜치**: `claude/fix-text-layout-single-line-011CUquQnjg8R34N5kh2xEFB`

---

## 기술 스택

### Backend
- **Java**: 17
- **Spring Boot**: 3.x
- **Spring Security**: 세션 기반 인증
- **Spring Data JPA**: ORM
- **MySQL**: 8.0

### Frontend
- **Thymeleaf**: 템플릿 엔진
- **HTML5 / CSS3**: 마크업 및 스타일링
- **JavaScript (Vanilla)**: 클라이언트 로직
- **Thymeleaf Layout Dialect**: 레이아웃 관리

### 개발 도구
- **Git**: 버전 관리
- **GitHub**: 저장소 호스팅
- **Maven**: 빌드 도구

---

## 개발 타임라인

### Phase 1: 헤더 레이아웃 수정 (2025.11.06)

#### 커밋: `6b07070` - Revert university name and fix header text layout

**문제 상황**:
- 헤더가 UI 슬라이드 9번처럼 표시됨 (2줄 레이아웃)
- 학교명이 "풀무대학교"로 잘못 변경됨

**요구사항**:
- UI 슬라이드 1~7번처럼 한 줄로 표시
- 학교명: "푸름대학교"만 표시 (CHAMP 텍스트 제거)
- 네비게이션 메뉴가 줄바꿈 없이 한 줄에 표시

**구현 내용**:
1. 학교명 "풀무대학교" → "푸름대학교" 복구
2. 헤더 좌측: "🎓 푸름대학교"만 표시 (기존: "푸름대학교 학생성장지원센터 CHAMP")
3. CSS 수정: `white-space: nowrap` 추가

**변경 파일**:
- `src/main/resources/templates/layout/header.html`
- `src/main/resources/static/css/common.css`

**결과**: ✅ 헤더가 한 줄로 깔끔하게 표시됨

---

#### 커밋: `00670e2` - Fix header layout per UI slides 1-7 requirements

**추가 보완**:
- 헤더 컨테이너에 `flex-wrap: nowrap` 추가
- 네비게이션 메뉴 간격 최적화
- 반응형 레이아웃 개선

**결과**: ✅ 모든 화면 크기에서 한 줄 레이아웃 유지

---

### Phase 2: Spring Security 인증 시스템 수정 (2025.11.06)

#### 커밋: `3566676` - Add comprehensive development documentation

**작업 내용**:
- DEVELOPMENT.md 문서 작성
- 프로젝트 구조, 완료 기능, TODO 리스트 정리
- 테스트 계정 정보 문서화

**파일**:
- `DEVELOPMENT.md` (신규 생성)

---

#### 커밋: `a66b0f9` - Fix Spring Security configuration and add password management pages

**문제 상황**:
- 로그인 페이지가 인증 필요로 무한 리다이렉트
- 비밀번호 변경 페이지 없음
- 비밀번호 찾기 페이지 없음

**구현 내용**:
1. **SecurityConfig.java 수정**:
   - 로그인 페이지 및 관련 API를 `permitAll()`에 추가
   - `/login`, `/password/**`, `/api/auth/**` 경로 허용
   - 정적 리소스 허용 (`/css/**`, `/js/**`, `/images/**`)

2. **비밀번호 변경 페이지 생성**:
   - `password-change.html` 생성
   - 현재 비밀번호, 새 비밀번호, 확인 입력 폼
   - AJAX로 `/api/auth/password/change` 호출

3. **비밀번호 찾기 페이지 생성**:
   - `password-reset.html` 생성
   - 학번, 이름, 생년월일 입력 폼
   - AJAX로 `/api/auth/password/reset` 호출

**변경 파일**:
- `src/main/java/com/scms/app/config/SecurityConfig.java`
- `src/main/resources/templates/password-change.html` (신규)
- `src/main/resources/templates/password-reset.html` (신규)

**결과**: ✅ 로그인 페이지 접근 가능, 비밀번호 관리 기능 추가

---

#### 커밋: `1813352` - Fix authentication issue for password change API

**문제 상황**:
- 최초 로그인 후 비밀번호 변경 시 401 Unauthorized 오류
- `/api/auth/password/change` 엔드포인트가 인증 필요

**원인 분석**:
- SecurityConfig의 permitAll 목록에 비밀번호 변경 API 누락
- 최초 로그인 사용자는 인증 상태이지만 접근 거부됨

**해결 방법**:
```java
.requestMatchers(
    "/api/auth/login",
    "/api/auth/logout",
    "/api/auth/password/change",  // ← 추가
    "/api/auth/password/reset",
    "/api/auth/me"
).permitAll()
```

**변경 파일**:
- `src/main/java/com/scms/app/config/SecurityConfig.java`

**결과**: ✅ 비밀번호 변경 기능 정상 작동

---

#### 커밋: `d3afc66` - Fix student login issue - Integrate Spring Security authentication

**문제 상황**:
- 관리자 계정은 로그인 성공
- 학생 계정은 로그인 후 모든 페이지 접근 시 401 오류

**원인 분석**:
- `AuthController`가 HttpSession에만 사용자 정보 저장
- Spring Security의 SecurityContext에는 인증 정보 없음
- `.anyRequest().authenticated()` 규칙에 의해 접근 차단

**해결 방법**:
AuthController의 로그인 메서드에 SecurityContext 통합 추가:

```java
@PostMapping("/login")
public ResponseEntity<LoginResponse> login(
        @Valid @RequestBody LoginRequest request,
        HttpServletRequest httpRequest,
        HttpSession session) {

    LoginResponse response = userService.login(request, httpRequest);

    // Session에 저장
    session.setAttribute("userId", response.getUserId());
    session.setAttribute("role", response.getRole());

    // ✅ Spring Security SecurityContext에 추가
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(
            response.getStudentNum(),
            null,
            Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + response.getRole().name())
            )
        );
    SecurityContextHolder.getContext().setAuthentication(authentication);

    return ResponseEntity.ok(response);
}
```

**변경 파일**:
- `src/main/java/com/scms/app/controller/AuthController.java`

**결과**: ✅ 학생 계정 로그인 및 페이지 접근 정상 작동

---

#### 커밋: `9529f4e` - Fix logout redirect to home page

**문제 상황**:
- 관리자 로그아웃 시 로그인 페이지로 리다이렉트
- 사용자 경험 저하 (홈으로 가야 자연스러움)

**해결 방법**:
HomeController의 로그아웃 메서드 수정:

```java
@GetMapping("/logout")
@PostMapping("/logout")
public String logout(HttpSession session) {
    session.invalidate();
    SecurityContextHolder.clearContext();
    return "redirect:/";  // 변경: redirect:/login → redirect:/
}
```

**변경 파일**:
- `src/main/java/com/scms/app/controller/HomeController.java`

**결과**: ✅ 로그아웃 시 홈 페이지로 리다이렉트

---

#### 커밋: `adf3977` - Fix logout URL mismatch between Spring Security and header

**문제 상황**:
- 학생 계정 로그아웃 작동 안 함
- header.html: `/logout` 사용
- SecurityConfig: `/api/logout` 설정
- URL 불일치로 로그아웃 실패

**해결 방법**:
SecurityConfig에서 로그아웃 URL을 `/logout`으로 통일:

```java
.logout(logout -> logout
    .logoutUrl("/logout")              // 변경: /api/logout → /logout
    .logoutSuccessUrl("/")
    .clearAuthentication(true)
    .invalidateHttpSession(true)
)
```

AuthController의 로그아웃 메서드도 SecurityContext 클리어 추가:

```java
@PostMapping("/logout")
public ResponseEntity<String> logout(HttpSession session) {
    session.invalidate();
    SecurityContextHolder.clearContext();  // ← 추가
    return ResponseEntity.ok("로그아웃 완료");
}
```

**변경 파일**:
- `src/main/java/com/scms/app/config/SecurityConfig.java`
- `src/main/java/com/scms/app/controller/AuthController.java`

**결과**: ✅ 학생/관리자 모두 로그아웃 정상 작동

---

### Phase 3: 홈 페이지 완전 구현 (2025.11.06)

#### 커밋: `b0c38c6` - Implement complete home page matching Woosuk University CHAMP design

**요구사항**:
- 우석대학교 CHAMP 웹사이트 스크린샷 (1.png ~ 5.png) 기반 구현
- 히어로 슬라이더, 역량 아이콘, 프로그램 카드 등 완전한 홈 페이지

**구현 내용**:

1. **히어로 슬라이더**:
   - 3개 슬라이드 자동 회전 (5초 간격)
   - 좌우 화살표 네비게이션
   - 슬라이드 내용:
     * 🎓 핵심역량 진단에 참여하세요!
     * 💼 비교과 프로그램
     * ⭐ CHAMP 마일리지
   - CSS 애니메이션 및 전환 효과
   - JavaScript 자동 슬라이더 구현

2. **4개 역량 아이콘 그리드**:
   - 📊 전체보기
   - 🧩 문제해결역량
   - 🏅 자기관리역량
   - 🏛️ 공감소통역량
   - 각 아이콘 클릭 시 `/assessment` 페이지로 이동
   - 호버 효과 (transform, shadow)

3. **필터 섹션**:
   - 행정부서 드롭다운 버튼
   - 📱 전체 행정부서
   - 스타일링 (border, hover effect)

4. **8개 프로그램 카드 그리드**:
   각 카드는 다음을 포함:
   - **고유 그라디언트 배경**: 8가지 다른 색상 조합
   - **D-day 뱃지**: D-3, D-4, 마감, 모집완료, D-24, D-35
   - **즐겨찾기 버튼**: ⭐ 아이콘
   - **HITS 카운터**: 160 HITS, 90 HITS, 713 HITS, 398 HITS 등
   - **부서명**: 취창업지원센터, 약학대학, 역량개발인증센터 등
   - **프로그램 제목**: 전공설계 포트폴리오 특강, 우석챔프 시즌제 장학금 등
   - **카테고리 뱃지**: 프로그램 분류
   - **신청/운영 기간**: 📅 신청일, ⏰ 운영일
   - **진행률 바**: 0% ~ 100%
   - **참여 인원**: 0/무제한, 160/무제한, 432/무제한, 승인필요

5. **전체보기 버튼**:
   - 중앙 정렬
   - `/programs` 페이지로 이동

6. **반응형 디자인**:
   - CSS Grid 레이아웃 (4열 → 2열 → 1열)
   - 호버 효과 (카드 확대, 그림자 강조)
   - 부드러운 전환 애니메이션

**기술 상세**:
- **CSS**: 그라디언트 배경, flexbox, grid, transform, transition
- **JavaScript**: 슬라이더 자동 회전, 이벤트 핸들러
- **Thymeleaf**: 레이아웃 템플릿 통합

**변경 파일**:
- `src/main/resources/templates/index.html` (591줄 추가, 133줄 삭제)

**코드 통계**:
- 총 727줄
- CSS: 약 400줄
- HTML: 약 280줄
- JavaScript: 약 47줄

**결과**: ✅ 완전한 홈 페이지 구현, 우석대학교 CHAMP 디자인 매칭

---

#### 커밋: `624b6b3` - Add UI reference images documentation

**작업 내용**:
- UI 참고 이미지 설명 문서 작성
- 각 이미지 파일 설명 및 사용 방법
- 구현 완료 사항 요약
- 새 세션에서 이미지 참고 방법 가이드

**파일**:
- `ui/README.md` (신규 생성)

**결과**: ✅ UI 이미지 문서화 완료

---

## 해결한 주요 문제들

### 1. 헤더 텍스트 레이아웃 문제
- **문제**: 헤더가 2줄로 표시되어 UI 요구사항 불일치
- **원인**: CSS에 줄바꿈 방지 속성 누락
- **해결**: `white-space: nowrap`, `flex-wrap: nowrap` 추가

### 2. 학교명 오류
- **문제**: "풀무대학교"로 잘못 변경됨
- **원인**: 잘못된 슬라이드(8번) 참고
- **해결**: 전체 코드베이스에서 "푸름대학교"로 복구

### 3. Spring Security 인증 통합 문제
- **문제**: 학생 계정 로그인 후 페이지 접근 불가
- **원인**: Session에만 저장, SecurityContext 미사용
- **해결**: SecurityContext에 Authentication 객체 추가

### 4. 로그아웃 URL 불일치
- **문제**: 학생 계정 로그아웃 실패
- **원인**: 헤더(`/logout`)와 SecurityConfig(`/api/logout`) URL 불일치
- **해결**: `/logout`으로 통일

### 5. 비밀번호 변경 API 인증 오류
- **문제**: 비밀번호 변경 시 401 Unauthorized
- **원인**: `/api/auth/password/change` 경로가 permitAll 목록에 없음
- **해결**: permitAll 목록에 추가

### 6. 로그아웃 후 리다이렉트 위치
- **문제**: 로그아웃 후 로그인 페이지로 이동
- **원인**: `redirect:/login` 설정
- **해결**: `redirect:/`로 변경 (홈으로 이동)

---

## 구현 완료된 기능

### 인증 및 권한
- ✅ 로그인 기능 (학생/관리자)
- ✅ 로그아웃 기능
- ✅ Spring Security 세션 기반 인증
- ✅ SecurityContext + HttpSession 통합
- ✅ 비밀번호 변경 (최초 로그인)
- ✅ 비밀번호 찾기 (학번, 이름, 생년월일 확인)
- ✅ 역할 기반 접근 제어 (ROLE_STUDENT, ROLE_ADMIN)

### 페이지
- ✅ 홈 페이지 (완전 구현)
  - 히어로 슬라이더
  - 4개 역량 아이콘
  - 프로그램 카드 그리드
  - 필터 섹션
  - 전체보기 버튼
- ✅ 로그인 페이지
- ✅ 비밀번호 변경 페이지
- ✅ 비밀번호 찾기 페이지

### 공통 컴포넌트
- ✅ 헤더 (한 줄 레이아웃)
- ✅ 공통 레이아웃 (Thymeleaf Layout Dialect)
- ✅ 공통 CSS 스타일

### 데이터베이스
- ✅ User 엔티티 (학생/관리자)
- ✅ 초기 테스트 데이터 로딩 (8명 학생, 1명 관리자)
- ✅ Soft delete 패턴

---

## 테스트 계정

### 관리자
- **학번**: 9999999
- **비밀번호**: admin123
- **이름**: 관리자

### 학생 (8명)
| 학번 | 이름 | 비밀번호 (생년월일 yyMMdd) | 학과 |
|------|------|---------------------------|------|
| 2024001 | 김철수 | 000101 | 컴퓨터공학과 |
| 2024002 | 이영희 | 000202 | 경영학과 |
| 2023001 | 박민수 | 990303 | 전자공학과 |
| 2023002 | 정수진 | 990404 | 경제학과 |
| 2022001 | 최동욱 | 980505 | 기계공학과 |
| 2022002 | 강미라 | 980606 | 화학공학과 |
| 2021001 | 윤서준 | 970707 | 건축학과 |
| 2021002 | 한지민 | 970808 | 디자인학과 |

---

## 향후 개발 계획

### 우선순위 1: 프로그램 관리 기능
- [ ] Program 엔티티 설계
- [ ] 프로그램 CRUD API 개발
- [ ] 프로그램 목록 페이지 (`/programs`)
- [ ] 프로그램 상세 페이지 (`/programs/{id}`)
- [ ] 프로그램 신청 기능
- [ ] 프로그램 검색 및 필터링
- [ ] 페이지네이션

### 우선순위 2: 역량 진단 시스템
- [ ] Assessment 엔티티 설계
- [ ] 역량 진단 문항 관리
- [ ] 역량 진단 페이지 (`/assessment`)
- [ ] 진단 결과 저장 및 조회
- [ ] 역량 그래프 시각화

### 우선순위 3: 마일리지 시스템
- [ ] Mileage 엔티티 설계
- [ ] 마일리지 적립/차감 로직
- [ ] 마일리지 내역 페이지 (`/mileage`)
- [ ] 마일리지 순위 시스템

### 우선순위 4: 상담 신청 시스템
- [ ] Counseling 엔티티 설계
- [ ] 상담 신청 페이지 (`/counseling`)
- [ ] 상담 일정 관리
- [ ] 상담 승인/거부 (관리자)

### 우선순위 5: 포트폴리오 시스템
- [ ] Portfolio 엔티티 설계
- [ ] 포트폴리오 작성 페이지 (`/portfolio`)
- [ ] 포트폴리오 조회 및 수정
- [ ] 첨부파일 업로드

### 우선순위 6: 설문조사 시스템
- [ ] Survey 엔티티 설계
- [ ] 설문 문항 관리
- [ ] 설문 참여 페이지 (`/survey`)
- [ ] 설문 결과 집계

### 기능 개선
- [ ] 필터 드롭다운 동작 구현
- [ ] 프로그램 즐겨찾기 기능
- [ ] 알림 시스템
- [ ] 관리자 대시보드
- [ ] 통계 및 리포트

---

## 기술 부채 및 개선 사항

### 보안
- [ ] CSRF 토큰 검증 강화
- [ ] XSS 방어 강화
- [ ] SQL Injection 방어 확인
- [ ] 비밀번호 복잡도 정책 추가
- [ ] 로그인 시도 횟수 제한

### 성능
- [ ] 데이터베이스 쿼리 최적화
- [ ] 인덱스 추가
- [ ] 캐싱 전략 (Redis)
- [ ] 이미지 최적화 및 CDN

### 코드 품질
- [ ] 단위 테스트 작성
- [ ] 통합 테스트 작성
- [ ] 코드 리뷰 프로세스 확립
- [ ] SonarQube 코드 분석

### UI/UX
- [ ] 모바일 반응형 개선
- [ ] 로딩 스피너 추가
- [ ] 에러 메시지 개선
- [ ] 다국어 지원 (i18n)

---

## 참고 문서

- **UI 디자인 참고**: `ui/README.md`
- **개발 요구사항 PPT**:
  - `ui (1).pptx`
  - `ui (2).pptx`
  - `ui (3).pptx`
- **우석대학교 CHAMP**: https://champ.woosuk.ac.kr

---

## 커밋 히스토리 요약

```
624b6b3 - Add UI reference images documentation (2025.11.06)
b0c38c6 - Implement complete home page matching Woosuk University CHAMP design (2025.11.06)
adf3977 - Fix logout URL mismatch between Spring Security and header (2025.11.06)
9529f4e - Fix logout redirect to home page (2025.11.06)
d3afc66 - Fix student login issue - Integrate Spring Security authentication (2025.11.06)
1813352 - Fix authentication issue for password change API (2025.11.06)
a66b0f9 - Fix Spring Security configuration and add password management pages (2025.11.06)
3566676 - Add comprehensive development documentation (2025.11.06)
00670e2 - Fix header layout per UI slides 1-7 requirements (2025.11.06)
6b07070 - Revert university name and fix header text layout (2025.11.06)
```

---

## 주요 학습 사항 및 베스트 프랙티스

### Spring Security 통합
- HttpSession과 SecurityContext를 함께 사용하는 하이브리드 방식
- permitAll 경로 관리의 중요성
- 인증 정보를 두 곳에 모두 저장하는 이유:
  - HttpSession: 사용자 상세 정보 (이름, 학과 등)
  - SecurityContext: 인증/인가 정보 (권한, 역할 등)

### Thymeleaf Layout
- Layout Dialect를 활용한 공통 레이아웃 관리
- Fragment를 통한 재사용 가능한 컴포넌트
- CSS와 JavaScript 블록 오버라이드

### CSS 디자인 패턴
- CSS Grid를 활용한 반응형 레이아웃
- Gradient 배경으로 시각적 차별화
- Transform과 Transition으로 부드러운 인터랙션
- `white-space: nowrap`로 텍스트 줄바꿈 방지

### JavaScript 패턴
- Vanilla JS로 슬라이더 구현
- `setInterval`을 활용한 자동 회전
- DOM 조작 최소화

### 개발 프로세스
- Git을 활용한 단계별 커밋
- 문제 발생 시 즉시 해결 및 커밋
- 문서화의 중요성 (README, 개발 로그)

---

## 버그 및 이슈 추적

### 해결됨
- [x] #1: 헤더 텍스트 2줄로 표시 → nowrap 추가로 해결
- [x] #2: 학교명 오류 (풀무대학교) → 푸름대학교로 복구
- [x] #3: 학생 로그인 실패 → SecurityContext 통합으로 해결
- [x] #4: 비밀번호 변경 401 오류 → permitAll 추가로 해결
- [x] #5: 로그아웃 시 로그인 페이지 이동 → 홈으로 변경
- [x] #6: 학생 로그아웃 안됨 → URL 통일로 해결

### 진행 중
- 없음

### 예정
- 프로그램 카드 데이터 동적화
- 필터 드롭다운 기능 구현

---

## 연락처 및 기여자

- **개발자**: Claude (AI Assistant)
- **프로젝트 관리**: seedevk8s
- **저장소**: https://github.com/seedevk8s/SCMS2

---

**문서 최종 업데이트**: 2025년 11월 7일
