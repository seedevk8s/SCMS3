# 🔄 세션 재개 가이드

**작성일:** 2025-11-15  
**프로젝트:** SCMS2 (푸름대학교 학생성장지원센터 CHAMP)  
**현재 경로:** `C:\Users\HPE\IdeaProjects\SCMS2`

---

## 📍 프로젝트 현황 요약

### ✅ 완료된 작업

#### 1. 기본 인프라
- ✅ Spring Boot 3.x + Java 17 프로젝트 설정
- ✅ MySQL 8.0 데이터베이스 연동
- ✅ Gradle 빌드 설정
- ✅ Docker Compose 구성

#### 2. 인증 시스템 (100% 완료)
- ✅ 사용자 테이블 설계 (users, login_history, counselors)
- ✅ BCrypt 비밀번호 암호화
- ✅ 로그인/로그아웃 API
- ✅ 비밀번호 변경/재설정 기능
- ✅ 계정 잠금 기능 (5회 실패 시)
- ✅ 로그인 이력 추적
- ✅ 세션 기반 인증

**API 엔드포인트:**
```
POST /api/auth/login           - 로그인
POST /api/auth/logout          - 로그아웃
POST /api/auth/password/change - 비밀번호 변경
POST /api/auth/password/reset  - 비밀번호 재설정
GET  /api/auth/me              - 현재 사용자 정보
```

#### 3. 기본 UI/UX (100% 완료)
- ✅ 공통 레이아웃 (header, footer, layout)
- ✅ 메인 페이지 (index.html)
- ✅ 로그인 페이지 (login.html)
- ✅ 비밀번호 변경 페이지 (password-change.html)
- ✅ 비밀번호 재설정 페이지 (password-reset.html)
- ✅ 반응형 CSS 디자인 시스템

#### 4. 비교과 프로그램 시스템 (진행 중)
- ✅ Program 엔티티 및 Repository
- ✅ ProgramApplication 엔티티 및 Repository
- ✅ ProgramService, ProgramApplicationService
- ✅ ProgramAdminController, ProgramApplicationController
- ✅ 프로그램 목록/상세 페이지 (programs.html, program-detail.html)
- ✅ 관리자 프로그램 목록/등록 페이지 (admin/program-list.html, admin/program-form.html)

#### 5. 초기 데이터
- ✅ 학생 계정 8개 (2024001~2024002, 2023001~2023002, 2022001~2022002, 2021001~2021002)
- ✅ 관리자 계정 1개 (9999999)
- ✅ 초기 비밀번호: 생년월일 6자리 (예: 030101)

---

## 🎯 다음 작업 우선순위

### Phase 1: 비교과 프로그램 시스템 완성 (추정 3-5일)

#### 1.1 프로그램 신청 기능 완성
- [ ] 프로그램 신청 처리 로직 검증
- [ ] 정원 체크 기능
- [ ] 중복 신청 방지
- [ ] 신청 내역 조회 페이지

#### 1.2 관리자 프로그램 관리 기능
- [ ] 신청자 목록 조회
- [ ] 신청 승인/거부 기능
- [ ] 참여 확인 처리
- [ ] 마일리지 적립 트리거

#### 1.3 테스트 데이터
- [ ] 샘플 프로그램 20개 생성
- [ ] 신청 테스트 데이터 생성

### Phase 2: 마일리지 시스템 구현 (추정 2-3일)

#### 2.1 데이터베이스 설계
```sql
CREATE TABLE mileage_rules (
    rule_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    activity_type VARCHAR(50) NOT NULL,  -- 'PROGRAM', 'COUNSELING', 'SURVEY'
    points INT NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE mileage_history (
    history_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    activity_type VARCHAR(50) NOT NULL,
    activity_id BIGINT,
    points INT NOT NULL,
    description VARCHAR(255),
    earned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);
```

#### 2.2 구현 목록
- [ ] MileageRule 엔티티
- [ ] MileageHistory 엔티티
- [ ] MileageService (적립, 조회, 통계)
- [ ] MileageController (학생용 API)
- [ ] MileageAdminController (관리자용 API)
- [ ] 마일리지 조회 페이지 (학생)
- [ ] 마일리지 관리 페이지 (관리자)

### Phase 3: 통합상담 시스템 구현 (추정 3-4일)

#### 3.1 데이터베이스 설계
```sql
CREATE TABLE counseling_sessions (
    session_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    counselor_id BIGINT,
    session_type VARCHAR(50) NOT NULL,  -- 'CAREER', 'ACADEMIC', 'PERSONAL'
    status VARCHAR(20) NOT NULL,        -- 'PENDING', 'APPROVED', 'COMPLETED', 'CANCELLED'
    requested_date DATE,
    requested_time TIME,
    scheduled_date DATE,
    scheduled_time TIME,
    request_content TEXT,
    counselor_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES users(user_id),
    FOREIGN KEY (counselor_id) REFERENCES counselors(counselor_id)
);
```

