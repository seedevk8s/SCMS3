# 소셜 로그인 설정 가이드

## 개요
이 문서는 SCMS3 시스템에 Google, Kakao, Naver 소셜 로그인을 설정하는 방법을 안내합니다.

---

## 1. Kakao 설정 (KOE101 오류 해결)

### 1단계: 애플리케이션 생성
1. [Kakao Developers Console](https://developers.kakao.com/console/app) 접속
2. "애플리케이션 추가하기" 클릭
3. 앱 정보 입력
   - 앱 이름: `SCMS` (또는 원하는 이름)
   - 사업자명: `푸름대학교 학생성장지원센터`

### 2단계: 플랫폼 등록
1. 내 애플리케이션 > **앱 설정** > **플랫폼**
2. **Web 플랫폼 등록** 클릭
3. 사이트 도메인 입력: `http://localhost:8080`
4. 저장

### 3단계: 카카오 로그인 활성화
1. 내 애플리케이션 > **제품 설정** > **카카오 로그인**
2. **활성화 설정** 상태를 **ON**으로 변경
3. **Redirect URI** 등록
   - **Redirect URI 등록** 버튼 클릭
   - URI 입력: `http://localhost:8080/login/oauth2/code/kakao`
   - 저장

### 4단계: 동의 항목 설정 (필수!)
1. 내 애플리케이션 > **제품 설정** > **카카오 로그인** > **동의항목**
2. 다음 항목을 설정:

| 항목 | 설정 | 필수 여부 |
|------|------|-----------|
| 닉네임 | 필수 동의 | ✅ |
| 카카오계정(이메일) | 필수 동의 | ✅ |

3. **카카오계정(이메일)** 항목 설정 시:
   - "필수 동의"로 변경
   - 개인정보 보호정책 URL 입력 필요
     - 예시: `http://localhost:8080/privacy`
     - (없다면 임시로 `http://localhost:8080` 입력 가능)

### 5단계: REST API 키 복사
1. 내 애플리케이션 > **앱 설정** > **앱 키**
2. **REST API 키** 복사 (이것이 Client ID입니다)

### 6단계: application-local.yml 설정
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          kakao:
            client-id: 여기에_복사한_REST_API_키_입력
```

---

## 2. Google 설정

### 1단계: Google Cloud Console 접속
1. [Google Cloud Console](https://console.cloud.google.com/) 접속
2. 프로젝트 생성 또는 선택

### 2단계: OAuth 동의 화면 설정
1. **API 및 서비스** > **OAuth 동의 화면**
2. 사용자 유형: **외부** 선택
3. 앱 정보 입력
   - 앱 이름: `SCMS`
   - 사용자 지원 이메일: 본인 이메일
   - 개발자 연락처 정보: 본인 이메일
4. 저장 후 계속

### 3단계: OAuth 2.0 클라이언트 ID 생성
1. **API 및 서비스** > **사용자 인증 정보**
2. **사용자 인증 정보 만들기** > **OAuth 2.0 클라이언트 ID**
3. 애플리케이션 유형: **웹 애플리케이션**
4. 이름: `SCMS Web Client`
5. **승인된 리디렉션 URI** 추가:
   - `http://localhost:8080/login/oauth2/code/google`
6. 만들기 클릭
7. Client ID와 Client Secret 복사

### 4단계: application-local.yml 설정
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: 여기에_Client_ID_입력
            client-secret: 여기에_Client_Secret_입력
```

---

## 3. Naver 설정

### 1단계: 애플리케이션 등록
1. [Naver Developers](https://developers.naver.com/apps/#/register) 접속
2. 애플리케이션 등록 정보 입력
   - 애플리케이션 이름: `SCMS`
   - 사용 API: **네이버 로그인**
   - 제공 정보:
     - ✅ 회원이름
     - ✅ 이메일 주소
     - ✅ 프로필 사진

### 2단계: 서비스 URL 및 Callback URL 설정
1. **서비스 URL**: `http://localhost:8080`
2. **Callback URL**: `http://localhost:8080/login/oauth2/code/naver`
3. 등록하기 클릭

### 3단계: Client ID 및 Client Secret 확인
1. 애플리케이션 정보에서 **Client ID**와 **Client Secret** 확인
2. 복사

### 4단계: application-local.yml 설정
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          naver:
            client-id: 여기에_Client_ID_입력
            client-secret: 여기에_Client_Secret_입력
```

---

## 4. 전체 설정 파일 예시

`src/main/resources/application-local.yml` 파일 전체:

```yaml
# Local Development OAuth2 Configuration
# 이 파일은 .gitignore에 추가하여 커밋하지 마세요!

spring:
  security:
    oauth2:
      client:
        registration:
          # Google OAuth2
          google:
            client-id: 1234567890-abcdefg.apps.googleusercontent.com
            client-secret: GOCSPX-xxxxxxxxxxxxxxxxxxxxx

          # Kakao OAuth2 (REST API 키만 필요)
          kakao:
            client-id: abcdef1234567890abcdef1234567890

          # Naver OAuth2
          naver:
            client-id: aBcDeFgHiJkLmN
            client-secret: XxXxXxXxXx
```

---

## 5. 애플리케이션 실행

### 방법 1: Gradle 명령어로 실행
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

### 방법 2: IDE에서 실행
**IntelliJ IDEA:**
1. Run > Edit Configurations
2. Active profiles: `local` 입력
3. 실행

**Eclipse:**
1. Run Configurations
2. Arguments 탭 > VM arguments: `-Dspring.profiles.active=local`
3. 실행

---

## 6. 테스트

1. 브라우저에서 `http://localhost:8080/login` 접속
2. **외부회원** 탭 선택
3. 소셜 로그인 버튼(Google, Kakao, Naver) 클릭
4. 각 플랫폼 로그인 페이지로 이동 확인
5. 로그인 후 메인 페이지로 리디렉션 확인

---

## 7. 문제 해결

### Kakao KOE101 오류
- **원인**: Redirect URI 미등록, 카카오 로그인 비활성화, 동의항목 미설정
- **해결**: 위 1단계의 모든 과정을 확인

### Google redirect_uri_mismatch
- **원인**: Redirect URI가 Google Console에 등록되지 않음
- **해결**: Google Console에서 `http://localhost:8080/login/oauth2/code/google` 등록 확인

### Naver invalid_request
- **원인**: Callback URL 오류
- **해결**: Naver Developers에서 `http://localhost:8080/login/oauth2/code/naver` 등록 확인

### 환경 변수 미설정
- **원인**: application-local.yml 파일이 없거나 값이 올바르지 않음
- **해결**: application-local.yml 파일 생성 및 올바른 값 입력

---

## 8. 운영 환경 배포 시 주의사항

운영 환경에 배포할 때는:

1. **각 플랫폼에 운영 도메인 추가 등록**
   - Google: `https://yourdomain.com/login/oauth2/code/google`
   - Kakao: `https://yourdomain.com/login/oauth2/code/kakao`
   - Naver: `https://yourdomain.com/login/oauth2/code/naver`

2. **환경 변수로 설정**
   ```bash
   export GOOGLE_CLIENT_ID=your-production-google-id
   export GOOGLE_CLIENT_SECRET=your-production-google-secret
   export KAKAO_CLIENT_ID=your-production-kakao-id
   export NAVER_CLIENT_ID=your-production-naver-id
   export NAVER_CLIENT_SECRET=your-production-naver-secret
   ```

3. **application-local.yml은 서버에 포함하지 않음**
   - 운영 환경에서는 환경 변수만 사용

---

## 9. 참고 문서

- [Google OAuth 2.0 문서](https://developers.google.com/identity/protocols/oauth2)
- [Kakao 로그인 REST API](https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api)
- [Naver 로그인 API](https://developers.naver.com/docs/login/api/api.md)
