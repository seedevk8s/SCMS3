# 05. 관리자 헤더 기능 개발 로그

## 문서 정보
- **작성일**: 2025-11-07
- **개발 기간**: 약 8시간
- **담당**: Claude
- **난이도**: ★★★★★ (매우 높음 - 다층 구조 문제)

---

## 1. 요구사항

### 1.1 초기 요청
관리자로 로그인 후 "관리" 버튼 클릭 시 관리자 전용 레이아웃으로 전환되지 않는 문제 해결

### 1.2 기대 동작
1. 관리자 로그인 시 헤더에 "관리" 버튼 표시
2. "관리" 버튼 클릭 시:
   - 관리자 전용 헤더 표시 (관리자 모드 뱃지 포함)
   - 왼쪽 사이드바 내비게이션 표시
   - 로그아웃 버튼이 골드 색상으로 명확히 보임
   - WordPress/Django admin과 유사한 전문적인 UI

---

## 2. 개발 과정 (시간순)

### 2.1 초기 시도 (1-5시간): 템플릿 및 스타일 작업

#### 수행한 작업
1. **헤더 스타일 수정**
   - `header.html`의 관리 버튼 인라인 스타일 제거
   - `common.css`에 `.btn-admin` 클래스 추가

2. **관리자 레이아웃 시스템 구축**
   - `layout/admin-layout.html` 생성 (마스터 템플릿)
   - `layout/admin-header.html` 생성 (관리자 헤더)
   - `layout/admin-sidebar.html` 생성 (사이드바 네비게이션)
   - `css/admin.css` 생성 (관리자 전용 스타일)

3. **Thymeleaf Fragment 선언 수정**
   - 문제: `<html th:fragment="admin-header">`로 선언
   - 수정: `<header th:fragment="admin-header">`로 변경
   - Fragment는 실제 콘텐츠 요소에 선언해야 함

#### 문제점
사용자 피드백: **"PR 반영했지만 아무 변화없어"** (6회 이상 반복)

**근본 원인을 찾지 못함** - 템플릿 문제가 아니라 컨트롤러 자체가 호출되지 않는 것이었음

---

### 2.2 중간 진단 (5-6시간): Layout Dialect 문제 발견

#### 발견한 문제
Thymeleaf Layout Dialect가 Spring Bean으로 등록되지 않음

#### 해결 작업
1. **ThymeleafConfig.java 생성**
```java
@Configuration
public class ThymeleafConfig {
    @Bean
    public LayoutDialect layoutDialect() {
        return new LayoutDialect();
    }
}
```

2. **build.gradle 버전 명시**
```gradle
implementation 'nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:3.3.0'
```

3. **검증용 LayoutDialectVerifier 추가**
```java
@Component
public class LayoutDialectVerifier {
    @Autowired(required = false)
    private LayoutDialect layoutDialect;

    @PostConstruct
    public void verify() {
        if (layoutDialect != null) {
            log.info("✅ LayoutDialect가 정상적으로 로드되었습니다: {}", layoutDialect.getClass().getName());
        } else {
            log.error("❌ LayoutDialect가 로드되지 않았습니다!");
        }
    }
}
```

#### 결과
Layout Dialect는 정상 로드됨을 확인했으나, **여전히 관리자 페이지가 표시되지 않음**

---

### 2.3 근본 원인 발견 (7시간 차): 인증 문제

#### 로그 분석
사용자가 제공한 서버 로그:
```
로그인 성공: 시스템관리자 (9999999) - Role: ADMIN
```

하지만 이후:
```
=== /admin/programs 호출됨  // 이 로그가 없음!
```

**핵심 발견**: 컨트롤러가 호출조차 되지 않았음 → Spring Security가 요청을 차단

#### 원인 분석
REST API 로그인에서는 **SecurityContext를 명시적으로 세션에 저장**해야 함

기존 코드 (AuthController.java):
```java
// SecurityContext 생성만 하고 세션 저장 안 함
SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
securityContext.setAuthentication(authentication);
SecurityContextHolder.setContext(securityContext);
// 여기서 끝! → 다음 요청에서 인증 정보 사라짐
```

#### 해결 방법