#### 3.2 구현 목록
- [ ] CounselingSession 엔티티
- [ ] CounselingService
- [ ] CounselingController (학생용)
- [ ] CounselingAdminController (관리자/상담사용)
- [ ] 상담 신청 페이지 (학생)
- [ ] 상담 관리 페이지 (관리자/상담사)
- [ ] 상담 이력 조회 페이지

### Phase 4: 역량진단 시스템 구현 (추정 4-5일)

#### 4.1 데이터베이스 설계
```sql
CREATE TABLE competency_surveys (
    survey_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    start_date DATE,
    end_date DATE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE survey_questions (
    question_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    survey_id BIGINT NOT NULL,
    question_text TEXT NOT NULL,
    question_type VARCHAR(20) NOT NULL,  -- 'MULTIPLE_CHOICE', 'RATING', 'TEXT'
    category VARCHAR(50),                -- 'COMMUNICATION', 'PROBLEM_SOLVING', etc.
    display_order INT,
    FOREIGN KEY (survey_id) REFERENCES competency_surveys(survey_id)
);

CREATE TABLE survey_responses (
    response_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    survey_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    answer_value VARCHAR(255),
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (survey_id) REFERENCES competency_surveys(survey_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (question_id) REFERENCES survey_questions(question_id)
);

CREATE TABLE competency_results (
    result_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    survey_id BIGINT NOT NULL,
    category VARCHAR(50) NOT NULL,
    score INT NOT NULL,
    level VARCHAR(20),  -- 'BEGINNER', 'INTERMEDIATE', 'ADVANCED'
    assessed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (survey_id) REFERENCES competency_surveys(survey_id)
);
```

#### 4.2 구현 목록
- [ ] CompetencySurvey 엔티티
- [ ] SurveyQuestion 엔티티
- [ ] SurveyResponse 엔티티
- [ ] CompetencyResult 엔티티
- [ ] CompetencySurveyService
- [ ] CompetencySurveyController
- [ ] 역량진단 설문 페이지
- [ ] 역량진단 결과 페이지
- [ ] 관리자 설문 관리 페이지

---

## 📂 중요 파일 위치

### 설정 파일
```
src/main/resources/
├── application.yml           # 기본 설정
├── application-dev.yml       # 개발 환경 설정
└── application-prod.yml      # 운영 환경 설정
```

### 엔티티
```
src/main/java/com/scms/app/model/
├── User.java                 # 사용자 (학생, 상담사, 관리자)
├── Student.java              # 학생 추가 정보
├── Counselor.java            # 상담사 추가 정보
├── LoginHistory.java         # 로그인 이력
├── Program.java              # 비교과 프로그램
├── ProgramApplication.java   # 프로그램 신청
├── UserRole.java             # 역할 Enum
├── ProgramStatus.java        # 프로그램 상태 Enum
└── ApplicationStatus.java    # 신청 상태 Enum
```

### 서비스
```
src/main/java/com/scms/app/service/
├── UserService.java                 # 사용자 관리
├── StudentService.java              # 학생 관리
├── ProgramService.java              # 프로그램 관리
└── ProgramApplicationService.java   # 프로그램 신청 관리
```

### 컨트롤러
```
src/main/java/com/scms/app/controller/
├── AuthController.java                 # 인증 API
├── HomeController.java                 # 페이지 라우팅
├── UserController.java                 # 사용자 관리 API
├── StudentController.java              # 학생 관리 API
├── ProgramAdminController.java         # 프로그램 관리 (관리자)
└── ProgramApplicationController.java   # 프로그램 신청 (학생)
```

### 템플릿
```
src/main/resources/templates/
├── layout/                  # 공통 레이아웃
│   ├── layout.html         # 기본 레이아웃
│   ├── header.html         # 헤더
│   ├── footer.html         # 푸터
│   ├── admin-layout.html   # 관리자 레이아웃
│   ├── admin-header.html   # 관리자 헤더
│   └── admin-sidebar.html  # 관리자 사이드바
├── fragments/              # 재사용 가능 프래그먼트
│   ├── header.html
│   └── footer.html
├── admin/                  # 관리자 페이지
│   ├── program-list.html
│   └── program-form.html
├── students/               # 학생 페이지
│   └── list.html
├── index.html              # 메인 페이지
├── login.html              # 로그인
├── password-change.html    # 비밀번호 변경
├── password-reset.html     # 비밀번호 재설정
├── programs.html           # 프로그램 목록
└── program-detail.html     # 프로그램 상세
```

---

## 🔑 테스트 계정

### 학생 계정
```
학번: 2024001 | 비밀번호: 030101 | 이름: 김철수 | 학년: 1학년
학번: 2024002 | 비밀번호: 040215 | 이름: 이영희 | 학년: 1학년
학번: 2023001 | 비밀번호: 020310 | 이름: 박민수 | 학년: 2학년
학번: 2023002 | 비밀번호: 010825 | 이름: 최지은 | 학년: 2학년
학번: 2022001 | 비밀번호: 010620 | 이름: 정우진 | 학년: 3학년
학번: 2022002 | 비밀번호: 991105 | 이름: 강하늘 | 학년: 3학년
학번: 2021001 | 비밀번호: 000412 | 이름: 윤서현 | 학년: 4학년
학번: 2021002 | 비밀번호: 990228 | 이름: 임도윤 | 학년: 4학년
```

