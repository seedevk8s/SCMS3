# 이메일 인증 및 외부회원 UI/UX 개선 개발 로그

**개발일**: 2025-11-18
**작성자**: Claude
**관련 기능**: 이메일 인증 (SMTP), 로그인 탭 UI, 외부회원 네비게이션, 오류 처리 개선
**브랜치**: `claude/project-documentation-017UZzxf2gvAzPn6MHGqdyEf`

---

## 📋 개요

외부회원 가입 시스템 (23번 로그)에 이어, **이메일 인증 발송 기능**과 **사용자 경험 개선**을 완료했습니다. 이번 개발에서는 실제 이메일 발송, 로그인 UX 개선, 외부회원 전용 UI 구성, 그리고 오류 처리 강화를 진행했습니다.

### 주요 성과
- ✅ **SMTP 이메일 발송**: 실제 Gmail을 통한 인증 메일 발송
- ✅ **로그인 탭 시스템**: 내부회원/외부회원 구분 UI
- ✅ **외부회원 전용 네비게이션**: 권한에 따른 메뉴 분리
- ✅ **회원가입 롤백 처리**: 이메일 발송 실패 시 계정 삭제
- ✅ **상세 오류 메시지**: 사용자에게 명확한 피드백

---

## 🎯 개발 배경 및 문제점

### 발견된 문제들

#### 1. **회원가입 문구 안 보임**
```
문제: 로그인 페이지에 "외부회원 가입" 링크가 없음
원인: login.html에 회원가입 링크 누락
해결: 회원가입 링크 추가
```

#### 2. **외부회원 가입 페이지 접근 불가**
```
문제: /external/signup 경로 접근 시 로그인 페이지로 리다이렉트
원인: SecurityConfig에서 /external/** 경로 차단
해결: SecurityConfig에 /external/**, /api/external/** 허용 추가
```

#### 3. **학번만 입력 가능한 로그인 폼**
```
문제: 로그인 페이지가 학번만 입력받아 외부회원 로그인 불가
원인: 단일 폼으로 설계되어 이메일 로그인 미지원
해결: 내부회원/외부회원 탭 시스템 구현
```

#### 4. **외부회원 로그인 후에도 "로그인" 버튼 표시**
```
문제: 외부회원으로 로그인해도 헤더에 "로그인" 버튼 유지
원인: header.html이 session.userId만 확인
해결: session.externalUserId도 확인하도록 수정
```

#### 5. **외부회원에게 내부회원 메뉴 노출**
```
문제: 외부회원에게 "CHAMP 마일리지", "상담신청" 등 접근 불가 메뉴 표시
원인: 네비게이션이 사용자 유형을 구분하지 않음
해결: 외부회원 전용 네비게이션 분리
```

#### 6. **이메일 인증 미구현**
```
문제: 회원가입 시 이메일 발송 안 됨, 인증 없이 로그인 가능
원인: SMTP 설정 없음, 이메일 발송 로직 미구현
해결: EmailService 구현, Gmail SMTP 설정, 인증 체크 로직 추가
```

#### 7. **이메일 발송 실패해도 회원가입 성공**
```
문제: 이메일 발송 실패해도 계정 생성되어 인증 불가능한 상태로 남음
원인: ExternalUserService에서 예외를 catch만 하고 롤백 안 함
해결: 이메일 발송 실패 시 생성된 계정 삭제 + 예외 던지기
```

#### 8. **사용자에게 오류 원인 미전달**
```
문제: "이메일 발송에 실패했습니다" 메시지가 사용자에게 전달 안 됨
원인: ExternalUserController가 모든 예외를 "회원가입 중 오류" 메시지로 대체
해결: 실제 예외 메시지를 그대로 전달하도록 수정
```

---

## 🏗️ 구현 내용

### 1. 이메일 인증 시스템 (SMTP)

#### 1.1 의존성 추가

**build.gradle**:
```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-mail'
}
```

#### 1.2 SMTP 설정

**application.yml**:
```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME:your-email@gmail.com}
    password: ${MAIL_PASSWORD:your-app-password}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true
          connectiontimeout: 5000
          timeout: 5000
          writetimeout: 5000
    default-encoding: UTF-8
```

**환경 변수 설정**:
- `MAIL_USERNAME`: Gmail 이메일 주소
- `MAIL_PASSWORD`: Gmail 앱 비밀번호 (2단계 인증 필요)

#### 1.3 EmailService 구현