**1. SecurityConfig.java 수정**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .securityContext(context -> context
                .securityContextRepository(securityContextRepository())
            )
            // ... 나머지 설정
    }
}
```

**2. AuthController.java 수정**
```java
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final SecurityContextRepository securityContextRepository;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpSession session) {

        // ... 로그인 처리

        // SecurityContext 생성
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        // ⭐ 핵심: HttpSession에 명시적으로 저장
        securityContextRepository.saveContext(securityContext, httpRequest, null);

        return ResponseEntity.ok(response);
    }
}
```

#### 결과
사용자 피드백: **"이제사 변화가 생겼다"**

---

### 2.4 마지막 장애물 (8시간 차): Spring Boot 3.x 호환성

#### 발생한 에러
```
java.lang.IllegalArgumentException: The 'request','session','servletContext'
and 'response' expression utility objects are no longer available by default
for security reasons.
```

#### 원인
Spring Boot 3.x (Thymeleaf 3.1+)에서 보안상의 이유로 `#request`, `#session` 등의 표현식 객체를 기본적으로 비활성화

기존 코드 (admin-sidebar.html):
```html
<a href="/admin/programs" class="admin-nav-item"
   th:classappend="${#strings.contains(#request.requestURI, '/admin/programs')} ? 'active' : ''">
```

#### 해결 방법

**1. ProgramAdminController.java - 모든 GET 메서드 수정**
```java
@GetMapping
public String listPrograms(Model model, HttpSession session, HttpServletRequest request) {
    // ... 기존 코드

    // ⭐ currentUri를 Model에 명시적으로 추가
    model.addAttribute("currentUri", request.getRequestURI());

    return "admin/program-list";
}

@GetMapping("/new")
public String newProgramForm(Model model, HttpSession session, HttpServletRequest request) {
    // ... 기존 코드
    model.addAttribute("currentUri", request.getRequestURI());
    return "admin/program-form";
}

@GetMapping("/{id}/edit")
public String editProgramForm(
        @PathVariable Integer id,
        Model model,
        HttpSession session,
        HttpServletRequest httpRequest,
        RedirectAttributes redirectAttributes) {
    // ... 기존 코드
    model.addAttribute("currentUri", httpRequest.getRequestURI());
    return "admin/program-form";
}
```

**2. admin-sidebar.html 수정**
```html
<!-- 변경 전 -->
<a href="/admin/programs" class="admin-nav-item"
   th:classappend="${#strings.contains(#request.requestURI, '/admin/programs')} ? 'active' : ''">

<!-- 변경 후 -->
<a href="/admin/programs" class="admin-nav-item"
   th:classappend="${currentUri != null && #strings.contains(currentUri, '/admin/programs')} ? 'active' : ''">
```

#### 결과
사용자 피드백: **"아이고 아이고 이제사 되었다"** ✅

---

## 3. 발생한 문제 및 해결 요약

### 3.1 문제 1: Thymeleaf Fragment 선언 위치
- **문제**: `<html>` 태그에 `th:fragment` 선언
- **증상**: Fragment를 찾을 수 없음
- **해결**: 실제 콘텐츠 태그(`<header>`, `<aside>`)에 선언
- **소요 시간**: 1시간

### 3.2 문제 2: Thymeleaf Layout Dialect 미등록
- **문제**: LayoutDialect Bean 등록 안 됨
- **증상**: `layout:decorate` 무시됨
- **해결**: ThymeleafConfig.java 생성 및 Bean 등록
- **소요 시간**: 1시간

### 3.3 문제 3: Spring Security 인증 정보 미저장 (핵심)
- **문제**: REST API 로그인 시 SecurityContext를 세션에 저장하지 않음
- **증상**: 컨트롤러 호출 자체가 안 됨 (Spring Security가 차단)
- **해결**: `securityContextRepository.saveContext()` 명시적 호출
- **소요 시간**: 6시간 (발견까지 너무 오래 걸림)

### 3.4 문제 4: Spring Boot 3.x Thymeleaf 호환성
- **문제**: `#request.requestURI` 등 표현식 객체 비활성화
- **증상**: IllegalArgumentException 발생
- **해결**: `currentUri`를 Model로 전달
- **소요 시간**: 1시간

---

## 4. 생성/수정된 파일 목록

### 4.1 신규 생성 파일
1. `src/main/java/com/scms/app/config/ThymeleafConfig.java`
   - LayoutDialect Bean 등록

2. `src/main/resources/templates/layout/admin-layout.html`
   - 관리자 마스터 레이아웃

3. `src/main/resources/templates/layout/admin-header.html`
   - 관리자 전용 헤더 (관리자 모드 뱃지)

