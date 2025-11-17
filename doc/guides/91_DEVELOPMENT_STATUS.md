# SCMS2 개발 현황 문서

## 프로젝트 개요

**프로젝트명:** 푸름대학교 학생성장지원센터 CHAMP (Student Competency Management System 2)

**목적:** 학생들의 역량 관리, 비교과 프로그램 참여, 상담, 마일리지 관리 등을 통합 관리하는 시스템

**기술 스택:**
- Backend: Spring Boot 3.x, Java 17
- Database: MySQL 8.0
- ORM: Spring Data JPA
- Security: Spring Security + BCrypt 암호화
- Template Engine: Thymeleaf
- Build Tool: Gradle

---

## 현재 구현 완료 사항

### 1. 데이터베이스 설계 및 구축

#### 1.1 사용자 관련 테이블
- **users** - 통합 사용자 테이블 (학생, 상담사, 관리자)
  - 학번 기반 인증
  - BCrypt 비밀번호 암호화
  - 역할 기반 접근 제어 (STUDENT, COUNSELOR, ADMIN)
  - 계정 잠금 기능 (5회 로그인 실패 시)
  - Soft Delete 지원

- **login_history** - 로그인 이력 추적
  - IP 주소, User Agent 기록
  - 성공/실패 이력 관리
  - 실패 사유 저장

- **counselors** - 상담사 추가 정보
  - 전문 분야, 자격증
  - 상담 가능 여부

#### 1.2 학생 정보 테이블
- **students** - 학생 기본 정보 (기존 테이블, 추후 users 테이블로 통합 예정)

#### 1.3 역량 관리 테이블
- **competency_categories** - 역량 카테고리
- **competencies** - 역량 상세
- **student_competency_assessments** - 학생별 역량 평가 결과

### 2. 인증 시스템 (완료)

#### 2.1 REST API 컨트롤러 (`AuthController`)

**엔드포인트:**
- `POST /api/auth/login` - 로그인
- `POST /api/auth/logout` - 로그아웃
- `POST /api/auth/password/change` - 비밀번호 변경
- `POST /api/auth/password/reset` - 비밀번호 찾기/재설정
- `GET /api/auth/me` - 현재 로그인 사용자 정보 조회

**주요 기능:**
- 학번 + 비밀번호 인증
- 세션 기반 인증 (HttpSession 사용)
- 로그인 이력 자동 기록
- 최초 로그인 감지 (초기 비밀번호 사용 시)
- IP 주소 및 User Agent 추적

#### 2.2 사용자 서비스 (`UserService`)

**구현된 기능:**
- ✅ 로그인 처리
  - 비밀번호 검증 (BCrypt)
  - 계정 잠금 확인
  - 로그인 실패 횟수 관리 (5회 실패 시 계정 잠금)
  - 로그인 이력 저장

- ✅ 비밀번호 관리
  - 비밀번호 변경
  - 비밀번호 재설정 (생년월일 확인)
  - 초기 비밀번호: 생년월일 6자리 (YYMMDD)

- ✅ 사용자 CRUD
  - 사용자 생성 (관리자)
  - 사용자 정보 수정
  - 사용자 조회 (ID, 학번, 역할별)
  - 사용자 삭제 (Soft Delete)

- ✅ 계정 관리
  - 계정 잠금 해제

### 3. 프론트엔드 UI (완료)

#### 3.1 레이아웃 구조
- **header.html** - 헤더 (로고, 네비게이션, 로그인/로그아웃)
- **footer.html** - 푸터 (링크, 저작권 정보)
- **layout.html** - 공통 레이아웃 (Thymeleaf Layout Dialect)

#### 3.2 페이지
- **index.html** - 메인 페이지
  - 히어로 섹션 (역량진단 안내)
  - 빠른 링크 (역량진단, 비교과 프로그램, 통합상담, 마일리지)
  - 프로그램 카드 그리드

- **login.html** - 로그인 페이지
  - 학번 + 비밀번호 입력
  - 비밀번호 표시/숨기기
  - AJAX 로그인 처리
  - 최초 로그인 시 비밀번호 변경 유도

