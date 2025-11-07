# 프로젝트 개요 (Project Overview)

푸름대학교 학생성장지원센터 CHAMP 시스템

---

## 프로젝트 소개

**푸름대학교 학생성장지원센터 CHAMP (SCMS2)**는 학생들의 핵심역량을 관리하고, 비교과 프로그램을 운영하며, 마일리지를 통한 학생 활동을 장려하는 통합 학생 성장 지원 시스템입니다.

### 프로젝트 목표

1. **학생 역량 관리**: 핵심역량 진단 및 분석
2. **비교과 프로그램 운영**: 다양한 프로그램 신청 및 관리
3. **마일리지 시스템**: 활동 참여 독려 및 보상
4. **상담 서비스**: 학생 상담 신청 및 관리
5. **포트폴리오 관리**: 학생 활동 기록 및 관리

---

## 시스템 구조

### 기술 스택

#### Backend
- **Language**: Java 17
- **Framework**: Spring Boot 3.x
- **Security**: Spring Security (Session-based Authentication)
- **ORM**: Spring Data JPA
- **Database**: MySQL 8.0
- **Build Tool**: Maven

#### Frontend
- **Template Engine**: Thymeleaf
- **Markup**: HTML5
- **Styling**: CSS3
- **Script**: JavaScript (Vanilla)
- **Layout**: Thymeleaf Layout Dialect

### 아키텍처 패턴

- **MVC (Model-View-Controller)**: Spring MVC 패턴
- **Repository Pattern**: Spring Data JPA
- **Service Layer**: 비즈니스 로직 분리
- **DTO Pattern**: 데이터 전송 객체 사용
- **Soft Delete Pattern**: 데이터 논리적 삭제

---

## 주요 기능

### 1. 인증 및 권한 관리

**구현 완료**:
- 학생/관리자 로그인
- 세션 기반 인증 (Spring Security)
- 역할 기반 접근 제어 (RBAC)
- 비밀번호 변경 (최초 로그인 시)
- 비밀번호 찾기

**사용 기술**:
- Spring Security
- BCrypt 암호화
- HttpSession + SecurityContext

### 2. 홈 페이지

**구현 완료**:
- 히어로 슬라이더 (3개 슬라이드, 자동 회전)
- 4개 역량 아이콘 그리드
- 프로그램 카드 그리드 (8개)
- 필터 섹션
- 반응형 디자인

**사용 기술**:
- Thymeleaf 템플릿
- CSS Grid/Flexbox
- JavaScript 슬라이더
- CSS 애니메이션

### 3. 비교과 프로그램 관리

**계획 중**:
- 프로그램 목록 조회
- 프로그램 상세 정보
- 프로그램 신청/취소
- 프로그램 참여 현황
- 프로그램 검색/필터링

### 4. 핵심역량 진단

**계획 중**:
- 역량 진단 문항 응답
- 진단 결과 분석
- 역량 그래프 시각화
- 역량 향상 추천

### 5. CHAMP 마일리지

**계획 중**:
- 마일리지 적립/차감
- 마일리지 내역 조회
- 마일리지 순위
- 마일리지 보상

### 6. 상담 신청

**계획 중**:
- 상담 신청
- 상담 일정 조회
- 상담 이력 관리
- 상담 승인/거부 (관리자)

### 7. 포트폴리오

**계획 중**:
- 포트폴리오 작성
- 활동 기록 관리
- 첨부파일 업로드
- 포트폴리오 조회/수정

### 8. 설문조사

**계획 중**:
- 설문 참여
- 설문 결과 조회
- 설문 문항 관리 (관리자)
- 설문 결과 집계 (관리자)

---

## 데이터베이스 구조

### 현재 구현된 테이블

#### User (사용자)
```
- id (PK): Long
- studentNum: String (학번, 고유값)
- name: String (이름)
- password: String (BCrypt 암호화)
- email: String (이메일)
- phone: String (전화번호)
- birthdate: LocalDate (생년월일)
- department: String (학과)
- role: Role (STUDENT/ADMIN)
- firstLogin: Boolean (최초 로그인 여부)
- deleted: Boolean (소프트 삭제)
- createdAt: LocalDateTime
- updatedAt: LocalDateTime
```