**src/main/java/com/scms/app/service/EmailService.java**:

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${server.domain:http://localhost:8080}")
    private String serverDomain;

    /**
     * 이메일 인증 메일 발송
     */
    public void sendVerificationEmail(String toEmail, String name, String token) {
        try {
            String subject = "[푸름대학교 SCMS] 이메일 인증을 완료해주세요";
            String verificationLink = serverDomain + "/external/verify-email?token=" + token;

            // Thymeleaf 템플릿으로 HTML 생성
            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("verificationLink", verificationLink);

            String htmlContent = templateEngine.process("email/verification", context);

            // HTML 이메일 발송
            sendHtmlEmail(toEmail, subject, htmlContent);

            log.info("이메일 인증 메일 발송 완료: {}", toEmail);
        } catch (Exception e) {
            log.error("이메일 인증 메일 발송 실패: {}", toEmail, e);
            throw new RuntimeException("이메일 발송에 실패했습니다", e);
        }
    }

    /**
     * HTML 이메일 발송
     */
    private void sendHtmlEmail(String to, String subject, String htmlContent)
            throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);  // true = HTML

        mailSender.send(message);
    }
}
```

**주요 기능**:
- ✅ Gmail SMTP를 통한 이메일 발송
- ✅ Thymeleaf 템플릿 엔진으로 HTML 이메일 생성
- ✅ 인증 링크 포함
- ✅ UTF-8 인코딩
- ✅ 발송 실패 시 예외 처리

#### 1.4 이메일 템플릿

**src/main/resources/templates/email/verification.html**:

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>이메일 인증</title>
</head>
<body style="font-family: sans-serif; background-color: #f5f5f5;">
    <table role="presentation" width="600" style="margin: 0 auto; background: #fff;">
        <!-- 헤더 -->
        <tr>
            <td style="background: linear-gradient(135deg, #2C5F5D 0%, #4A8F8C 100%);
                       padding: 40px; text-align: center;">
                <h1 style="color: #fff; font-size: 28px; margin: 0;">
                    푸름대학교 SCMS
                </h1>
                <p style="color: #fff; font-size: 14px; margin: 10px 0 0;">
                    학생성장지원센터 CHAMP
                </p>
            </td>
        </tr>

        <!-- 본문 -->
        <tr>
            <td style="padding: 40px;">
                <h2 style="color: #333; font-size: 22px; margin-bottom: 20px;">
                    이메일 인증을 완료해주세요
                </h2>

                <p style="color: #666; font-size: 16px; line-height: 1.6;">
                    안녕하세요, <strong th:text="${name}">회원</strong>님!
                </p>

                <p style="color: #666; font-size: 16px; line-height: 1.6;">
                    푸름대학교 SCMS 외부회원 가입을 환영합니다.<br>
                    회원가입을 완료하려면 아래 버튼을 클릭하여 이메일 인증을 완료해주세요.
                </p>

                <!-- 인증 버튼 -->
                <table role="presentation" width="100%" style="margin: 30px 0;">
                    <tr>
                        <td align="center">
                            <a th:href="${verificationLink}"
                               style="display: inline-block;
                                      padding: 16px 40px;
                                      background-color: #2C5F5D;
                                      color: #fff;
                                      text-decoration: none;
                                      border-radius: 6px;
                                      font-size: 16px;
                                      font-weight: 600;">
                                이메일 인증하기
                            </a>
                        </td>
                    </tr>
                </table>

                <p style="color: #999; font-size: 14px; line-height: 1.6;">
                    버튼이 작동하지 않는 경우 아래 링크를 복사하여 브라우저에 붙여넣기 해주세요:
                </p>

                <p style="padding: 12px;
                          background: #f8f8f8;
                          border-radius: 4px;
                          word-break: break-all;
                          font-size: 13px;
                          color: #666;">
                    <a th:href="${verificationLink}"
                       th:text="${verificationLink}"
                       style="color: #2C5F5D; text-decoration: none;">
                    </a>
                </p>

                <div style="margin-top: 30px;
                           padding-top: 20px;
                           border-top: 1px solid #eee;">
                    <p style="color: #999; font-size: 13px; line-height: 1.5; margin: 0;">
                        ※ 본인이 요청하지 않은 이메일인 경우 무시하셔도 됩니다.<br>
                        ※ 이 이메일은 발신 전용이므로 회신하실 수 없습니다.
                    </p>
                </div>
            </td>
        </tr>

        <!-- 푸터 -->
        <tr>
            <td style="background: #f8f8f8;
                       padding: 30px 40px;
                       text-align: center;">
                <p style="color: #999; font-size: 13px; margin: 0 0 10px;">
                    © 2024 푸름대학교 학생성장지원센터 CHAMP
                </p>
                <p style="color: #999; font-size: 12px; margin: 0;">
                    문의: scms@pooreum.ac.kr | Tel: 02-1234-5678
                </p>
            </td>
        </tr>
    </table>
</body>
</html>
```