### 관리자 계정
```
학번: 9999999 | 비밀번호: admin123 | 이름: 관리자
```

---

## 🚀 빠른 시작 (세션 재개 시)

### 1. 프로젝트 재개 명령어
```bash
# 프로젝트 디렉토리 이동
cd C:\Users\HPE\IdeaProjects\SCMS2

# Docker 컨테이너 확인
docker-compose ps

# Docker 시작 (필요시)
docker-compose up -d

# 애플리케이션 실행
./gradlew bootRun

# 또는 IDE에서 Scms2Application.java 실행
```

### 2. 접속 정보
- **애플리케이션:** http://localhost:8080
- **데이터베이스:** localhost:3306
  - 사용자명: scms_user
  - 비밀번호: scms_password
  - 데이터베이스: scms_db

### 3. 현재 작업 상태 확인
```bash
# Git 상태 확인
git status

# 최근 커밋 확인
git log --oneline -10

# 브랜치 확인
git branch
```

---

## 📋 다음 세션 체크리스트

### 세션 시작 시 확인 사항
- [ ] Docker 컨테이너 실행 상태 확인
- [ ] 데이터베이스 연결 확인
- [ ] 애플리케이션 정상 구동 확인
- [ ] 최근 변경사항 Git 상태 확인
- [ ] 이전 세션 작업 내용 리뷰

### 작업 전 준비
- [ ] 작업할 기능 우선순위 결정
- [ ] 필요한 테이블 스키마 확인
- [ ] 참고 문서 확인 (프로젝트 수행 계획서, 요구사항 정의서)

### 작업 중
- [ ] 코드 작성
- [ ] 단위 테스트 작성
- [ ] 통합 테스트 확인
- [ ] 브라우저에서 동작 확인

### 작업 종료 시
- [ ] Git 커밋 (의미 있는 단위로)
- [ ] 작업 내용 문서화
- [ ] SESSION_RESUME.md 업데이트
- [ ] 다음 작업 우선순위 메모

---

## 🔗 참고 문서

### 프로젝트 문서
1. **프로젝트 수행 계획서**  
   https://docs.google.com/document/d/1LPxYcGUIk_J7sn4BlCQeZrpfCZGavj8dZMRhIfEAAh4/edit

2. **요구사항 정리 문서**  
   https://docs.google.com/spreadsheets/d/104q5eTg701of5WxGEBqalPelumekEeqCLGjKmC2mPG4/edit

### UI 참고
1. **푸름대학교 학생성장지원센터 CHAMP**  
   https://champ.woosuk.ac.kr/ko/  
   (접근 불가 시 캡처 화면 제공 가능)

2. **TOAST UI**  
   https://ui.toast.com/

### 로컬 문서
- `README.md` - 프로젝트 개요 및 빠른 시작
- `DEVELOPMENT.md` - 개발 현황 및 상세 문서
- `SETUP.md` - 환경 설정 가이드
- `database/README.md` - 데이터베이스 스키마 및 설정

---

## 💡 개발 팁

### 1. 새로운 기능 추가 시 순서
1. 엔티티 설계 및 생성
2. Repository 인터페이스 작성
3. Service 클래스 구현
4. Controller 작성 (API 또는 페이지)
5. 템플릿 작성 (필요시)
6. 테스트 작성 및 검증

### 2. Git 커밋 컨벤션
```
feat: 새로운 기능 추가
fix: 버그 수정
docs: 문서 수정
style: 코드 포맷팅
refactor: 코드 리팩토링
test: 테스트 코드 추가
chore: 빌드 설정 등 기타 변경
```

### 3. 코드 스타일
- Java: Google Java Style Guide
- 들여쓰기: 스페이스 4칸
- 네이밍: camelCase (변수, 메서드), PascalCase (클래스)

---

## 🎯 현재 세션 목표 (예시)

### 목표 1: 비교과 프로그램 신청 기능 완성
- [ ] 프로그램 신청 처리 로직 검증
- [ ] 정원 체크 기능
- [ ] 중복 신청 방지
- [ ] 신청 내역 조회 페이지

### 목표 2: 테스트 데이터 생성
- [ ] 샘플 프로그램 20개 생성
- [ ] 신청 테스트 데이터 생성

---

**마지막 업데이트:** 2025-11-15  
**다음 업데이트 예정:** 세션 작업 완료 시

---

## 📌 빠른 명령어 모음

```bash
# 애플리케이션 실행
./gradlew bootRun

# 빌드
./gradlew clean build

# 테스트
./gradlew test

# Docker 시작
docker-compose up -d

# Docker 중지
docker-compose down

# Git 상태 확인
git status

# Git 커밋
git add .
git commit -m "feat: 기능 설명"

# Git 푸시
git push origin main
```