### 계획 중인 테이블

#### Program (프로그램)
- 프로그램 정보 (제목, 설명, 부서)
- 신청 기간, 운영 기간
- 정원, 현재 참여 인원
- 마일리지 점수
- 역량 카테고리

#### ProgramApplication (프로그램 신청)
- 학생-프로그램 다대다 관계
- 신청 상태 (대기/승인/거부/취소)
- 신청일, 승인일

#### Assessment (역량 진단)
- 진단 문항
- 진단 결과
- 역량 점수

#### Mileage (마일리지)
- 적립/차감 내역
- 사유
- 잔액

#### Counseling (상담)
- 상담 신청 정보
- 상담 일정
- 상담 내용
- 상담 상태

#### Portfolio (포트폴리오)
- 활동 기록
- 첨부파일
- 작성일, 수정일

#### Survey (설문조사)
- 설문 제목, 설명
- 설문 문항
- 설문 응답

---

## 디렉토리 구조

```
SCMS2/
├── src/
│   ├── main/
│   │   ├── java/com/scms/app/
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java        # Spring Security 설정
│   │   │   │   ├── WebConfig.java             # Web MVC 설정
│   │   │   │   └── DataLoader.java            # 초기 데이터 로딩
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java        # 인증 API
│   │   │   │   ├── UserController.java        # 사용자 관리 API
│   │   │   │   └── HomeController.java        # 페이지 라우팅
│   │   │   ├── service/
│   │   │   │   └── UserService.java           # 사용자 비즈니스 로직
│   │   │   ├── repository/
│   │   │   │   └── UserRepository.java        # 사용자 JPA Repository
│   │   │   ├── entity/
│   │   │   │   └── User.java                  # 사용자 엔티티
│   │   │   └── dto/
│   │   │       ├── LoginRequest.java          # 로그인 요청 DTO
│   │   │       └── LoginResponse.java         # 로그인 응답 DTO
│   │   └── resources/
│   │       ├── templates/
│   │       │   ├── layout/
│   │       │   │   ├── layout.html            # 공통 레이아웃
│   │       │   │   └── header.html            # 공통 헤더
│   │       │   ├── index.html                 # 홈 페이지
│   │       │   ├── login.html                 # 로그인 페이지
│   │       │   ├── password-change.html       # 비밀번호 변경
│   │       │   └── password-reset.html        # 비밀번호 찾기
│   │       ├── static/
│   │       │   ├── css/
│   │       │   │   └── common.css             # 공통 스타일
│   │       │   ├── js/                        # JavaScript 파일
│   │       │   └── images/                    # 이미지 파일
│   │       └── application.yml                # 애플리케이션 설정
│   └── test/                                  # 테스트 코드
├── doc/
│   ├── DEVELOPMENT_LOG.md                     # 개발 로그
│   └── PROJECT_OVERVIEW.md                    # 프로젝트 개요 (이 문서)
├── ui/
│   ├── README.md                              # UI 참고 이미지 설명
│   ├── 1.png ~ 5.png                          # 우석대 CHAMP 스크린샷
│   └── ui (1-3).pptx                          # 초기 요구사항 PPT
├── pom.xml                                    # Maven 설정
└── README.md                                  # 프로젝트 README
```

---

## 개발 환경 설정

### 필요 소프트웨어

- **JDK**: Java 17 이상
- **MySQL**: 8.0 이상
- **Maven**: 3.6 이상
- **IDE**: IntelliJ IDEA / Eclipse / VS Code

### 데이터베이스 설정

```sql
CREATE DATABASE scms2 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'scms_user'@'localhost' IDENTIFIED BY 'scms_password';
GRANT ALL PRIVILEGES ON scms2.* TO 'scms_user'@'localhost';
FLUSH PRIVILEGES;
```

### 애플리케이션 설정

`src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/scms2?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul
    username: scms_user
    password: scms_password
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

### 실행 방법

```bash
# 프로젝트 클론
git clone https://github.com/seedevk8s/SCMS2.git
cd SCMS2

# Maven 빌드
mvn clean install

# 애플리케이션 실행
mvn spring-boot:run