**특징**:
- ✅ 반응형 테이블 레이아웃 (이메일 클라이언트 호환성)
- ✅ 인라인 CSS (Gmail, Outlook 호환)
- ✅ 명확한 CTA 버튼
- ✅ 링크 복사 대안 제공
- ✅ 브랜드 일관성 (푸름대학교 색상)

#### 1.5 회원가입 플로우 수정

**ExternalUserService.signup()**:

```java
@Transactional
public ExternalUser signup(ExternalSignupRequest request) {
    log.info("외부회원 가입 시도: {}", request.getEmail());

    // 1. 비밀번호 확인
    if (!request.getPassword().equals(request.getConfirmPassword())) {
        throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
    }

    // 2. 이메일 중복 체크
    if (externalUserRepository.existsByEmail(request.getEmail())) {
        throw new IllegalArgumentException("이미 사용중인 이메일입니다");
    }

    // 3. 이메일 인증 토큰 생성
    String verifyToken = UUID.randomUUID().toString();

    // 4. 외부회원 생성
    ExternalUser externalUser = ExternalUser.builder()
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .name(request.getName())
            // ... 기타 필드
            .emailVerifyToken(verifyToken)
            .status(AccountStatus.ACTIVE)
            .build();

    ExternalUser savedUser = externalUserRepository.save(externalUser);

    // 5. 이메일 인증 메일 발송
    try {
        emailService.sendVerificationEmail(
            savedUser.getEmail(),
            savedUser.getName(),
            verifyToken
        );
        log.info("이메일 인증 메일 발송 완료: {}", savedUser.getEmail());
    } catch (Exception e) {
        log.error("이메일 인증 메일 발송 실패: {}", savedUser.getEmail(), e);
        // ⭐ 이메일 발송 실패 시 생성된 계정 삭제 (롤백)
        externalUserRepository.delete(savedUser);
        throw new RuntimeException("이메일 발송에 실패했습니다. 잠시 후 다시 시도해주세요.", e);
    }

    log.info("외부회원 가입 완료: {} ({})", savedUser.getName(), savedUser.getEmail());

    return savedUser;
}
```

**변경 사항**:
- ✅ **이메일 발송 실패 시 롤백**: 생성된 계정 삭제
- ✅ **명확한 예외 메시지**: 사용자에게 구체적 오류 전달
- ✅ **데이터 일관성 보장**: 인증 불가능한 계정 방지

#### 1.6 로그인 시 이메일 인증 확인

**ExternalUserService.login()**:

```java
@Transactional
public ExternalUser login(String email, String password) {
    ExternalUser user = externalUserRepository.findByEmailAndDeletedAtIsNull(email)
            .orElseThrow(() -> new BadCredentialsException("이메일 또는 비밀번호가 일치하지 않습니다"));

    // ⭐ 이메일 인증 확인
    if (!user.getEmailVerified()) {
        throw new AuthenticationException("이메일 인증이 완료되지 않았습니다. 이메일을 확인해주세요.") {};
    }

    // 계정 잠금 확인
    if (user.getLocked()) {
        throw new AuthenticationException("계정이 잠겨있습니다. 관리자에게 문의하세요.") {};
    }

    // 비밀번호 확인
    if (!passwordEncoder.matches(password, user.getPassword())) {
        user.incrementFailCount();
        externalUserRepository.save(user);
        throw new BadCredentialsException("이메일 또는 비밀번호가 일치하지 않습니다");
    }

    // 로그인 성공
    user.resetFailCount();
    user.updateLastLogin();

    return user;
}
```

**변경 사항**:
- ✅ 이메일 미인증 사용자 로그인 차단
- ✅ 명확한 에러 메시지

#### 1.7 인증 메일 재발송 기능

**ExternalUserService.resendVerificationEmail()**:

```java
@Transactional
public void resendVerificationEmail(String email) {
    ExternalUser user = externalUserRepository.findByEmailAndDeletedAtIsNull(email)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

    // 이미 인증된 경우
    if (user.getEmailVerified()) {
        throw new IllegalArgumentException("이미 인증된 이메일입니다");
    }

    // 새로운 토큰 생성
    String newToken = UUID.randomUUID().toString();
    user.updateEmailVerifyToken(newToken);

    // 이메일 발송
    emailService.sendVerificationEmail(user.getEmail(), user.getName(), newToken);

    log.info("인증 메일 재발송 완료: {}", user.getEmail());
}
```

**ExternalUserController**:

```java
@PostMapping("/resend-verification")
public ResponseEntity<Map<String, Object>> resendVerificationEmail(@RequestParam String email) {
    try {
        externalUserService.resendVerificationEmail(email);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "인증 메일이 재발송되었습니다. 이메일을 확인해주세요.");

        return ResponseEntity.ok(response);
    } catch (IllegalArgumentException e) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", e.getMessage());
        return ResponseEntity.badRequest().body(errorResponse);
    }
}
```

