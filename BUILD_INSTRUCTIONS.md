# 빌드 문제 해결 방법

## 문제
`ClassNotFoundException: jakarta.mail.MessagingException` 오류 발생

## 원인
spring-boot-starter-mail 의존성이 다운로드되지 않음

## 해결 방법

### 방법 1: IntelliJ IDEA 사용 시
1. IntelliJ IDEA에서 프로젝트 열기
2. `View` → `Tool Windows` → `Gradle` 
3. Gradle 탭에서 새로고침 버튼 (🔄) 클릭
4. 또는: `File` → `Invalidate Caches / Restart` → `Invalidate and Restart`

### 방법 2: Eclipse 사용 시
1. 프로젝트 우클릭
2. `Gradle` → `Refresh Gradle Project`

### 방법 3: 명령줄에서 빌드 (네트워크 문제 해결 후)
```bash
./gradlew clean build -x test
```

## 확인
애플리케이션 시작 시 오류 없이 실행되면 성공
