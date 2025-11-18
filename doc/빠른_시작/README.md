# 소셜 로그인 빠른 시작 가이드

## 🚀 5분 안에 시작하기

### 1️⃣ Kakao 설정 (가장 빠름)

#### A. 카카오 개발자 콘솔 설정

**1. 애플리케이션 생성**
- https://developers.kakao.com/console/app 접속
- "애플리케이션 추가하기" → 앱 이름 입력

**2. Web 플랫폼 등록**
- 앱 설정 > 플랫폼 > Web 플랫폼 등록
- 사이트 도메인: `http://localhost:8080`

**3. 카카오 로그인 활성화 ⭐**
- 제품 설정 > 카카오 로그인
- 활성화 설정: **ON**
- Redirect URI: `http://localhost:8080/login/oauth2/code/kakao` 등록

**4. 동의항목 설정 ⭐**
- 제품 설정 > 카카오 로그인 > 동의항목
- 닉네임: **필수 동의**
- 카카오계정(이메일): **필수 동의**
  - 개인정보 보호정책 URL: `http://localhost:8080/privacy` (임시)

**5. REST API 키 복사**
- 앱 설정 > 앱 키 > REST API 키 복사

#### B. 프로젝트 설정

**application-local.yml 파일 수정**
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          kakao:
            client-id: 여기에_복사한_REST_API_키_붙여넣기
```

#### C. 실행
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

#### D. 테스트
1. http://localhost:8080/login 접속
2. 외부회원 탭 > Kakao 버튼 클릭
3. 카카오 로그인 완료!

---

### 2️⃣ Google 설정

**1. Google Cloud Console**
- https://console.cloud.google.com/ 접속
- 프로젝트 생성

**2. OAuth 동의 화면**
- API 및 서비스 > OAuth 동의 화면
- 외부 선택 > 앱 이름 입력

**3. 사용자 인증 정보**
- OAuth 2.0 클라이언트 ID 만들기
- 웹 애플리케이션 선택
- 승인된 리디렉션 URI: `http://localhost:8080/login/oauth2/code/google`

**4. application-local.yml**
```yaml
google:
  client-id: 복사한_Client_ID
  client-secret: 복사한_Client_Secret
```

---

### 3️⃣ Naver 설정

**1. Naver Developers**
- https://developers.naver.com/apps/#/register 접속
- 사용 API: 네이버 로그인
- 제공 정보: 회원이름, 이메일주소, 프로필 사진

**2. Callback URL**
- `http://localhost:8080/login/oauth2/code/naver`

**3. application-local.yml**
```yaml
naver:
  client-id: 복사한_Client_ID
  client-secret: 복사한_Client_Secret
```

---

## ✅ 체크리스트

### Kakao (KOE101 오류 방지)
- [ ] Web 플랫폼 등록 완료
- [ ] 카카오 로그인 **활성화 ON**
- [ ] Redirect URI 등록 (`http://localhost:8080/login/oauth2/code/kakao`)
- [ ] 닉네임 필수 동의 설정
- [ ] 이메일 필수 동의 설정
- [ ] REST API 키를 application-local.yml에 입력

### Google
- [ ] OAuth 동의 화면 설정
- [ ] 리디렉션 URI 등록
- [ ] Client ID/Secret을 application-local.yml에 입력

### Naver
- [ ] Callback URL 등록
- [ ] Client ID/Secret을 application-local.yml에 입력

---

## 🎯 전체 설정 파일 예시

**src/main/resources/application-local.yml**
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: 1234567890-abc.apps.googleusercontent.com
            client-secret: GOCSPX-xxxxxxxxxxxx

          kakao:
            client-id: abcdef1234567890abcdef1234

          naver:
            client-id: aBcDeFgHiJ
            client-secret: XxXxXxXxXx
```

---

## 📌 자주 묻는 질문

**Q: Kakao KOE101 오류가 계속 나요**
- A: 카카오 로그인 활성화가 **ON**인지 확인
- A: Redirect URI가 정확한지 확인
- A: 이메일 동의항목이 **필수 동의**로 설정되었는지 확인

**Q: application-local.yml은 어디에 있나요?**
- A: `src/main/resources/application-local.yml` 직접 생성
- A: 이미 생성되어 있으면 값만 수정

**Q: 실행이 안돼요**
- A: `--spring.profiles.active=local` 옵션 확인
- A: application-local.yml 파일 경로 확인

**Q: 로그인 버튼을 클릭해도 아무 반응이 없어요**
- A: 브라우저 콘솔(F12) 에러 확인
- A: 서버 로그 확인
- A: Client ID가 올바르게 입력되었는지 확인

---

## 📚 더 자세한 정보

상세한 설정 가이드: [SOCIAL_LOGIN_SETUP.md](./SOCIAL_LOGIN_SETUP.md)