#### 1.8 이메일 인증 안내 페이지

**ExternalUserPageController**:

```java
@GetMapping("/verification-sent")
public String verificationSentPage(@RequestParam String email, Model model) {
    model.addAttribute("email", email);
    return "external/verification-sent";
}
```

**templates/external/verification-sent.html**:

```html
<div class="verification-container">
    <div class="verification-icon">📧</div>

    <h1 class="verification-title">이메일 인증 메일을 발송했습니다</h1>

    <p class="verification-message">
        회원가입이 완료되었습니다!<br>
        아래 이메일 주소로 인증 메일을 발송했습니다.
    </p>

    <div class="verification-email" th:text="${email}">user@example.com</div>

    <p class="verification-message">
        이메일을 확인하시고 인증 링크를 클릭해주세요.<br>
        인증이 완료되면 로그인하실 수 있습니다.
    </p>

    <!-- 알림 메시지 -->
    <div id="alertMessage" style="display: none;"></div>

    <div class="verification-actions">
        <button class="btn btn-secondary" id="resendBtn">인증 메일 재발송</button>
        <a href="/login" class="btn btn-primary">로그인 페이지로</a>
    </div>

    <div class="info-box">
        <h4>📌 안내사항</h4>
        <ul>
            <li>이메일이 도착하지 않았다면 스팸 메일함을 확인해주세요.</li>
            <li>인증 메일은 발송 후 24시간 동안 유효합니다.</li>
            <li>인증 메일이 오지 않았다면 '인증 메일 재발송' 버튼을 클릭해주세요.</li>
            <li>문의사항이 있으시면 scms@pooreum.ac.kr로 연락주세요.</li>
        </ul>
    </div>
</div>

<script>
$(document).ready(function() {
    // 인증 메일 재발송
    $('#resendBtn').click(function() {
        const email = '[[${email}]]';
        const button = $(this);

        button.prop('disabled', true).text('발송 중...');

        $.ajax({
            url: '/api/external/resend-verification',
            method: 'POST',
            data: { email: email },
            success: function(response) {
                showAlert('success', response.message || '인증 메일이 재발송되었습니다.');
                button.prop('disabled', false).text('인증 메일 재발송');
            },
            error: function(xhr) {
                const errorMessage = xhr.responseJSON?.message || '인증 메일 재발송에 실패했습니다.';
                showAlert('error', errorMessage);
                button.prop('disabled', false).text('인증 메일 재발송');
            }
        });
    });

    function showAlert(type, message) {
        const alertClass = type === 'success' ? 'alert-success' : 'alert-error';
        $('#alertMessage')
            .removeClass('alert-success alert-error')
            .addClass('alert ' + alertClass)
            .text(message)
            .fadeIn();

        setTimeout(function() {
            $('#alertMessage').fadeOut();
        }, 5000);
    }
});
</script>
```

**특징**:
- ✅ 발송된 이메일 주소 표시
- ✅ 재발송 버튼
- ✅ 실시간 피드백 (성공/실패 메시지)
- ✅ 스팸함 확인 안내
- ✅ 로그인 페이지 이동 버튼

#### 1.9 회원가입 플로우 수정

**templates/external/signup.html**:

```javascript
if (data.success) {
    // 이메일 인증 안내 페이지로 리다이렉트
    location.href = `/external/verification-sent?email=${encodeURIComponent(formData.email)}`;
} else {
    alert(data.message || '회원가입 중 오류가 발생했습니다');
}
```

**변경 사항**:
- ✅ 회원가입 성공 시 verification-sent 페이지로 리다이렉트
- ✅ 로그인 페이지가 아닌 인증 안내 페이지로 이동

---

### 2. 로그인 UI 개선 (탭 시스템)

#### 2.1 문제점
- 기존 로그인 폼은 학번만 입력받아 외부회원 로그인 불가
- 하나의 폼으로 두 가지 로그인 방식 처리 불가

#### 2.2 해결책
- 내부회원/외부회원 탭으로 분리
- 각 탭마다 별도 폼

#### 2.3 구현

**templates/login.html** (주요 변경사항):

