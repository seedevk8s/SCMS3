# 데이터베이스 설정 가이드

푸름대학교 학생성장지원센터 CHAMP 시스템의 데이터베이스 설정 가이드입니다.

## 목차
1. [Docker를 이용한 MySQL 설정 (권장)](#1-docker를-이용한-mysql-설정-권장)
2. [로컬 MySQL 설정](#2-로컬-mysql-설정)
3. [데이터베이스 스키마 설명](#3-데이터베이스-스키마-설명)
4. [초기 데이터](#4-초기-데이터)

---

## 1. Docker를 이용한 MySQL 설정 (권장)

### 사전 요구사항
- Docker 및 Docker Compose 설치 필요
- [Docker 설치 가이드](https://docs.docker.com/get-docker/)

### 설정 방법

#### 1.1 Docker Compose 실행
프로젝트 루트 디렉토리에서 다음 명령어를 실행합니다:

```bash
# MySQL 컨테이너 시작
docker-compose up -d

# 컨테이너 상태 확인
docker-compose ps

# 로그 확인
docker-compose logs -f mysql
```

#### 1.2 데이터베이스 초기화 확인
컨테이너가 시작되면 `database/schema.sql` 파일이 자동으로 실행되어 테이블과 샘플 데이터가 생성됩니다.

```bash
# MySQL 컨테이너 접속
docker exec -it scms2-mysql mysql -uroot -ppassword

# MySQL 콘솔에서 확인
USE scms2;
SHOW TABLES;
SELECT * FROM students;
```

#### 1.3 컨테이너 관리 명령어
```bash
# 컨테이너 중지
docker-compose stop

# 컨테이너 재시작
docker-compose restart

# 컨테이너 삭제 (데이터는 볼륨에 보존됨)
docker-compose down

# 컨테이너와 볼륨 모두 삭제 (주의: 데이터 손실!)
docker-compose down -v
```

### 1.4 연결 정보
- **호스트**: localhost
- **포트**: 3306
- **데이터베이스**: scms2
- **사용자**: root
- **비밀번호**: password

또는

- **사용자**: scms2user
- **비밀번호**: scms2pass

---

## 2. 로컬 MySQL 설정 ⭐ **권장 방법**

로컬에 MySQL이 이미 설치되어 있는 경우 아래 방법을 따릅니다.

### 2.1 MySQL 접속
```bash
mysql -u root -p
```

### 2.2 데이터베이스 생성
```sql
-- 데이터베이스만 생성 (사용자는 선택사항)
CREATE DATABASE IF NOT EXISTS scms2 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- (선택) 전용 사용자 생성 및 권한 부여
CREATE USER IF NOT EXISTS 'scms2user'@'localhost' IDENTIFIED BY 'scms2pass';
GRANT ALL PRIVILEGES ON scms2.* TO 'scms2user'@'localhost';
FLUSH PRIVILEGES;
```

### 2.3 스키마 적용 (중요!)
```bash
# 프로젝트 루트 디렉토리에서 실행
mysql -u root -p scms2 < database/schema.sql
```

이 명령어는 다음을 생성합니다:
- ✅ **users** 테이블 (로그인 계정)
- ✅ **students** 테이블 (학생 정보)
- ✅ **login_history** 테이블 (로그인 이력)
- ✅ **counselors** 테이블 (상담사 정보)
- ✅ **역량 관련 테이블들**
- ✅ **샘플 데이터** (students 테이블에 8명)

### 2.4 Spring Boot 애플리케이션 실행
애플리케이션을 처음 실행하면 **DataLoader**가 자동으로 로그인 가능한 사용자 계정을 생성합니다!

```bash
mvn spring-boot:run
```

콘솔에서 다음과 같은 로그를 확인할 수 있습니다:
```
초기 사용자 데이터를 생성합니다...
사용자 생성: 김철수 (학번: 2024001, 초기 비밀번호: 030101)
사용자 생성: 이영희 (학번: 2024002, 초기 비밀번호: 040215)
...
관리자 계정 생성: 시스템관리자 (학번: 9999999)
초기 사용자 데이터 생성 완료!
```

### 2.5 application.yml 설정 확인
`src/main/resources/application.yml` 파일에서 데이터베이스 연결 정보를 확인하고 필요시 수정합니다:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/scms2?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul&characterEncoding=UTF-8
    username: root
    password: password  # 본인의 MySQL root 비밀번호로 변경
    driver-class-name: com.mysql.cj.jdbc.Driver
```

---

## 3. 데이터베이스 스키마 설명

### 3.1 테이블 구조

#### students (학생 정보)
| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| id | BIGINT | 기본키 (자동증가) |
| student_id | VARCHAR(50) | 학번 (유니크) |
| name | VARCHAR(100) | 이름 |
| email | VARCHAR(100) | 이메일 (유니크) |
| phone | VARCHAR(20) | 전화번호 |
| department | VARCHAR(100) | 학과 |
| grade | VARCHAR(20) | 학년 |
| status | VARCHAR(20) | 상태 (재학, 휴학, 졸업, 자퇴) |
| created_at | DATETIME | 생성일시 |
| updated_at | DATETIME | 수정일시 |

#### competency_categories (역량 카테고리)
| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| id | BIGINT | 기본키 (자동증가) |
| name | VARCHAR(100) | 역량 카테고리명 |
| description | TEXT | 설명 |
| created_at | DATETIME | 생성일시 |
| updated_at | DATETIME | 수정일시 |

#### competencies (역량)
| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| id | BIGINT | 기본키 (자동증가) |
| category_id | BIGINT | 역량 카테고리 ID (외래키) |
| name | VARCHAR(100) | 역량명 |
| description | TEXT | 설명 |
| created_at | DATETIME | 생성일시 |
| updated_at | DATETIME | 수정일시 |

#### student_competency_assessments (학생 역량 평가)
| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| id | BIGINT | 기본키 (자동증가) |
| student_id | BIGINT | 학생 ID (외래키) |
| competency_id | BIGINT | 역량 ID (외래키) |
| score | INT | 점수 (0-100) |
| assessment_date | DATE | 평가일 |
| assessor | VARCHAR(100) | 평가자 |
| notes | TEXT | 비고 |
| created_at | DATETIME | 생성일시 |
| updated_at | DATETIME | 수정일시 |

### 3.2 ER 다이어그램
```
┌────────────────┐
│   students     │
├────────────────┤
│ id (PK)        │
│ student_id     │───┐
│ name           │   │
│ email          │   │
│ phone          │   │
│ department     │   │
│ grade          │   │
│ status         │   │
└────────────────┘   │
                     │
                     │ 1:N
                     │
                     ▼
┌───────────────────────────────────┐
│ student_competency_assessments    │
├───────────────────────────────────┤
│ id (PK)                           │
│ student_id (FK)                   │
│ competency_id (FK)                │
│ score                             │
│ assessment_date                   │
│ assessor                          │
└───────────────────────────────────┘
                     ▲
                     │ N:1
                     │
┌────────────────┐   │
│ competencies   │───┘
├────────────────┤
│ id (PK)        │
│ category_id(FK)│
│ name           │
│ description    │
└────────────────┘
         ▲
         │ N:1
         │
┌─────────────────────┐
│ competency_categories│
├─────────────────────┤
│ id (PK)             │
│ name                │
│ description         │
└─────────────────────┘
```

---

## 4. 초기 데이터

### 4.1 테스트 로그인 계정 🔑

애플리케이션 첫 실행 시 자동으로 생성되는 테스트 계정입니다:

#### 학생 계정

| 학번 | 이름 | 비밀번호 | 학과 | 학년 | 비고 |
|------|------|----------|------|------|------|
| **2024001** | 김철수 | **030101** | 컴퓨터공학과 | 1 | 생년월일: 2003-01-01 |
| **2024002** | 이영희 | **040215** | 소프트웨어학과 | 2 | 생년월일: 2004-02-15 |
| **2023001** | 박민수 | **020310** | 정보보안학과 | 2 | 생년월일: 2002-03-10 |
| **2023002** | 최지은 | **020505** | 컴퓨터공학과 | 2 | 생년월일: 2002-05-05 |
| **2022001** | 정우진 | **010620** | 인공지능학과 | 3 | 생년월일: 2001-06-20 |
| **2022002** | 강민지 | **010815** | 데이터사이언스학과 | 3 | 생년월일: 2001-08-15 |
| **2021001** | 윤서준 | **000911** | 컴퓨터공학과 | 4 | 생년월일: 2000-09-11 |
| **2021002** | 임하늘 | **001225** | 소프트웨어학과 | 4 | 생년월일: 2000-12-25 |

#### 관리자 계정

| 학번 | 이름 | 비밀번호 | 역할 |
|------|------|----------|------|
| **9999999** | 시스템관리자 | **admin123** | ADMIN |

#### 로그인 방법

1. 브라우저에서 `http://localhost:8080` 접속
2. 로그인 페이지에서 **학번**과 **비밀번호** 입력
3. 예시:
   - 학번: `2024001`
   - 비밀번호: `030101`

#### 비밀번호 규칙

- **초기 비밀번호**: 생년월일 6자리 (yyMMdd)
  - 예: 2003년 1월 1일 → `030101`
- **최초 로그인 시**: 비밀번호 변경 필요
- **로그인 5회 실패 시**: 계정 자동 잠금

### 4.2 샘플 학생 데이터 (students 테이블)

`students` 테이블에는 8명의 샘플 데이터가 포함되어 있습니다:

| 학번 | 이름 | 이메일 | 학과 | 학년 | 상태 |
|------|------|--------|------|------|------|
| 2024001 | 김철수 | kim.cs@example.com | 컴퓨터공학과 | 1 | 재학 |
| 2024002 | 이영희 | lee.yh@example.com | 소프트웨어학과 | 2 | 재학 |
| 2023001 | 박민수 | park.ms@example.com | 정보보안학과 | 2 | 재학 |
| 2023002 | 최지은 | choi.je@example.com | 컴퓨터공학과 | 2 | 휴학 |
| 2022001 | 정우진 | jung.wj@example.com | 인공지능학과 | 3 | 재학 |
| 2022002 | 강민지 | kang.mj@example.com | 데이터사이언스학과 | 3 | 재학 |
| 2021001 | 윤서준 | yoon.sj@example.com | 컴퓨터공학과 | 4 | 재학 |
| 2021002 | 임하늘 | im.hn@example.com | 소프트웨어학과 | 4 | 졸업 |

### 4.3 추가 데이터 입력
필요한 경우 MySQL 콘솔에서 직접 데이터를 추가할 수 있습니다:

```sql
-- 학생 추가 예시
INSERT INTO students (student_id, name, email, phone, department, grade, status)
VALUES ('2024003', '홍길동', 'hong.gd@example.com', '010-9999-8888', '컴퓨터공학과', '1', '재학');

-- 역량 카테고리 추가 예시
INSERT INTO competency_categories (name, description)
VALUES ('전공역량', '전공 관련 핵심 역량');

-- 역량 추가 예시
INSERT INTO competencies (category_id, name, description)
VALUES (1, '프로그래밍 능력', 'Java, Python 등 프로그래밍 언어 활용 능력');
```

---

## 5. 문제 해결

### 5.1 연결 오류
```
Communications link failure
```
- MySQL 서버가 실행 중인지 확인
- 포트 3306이 다른 서비스에서 사용 중인지 확인
- 방화벽 설정 확인

### 5.2 인증 오류
```
Access denied for user
```
- 사용자명과 비밀번호 확인
- MySQL 사용자 권한 확인

### 5.3 데이터베이스 초기화
기존 데이터를 모두 삭제하고 처음부터 다시 시작하려면:

```bash
# Docker 사용 시
docker-compose down -v
docker-compose up -d

# 로컬 MySQL 사용 시
mysql -u root -p
DROP DATABASE scms2;
CREATE DATABASE scms2 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
exit
mysql -u root -p scms2 < database/schema.sql
```

---

## 6. 참고사항

- 개발 환경에서는 `application-dev.yml` 프로파일이 사용됩니다
- `ddl-auto: create-drop` 설정은 애플리케이션 재시작 시 테이블을 재생성합니다
- 프로덕션 환경에서는 `ddl-auto: validate` 또는 `none`을 사용하세요
- 데이터 백업은 정기적으로 수행하세요

```bash
# 데이터베이스 백업
docker exec scms2-mysql mysqldump -uroot -ppassword scms2 > backup_$(date +%Y%m%d).sql

# 데이터베이스 복원
docker exec -i scms2-mysql mysql -uroot -ppassword scms2 < backup_20250101.sql
```

---

## 문의
데이터베이스 설정에 문제가 있거나 질문이 있으시면 개발팀에 문의해주세요.