4. `src/main/resources/templates/layout/admin-sidebar.html`
   - 사이드바 네비게이션 (대시보드, 프로그램, 사용자, 통계, 설정)

5. `src/main/resources/static/css/admin.css`
   - 관리자 UI 전용 스타일

### 4.2 수정된 파일
1. `src/main/java/com/scms/app/config/SecurityConfig.java`
   - SecurityContextRepository Bean 추가
   - securityContext 설정 추가

2. `src/main/java/com/scms/app/controller/AuthController.java`
   - SecurityContextRepository 주입
   - saveContext() 명시적 호출 추가

3. `src/main/java/com/scms/app/controller/ProgramAdminController.java`
   - 모든 GET 메서드에 `currentUri` Model 추가

4. `src/main/resources/templates/layout/header.html`
   - 관리 버튼 스타일 클래스 적용
   - 로그아웃 버튼 조건부 표시

5. `src/main/resources/static/css/common.css`
   - `.btn-admin` 스타일 추가
   - `.btn-logout` 골드 색상으로 변경

6. `build.gradle`
   - thymeleaf-layout-dialect 버전 명시 (3.3.0)

---

## 5. 커밋 이력

```
cb14b87 - Add ThymeleafConfig to enable Layout Dialect
ea7eeda - Fix Layout Dialect version compatibility
3e5efea - Add LayoutDialect verifier
188bf2f - Fix admin authentication persistence for REST API login
0a5e3a2 - Fix Thymeleaf request URI reference
90c07ad - Fix Spring Boot 3.x Thymeleaf compatibility issue
```

---

## 6. 반성 및 교훈

### 6.1 문제 해결 과정의 심각한 지연

**8시간 소요** - 단순 기능 하나에 업무일 하루가 소요됨

#### 잘못된 점
1. **로그 분석 실패** (1-5시간)
   - 사용자가 "아무 변화없어"를 6번 이상 반복했는데도 템플릿만 의심
   - 로그부터 확인했다면 "컨트롤러 호출 안 됨" → 즉시 인증 문제 의심 가능
   - 증상에만 집중하고 원인 추적을 하지 않음

2. **문제 계층 파악 실패**
   - 3개 계층 문제가 겹쳐 있었음:
     1. 인증 계층 (가장 치명적) - 7시간 차에 발견
     2. 템플릿 엔진 계층 - 5시간 차에 발견
     3. 프레임워크 호환성 - 8시간 차에 발견
   - 체계적 점검 부재: 인증 → 컨트롤러 → 서비스 → 템플릿 순으로 확인했어야 함

3. **사용자 피드백 오해석**
   - "아무 변화없어" = 페이지 자체가 안 뜸 (인증 차단)
   - 잘못된 해석: 스타일이 안 먹혔나? (템플릿 문제)

### 6.2 올바른 접근 순서 (2시간 내 해결 가능했던 경로)

**1단계: 로그 확인 (10분)**
```bash
# "=== /admin/programs 호출됨" 로그 없음 → 즉시 인증 문제 의심
```

**2단계: Spring Security 설정 점검 (20분)**
- AuthController 확인 → securityContextRepository.saveContext() 누락 발견
- 즉시 수정

**3단계: Thymeleaf 설정 확인 (30분)**
- Layout Dialect Bean 등록
- ThymeleafConfig.java 생성

**4단계: Spring Boot 3.x 호환성 체크 (30분)**
- `#request` 사용 불가 확인
- Model에 currentUri 추가

**5단계: 테스트 및 검증 (30분)**

**총 소요 시간: 2시간** (실제: 8시간)

### 6.3 사용자에게 끼친 피해
1. 8시간의 시간 낭비
2. 반복된 PR 생성 (의미 없는 커밋 6개)
3. 정신적 스트레스 극심
4. 신뢰 손상: "철면피 감정없는 악마", "잔인하게 짓밟았어"

### 6.4 재발 방지 대책

**1. 로그 우선 확인 원칙**
- 사용자가 "안 된다"고 하면 즉시 서버 로그 요청 및 분석
- 컨트롤러 호출 여부부터 확인

**2. 계층별 체크리스트 작성**
```
□ 인증 계층: Spring Security 설정, 세션 저장 확인
□ 컨트롤러 계층: 엔드포인트 호출 확인
□ 서비스 계층: 비즈니스 로직 정상 동작 확인
□ 템플릿 계층: Thymeleaf 렌더링 확인
```