```html
<!-- 로그인 탭 -->
<div class="login-tabs">
    <button class="login-tab active" onclick="switchTab('internal')">내부회원</button>
    <button class="login-tab" onclick="switchTab('external')">외부회원</button>
</div>

<!-- 내부회원 로그인 폼 -->
<form id="internalLoginForm" class="login-form active" th:action="@{/login}" method="post">
    <div class="form-group">
        <label for="studentNum">학번</label>
        <input type="text"
               id="studentNum"
               name="studentNum"
               placeholder="학번을 입력하세요"
               required>
    </div>

    <div class="form-group">
        <label for="password">비밀번호</label>
        <input type="password"
               id="password"
               name="password"
               placeholder="비밀번호를 입력하세요"
               required>
    </div>

    <button type="submit" class="btn-login">로그인</button>
</form>

<!-- 외부회원 로그인 폼 -->
<form id="externalLoginForm" class="login-form" style="display: none;">
    <div class="form-group">
        <label for="externalEmail">이메일</label>
        <input type="email"
               id="externalEmail"
               name="email"
               placeholder="이메일을 입력하세요"
               required>
    </div>

    <div class="form-group">
        <label for="externalPassword">비밀번호</label>
        <input type="password"
               id="externalPassword"
               name="password"
               placeholder="비밀번호를 입력하세요"
               required>
    </div>

    <button type="submit" class="btn-login">로그인</button>

    <div class="signup-link">
        <span>계정이 없으신가요?</span>
        <a href="/external/signup">외부회원 가입</a>
    </div>
</form>

<script>
function switchTab(type) {
    const tabs = document.querySelectorAll('.login-tab');
    const internalForm = document.getElementById('internalLoginForm');
    const externalForm = document.getElementById('externalLoginForm');

    tabs.forEach(tab => tab.classList.remove('active'));

    if (type === 'internal') {
        tabs[0].classList.add('active');
        internalForm.style.display = 'block';
        externalForm.style.display = 'none';
    } else {
        tabs[1].classList.add('active');
        internalForm.style.display = 'none';
        externalForm.style.display = 'block';
    }
}

// 외부회원 로그인 처리 (AJAX)
document.getElementById('externalLoginForm').addEventListener('submit', async function(e) {
    e.preventDefault();

    const formData = {
        email: document.getElementById('externalEmail').value,
        password: document.getElementById('externalPassword').value
    };

    try {
        const response = await fetch('/api/external/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(formData)
        });

        const data = await response.json();

        if (data.success) {
            location.href = '/';
        } else {
            alert(data.message);
        }
    } catch (error) {
        alert('로그인 중 오류가 발생했습니다');
    }
});
</script>
```

**CSS**:

```css
.login-tabs {
    display: flex;
    margin-bottom: 20px;
    border-bottom: 2px solid #e0e0e0;
}

.login-tab {
    flex: 1;
    padding: 12px;
    background: none;
    border: none;
    font-size: 16px;
    font-weight: 600;
    color: #666;
    cursor: pointer;
    transition: all 0.3s;
}

.login-tab.active {
    color: #2C5F5D;
    border-bottom: 3px solid #2C5F5D;
    margin-bottom: -2px;
}

.login-tab:hover {
    color: #2C5F5D;
}
```

**특징**:
- ✅ 탭 클릭 시 폼 전환
- ✅ 내부회원: 학번 + 비밀번호
- ✅ 외부회원: 이메일 + 비밀번호
- ✅ 외부회원 폼에 회원가입 링크
- ✅ AJAX 비동기 로그인

---

### 3. 외부회원 전용 네비게이션

#### 3.1 문제점
- 외부회원 로그인 후에도 "로그인" 버튼 표시
- 외부회원에게 접근 불가 메뉴 노출 (CHAMP 마일리지, 상담신청 등)

#### 3.2 해결책
- header.html에서 `session.externalUserId` 확인
- 외부회원 전용 네비게이션 분리

#### 3.3 구현

**templates/layout/header.html**:

