# SCMS2 Spring Boot Setup Guide

## 프로젝트 개요
학생 역량 관리 시스템 (Student Competency Management System v2.0)

## 기술 스택
- **Backend**: Spring Boot 3.2.0
- **Template Engine**: Thymeleaf
- **Database**: MySQL 8.0+
- **Build Tool**: Gradle
- **Frontend**: Bootstrap 5.3.2, TOAST UI (Grid, Editor, Calendar, Chart)
- **Java Version**: 17

## 사전 요구사항
1. JDK 17 이상
2. MySQL 8.0 이상
3. Gradle 8.0+ (또는 포함된 Gradle Wrapper 사용)

## 데이터베이스 설정

### 1. MySQL 데이터베이스 생성
```sql
-- 개발 환경
CREATE DATABASE scms2_dev CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 운영 환경
CREATE DATABASE scms2_prod CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 기본 환경
CREATE DATABASE scms2 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 사용자 권한 설정 (선택사항)
```sql
CREATE USER 'scms2_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON scms2.* TO 'scms2_user'@'localhost';
GRANT ALL PRIVILEGES ON scms2_dev.* TO 'scms2_user'@'localhost';
GRANT ALL PRIVILEGES ON scms2_prod.* TO 'scms2_user'@'localhost';
FLUSH PRIVILEGES;
```

### 3. application.yml 설정 수정
`src/main/resources/application.yml` 파일에서 데이터베이스 연결 정보를 수정하세요:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/scms2?useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8
    username: root  # 또는 scms2_user
    password: password  # 실제 비밀번호로 변경
```

## 프로젝트 빌드 및 실행

### 1. 프로젝트 클론
```bash
git clone <repository-url>
cd SCMS2
```

### 2. Gradle Wrapper 초기화 (처음 한 번만)
```bash
gradle wrapper
```

### 3. 빌드
```bash
./gradlew clean build
```

### 4. 실행
```bash
./gradlew bootRun
```

또는 JAR 파일 직접 실행:
```bash
java -jar build/libs/scms2-1.0.0.jar
```

### 5. 프로파일별 실행
```bash
# 개발 환경
./gradlew bootRun --args='--spring.profiles.active=dev'

# 운영 환경
./gradlew bootRun --args='--spring.profiles.active=prod'
```

## 접속 정보
- **URL**: http://localhost:8080
- **학생 관리**: http://localhost:8080/students

## 프로젝트 구조
```
SCMS2/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── scms/
│   │   │           └── app/
│   │   │               ├── Scms2Application.java     # 메인 애플리케이션
│   │   │               ├── config/                   # 설정 클래스
│   │   │               ├── controller/               # 컨트롤러
│   │   │               ├── model/                    # 엔티티 클래스
│   │   │               ├── repository/               # JPA 리포지토리
│   │   │               ├── service/                  # 서비스 계층
│   │   │               └── dto/                      # DTO 클래스
│   │   └── resources/
│   │       ├── application.yml                       # 기본 설정
│   │       ├── application-dev.yml                   # 개발 환경 설정
│   │       ├── application-prod.yml                  # 운영 환경 설정
│   │       ├── static/                              # 정적 리소스
│   │       │   ├── css/
│   │       │   ├── js/
│   │       │   └── images/
│   │       └── templates/                           # Thymeleaf 템플릿
│   │           ├── layout/                          # 레이아웃
│   │           ├── fragments/                       # 공통 조각
│   │           ├── students/                        # 학생 관리 뷰
│   │           └── index.html                       # 홈페이지
│   └── test/
│       └── java/                                    # 테스트 코드
├── build.gradle                                     # Gradle 빌드 설정
├── settings.gradle                                  # Gradle 설정
└── README.md
```

## 주요 기능

### 1. 학생 관리
- TOAST UI Grid를 사용한 학생 목록 표시
- 학생 추가, 수정, 삭제 (CRUD)
- 학번, 이름, 이메일, 전화번호, 학과, 학년, 상태 관리

### 2. TOAST UI 통합
- **TOAST UI Grid**: 학생 목록 표시 및 관리
- **TOAST UI Chart**: 통계 차트 표시
- **TOAST UI Editor**: 텍스트 에디터 (추후 활용)
- **TOAST UI Calendar**: 일정 관리 (추후 활용)

### 3. Thymeleaf Layout
- 레이아웃 다이얼렉트를 사용한 템플릿 구조
- 재사용 가능한 헤더, 푸터 프래그먼트

## API 엔드포인트

### 학생 관리 API
- `GET /students` - 학생 목록 페이지
- `GET /students/api` - 전체 학생 조회 (JSON)
- `GET /students/api/{id}` - 특정 학생 조회 (JSON)
- `POST /students/api` - 학생 추가 (JSON)
- `PUT /students/api/{id}` - 학생 수정 (JSON)
- `DELETE /students/api/{id}` - 학생 삭제

## 개발 가이드

### Hot Reload 설정
Spring Boot DevTools가 포함되어 있어 코드 변경 시 자동으로 재시작됩니다.

### 로깅
- 애플리케이션 로그: `com.scms` 패키지는 DEBUG 레벨
- Spring 프레임워크: INFO 레벨
- Hibernate: INFO 레벨

### 데이터베이스 초기화
JPA의 `ddl-auto` 설정:
- **개발(dev)**: `create-drop` - 매번 재생성
- **기본**: `update` - 변경사항만 업데이트
- **운영(prod)**: `validate` - 검증만 수행

## 트러블슈팅

### 1. 데이터베이스 연결 오류
- MySQL 서비스가 실행 중인지 확인
- application.yml의 데이터베이스 정보 확인
- 방화벽 설정 확인

### 2. 포트 충돌
application.yml에서 포트 변경:
```yaml
server:
  port: 8081  # 원하는 포트로 변경
```

### 3. Gradle 빌드 오류
```bash
./gradlew clean
./gradlew build --refresh-dependencies
```

## 참고 자료
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Thymeleaf Documentation](https://www.thymeleaf.org/)
- [TOAST UI Documentation](https://ui.toast.com/)
- [MySQL Documentation](https://dev.mysql.com/doc/)

## 라이센스
Copyright © 2024 SCMS2 Project