#### 3.3 스타일
- **common.css** - 공통 스타일
  - 변수 기반 디자인 시스템
  - 반응형 레이아웃
  - 헤더 한 줄 고정 (UI 슬라이드 1-7 요구사항 반영)

### 4. 초기 데이터 로더 (`DataLoader`)

**자동 생성 데이터:**
- 학생 계정 8개
  - 학번: 2024001~2024002 (1학년), 2023001~2023002 (2학년), 2022001~2022002 (3학년), 2021001~2021002 (4학년)
  - 초기 비밀번호: 각 학생의 생년월일 6자리

- 관리자 계정 1개
  - 학번: 9999999
  - 비밀번호: admin123

**로그인 정보:**
```
학생 계정 예시:
- 학번: 2024001
- 비밀번호: 030101 (2003년 1월 1일 생)

관리자 계정:
- 학번: 9999999
- 비밀번호: admin123
```

### 5. 보안 설정 (`SecurityConfig`)

- Spring Security 설정
- BCrypt 비밀번호 인코더
- CORS 설정
- API 엔드포인트 권한 관리

---

## 데이터 플로우

### 로그인 프로세스

```
1. 사용자가 login.html에서 학번/비밀번호 입력
2. AJAX로 POST /api/auth/login 호출
3. AuthController가 요청 수신
4. UserService.login() 호출
   - 학번으로 사용자 조회
   - 계정 잠금 확인
   - 비밀번호 검증 (BCrypt)
   - 로그인 실패 시 fail_cnt 증가 (5회 시 계정 잠금)
   - 로그인 이력 저장 (login_history)
5. 세션에 사용자 정보 저장
6. LoginResponse 반환
7. 최초 로그인이면 비밀번호 변경 페이지로 리다이렉트
8. 아니면 메인 페이지로 이동
```

### 비밀번호 재설정 프로세스

```
1. 사용자가 "비밀번호 찾기" 클릭
2. 학번, 이름, 생년월일 입력
3. POST /api/auth/password/reset 호출
4. UserService.resetPassword() 호출
   - 학번으로 사용자 조회
   - 이름, 생년월일 일치 확인
   - 비밀번호를 생년월일 6자리로 초기화
   - 계정 잠금 해제
5. 사용자는 초기 비밀번호로 로그인 가능
```

---

## 프로젝트 구조

```
SCMS2/
├── src/main/
│   ├── java/com/scms/app/
│   │   ├── config/
│   │   │   ├── DataLoader.java          # 초기 데이터 로더
│   │   │   ├── SecurityConfig.java      # Spring Security 설정
│   │   │   └── WebConfig.java           # Web 설정
│   │   ├── controller/
│   │   │   ├── AuthController.java      # 인증 API
│   │   │   ├── HomeController.java      # 페이지 컨트롤러
│   │   │   ├── UserController.java      # 사용자 관리 API
│   │   │   └── StudentController.java   # 학생 관리 API
│   │   ├── dto/
│   │   │   ├── LoginRequest.java        # 로그인 요청
│   │   │   ├── LoginResponse.java       # 로그인 응답
│   │   │   ├── PasswordChangeRequest.java
│   │   │   ├── PasswordResetRequest.java
│   │   │   └── User*.java               # 사용자 DTO들
│   │   ├── exception/
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   ├── UserNotFoundException.java
│   │   │   ├── InvalidPasswordException.java
│   │   │   ├── AccountLockedException.java
│   │   │   └── DuplicateUserException.java
│   │   ├── model/
│   │   │   ├── User.java                # 사용자 엔티티
│   │   │   ├── Student.java             # 학생 엔티티
│   │   │   ├── Counselor.java           # 상담사 엔티티
│   │   │   ├── LoginHistory.java        # 로그인 이력
│   │   │   └── UserRole.java            # 역할 Enum
│   │   ├── repository/
│   │   │   ├── UserRepository.java
│   │   │   ├── StudentRepository.java
│   │   │   ├── CounselorRepository.java
│   │   │   └── LoginHistoryRepository.java
│   │   └── service/
│   │       ├── UserService.java         # 사용자 서비스
│   │       └── StudentService.java      # 학생 서비스
│   └── resources/
│       ├── static/
│       │   └── css/
│       │       ├── common.css           # 공통 스타일
│       │       └── style.css
│       └── templates/
│           ├── layout/
│           │   ├── header.html          # 헤더
│           │   ├── footer.html          # 푸터
│           │   └── layout.html          # 공통 레이아웃
│           ├── index.html               # 메인 페이지
│           └── login.html               # 로그인 페이지
├── database/
│   ├── schema.sql                       # DB 스키마
│   └── README.md
└── README.md
```