# 브라우저에서 접속
# http://localhost:8080
```

---

## 테스트 계정

### 관리자
- **학번**: 9999999
- **비밀번호**: admin123

### 학생
- **학번**: 2024001 ~ 2021002 (8명)
- **비밀번호**: 생년월일 (yyMMdd 형식)
  - 예: 2000년 1월 1일 → 000101

---

## API 엔드포인트

### 인증 API

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| POST | `/api/auth/login` | 로그인 | 공개 |
| POST | `/api/auth/logout` | 로그아웃 | 인증 필요 |
| POST | `/api/auth/password/change` | 비밀번호 변경 | 공개 |
| POST | `/api/auth/password/reset` | 비밀번호 찾기 | 공개 |
| GET | `/api/auth/me` | 현재 사용자 정보 | 인증 필요 |

### 사용자 관리 API

| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | `/api/users` | 사용자 목록 조회 | 관리자 |
| GET | `/api/users/{id}` | 사용자 상세 조회 | 관리자 |
| POST | `/api/users` | 사용자 생성 | 관리자 |
| PUT | `/api/users/{id}` | 사용자 수정 | 관리자 |
| DELETE | `/api/users/{id}` | 사용자 삭제 | 관리자 |

### 페이지 라우팅

| Method | Path | 설명 | 권한 |
|--------|------|------|------|
| GET | `/` | 홈 페이지 | 공개 |
| GET | `/login` | 로그인 페이지 | 공개 |
| GET | `/password/change` | 비밀번호 변경 페이지 | 공개 |
| GET | `/password/reset` | 비밀번호 찾기 페이지 | 공개 |
| GET | `/programs` | 프로그램 목록 | 인증 필요 |
| GET | `/mileage` | 마일리지 페이지 | 인증 필요 |
| GET | `/counseling` | 상담 신청 페이지 | 인증 필요 |
| GET | `/assessment` | 역량 진단 페이지 | 인증 필요 |
| GET | `/portfolio` | 포트폴리오 페이지 | 인증 필요 |
| GET | `/survey` | 설문조사 페이지 | 인증 필요 |

---

## 보안 정책

### 인증 및 인가
- 세션 기반 인증 (Spring Security)
- BCrypt 비밀번호 암호화
- 역할 기반 접근 제어 (RBAC)
- SecurityContext + HttpSession 하이브리드

### CSRF 보호
- Spring Security CSRF 토큰 사용
- 모든 POST/PUT/DELETE 요청에 토큰 필요

### XSS 방어
- Thymeleaf 자동 이스케이프
- 사용자 입력 검증

### SQL Injection 방어
- JPA Prepared Statement 사용
- 파라미터 바인딩

---

## 성능 최적화

### 데이터베이스
- [ ] 인덱스 추가 (studentNum, email)
- [ ] 쿼리 최적화 (N+1 문제 해결)
- [ ] 커넥션 풀 설정

### 캐싱
- [ ] Spring Cache 적용
- [ ] Redis 도입 검토

### 프론트엔드
- [ ] CSS/JS 압축
- [ ] 이미지 최적화
- [ ] CDN 사용 검토

---

## 배포

### 개발 환경
- **URL**: http://localhost:8080
- **Database**: localhost MySQL

### 운영 환경 (계획)
- **Server**: AWS EC2 / Azure VM
- **Database**: AWS RDS / Azure Database
- **CI/CD**: GitHub Actions
- **Monitoring**: Spring Boot Actuator + Prometheus + Grafana

---

## 라이선스

이 프로젝트는 푸름대학교 학생성장지원센터를 위한 내부 시스템으로, 상업적 사용이 제한됩니다.

---

## 참고 자료

- **우석대학교 CHAMP**: https://champ.woosuk.ac.kr
- **Spring Boot Documentation**: https://spring.io/projects/spring-boot
- **Spring Security Documentation**: https://spring.io/projects/spring-security
- **Thymeleaf Documentation**: https://www.thymeleaf.org

---

## 연락처

- **프로젝트 관리자**: seedevk8s
- **GitHub**: https://github.com/seedevk8s/SCMS2
- **이슈 트래킹**: GitHub Issues

---

**문서 작성일**: 2025년 11월 7일
**마지막 업데이트**: 2025년 11월 7일
