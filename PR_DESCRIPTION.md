# Pull Request: 소셜 로그인 (Google, Kakao, Naver) 기능 구현

## 📋 개요
외부회원 가입에 Google, Kakao, Naver 소셜 로그인 기능을 추가했습니다.

## 🎯 주요 기능

### 소셜 로그인 제공자
- ✅ **Google** OAuth2 로그인
- ✅ **Kakao** OAuth2 로그인
- ✅ **Naver** OAuth2 로그인

### 주요 기능
- 소셜 계정으로 간편 로그인/회원가입
- 최초 로그인 시 자동 회원가입 (이메일 인증 완료 상태)
- 기존 회원은 프로필 정보 자동 업데이트
- 소셜 로그인 사용자와 일반 사용자 통합 관리
- 세션 기반 인증 유지

## 🔧 구현 내용

### 1. 데이터베이스
- **V10 마이그레이션**: external_users 테이블에 소셜 로그인 필드 추가
  - `provider`: 로그인 제공자 (LOCAL, GOOGLE, KAKAO, NAVER)
  - `provider_id`: 제공자별 사용자 고유 ID
  - `profile_image_url`: 프로필 이미지 URL
  - `password` 필드 NULL 허용 (소셜 로그인 사용자는 비밀번호 없음)

### 2. 백엔드 구현

**의존성**
- Spring Security OAuth2 Client 추가

**OAuth2 클래스**
- `OAuth2UserInfo` 인터페이스 및 구현체 (Google, Kakao, Naver)
- `CustomOAuth2UserService`: OAuth2 사용자 정보 처리 및 DB 저장
- `CustomOAuth2User`: OAuth2 Principal 객체
- `OAuth2AuthenticationSuccessHandler`: 로그인 성공 처리
- `OAuth2AuthenticationFailureHandler`: 로그인 실패 처리

**엔티티 및 설정**
- `ExternalUser`: provider, providerId, profileImageUrl 필드 추가
- `ExternalUserRepository`: 소셜 로그인 사용자 조회 메서드 추가
- `SecurityConfig`: OAuth2 로그인 설정 통합
- `application.yml`: Google, Kakao, Naver OAuth2 클라이언트 설정

### 3. 프론트엔드 구현

**로그인 페이지** (`login.html`)
- 외부회원 탭에 소셜 로그인 버튼 추가
- Google, Kakao, Naver 브랜드 컬러 적용

**회원가입 페이지** (`signup.html`)
- 상단에 소셜 로그인 버튼 추가
- "또는 이메일로 가입" 구분선 추가

## 📚 문서

### 설정 가이드
- **빠른 시작**: `doc/가이드/빠른_시작.md`
- **상세 가이드**: `doc/가이드/소셜_로그인_설정.md`

### Kakao KOE101 오류 해결
- Kakao OAuth 설정 간소화 (client-secret 제거)
- 필수 설정 체크리스트 제공
- 단계별 설정 가이드 작성

## 🧪 테스트 방법

### 1. OAuth 클라이언트 설정
각 플랫폼에서 OAuth 클라이언트를 발급받아 `application-local.yml`에 설정:

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: YOUR_GOOGLE_CLIENT_ID
            client-secret: YOUR_GOOGLE_CLIENT_SECRET
          kakao:
            client-id: YOUR_KAKAO_REST_API_KEY
          naver:
            client-id: YOUR_NAVER_CLIENT_ID
            client-secret: YOUR_NAVER_CLIENT_SECRET
```

### 2. 애플리케이션 실행
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

### 3. 테스트 시나리오
1. http://localhost:8080/login 접속
2. 외부회원 탭 선택
3. 소셜 로그인 버튼(Google, Kakao, Naver) 클릭
4. 각 플랫폼 로그인 페이지로 이동 확인
5. 로그인 후 메인 페이지로 리디렉션 확인
6. 세션 정보 확인

## 📁 변경된 파일

### 신규 생성 (13개)
- `V10__add_social_login_to_external_users.sql`
- `CustomOAuth2UserService.java`
- `CustomOAuth2User.java`
- `OAuth2AuthenticationSuccessHandler.java`
- `OAuth2AuthenticationFailureHandler.java`
- `OAuth2UserInfo.java`
- `GoogleUserInfo.java`
- `KakaoUserInfo.java`
- `NaverUserInfo.java`
- `OAuth2UserInfoFactory.java`
- `application-local.yml` (템플릿)
- `doc/가이드/소셜_로그인_설정.md`
- `doc/가이드/빠른_시작.md`

### 수정 (7개)
- `build.gradle`
- `application.yml`
- `SecurityConfig.java`
- `ExternalUser.java`
- `ExternalUserRepository.java`
- `login.html`
- `signup.html`

## ⚠️ 주의사항

### 환경 설정 필수
이 기능을 사용하려면 각 플랫폼에서 OAuth 클라이언트를 발급받아 설정해야 합니다:
- Google Cloud Console
- Kakao Developers
- Naver Developers

자세한 내용은 `doc/가이드/소셜_로그인_설정.md` 참조

### 보안
- `application-local.yml`은 `.gitignore`에 포함되어 커밋되지 않음
- 운영 환경에서는 환경 변수로 설정 권장

## 🔗 관련 링크
- [Google OAuth 2.0 문서](https://developers.google.com/identity/protocols/oauth2)
- [Kakao 로그인 REST API](https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api)
- [Naver 로그인 API](https://developers.naver.com/docs/login/api/api.md)