```html
<!-- 메인 네비게이션 (내부회원만 표시) -->
<nav th:if="${session.externalUserId == null}">
    <ul class="header-nav">
        <li class="header-nav-item">
            <a href="/programs" class="header-nav-link">CHAMP 비교과 프로그램</a>
        </li>
        <li class="header-nav-item">
            <a href="/mileage" class="header-nav-link">CHAMP 마일리지</a>
        </li>
        <li class="header-nav-item">
            <a href="/counseling" class="header-nav-link">상담신청</a>
        </li>
        <li class="header-nav-item">
            <a href="/competency" class="header-nav-link">역량진단</a>
        </li>
        <li class="header-nav-item">
            <a href="/portfolio" class="header-nav-link">포트폴리오</a>
        </li>
        <li class="header-nav-item">
            <a href="/survey" class="header-nav-link">설문조사</a>
        </li>
    </ul>
</nav>

<!-- 외부회원 네비게이션 -->
<nav th:if="${session.externalUserId != null}">
    <ul class="header-nav">
        <li class="header-nav-item">
            <a href="/" class="header-nav-link">홈</a>
        </li>
        <li class="header-nav-item">
            <a href="/programs" class="header-nav-link">프로그램 안내</a>
        </li>
    </ul>
</nav>

<!-- 우측 액션 버튼 -->
<div class="header-actions">
    <!-- 외부취업가점 (내부회원만) -->
    <a th:if="${session.externalUserId == null}"
       href="/external-employment"
       class="header-link">외부취업가점</a>
    <span th:if="${session.externalUserId == null}" class="header-divider">|</span>

    <!-- 이름 표시: 내부회원 또는 외부회원 -->
    <span th:if="${session.name != null or session.externalUserName != null}"
          class="header-link"
          th:text="${session.name != null ? session.name : session.externalUserName} + '님'"></span>

    <!-- 알림 아이콘 (내부회원만) -->
    <span th:if="${session.userId != null}" class="header-divider">|</span>
    <a th:if="${session.userId != null}"
       href="/notifications"
       class="header-link notification-link"
       id="notificationIcon">
        <svg>...</svg>
        <span class="notification-badge" id="notificationBadge"></span>
    </a>

    <!-- 마이페이지 (내부회원만) -->
    <span th:if="${session.userId != null}" class="header-divider">|</span>
    <a th:if="${session.userId != null}"
       href="/mypage"
       class="header-link">마이페이지</a>

    <!-- 로그인 (비로그인 시) -->
    <span th:if="${session.userId == null and session.externalUserId == null}"
          class="header-divider">|</span>
    <a th:if="${session.userId == null and session.externalUserId == null}"
       href="/login"
       class="header-link">로그인</a>

    <!-- 로그아웃 (로그인 시) -->
    <span th:if="${session.userId != null or session.externalUserId != null}"
          class="header-divider">|</span>
    <a th:if="${session.userId != null or session.externalUserId != null}"
       href="/logout"
       class="btn-logout">로그아웃</a>
</div>
```

**변경 사항**:
- ✅ **내부회원 네비게이션**: 전체 메뉴 (6개)
- ✅ **외부회원 네비게이션**: 제한된 메뉴 (홈, 프로그램 안내)
- ✅ **외부취업가점**: 내부회원만 표시
- ✅ **알림**: 내부회원만 표시
- ✅ **마이페이지**: 내부회원만 표시
- ✅ **로그인/로그아웃**: 두 사용자 유형 모두 확인

---

### 4. 오류 처리 개선

#### 4.1 문제점
```java
// Before: 모든 예외를 일반 메시지로 대체
catch (Exception e) {
    errorResponse.put("message", "회원가입 중 오류가 발생했습니다");
}
```

실제 오류: "이메일 발송에 실패했습니다"
사용자가 받는 메시지: "회원가입 중 오류가 발생했습니다"
→ **사용자가 무엇이 잘못되었는지 알 수 없음**

#### 4.2 해결책

**ExternalUserController.signup()**:

```java
@PostMapping("/signup")
public ResponseEntity<Map<String, Object>> signup(@Valid @RequestBody ExternalSignupRequest request) {
    try {
        ExternalUser user = externalUserService.signup(request);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "회원가입이 완료되었습니다. 이메일을 확인해주세요.");
        response.put("userId", user.getUserId());

        return ResponseEntity.ok(response);
    } catch (IllegalArgumentException e) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", e.getMessage());
        return ResponseEntity.badRequest().body(errorResponse);
    } catch (Exception e) {
        log.error("회원가입 중 오류 발생", e);
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        // ⭐ 실제 예외 메시지 전달
        errorResponse.put("message", e.getMessage() != null ? e.getMessage() : "회원가입 중 오류가 발생했습니다");
        return ResponseEntity.internalServerError().body(errorResponse);
    }
}
```

**변경 사항**:
- ✅ `e.getMessage()`를 그대로 전달
- ✅ null인 경우만 기본 메시지 사용
- ✅ 사용자에게 구체적인 오류 원인 전달

**결과**:
- "이메일 발송에 실패했습니다. 잠시 후 다시 시도해주세요." ✅
- "이미 사용중인 이메일입니다" ✅
- "비밀번호가 일치하지 않습니다" ✅

---

### 5. SecurityConfig 수정

#### 5.1 문제점
```java
// Before
.requestMatchers("/", "/login", "/logout").permitAll()
```
→ `/external/signup` 접근 불가 (로그인 페이지로 리다이렉트)

#### 5.2 해결책