---

## 미구현 기능 및 TODO

### 1. 핵심 기능 (우선순위 높음)

#### 1.1 비교과 프로그램 관리
- [ ] 프로그램 등록/수정/삭제 (관리자)
- [ ] 프로그램 목록 조회
- [ ] 프로그램 상세 보기
- [ ] 프로그램 신청/취소 (학생)
- [ ] 신청 인원 관리

#### 1.2 CHAMP 마일리지 시스템
- [ ] 마일리지 적립 규칙 설정
- [ ] 활동별 마일리지 자동 적립
- [ ] 마일리지 내역 조회
- [ ] 마일리지 통계 (학생별, 학년별)

#### 1.3 통합상담 시스템
- [ ] 상담 신청 (학생)
- [ ] 상담 일정 관리
- [ ] 상담사 배정
- [ ] 상담 기록 관리
- [ ] 상담 이력 조회

#### 1.4 역량진단 시스템
- [ ] 진단 문항 관리
- [ ] 진단 실시
- [ ] 진단 결과 저장
- [ ] 결과 분석 및 리포트
- [ ] 역량별 추천 프로그램 연계

#### 1.5 포트폴리오 시스템
- [ ] 학생 포트폴리오 작성
- [ ] 교과/비교과 활동 기록
- [ ] 자격증/수상 내역 등록
- [ ] 포트폴리오 출력/다운로드

### 2. 관리 기능

#### 2.1 사용자 관리 (관리자)
- [x] 사용자 생성
- [x] 사용자 수정
- [x] 사용자 삭제
- [x] 계정 잠금 해제
- [ ] 사용자 관리 UI (관리자 페이지)
- [ ] 대량 사용자 등록 (Excel 업로드)

#### 2.2 통계 및 리포트
- [ ] 대시보드
- [ ] 프로그램 참여 통계
- [ ] 마일리지 통계
- [ ] 상담 통계
- [ ] 역량 진단 결과 통계

### 3. 추가 기능

#### 3.1 설문조사 시스템
- [ ] 설문 작성 (관리자)
- [ ] 설문 참여 (학생)
- [ ] 설문 결과 분석

#### 3.2 알림 시스템
- [ ] 이메일 알림
- [ ] 시스템 알림 (벨 아이콘)
- [ ] 프로그램 신청 마감 알림
- [ ] 상담 일정 알림

#### 3.3 게시판
- [ ] 공지사항
- [ ] FAQ
- [ ] 자료실

---

## 다음 개발 단계 권장 사항

### Phase 1: 핵심 기능 구현 (2-3주)
1. 비교과 프로그램 관리 시스템 구현
   - DB 테이블 설계 (programs, program_applications)
   - 프로그램 CRUD API
   - 프로그램 목록/상세 페이지
   - 프로그램 신청 기능

2. 마일리지 시스템 구현
   - DB 테이블 설계 (mileage_rules, mileage_history)
   - 마일리지 적립 로직
   - 마일리지 조회 API
   - 마일리지 페이지

### Phase 2: 상담 및 역량진단 (2-3주)
3. 통합상담 시스템 구현
   - DB 테이블 설계 (counseling_sessions, counseling_records)
   - 상담 신청 및 관리 API
   - 상담 페이지

4. 역량진단 시스템 구현
   - 진단 문항 DB 구축
   - 진단 실시 및 결과 저장
   - 결과 분석 로직

### Phase 3: 관리 및 통계 (1-2주)
5. 관리자 페이지 구현
   - 사용자 관리 UI
   - 프로그램 관리 UI
   - 통계 대시보드