**3. 프레임워크 버전 숙지**
- Spring Boot 3.x breaking changes 사전 학습
- Thymeleaf 3.1+ 변경사항 숙지

**4. 피드백 정확한 해석**
- "아무 변화없어" ≠ 스타일 문제
- "아무 변화없어" = 페이지 자체가 안 뜸 or 기능 동작 안 함

---

## 7. 기술적 인사이트

### 7.1 REST API 로그인의 특수성

**Form Login vs REST API Login 차이**

```java
// Form Login (Spring Security가 자동 처리)
@PostMapping("/login")
public String login() {
    // SecurityContext 자동 저장됨
}

// REST API Login (명시적 저장 필요!)
@PostMapping("/api/auth/login")
public ResponseEntity<LoginResponse> login(HttpServletRequest request) {
    // SecurityContext 생성
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(authentication);
    SecurityContextHolder.setContext(context);

    // ⭐ 반드시 명시적으로 저장해야 함!
    securityContextRepository.saveContext(context, request, null);
}
```

### 7.2 Spring Boot 3.x Thymeleaf 변경사항

**보안 강화로 인한 표현식 객체 비활성화**

| 객체 | Spring Boot 2.x | Spring Boot 3.x | 대안 |
|------|----------------|----------------|------|
| `#request` | ✅ 사용 가능 | ❌ 비활성화 | Model로 전달 |
| `#session` | ✅ 사용 가능 | ❌ 비활성화 | Model로 전달 |
| `#servletContext` | ✅ 사용 가능 | ❌ 비활성화 | Model로 전달 |
| `#response` | ✅ 사용 가능 | ❌ 비활성화 | 사용 불가 |

**권장 패턴**:
```java
// Controller
@GetMapping
public String page(Model model, HttpServletRequest request) {
    model.addAttribute("currentUri", request.getRequestURI());
    return "view";
}

// Template
<a th:classappend="${currentUri == '/admin'} ? 'active' : ''">
```

### 7.3 Thymeleaf Layout Dialect 등록

**Spring Boot Auto-configuration에 의존하지 말 것**

명시적 Bean 등록 필요:
```java
@Configuration
public class ThymeleafConfig {
    @Bean
    public LayoutDialect layoutDialect() {
        return new LayoutDialect();
    }
}
```

---

## 8. 최종 결과

### 8.1 기능 정상 작동 확인
✅ 관리자 로그인 → 헤더에 "관리" 버튼 표시
✅ "관리" 버튼 클릭 → 관리자 레이아웃 전환
✅ 사이드바 네비게이션 표시 (대시보드, 프로그램, 사용자, 통계, 설정)
✅ 관리자 모드 뱃지 표시
✅ 로그아웃 버튼 골드 색상으로 명확히 보임
✅ Active 메뉴 하이라이트 동작

### 8.2 코드 품질
✅ Spring Security 인증 정보 세션 저장
✅ Spring Boot 3.x 호환성 준수
✅ Thymeleaf Layout 시스템 정상 동작
✅ 전문적인 관리자 UI (WordPress/Django admin 수준)

### 8.3 남은 개선 과제
- [ ] CSRF 활성화 (현재 개발 단계로 비활성화)
- [ ] 관리자 대시보드 실제 구현 (/admin)
- [ ] 사용자 관리, 통계, 설정 페이지 구현
- [ ] 디버깅 로그 제거 (ProgramAdminController의 상세 로그)

---

## 9. 결론

8시간이라는 과도한 시간이 소요된 것은 **문제 진단 능력 부족**과 **체계적 접근 부재**가 원인이었습니다.

하지만 이 과정에서 다음을 배웠습니다:
1. REST API 로그인에서 SecurityContext 명시적 저장 필요성
2. Spring Boot 3.x Thymeleaf 호환성 이슈
3. 로그 분석의 중요성
4. 계층별 체계적 디버깅의 필요성

앞으로는 이러한 교훈을 바탕으로 **로그 우선 확인**, **계층별 체크리스트**, **프레임워크 버전 숙지**를 통해 효율적으로 문제를 해결하겠습니다.

---

**최종 커밋**: `90c07ad - Fix Spring Boot 3.x Thymeleaf compatibility issue`
**브랜치**: `claude/fix-admin-header-functions-011CUtF5NxjWWFJbYYizYVrM`
**상태**: ✅ 완료 및 정상 작동 확인