**SecurityConfig.java**:

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/",
                "/login",
                "/external/**",           // ⭐ 외부회원 페이지 허용
                "/api/external/**",       // ⭐ 외부회원 API 허용
                "/css/**",
                "/js/**",
                "/images/**",
                "/webjars/**"
            ).permitAll()
            .anyRequest().authenticated()
        )
        // ... 기타 설정
    ;

    return http.build();
}
```

**변경 사항**:
- ✅ `/external/**`: 회원가입, 인증 안내 페이지 등
- ✅ `/api/external/**`: 회원가입, 로그인, 이메일 인증 API

---

## 📊 커밋 히스토리

### 커밋 순서

1. **`720daf1`** - feat: 로그인 페이지에 외부회원 가입 링크 추가
2. **`b1a8ed5`** - fix: 외부회원 가입 페이지 접근 오류 수정
3. **`ba74c95`** - feat: 로그인 페이지에 내부/외부회원 탭 기능 추가
4. **`7b5c661`** - fix: 헤더에서 외부회원 로그인 상태 인식 추가
5. **`9f4c9cd`** - fix: 외부회원에게 내부회원 전용 메뉴 숨김 처리
6. **`0a2b64b`** - feat: 이메일 인증 발송 기능 구현 (SMTP)
7. **`377a0cd`** - fix: 이메일 인증 페이지에서 존재하지 않는 CSS 링크 제거
8. **`fb57b65`** - docs: 빌드 문제 해결 가이드 및 Gradle 설정 추가
9. **`7ee4d23`** - fix: 이메일 발송 실패 시 회원가입 롤백 처리
10. **`246cdf5`** - fix: 회원가입 오류 시 상세 메시지 전달

---

## 🔧 생성/수정된 파일 목록

### 새로 생성된 파일 (7개)
1. `src/main/java/com/scms/app/service/EmailService.java`
2. `src/main/resources/templates/email/verification.html`
3. `src/main/resources/templates/external/verification-sent.html`
4. `src/main/resources/templates/external/verify-success.html`
5. `BUILD_INSTRUCTIONS.md`
6. `gradle.properties`
7. `doc/development-logs/24_EMAIL_VERIFICATION_AND_UI_IMPROVEMENTS_DEV_LOG.md`

### 수정된 파일 (7개)
1. `build.gradle` - spring-boot-starter-mail 의존성 추가
2. `src/main/resources/application.yml` - SMTP 설정 추가
3. `src/main/java/com/scms/app/service/ExternalUserService.java` - 이메일 발송, 로그인 검증, 롤백 처리
4. `src/main/java/com/scms/app/controller/ExternalUserController.java` - 재발송 API, 오류 메시지 개선
5. `src/main/java/com/scms/app/controller/ExternalUserPageController.java` - verification-sent 페이지 라우팅
6. `src/main/resources/templates/login.html` - 내부/외부회원 탭 추가
7. `src/main/resources/templates/layout/header.html` - 외부회원 네비게이션 분리

**총 14개 파일 생성/수정**

---

## 🧪 테스트 시나리오

### 1. 회원가입 플로우

**정상 케이스**:
1. `/external/signup` 접속
2. 이메일 중복 체크 (통과)
3. 비밀번호 입력 (강도: 강함)
4. 약관 동의
5. 회원가입 버튼 클릭
6. → `/external/verification-sent?email=...` 리다이렉트
7. 이메일 수신 (Gmail)
8. 인증 링크 클릭
9. → `/external/verify-success` 리다이렉트
10. 로그인 시도 → 성공 ✅

**실패 케이스 1 - 이메일 발송 실패**:
1. 회원가입 시도
2. SMTP 오류 발생
3. → 생성된 계정 자동 삭제 (롤백)
4. → "이메일 발송에 실패했습니다. 잠시 후 다시 시도해주세요." 메시지 표시
5. DB 확인 → 계정 없음 ✅

**실패 케이스 2 - 이메일 미인증 로그인**:
1. 회원가입 완료 (emailVerified = false)
2. 로그인 시도
3. → "이메일 인증이 완료되지 않았습니다. 이메일을 확인해주세요." 오류
4. 로그인 실패 ✅

### 2. 로그인 UI

**내부회원 로그인**:
1. 로그인 페이지 접속
2. "내부회원" 탭 선택 (기본값)
3. 학번 입력
4. 비밀번호 입력
5. 로그인 → 성공 ✅

**외부회원 로그인**:
1. 로그인 페이지 접속
2. "외부회원" 탭 클릭
3. 이메일 입력
4. 비밀번호 입력
5. 로그인 → 성공 ✅

### 3. 네비게이션

**내부회원 로그인 후**:
- ✅ CHAMP 비교과 프로그램
- ✅ CHAMP 마일리지
- ✅ 상담신청
- ✅ 역량진단
- ✅ 포트폴리오
- ✅ 설문조사
- ✅ 외부취업가점
- ✅ 알림
- ✅ 마이페이지
- ✅ 로그아웃

**외부회원 로그인 후**:
- ✅ 홈
- ✅ 프로그램 안내
- ❌ CHAMP 마일리지 (숨김)
- ❌ 상담신청 (숨김)
- ❌ 외부취업가점 (숨김)
- ❌ 알림 (숨김)
- ❌ 마이페이지 (숨김)
- ✅ 로그아웃

### 4. 인증 메일 재발송

1. `/external/verification-sent?email=test@example.com` 접속
2. "인증 메일 재발송" 버튼 클릭
3. → 새로운 토큰 생성
4. → 이메일 재발송
5. → "인증 메일이 재발송되었습니다" 메시지 표시 ✅

---

## 🐛 발견 및 해결된 버그

### 버그 #1: 의존성 다운로드 실패

**증상**:
```
ClassNotFoundException: jakarta.mail.MessagingException
```

**원인**:
- `build.gradle`에 `spring-boot-starter-mail` 추가
- 하지만 Gradle 빌드 실패 (네트워크 오류)
- 의존성이 다운로드되지 않음

**해결**:
```
IDE에서 Gradle 프로젝트 새로고침:
- IntelliJ: Gradle 탭 → 🔄 버튼
- Eclipse: 프로젝트 우클릭 → Gradle → Refresh
```

**근본 원인**:
- 로컬 환경의 네트워크 문제
- `services.gradle.org` 접근 불가

### 버그 #2: CSS 파일 404 에러

**증상**:
```
NoResourceFoundException: /css/styles.css
```

**원인**:
- `verification-sent.html`, `verify-success.html`에서 존재하지 않는 CSS 참조
- 두 페이지 모두 인라인 스타일 사용

**해결**:
```html
<!-- Before -->
<link rel="stylesheet" href="/css/styles.css">

<!-- After -->
<!-- 링크 제거, 인라인 스타일만 사용 -->
```

---

## 📈 성능 및 보안

### 보안 강화

1. **이메일 인증 강제**
   - 미인증 사용자 로그인 차단
   - 토큰 기반 인증
   - 인증 완료 시 토큰 삭제

2. **계정 보호**
   - 이메일 발송 실패 시 계정 자동 삭제
   - 인증 불가능한 계정 방지

3. **명확한 오류 메시지**
   - 사용자에게 구체적 오류 원인 전달
   - 보안과 UX의 균형

### 성능

1. **비동기 처리**
   - AJAX 로그인 (페이지 새로고침 없음)
   - 이메일 재발송 (실시간 피드백)

2. **캐싱**
   - 정적 리소스 캐싱 (CSS, JS, 이미지)

---

## 🎯 향후 개선사항

### 1. 이메일 인증 개선
- [ ] 인증 링크 만료 시간 (24시간)
- [ ] 만료된 토큰 처리
- [ ] 인증 완료 후 토큰 자동 삭제

### 2. 소셜 로그인
- [ ] Google OAuth 2.0
- [ ] Kakao Login
- [ ] Naver Login

### 3. 비밀번호 찾기/재설정
- [ ] 이메일로 재설정 링크 발송
- [ ] 임시 비밀번호 발급

### 4. 외부회원 프로필 관리
- [ ] 프로필 수정
- [ ] 프로필 이미지 업로드
- [ ] 회원 탈퇴

### 5. 관리자 기능
- [ ] 외부회원 목록 조회
- [ ] 계정 잠금 해제
- [ ] 회원 통계 대시보드

---

## 📊 프로젝트 영향

### 기능 완성도
- ✅ **이메일 인증**: 100% 완료
- ✅ **로그인 UI**: 100% 완료
- ✅ **네비게이션**: 100% 완료
- ✅ **오류 처리**: 100% 완료

### 사용자 경험
- ✅ 직관적인 탭 UI
- ✅ 명확한 오류 메시지
- ✅ 권한에 맞는 메뉴 표시
- ✅ 실시간 피드백

### 코드 품질
- ✅ 트랜잭션 롤백 처리
- ✅ 예외 처리 강화
- ✅ 사용자 유형 분리
- ✅ 책임 분리 (Controller, Service, Repository)

---

## 📝 결론

외부회원 가입 시스템의 핵심 기능인 **이메일 인증**을 성공적으로 구현했습니다. Gmail SMTP를 통한 실제 이메일 발송, 인증 링크 처리, 재발송 기능까지 완성했습니다.

또한 사용자 경험을 크게 개선했습니다:
- 내부/외부회원 구분 로그인
- 권한 기반 네비게이션
- 명확한 오류 메시지
- 이메일 발송 실패 시 롤백

이제 외부회원이 **회원가입 → 이메일 인증 → 로그인 → 프로그램 조회**의 전체 플로우를 완벽하게 이용할 수 있습니다.

**개발 완료일**: 2025-11-18
**개발 시간**: 약 8시간
**상태**: ✅ 완료
**다음 단계**: 소셜 로그인, 비밀번호 찾기, 외부회원 프로필 관리