6. 포트폴리오 시스템 구현

### Phase 4: 추가 기능 및 최적화 (1-2주)
7. 설문조사, 알림, 게시판 구현
8. 성능 최적화 및 테스트

---

## 기술적 고려사항

### 1. 인증 및 권한
- 현재: 세션 기반 인증
- 고려사항: JWT 토큰 기반 인증으로 전환 검토 (모바일 앱 지원 시)

### 2. 데이터베이스
- users와 students 테이블 통합 필요
- 인덱스 최적화
- 쿼리 성능 모니터링

### 3. 보안
- ✅ BCrypt 비밀번호 암호화
- ✅ 계정 잠금 기능
- ✅ Soft Delete
- TODO: CSRF 토큰
- TODO: XSS 방어
- TODO: SQL Injection 방어 (JPA 사용 중이므로 대부분 방어됨)

### 4. 성능
- 페이징 처리 (프로그램 목록, 사용자 목록)
- 캐싱 전략 (Redis 검토)
- 대량 데이터 처리 (Excel 업로드)

### 5. 모니터링 및 로깅
- 로그 레벨 관리 (Slf4j)
- 에러 추적 (GlobalExceptionHandler)
- 성능 모니터링 (Spring Boot Actuator 검토)

---

## 참고 문서

- UI 디자인: `ui (2).pptx`
- DB 스키마: `database/schema.sql`
- API 문서: (Swagger 추가 예정)

---

**작성일:** 2025-11-06
**최종 업데이트:** 2025-11-17
**작성자:** Claude AI Assistant
**버전:** 2.0

---

## 📋 최근 업데이트 (2025-11-17)

### ✅ 비교과 프로그램 시스템 (완료)

#### 프로그램 관리
- ✅ 프로그램 목록 조회 (페이지네이션, 필터링, 검색)
- ✅ 프로그램 상세 보기
- ✅ 프로그램 신청/취소 기능
- ✅ 정원 관리 및 상태 표시
- ✅ DataLoader 자동 데이터 생성 (50개 프로그램)

#### 신청 관리
- ✅ 프로그램 신청 (ProgramApplication Entity)
- ✅ 5가지 상태 관리 (PENDING, APPROVED, REJECTED, CANCELLED, COMPLETED)
- ✅ 나의 신청내역 조회
- ✅ LazyInitializationException 해결 (JOIN FETCH)

#### 관리자 기능 (2025-11-17 완료)
- ✅ 신청자 목록 조회
- ✅ 신청 승인/거부/완료 처리
- ✅ 거부 사유 입력 모달
- ✅ 신청 통계 표시
- ✅ Excel 다운로드 기능 (Apache POI)
- 📄 문서: `13_ADMIN_APPLICATION_MANAGEMENT_DEVELOPMENT_LOG.md`

#### 후기/리뷰 시스템 (2025-11-17 완료)
- ✅ 후기 작성 (참여 완료 학생만)
- ✅ 별점 평가 (1-5점)
- ✅ 후기 수정/삭제 (본인만)
- ✅ 평균 별점 계산 및 표시
- ✅ Soft Delete 패턴
- ✅ N+1 문제 해결 (JOIN FETCH)
- ✅ 테스트 데이터 자동 생성
- 📄 문서: `14_PROGRAM_REVIEW_SYSTEM_DEVELOPMENT_LOG.md`

#### 마이페이지 (2025-11-16 완료)
- ✅ 신청한 프로그램 목록 조회
- ✅ 통계 대시보드 (전체/대기/승인/완료/거부/취소)
- ✅ 상태별 필터링 (6가지 상태)
- ✅ 프로그램 카드 그리드 (반응형)
- ✅ 취소 기능 (PENDING/APPROVED)
- ✅ Empty State 처리
- ✅ URL 기반 필터링
- 📍 위치: `/mypage`

### 🎯 다음 우선순위: 첨부파일 관리
- 프로그램 자료 첨부 (관리자)
- 첨부파일 다운로드 (학생)
- 파일 목록 표시
- 파일 크기/형식 제한
