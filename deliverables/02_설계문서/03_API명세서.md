# API 명세서

**프로젝트명**: 푸름대학교 학생성장지원센터 CHAMP (SCMS3)
**작성일**: 2025-11-18
**버전**: 1.0
**작성자**: 개발팀
**Base URL**: `http://localhost:8080`

---

## 1. 문서 개요

### 1.1 목적
본 문서는 SCMS3 시스템의 모든 REST API 엔드포인트를 상세히 기술하여, 프론트엔드 개발자와 API 사용자가 올바르게 API를 호출할 수 있도록 합니다.

### 1.2 범위
- 모든 REST API 엔드포인트 명세
- 요청/응답 형식
- 권한 및 인증 요구사항
- 에러 코드 및 처리

### 1.3 대상 독자
- 프론트엔드 개발자
- 백엔드 개발자
- API 통합 담당자
- QA 엔지니어

---

## 2. API 개요

### 2.1 API 통계
- **총 API 개수**: 89개
- **인증 API**: 5개
- **프로그램 관리**: 16개
- **마일리지**: 13개
- **상담**: 9개
- **포트폴리오**: 9개
- **설문**: 12개
- **외부취업가점**: 12개
- **알림**: 7개
- **역량평가**: 10개
- **페이지**: 8개 (HTML 반환)

### 2.2 API 버전
- **현재 버전**: v1
- **버전 관리**: URL 경로에 버전 번호 포함 (향후 도입 예정)

### 2.3 프로토콜
- **HTTP/HTTPS**: HTTP (개발), HTTPS (운영)
- **포트**: 8080 (개발), 443 (운영)

---

## 3. 인증 및 권한

### 3.1 인증 방식
- **세션 기반 인증**: HttpSession + Spring Security
- **쿠키**: `JSESSIONID` 쿠키로 세션 유지
- **CSRF 토큰**: POST/PUT/DELETE 요청 시 CSRF 토큰 필요

### 3.2 권한 레벨

| 권한 | 설명 | 역할 |
|------|------|------|
| **PUBLIC** | 인증 불필요 | 누구나 접근 가능 |
| **AUTHENTICATED** | 로그인 필요 | 로그인한 사용자 |
| **STUDENT** | 학생 권한 | 학생 역할 |
| **COUNSELOR** | 상담사 권한 | 상담사 역할 |
| **ADMIN** | 관리자 권한 | 관리자 역할 |

### 3.3 권한 확인 방법
- Spring Security의 `@PreAuthorize` 어노테이션 사용
- 세션에서 `userId`, `role`, `isAdmin` 확인

---

## 4. 공통 사항

### 4.1 요청 헤더

| 헤더 | 필수 | 설명 |
|------|------|------|
| `Content-Type` | POST/PUT 시 | `application/json` 또는 `multipart/form-data` |
| `Cookie` | 인증 필요 시 | `JSESSIONID={세션ID}` |
| `X-CSRF-TOKEN` | POST/PUT/DELETE 시 | CSRF 토큰 |

### 4.2 응답 형식

#### 4.2.1 성공 응답 (JSON)
```json
{
  "success": true,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    // 응답 데이터
  }
}
```

#### 4.2.2 에러 응답 (JSON)
```json
{
  "success": false,
  "message": "에러 메시지",
  "errorCode": "ERROR_CODE",
  "timestamp": "2025-11-18T10:30:00"
}
```

### 4.3 HTTP 상태 코드

| 상태 코드 | 설명 |
|----------|------|
| 200 OK | 성공 |
| 201 Created | 리소스 생성 성공 |
| 204 No Content | 성공 (응답 바디 없음) |
| 400 Bad Request | 잘못된 요청 |
| 401 Unauthorized | 인증 실패 |
| 403 Forbidden | 권한 없음 |
| 404 Not Found | 리소스 없음 |
| 500 Internal Server Error | 서버 오류 |

### 4.4 페이지네이션

페이지네이션을 지원하는 API는 다음 파라미터를 사용합니다:

| 파라미터 | 타입 | 기본값 | 설명 |
|---------|------|--------|------|
| `page` | int | 0 | 페이지 번호 (0부터 시작) |
| `size` | int | 10 | 페이지 크기 |
| `sort` | string | - | 정렬 기준 (예: `createdAt,desc`) |

응답 예시:
```json
{
  "content": [...],
  "totalElements": 100,
  "totalPages": 10,
  "number": 0,
  "size": 10,
  "first": true,
  "last": false
}
```

---

## 5. API 상세 명세

### 5.1 인증 API (AuthController)

**Base URL**: `/api/auth`

#### 5.1.1 로그인

**POST** `/api/auth/login`

학생번호와 비밀번호로 로그인하여 세션을 생성합니다.

**Request Body**
```json
{
  "studentNum": 2024001,
  "password": "030101"
}
```

**Response** (200 OK)
```json
{
  "userId": 1,
  "studentNum": 2024001,
  "name": "김철수",
  "role": "STUDENT",
  "isFirstLogin": false
}
```

**권한**: PUBLIC

**에러**
- 400: 학번 또는 비밀번호 누락
- 401: 학번 또는 비밀번호 불일치
- 423: 계정 잠금 (로그인 5회 실패)

---

#### 5.1.2 로그아웃

**POST** `/api/auth/logout`

현재 세션을 무효화하고 로그아웃합니다.

**Request**: 없음

**Response** (200 OK)
```json
{
  "message": "로그아웃 완료"
}
```

**권한**: PUBLIC

---

#### 5.1.3 비밀번호 변경

**POST** `/api/auth/password/change`

로그인한 사용자의 비밀번호를 변경합니다.

**Request Body**
```json
{
  "oldPassword": "030101",
  "newPassword": "newPassword123!",
  "confirmPassword": "newPassword123!"
}
```

**Response** (200 OK)
```json
{
  "message": "비밀번호가 변경되었습니다"
}
```

**권한**: AUTHENTICATED

**에러**
- 400: 비밀번호 불일치
- 401: 현재 비밀번호 틀림
- 422: 비밀번호 형식 오류 (최소 8자, 영문+숫자)

---

#### 5.1.4 비밀번호 재설정

**POST** `/api/auth/password/reset`

학번, 이름, 생년월일로 본인 확인 후 비밀번호를 생년월일로 초기화합니다.

**Request Body**
```json
{
  "studentNum": 2024001,
  "name": "김철수",
  "birthDate": "2003-01-01"
}
```

**Response** (200 OK)
```json
{
  "message": "비밀번호가 초기화되었습니다. 생년월일(6자리)로 로그인해주세요."
}
```

**권한**: PUBLIC

**에러**
- 404: 사용자 정보 불일치

---

#### 5.1.5 현재 로그인 사용자 정보 조회

**GET** `/api/auth/me`

현재 세션의 사용자 정보를 조회합니다.

**Request**: 없음

**Response** (200 OK)
```json
{
  "userId": 1,
  "studentNum": 2024001,
  "name": "김철수",
  "role": "STUDENT",
  "email": "kim.cs@example.com",
  "department": "컴퓨터공학과"
}
```

**권한**: AUTHENTICATED

**에러**
- 401: 로그인되지 않음

---

### 5.2 프로그램 관리 API

#### 5.2.1 프로그램 신청 (ProgramApplicationController)

**Base URL**: `/api/programs`

##### 5.2.1.1 프로그램 신청

**POST** `/api/programs/{programId}/apply`

특정 프로그램에 신청합니다.

**Path Parameters**
- `programId` (Integer): 프로그램 ID

**Request**: 없음

**Response** (200 OK)
```json
{
  "success": true,
  "message": "프로그램 신청이 완료되었습니다.",
  "application": {
    "id": 1,
    "programId": 10,
    "userId": 1,
    "status": "PENDING",
    "applicationDate": "2025-11-18T10:30:00",
    "processedDate": null
  }
}
```

**권한**: AUTHENTICATED (학생)

**에러**
- 400: 이미 신청한 프로그램
- 400: 신청 기간이 아님
- 400: 정원 초과
- 404: 프로그램을 찾을 수 없음

---

##### 5.2.1.2 프로그램 신청 취소

**DELETE** `/api/programs/applications/{applicationId}`

프로그램 신청을 취소합니다.

**Path Parameters**
- `applicationId` (Integer): 신청 ID

**Request**: 없음

**Response** (200 OK)
```json
{
  "success": true,
  "message": "신청이 취소되었습니다."
}
```

**권한**: AUTHENTICATED (본인 신청만)

**에러**
- 403: 본인의 신청이 아님
- 400: 이미 승인된 신청은 취소 불가

---

##### 5.2.1.3 나의 신청 내역 조회

**GET** `/api/programs/applications/my`

로그인한 사용자의 모든 프로그램 신청 내역을 조회합니다.

**Request**: 없음

**Response** (200 OK)
```json
[
  {
    "id": 1,
    "programId": 10,
    "programTitle": "AI 기초 교육",
    "status": "APPROVED",
    "applicationDate": "2025-11-10T10:00:00",
    "processedDate": "2025-11-11T14:30:00"
  },
  {
    "id": 2,
    "programId": 15,
    "programTitle": "창업 아이디어 경진대회",
    "status": "PENDING",
    "applicationDate": "2025-11-18T09:00:00",
    "processedDate": null
  }
]
```

**권한**: AUTHENTICATED

---

##### 5.2.1.4 특정 프로그램 신청 상태 조회

**GET** `/api/programs/{programId}/my-application`

특정 프로그램에 대한 나의 신청 상태를 확인합니다.

**Path Parameters**
- `programId` (Integer): 프로그램 ID

**Request**: 없음

**Response** (200 OK)
```json
{
  "applied": true,
  "application": {
    "id": 1,
    "status": "APPROVED",
    "applicationDate": "2025-11-10T10:00:00"
  }
}
```

**권한**: AUTHENTICATED

---

##### 5.2.1.5 프로그램별 신청 내역 조회 (관리자)

**GET** `/api/programs/{programId}/applications`

특정 프로그램의 모든 신청 내역을 조회합니다.

**Path Parameters**
- `programId` (Integer): 프로그램 ID

**Request**: 없음

**Response** (200 OK)
```json
[
  {
    "id": 1,
    "userId": 1,
    "userName": "김철수",
    "studentNum": 2024001,
    "department": "컴퓨터공학과",
    "status": "APPROVED",
    "applicationDate": "2025-11-10T10:00:00"
  }
]
```

**권한**: ADMIN

---

##### 5.2.1.6 신청 내역 Excel 다운로드 (관리자)

**GET** `/api/programs/{programId}/applications/excel`

프로그램 신청자 목록을 Excel 파일로 다운로드합니다.

**Path Parameters**
- `programId` (Integer): 프로그램 ID

**Request**: 없음

**Response** (200 OK)
- Content-Type: `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`
- File: `신청자_목록_{programId}.xlsx`

**권한**: ADMIN

---

##### 5.2.1.7 신청 승인 (관리자)

**POST** `/api/programs/applications/{applicationId}/approve`

프로그램 신청을 승인합니다.

**Path Parameters**
- `applicationId` (Integer): 신청 ID

**Request**: 없음

**Response** (200 OK)
```json
{
  "success": true,
  "message": "신청이 승인되었습니다."
}
```

**권한**: ADMIN

**에러**
- 400: 이미 처리된 신청

---

##### 5.2.1.8 신청 거부 (관리자)

**POST** `/api/programs/applications/{applicationId}/reject`

프로그램 신청을 거부합니다.

**Path Parameters**
- `applicationId` (Integer): 신청 ID

**Request Body**
```json
{
  "reason": "정원 초과"
}
```

**Response** (200 OK)
```json
{
  "success": true,
  "message": "신청이 거부되었습니다."
}
```

**권한**: ADMIN

---

##### 5.2.1.9 참여 완료 처리 (관리자)

**POST** `/api/programs/applications/{applicationId}/complete`

프로그램 참여를 완료 처리합니다.

**Path Parameters**
- `applicationId` (Integer): 신청 ID

**Request**: 없음

**Response** (200 OK)
```json
{
  "success": true,
  "message": "참여 완료 처리되었습니다. 마일리지가 지급되었습니다."
}
```

**권한**: ADMIN

---

##### 5.2.1.10 프로그램 신청 통계 조회 (관리자)

**GET** `/api/programs/{programId}/applications/stats`

프로그램별 신청 통계를 조회합니다.

**Path Parameters**
- `programId` (Integer): 프로그램 ID

**Request**: 없음

**Response** (200 OK)
```json
{
  "total": 50,
  "pending": 10,
  "approved": 30,
  "rejected": 5,
  "cancelled": 3,
  "completed": 2
}
```

**권한**: ADMIN

---

#### 5.2.2 프로그램 관리 (ProgramAdminController)

**Base URL**: `/admin/programs` (HTML 페이지 반환)

##### 5.2.2.1 프로그램 목록 조회

**GET** `/admin/programs`

관리자용 프로그램 관리 페이지를 표시합니다.

**Request**: 없음

**Response**: HTML View (`admin/program-list`)

**권한**: ADMIN

---

##### 5.2.2.2 프로그램 등록 폼

**GET** `/admin/programs/new`

프로그램 등록 폼 페이지를 표시합니다.

**Request**: 없음

**Response**: HTML View (`admin/program-form`)

**권한**: ADMIN

---

##### 5.2.2.3 프로그램 등록 처리

**POST** `/admin/programs/new`

새 프로그램을 등록합니다.

**Request Body** (Form Data)
- `title` (String): 제목
- `description` (String): 설명
- `content` (String): 상세 내용
- `department` (String): 주관 부서
- `college` (String): 단과대
- `category` (String): 카테고리
- `subCategory` (String): 하위 카테고리
- `applicationStartDate` (DateTime): 신청 시작일
- `applicationEndDate` (DateTime): 신청 종료일
- `maxParticipants` (Integer): 최대 참여 인원
- `thumbnailUrl` (String): 썸네일 URL
- `status` (String): 상태 (UPCOMING/ONGOING/COMPLETED/CANCELLED)

**Response**: Redirect to `/admin/programs`

**권한**: ADMIN

---

##### 5.2.2.4 프로그램 수정 폼

**GET** `/admin/programs/{id}/edit`

프로그램 수정 폼 페이지를 표시합니다.

**Path Parameters**
- `id` (Integer): 프로그램 ID

**Request**: 없음

**Response**: HTML View (`admin/program-form`)

**권한**: ADMIN

---

##### 5.2.2.5 프로그램 수정 처리

**POST** `/admin/programs/{id}/edit`

프로그램 정보를 수정합니다.

**Path Parameters**
- `id` (Integer): 프로그램 ID

**Request Body**: (Form Data - 5.2.2.3과 동일)

**Response**: Redirect to `/admin/programs`

**권한**: ADMIN

---

##### 5.2.2.6 프로그램 삭제

**POST** `/admin/programs/{id}/delete`

프로그램을 삭제합니다.

**Path Parameters**
- `id` (Integer): 프로그램 ID

**Request**: 없음

**Response**: Redirect to `/admin/programs`

**권한**: ADMIN

---

### 5.3 마일리지 API

#### 5.3.1 학생용 마일리지 API (MileageController)

**Base URL**: `/api/mileage`

##### 5.3.1.1 마일리지 페이지

**GET** `/mileage`

마일리지 메인 페이지를 표시합니다.

**Request**: 없음

**Response**: HTML View (`mileage`)

**권한**: AUTHENTICATED

---

##### 5.3.1.2 내 총 마일리지 조회

**GET** `/api/mileage/my`

로그인한 사용자의 총 마일리지를 조회합니다.

**Request**: 없음

**Response** (200 OK)
```json
{
  "totalMileage": 1250
}
```

**권한**: AUTHENTICATED

---

##### 5.3.1.3 마일리지 적립 내역 조회

**GET** `/api/mileage/history`

로그인한 사용자의 마일리지 적립 내역을 조회합니다.

**Request**: 없음

**Response** (200 OK)
```json
[
  {
    "id": 1,
    "points": 100,
    "reason": "AI 기초 교육 프로그램 참여 완료",
    "referenceType": "PROGRAM",
    "referenceId": 10,
    "balance": 1250,
    "createdAt": "2025-11-18T10:00:00"
  },
  {
    "id": 2,
    "points": 50,
    "reason": "역량 진단 설문 참여",
    "referenceType": "SURVEY",
    "referenceId": 5,
    "balance": 1150,
    "createdAt": "2025-11-17T14:30:00"
  }
]
```

**권한**: AUTHENTICATED

---

##### 5.3.1.4 활동 타입별 마일리지 통계

**GET** `/api/mileage/statistics`

활동 타입별 마일리지 통계를 조회합니다.

**Request**: 없음

**Response** (200 OK)
```json
{
  "PROGRAM": {
    "count": 5,
    "totalPoints": 500
  },
  "SURVEY": {
    "count": 10,
    "totalPoints": 500
  },
  "COUNSELING": {
    "count": 2,
    "totalPoints": 100
  },
  "PORTFOLIO": {
    "count": 3,
    "totalPoints": 150
  }
}
```

**권한**: AUTHENTICATED

---

##### 5.3.1.5 마일리지 랭킹 조회

**GET** `/api/mileage/ranking`

마일리지 랭킹 및 내 순위를 조회합니다.

**Request**: 없음

**Response** (200 OK)
```json
{
  "rankings": [
    {
      "rank": 1,
      "userId": 5,
      "userName": "정우진",
      "studentNum": 2022001,
      "department": "인공지능학과",
      "totalMileage": 2500
    },
    {
      "rank": 2,
      "userId": 3,
      "userName": "박민수",
      "studentNum": 2023001,
      "department": "정보보안학과",
      "totalMileage": 2100
    }
  ],
  "myRanking": {
    "rank": 15,
    "totalMileage": 1250
  }
}
```

**권한**: AUTHENTICATED

---

##### 5.3.1.6 월별 마일리지 통계

**GET** `/api/mileage/monthly-statistics`

월별 마일리지 적립 통계를 조회합니다.

**Request**: 없음

**Response** (200 OK)
```json
[
  {
    "month": "2025-11",
    "totalPoints": 300,
    "count": 6
  },
  {
    "month": "2025-10",
    "totalPoints": 450,
    "count": 9
  }
]
```

**권한**: AUTHENTICATED

---

##### 5.3.1.7 활성화된 마일리지 규칙 조회

**GET** `/api/mileage/rules`

마일리지 획득 방법 안내 (활성화된 규칙만) 를 조회합니다.

**Request**: 없음

**Response** (200 OK)
```json
[
  {
    "id": 1,
    "activityType": "PROGRAM",
    "activityName": "비교과 프로그램 참여",
    "points": 100,
    "description": "비교과 프로그램 완료 시 지급",
    "isActive": true
  },
  {
    "id": 2,
    "activityType": "SURVEY",
    "activityName": "설문조사 참여",
    "points": 50,
    "description": "설문조사 완료 시 지급",
    "isActive": true
  }
]
```

**권한**: PUBLIC

---

#### 5.3.2 관리자용 마일리지 API (MileageAdminController)

**Base URL**: `/admin/mileage`

##### 5.3.2.1 마일리지 관리 페이지

**GET** `/admin/mileage`

관리자용 마일리지 관리 페이지를 표시합니다.

**Request**: 없음

**Response**: HTML View (`admin/mileage-admin`)

**권한**: ADMIN

---

##### 5.3.2.2 모든 마일리지 규칙 조회

**GET** `/admin/mileage/api/rules`

모든 마일리지 규칙 (비활성화 포함)을 조회합니다.

**Request**: 없음

**Response** (200 OK)
```json
[
  {
    "id": 1,
    "activityType": "PROGRAM",
    "activityName": "비교과 프로그램 참여",
    "points": 100,
    "description": "비교과 프로그램 완료 시 지급",
    "isActive": true
  }
]
```

**권한**: ADMIN

---

##### 5.3.2.3 마일리지 규칙 생성

**POST** `/admin/mileage/api/rules`

새 마일리지 규칙을 생성합니다.

**Request Body**
```json
{
  "activityType": "CONTEST",
  "activityName": "공모전 참여",
  "points": 200,
  "description": "공모전 참여 완료 시 지급"
}
```

**Response** (201 Created)
```json
{
  "message": "마일리지 규칙이 생성되었습니다.",
  "ruleId": 10
}
```

**권한**: ADMIN

---

##### 5.3.2.4 마일리지 규칙 수정

**PUT** `/admin/mileage/api/rules/{ruleId}`

마일리지 규칙을 수정합니다.

**Path Parameters**
- `ruleId` (Long): 규칙 ID

**Request Body**
```json
{
  "activityName": "공모전 입상",
  "points": 300,
  "description": "공모전 입상 시 지급",
  "isActive": true
}
```

**Response** (200 OK)
```json
{
  "message": "마일리지 규칙이 수정되었습니다."
}
```

**권한**: ADMIN

---

##### 5.3.2.5 마일리지 규칙 삭제

**DELETE** `/admin/mileage/api/rules/{ruleId}`

마일리지 규칙을 삭제합니다.

**Path Parameters**
- `ruleId` (Long): 규칙 ID

**Request**: 없음

**Response** (200 OK)
```json
{
  "message": "마일리지 규칙이 삭제되었습니다."
}
```

**권한**: ADMIN

---

##### 5.3.2.6 마일리지 수동 지급/차감

**POST** `/admin/mileage/api/award`

관리자가 수동으로 마일리지를 지급하거나 차감합니다.

**Request Body**
```json
{
  "userId": 1,
  "points": 100,
  "activityName": "특별 활동 참여",
  "description": "학과 행사 진행 도우미"
}
```

**Response** (201 Created)
```json
{
  "message": "마일리지가 지급되었습니다.",
  "historyId": 50
}
```

**권한**: ADMIN

**참고**: `points`가 음수일 경우 차감

---

##### 5.3.2.7 전체 사용자 마일리지 랭킹 조회

**GET** `/admin/mileage/api/ranking`

전체 사용자 마일리지 랭킹을 조회합니다.

**Request**: 없음

**Response** (200 OK)
```json
[
  {
    "rank": 1,
    "userId": 5,
    "userName": "정우진",
    "studentNum": 2022001,
    "department": "인공지능학과",
    "totalMileage": 2500
  }
]
```

**권한**: ADMIN

---

### 5.4 상담 API (ConsultationController)

**Base URL**: `/api/consultations`

#### 5.4.1 상담 신청

**POST** `/api/consultations`

학생이 상담을 신청합니다.

**Request Body**
```json
{
  "counselingType": "CAREER",
  "preferredDate": "2025-11-20T14:00:00",
  "topic": "진로 상담",
  "description": "졸업 후 진로에 대해 상담받고 싶습니다."
}
```

**Response** (201 Created)
```json
{
  "success": true,
  "message": "상담 신청이 완료되었습니다.",
  "consultation": {
    "id": 1,
    "studentId": 1,
    "counselingType": "CAREER",
    "status": "PENDING",
    "preferredDate": "2025-11-20T14:00:00",
    "topic": "진로 상담"
  }
}
```

**권한**: AUTHENTICATED (학생)

---

#### 5.4.2 내 상담 내역 조회

**GET** `/api/consultations/my`

로그인한 사용자의 모든 상담 내역을 조회합니다.

**Request**: 없음

**Response** (200 OK)
```json
[
  {
    "id": 1,
    "counselingType": "CAREER",
    "status": "CONFIRMED",
    "scheduledDate": "2025-11-20T14:00:00",
    "topic": "진로 상담",
    "counselorName": "김상담"
  }
]
```

**권한**: AUTHENTICATED

---

#### 5.4.3 상담 상세 조회

**GET** `/api/consultations/{sessionId}`

특정 상담의 상세 정보를 조회합니다.

**Path Parameters**
- `sessionId` (Long): 세션 ID

**Request**: 없음

**Response** (200 OK)
```json
{
  "id": 1,
  "studentId": 1,
  "studentName": "김철수",
  "counselorId": 100,
  "counselorName": "김상담",
  "counselingType": "CAREER",
  "status": "CONFIRMED",
  "scheduledDate": "2025-11-20T14:00:00",
  "topic": "진로 상담",
  "description": "졸업 후 진로에 대해 상담받고 싶습니다.",
  "location": "상담센터 3층 302호",
  "notes": "관련 자료 준비 바랍니다."
}
```

**권한**: 본인 또는 배정된 상담사만

---

#### 5.4.4 상담 취소

**PUT** `/api/consultations/{sessionId}/cancel`

상담 신청을 취소합니다.

**Path Parameters**
- `sessionId` (Long): 세션 ID

**Request**: 없음

**Response** (200 OK)
```json
{
  "success": true,
  "message": "상담 신청이 취소되었습니다."
}
```

**권한**: 신청한 학생만

**에러**
- 400: 이미 완료된 상담은 취소 불가

---

#### 5.4.5 상담 승인 (상담사)

**PUT** `/api/consultations/{sessionId}/approve`

상담사가 상담 신청을 승인합니다.

**Path Parameters**
- `sessionId` (Long): 세션 ID

**Request Body**
```json
{
  "scheduledDate": "2025-11-20T14:00:00",
  "location": "상담센터 3층 302호",
  "notes": "관련 자료 준비 바랍니다."
}
```

**Response** (200 OK)
```json
{
  "success": true,
  "message": "상담이 승인되었습니다.",
  "consultation": {
    "id": 1,
    "status": "CONFIRMED",
    "scheduledDate": "2025-11-20T14:00:00"
  }
}
```

**권한**: COUNSELOR 또는 ADMIN

---

#### 5.4.6 상담 거부 (상담사)

**PUT** `/api/consultations/{sessionId}/reject`

상담사가 상담 신청을 거부합니다.

**Path Parameters**
- `sessionId` (Long): 세션 ID

**Request Body**
```json
{
  "rejectReason": "해당 시간대에 다른 상담이 예정되어 있습니다."
}
```

**Response** (200 OK)
```json
{
  "success": true,
  "message": "상담이 거부되었습니다.",
  "consultation": {
    "id": 1,
    "status": "REJECTED",
    "rejectReason": "해당 시간대에 다른 상담이 예정되어 있습니다."
  }
}
```

**권한**: COUNSELOR 또는 ADMIN

---

#### 5.4.7 상담 기록 작성 (상담사)

**POST** `/api/consultations/{sessionId}/record`

상담 종료 후 기록을 작성합니다.

**Path Parameters**
- `sessionId` (Long): 세션 ID

**Request Body**
```json
{
  "summary": "진로 및 취업 준비 방향 상담",
  "detailNotes": "학생의 관심 분야와 역량을 고려하여 AI 분야 진출을 권장함.",
  "recommendations": "AI 관련 자격증 취득 및 프로젝트 경험 쌓기",
  "followUpNeeded": true
}
```

**Response** (201 Created)
```json
{
  "success": true,
  "message": "상담 기록이 저장되었습니다.",
  "record": {
    "id": 1,
    "sessionId": 1,
    "summary": "진로 및 취업 준비 방향 상담",
    "followUpNeeded": true
  }
}
```

**권한**: COUNSELOR

---

#### 5.4.8 학생 피드백 작성

**PUT** `/api/consultations/{sessionId}/feedback`

상담 받은 학생이 피드백을 작성합니다.

**Path Parameters**
- `sessionId` (Long): 세션 ID

**Request Body**
```json
{
  "satisfactionRating": 5,
  "feedback": "매우 유익한 상담이었습니다. 진로 방향을 명확히 할 수 있었습니다."
}
```

**Response** (200 OK)
```json
{
  "success": true,
  "message": "피드백이 저장되었습니다.",
  "record": {
    "id": 1,
    "satisfactionRating": 5,
    "feedback": "매우 유익한 상담이었습니다."
  }
}
```

**권한**: 상담 받은 학생만

---

#### 5.4.9 상담사 목록 조회

**GET** `/api/consultations/counselors`

모든 상담사 목록을 조회합니다.

**Request**: 없음

**Response** (200 OK)
```json
[
  {
    "counselorId": 100,
    "name": "김상담",
    "specialization": "진로 상담",
    "introduction": "10년 경력의 진로 상담 전문가입니다.",
    "availableSlots": [
      {
        "dayOfWeek": 1,
        "startTime": "09:00",
        "endTime": "12:00"
      }
    ]
  }
]
```

**권한**: PUBLIC

---

### 5.5 포트폴리오 API (PortfolioController)

**Base URL**: `/api/portfolios`

#### 5.5.1 사용자의 포트폴리오 목록 조회

**GET** `/api/portfolios`

로그인한 사용자의 모든 포트폴리오를 조회합니다.

**Request**: 없음

**Response** (200 OK)
```json
{
  "success": true,
  "portfolios": [
    {
      "id": 1,
      "title": "나의 성장 포트폴리오",
      "description": "대학 생활 동안의 활동 기록",
      "visibility": "PUBLIC",
      "viewCount": 150,
      "createdAt": "2025-01-01T00:00:00"
    }
  ],
  "count": 1
}
```

**권한**: AUTHENTICATED

---

#### 5.5.2 포트폴리오 상세 조회

**GET** `/api/portfolios/{portfolioId}`

포트폴리오 상세 정보를 조회합니다. (조회수 증가)

**Path Parameters**
- `portfolioId` (Long): 포트폴리오 ID

**Request**: 없음

**Response** (200 OK)
```json
{
  "success": true,
  "portfolio": {
    "id": 1,
    "userId": 1,
    "userName": "김철수",
    "title": "나의 성장 포트폴리오",
    "description": "대학 생활 동안의 활동 기록",
    "content": "상세 내용...",
    "category": "ACADEMIC",
    "tags": ["AI", "프로젝트", "공모전"],
    "visibility": "PUBLIC",
    "viewCount": 151,
    "createdAt": "2025-01-01T00:00:00",
    "items": [
      {
        "id": 1,
        "itemType": "PROGRAM",
        "title": "AI 기초 교육 프로그램",
        "description": "프로그램 참여 내용",
        "startDate": "2025-03-01",
        "endDate": "2025-06-30"
      }
    ]
  }
}
```

**권한**: 공개 범위에 따라 다름 (PUBLIC: 누구나, UNLISTED: 링크 있는 사람, PRIVATE: 작성자만)

---

#### 5.5.3 포트폴리오 생성

**POST** `/api/portfolios`

새 포트폴리오를 생성합니다.

**Request Body**
```json
{
  "title": "나의 성장 포트폴리오",
  "description": "대학 생활 동안의 활동 기록",
  "content": "상세 내용...",
  "category": "ACADEMIC",
  "tags": ["AI", "프로젝트"],
  "thumbnailUrl": "https://example.com/thumb.jpg",
  "visibility": "PUBLIC"
}
```

**Response** (201 Created)
```json
{
  "success": true,
  "message": "포트폴리오가 생성되었습니다.",
  "portfolio": {
    "id": 1,
    "title": "나의 성장 포트폴리오",
    "createdAt": "2025-11-18T10:00:00"
  }
}
```

**권한**: AUTHENTICATED

---

#### 5.5.4 포트폴리오 수정

**PUT** `/api/portfolios/{portfolioId}`

포트폴리오 정보를 수정합니다.

**Path Parameters**
- `portfolioId` (Long): 포트폴리오 ID

**Request Body**: (5.5.3과 동일)

**Response** (200 OK)
```json
{
  "success": true,
  "message": "포트폴리오가 수정되었습니다.",
  "portfolio": {
    "id": 1,
    "title": "수정된 제목"
  }
}
```

**권한**: 작성자만

---

#### 5.5.5 포트폴리오 삭제

**DELETE** `/api/portfolios/{portfolioId}`

포트폴리오를 삭제합니다.

**Path Parameters**
- `portfolioId` (Long): 포트폴리오 ID

**Request**: 없음

**Response** (200 OK)
```json
{
  "success": true,
  "message": "포트폴리오가 삭제되었습니다."
}
```

**권한**: 작성자만

---

#### 5.5.6 포트폴리오 공개 범위 변경

**PUT** `/api/portfolios/{portfolioId}/visibility`

포트폴리오 공개 범위를 설정합니다.

**Path Parameters**
- `portfolioId` (Long): 포트폴리오 ID

**Query Parameters**
- `visibility` (String): PUBLIC, UNLISTED, PRIVATE

**Request**: 없음

**Response** (200 OK)
```json
{
  "success": true,
  "message": "공개 범위가 변경되었습니다.",
  "visibility": "PUBLIC"
}
```

**권한**: 작성자만

---

#### 5.5.7 공유 링크 생성

**POST** `/api/portfolios/{portfolioId}/share`

포트폴리오 공유 링크를 생성합니다.

**Path Parameters**
- `portfolioId` (Long): 포트폴리오 ID

**Query Parameters**
- `expirationDays` (Integer, optional): 만료 일수 (기본값: 30)

**Request**: 없음

**Response** (201 Created)
```json
{
  "success": true,
  "message": "공유 링크가 생성되었습니다.",
  "share": {
    "shareToken": "abc123def456",
    "shareUrl": "http://localhost:8080/api/portfolios/shared/abc123def456",
    "expiresAt": "2025-12-18T10:00:00"
  }
}
```

**권한**: 작성자만

---

#### 5.5.8 공유 링크로 포트폴리오 조회

**GET** `/api/portfolios/shared/{shareToken}`

공유 링크를 통해 포트폴리오를 조회합니다.

**Path Parameters**
- `shareToken` (String): 공유 토큰

**Request**: 없음

**Response** (200 OK)
```json
{
  "success": true,
  "portfolio": {
    // 포트폴리오 상세 정보
  }
}
```

**권한**: 유효한 공유 토큰 필요

**에러**
- 404: 토큰이 유효하지 않음
- 410: 토큰이 만료됨

---

#### 5.5.9 포트폴리오 검색

**GET** `/api/portfolios/search`

키워드로 포트폴리오를 검색합니다.

**Query Parameters**
- `keyword` (String): 검색 키워드

**Request**: 없음

**Response** (200 OK)
```json
{
  "success": true,
  "portfolios": [
    {
      "id": 1,
      "title": "AI 프로젝트 포트폴리오",
      "description": "AI 관련 프로젝트 모음"
    }
  ],
  "count": 1
}
```

**권한**: AUTHENTICATED

---

### 5.6 설문 API (SurveyController)

**Base URL**: `/api/surveys`

#### 5.6.1 설문 생성 (관리자)

**POST** `/api/surveys`

새 설문을 생성합니다.

**Request Body**
```json
{
  "title": "역량 진단 설문",
  "description": "학생 역량 진단을 위한 설문입니다.",
  "startDate": "2025-11-18T00:00:00",
  "endDate": "2025-12-31T23:59:59",
  "questions": [
    {
      "questionText": "의사소통 능력이 우수하다고 생각하십니까?",
      "questionType": "RATING",
      "isRequired": true,
      "orderNum": 1,
      "competencyId": 1
    }
  ]
}
```

**Response** (201 Created)
```json
{
  "success": true,
  "message": "설문이 생성되었습니다.",
  "data": {
    "id": 1,
    "title": "역량 진단 설문",
    "createdAt": "2025-11-18T10:00:00"
  }
}
```

**권한**: ADMIN

---

#### 5.6.2 설문 상세 조회

**GET** `/api/surveys/{surveyId}`

설문 상세 정보를 조회합니다.

**Path Parameters**
- `surveyId` (Long): 설문 ID

**Request**: 없음

**Response** (200 OK)
```json
{
  "success": true,
  "message": "설문 조회 성공",
  "data": {
    "id": 1,
    "title": "역량 진단 설문",
    "description": "학생 역량 진단을 위한 설문입니다.",
    "startDate": "2025-11-18T00:00:00",
    "endDate": "2025-12-31T23:59:59",
    "isActive": true,
    "questions": [
      {
        "id": 1,
        "questionText": "의사소통 능력이 우수하다고 생각하십니까?",
        "questionType": "RATING",
        "isRequired": true,
        "orderNum": 1,
        "options": []
      }
    ]
  }
}
```

**권한**: PUBLIC

---

#### 5.6.3 활성 설문 목록 조회 (페이지네이션)

**GET** `/api/surveys`

활성화된 설문 목록을 조회합니다.

**Query Parameters**
- `page` (int, default: 0): 페이지 번호
- `size` (int, default: 10): 페이지 크기

**Request**: 없음

**Response** (200 OK)
```json
{
  "success": true,
  "message": "설문 목록 조회 성공",
  "data": {
    "content": [
      {
        "id": 1,
        "title": "역량 진단 설문",
        "description": "학생 역량 진단을 위한 설문입니다.",
        "startDate": "2025-11-18T00:00:00",
        "endDate": "2025-12-31T23:59:59"
      }
    ],
    "totalElements": 5,
    "totalPages": 1,
    "number": 0,
    "size": 10
  }
}
```

**권한**: PUBLIC

---

#### 5.6.4 진행 중인 설문 목록 조회

**GET** `/api/surveys/ongoing`

현재 진행 중인 설문 목록을 조회합니다.

**Request**: 없음

**Response** (200 OK)
```json
{
  "success": true,
  "message": "진행 중인 설문 조회 성공",
  "data": [
    {
      "id": 1,
      "title": "역량 진단 설문",
      "endDate": "2025-12-31T23:59:59"
    }
  ]
}
```

**권한**: PUBLIC

---

#### 5.6.5 사용자가 응답 가능한 설문 목록

**GET** `/api/surveys/available`

로그인한 사용자가 아직 응답하지 않은 설문 목록을 조회합니다.

**Request**: 없음

**Response** (200 OK)
```json
{
  "success": true,
  "message": "응답 가능한 설문 조회 성공",
  "data": [
    {
      "id": 1,
      "title": "역량 진단 설문",
      "description": "학생 역량 진단을 위한 설문입니다."
    }
  ]
}
```

**권한**: AUTHENTICATED

---

#### 5.6.6 설문 수정 (관리자)

**PUT** `/api/surveys/{surveyId}`

설문 정보를 수정합니다.

**Path Parameters**
- `surveyId` (Long): 설문 ID

**Request Body**: (5.6.1과 동일)

**Response** (200 OK)
```json
{
  "success": true,
  "message": "설문이 수정되었습니다.",
  "data": {
    "id": 1,
    "title": "수정된 설문 제목"
  }
}
```

**권한**: ADMIN

---

#### 5.6.7 설문 삭제 (관리자)

**DELETE** `/api/surveys/{surveyId}`

설문을 삭제합니다.

**Path Parameters**
- `surveyId` (Long): 설문 ID

**Request**: 없음

**Response** (200 OK)
```json
{
  "success": true,
  "message": "설문이 삭제되었습니다."
}
```

**권한**: ADMIN

---

#### 5.6.8 설문 활성화/비활성화 (관리자)

**POST** `/api/surveys/{surveyId}/toggle-active`

설문 활성화 상태를 토글합니다.

**Path Parameters**
- `surveyId` (Long): 설문 ID

**Request**: 없음

**Response** (200 OK)
```json
{
  "success": true,
  "message": "설문이 비활성화되었습니다."
}
```

**권한**: ADMIN

---

#### 5.6.9 설문 응답 제출

**POST** `/api/surveys/submit`

설문 응답을 제출합니다.

**Request Body**
```json
{
  "surveyId": 1,
  "answers": [
    {
      "questionId": 1,
      "optionId": null,
      "answerText": null,
      "answerScore": 5
    },
    {
      "questionId": 2,
      "optionId": 3,
      "answerText": null,
      "answerScore": null
    }
  ]
}
```

**Response** (201 Created)
```json
{
  "success": true,
  "message": "설문 응답이 제출되었습니다.",
  "data": 1
}
```

**권한**: AUTHENTICATED

**에러**
- 400: 이미 응답한 설문
- 400: 필수 문항 미응답

---

#### 5.6.10 설문 통계 조회 (관리자)

**GET** `/api/surveys/{surveyId}/statistics`

설문 응답 통계를 조회합니다.

**Path Parameters**
- `surveyId` (Long): 설문 ID

**Request**: 없음

**Response** (200 OK)
```json
{
  "success": true,
  "message": "설문 통계 조회 성공",
  "data": {
    "surveyId": 1,
    "totalResponses": 50,
    "questionStatistics": [
      {
        "questionId": 1,
        "questionText": "의사소통 능력이 우수하다고 생각하십니까?",
        "averageScore": 4.2,
        "distribution": {
          "1": 2,
          "2": 5,
          "3": 10,
          "4": 20,
          "5": 13
        }
      }
    ]
  }
}
```

**권한**: ADMIN

---

#### 5.6.11 사용자의 응답 내역 조회

**GET** `/api/surveys/my-responses`

내가 응답한 설문 목록을 조회합니다.

**Request**: 없음

**Response** (200 OK)
```json
{
  "success": true,
  "message": "응답 내역 조회 성공",
  "data": [
    {
      "id": 1,
      "title": "역량 진단 설문",
      "submittedAt": "2025-11-18T10:30:00"
    }
  ]
}
```

**권한**: AUTHENTICATED

---

### 5.7 외부취업가점 API (ExternalEmploymentController)

**Base URL**: `/api/external-employments`

#### 5.7.1 외부취업 활동 등록

**POST** `/api/external-employments`

새 외부취업 활동을 등록합니다.

**Request Body**
```json
{
  "companyName": "삼성전자",
  "employmentType": "INTERNSHIP",
  "startDate": "2025-01-01",
  "endDate": "2025-06-30",
  "position": "SW 개발 인턴",
  "description": "모바일 앱 개발 업무 수행",
  "proofDocumentUrl": "https://example.com/proof.pdf"
}
```

**Response** (201 Created)
```json
{
  "id": 1,
  "userId": 1,
  "companyName": "삼성전자",
  "employmentType": "INTERNSHIP",
  "startDate": "2025-01-01",
  "endDate": "2025-06-30",
  "position": "SW 개발 인턴",
  "creditPoints": 5.0,
  "status": "PENDING",
  "createdAt": "2025-11-18T10:00:00"
}
```

**권한**: STUDENT

---

#### 5.7.2 외부취업 활동 수정

**PUT** `/api/external-employments/{id}`

외부취업 활동 정보를 수정합니다.

**Path Parameters**
- `id` (Long): 활동 ID

**Request Body**: (5.7.1과 동일)

**Response** (200 OK)
```json
{
  "id": 1,
  "companyName": "수정된 회사명",
  "updatedAt": "2025-11-18T11:00:00"
}
```

**권한**: STUDENT (본인 활동만)

---

#### 5.7.3 외부취업 활동 삭제

**DELETE** `/api/external-employments/{id}`

외부취업 활동을 삭제합니다.

**Path Parameters**
- `id` (Long): 활동 ID

**Request**: 없음

**Response**: 204 No Content

**권한**: STUDENT (본인 활동만)

---

#### 5.7.4 내 외부취업 활동 목록 조회

**GET** `/api/external-employments/my`

로그인한 학생의 모든 외부취업 활동을 조회합니다.

**Request**: 없음

**Response** (200 OK)
```json
[
  {
    "id": 1,
    "companyName": "삼성전자",
    "employmentType": "INTERNSHIP",
    "startDate": "2025-01-01",
    "endDate": "2025-06-30",
    "position": "SW 개발 인턴",
    "creditPoints": 5.0,
    "status": "APPROVED"
  }
]
```

**권한**: STUDENT

---

#### 5.7.5 외부취업 활동 상세 조회

**GET** `/api/external-employments/{id}`

특정 외부취업 활동의 상세 정보를 조회합니다.

**Path Parameters**
- `id` (Long): 활동 ID

**Request**: 없음

**Response** (200 OK)
```json
{
  "id": 1,
  "userId": 1,
  "userName": "김철수",
  "companyName": "삼성전자",
  "employmentType": "INTERNSHIP",
  "startDate": "2025-01-01",
  "endDate": "2025-06-30",
  "position": "SW 개발 인턴",
  "description": "모바일 앱 개발 업무 수행",
  "creditPoints": 5.0,
  "status": "APPROVED",
  "approvedBy": "관리자",
  "approvedAt": "2025-11-19T10:00:00"
}
```

**권한**: PUBLIC

---

#### 5.7.6 내 총 획득 가점 조회

**GET** `/api/external-employments/my/total-credits`

로그인한 학생의 총 획득 가점을 조회합니다.

**Request**: 없음

**Response** (200 OK)
```json
{
  "totalCredits": 10.5
}
```

**권한**: STUDENT

---

#### 5.7.7 승인 대기 중인 활동 목록 조회 (관리자)

**GET** `/api/external-employments/admin/pending`

승인 대기 중인 외부취업 활동 목록을 조회합니다.

**Query Parameters**
- `page` (int, default: 0): 페이지 번호
- `size` (int, default: 10): 페이지 크기

**Request**: 없음

**Response** (200 OK)
```json
{
  "content": [
    {
      "id": 1,
      "userId": 1,
      "userName": "김철수",
      "companyName": "삼성전자",
      "employmentType": "INTERNSHIP",
      "status": "PENDING",
      "createdAt": "2025-11-18T10:00:00"
    }
  ],
  "totalElements": 15,
  "totalPages": 2,
  "number": 0,
  "size": 10
}
```

**권한**: ADMIN

---

#### 5.7.8 승인된 활동 목록 조회 (관리자)

**GET** `/api/external-employments/admin/verified`

승인된 외부취업 활동 목록을 조회합니다.

**Query Parameters**
- `page` (int, default: 0): 페이지 번호
- `size` (int, default: 10): 페이지 크기

**Request**: 없음

**Response** (200 OK): (5.7.7과 동일한 형식)

**권한**: ADMIN

---

#### 5.7.9 활동 승인/거절 (관리자)

**POST** `/api/external-employments/{id}/verify`

관리자가 외부취업 활동을 승인하거나 거절합니다.

**Path Parameters**
- `id` (Long): 활동 ID

**Request Body**
```json
{
  "approve": true,
  "rejectReason": null
}
```

또는
```json
{
  "approve": false,
  "rejectReason": "증빙 서류가 불충분합니다."
}
```

**Response** (200 OK)
```json
{
  "id": 1,
  "status": "APPROVED",
  "approvedBy": "관리자",
  "approvedAt": "2025-11-19T10:00:00"
}
```

**권한**: ADMIN

---

#### 5.7.10 가점 자동 계산 미리보기

**GET** `/api/external-employments/calculate-credits`

취업 유형과 기간으로 가점을 자동 계산합니다.

**Query Parameters**
- `employmentType` (EmploymentType): FULL_TIME, PART_TIME, INTERNSHIP, CONTRACT
- `durationMonths` (Integer): 근무 기간 (개월)

**Request**: 없음

**Response** (200 OK)
```json
{
  "credits": 5.0
}
```

**권한**: PUBLIC

---

#### 5.7.11 외부취업 통계 조회 (관리자)

**GET** `/api/external-employments/admin/statistics`

외부취업 활동 전체 통계를 조회합니다.

**Request**: 없음

**Response** (200 OK)
```json
{
  "totalCount": 100,
  "pendingCount": 15,
  "verifiedCount": 80,
  "rejectedCount": 5,
  "totalCreditsAwarded": 350.5,
  "typeStatistics": {
    "FULL_TIME": 20,
    "PART_TIME": 30,
    "INTERNSHIP": 40,
    "CONTRACT": 10
  },
  "monthlyStatistics": [
    {
      "month": "2025-11",
      "count": 10,
      "totalCredits": 45.0
    }
  ]
}
```

**권한**: ADMIN

---

### 5.8 알림 API (NotificationController)

**Base URL**: `/api/notifications`

#### 5.8.1 알림 목록 조회

**GET** `/api/notifications`

로그인한 사용자의 모든 알림을 조회합니다.

**Request**: 없음

**Response** (200 OK)
```json
[
  {
    "id": 1,
    "type": "APPLICATION_APPROVED",
    "title": "프로그램 신청 승인",
    "message": "AI 기초 교육 프로그램 신청이 승인되었습니다.",
    "linkUrl": "/programs/10",
    "isRead": false,
    "createdAt": "2025-11-18T10:00:00"
  }
]
```

**권한**: AUTHENTICATED

---

#### 5.8.2 읽지 않은 알림 목록 조회

**GET** `/api/notifications/unread`

읽지 않은 알림만 조회합니다.

**Request**: 없음

**Response** (200 OK): (5.8.1과 동일한 형식)

**권한**: AUTHENTICATED

---

#### 5.8.3 읽지 않은 알림 개수 조회

**GET** `/api/notifications/unread-count`

읽지 않은 알림 개수를 조회합니다.

**Request**: 없음

**Response** (200 OK)
```json
{
  "count": 5
}
```

**권한**: AUTHENTICATED

---

#### 5.8.4 알림 읽음 처리

**PUT** `/api/notifications/{notificationId}/read`

특정 알림을 읽음으로 표시합니다.

**Path Parameters**
- `notificationId` (Integer): 알림 ID

**Request**: 없음

**Response** (200 OK)
```json
{
  "message": "알림이 읽음 처리되었습니다."
}
```

**권한**: 본인 알림만

---

#### 5.8.5 모든 알림 읽음 처리

**PUT** `/api/notifications/read-all`

모든 알림을 읽음으로 표시합니다.

**Request**: 없음

**Response** (200 OK)
```json
{
  "message": "모든 알림이 읽음 처리되었습니다.",
  "count": 5
}
```

**권한**: AUTHENTICATED

---

#### 5.8.6 알림 삭제

**DELETE** `/api/notifications/{notificationId}`

특정 알림을 삭제합니다.

**Path Parameters**
- `notificationId` (Integer): 알림 ID

**Request**: 없음

**Response** (200 OK)
```json
{
  "message": "알림이 삭제되었습니다."
}
```

**권한**: 본인 알림만

---

#### 5.8.7 모든 알림 삭제

**DELETE** `/api/notifications/all`

모든 알림을 삭제합니다.

**Request**: 없음

**Response** (200 OK)
```json
{
  "message": "모든 알림이 삭제되었습니다."
}
```

**권한**: AUTHENTICATED

---

### 5.9 역량평가 API (CompetencyAssessmentController)

**Base URL**: `/api/assessments`

#### 5.9.1 평가 저장 (단건)

**POST** `/api/assessments`

역량 평가를 저장합니다.

**Request Body**
```json
{
  "studentId": 1,
  "competencyId": 1,
  "score": 85,
  "assessorId": 100,
  "assessmentType": "SELF",
  "notes": "의사소통 능력이 향상되었습니다."
}
```

**Response** (201 Created)
```json
{
  "id": 1,
  "studentId": 1,
  "competencyId": 1,
  "competencyName": "의사소통",
  "score": 85,
  "assessmentDate": "2025-11-18",
  "assessorId": 100,
  "assessorName": "김교수"
}
```

**권한**: AUTHENTICATED (평가자)

---

#### 5.9.2 평가 일괄 저장

**POST** `/api/assessments/batch`

여러 역량을 동시에 평가합니다.

**Request Body**
```json
[
  {
    "studentId": 1,
    "competencyId": 1,
    "score": 85
  },
  {
    "studentId": 1,
    "competencyId": 2,
    "score": 90
  }
]
```

**Response** (201 Created)
```json
[
  {
    "id": 1,
    "competencyId": 1,
    "score": 85
  },
  {
    "id": 2,
    "competencyId": 2,
    "score": 90
  }
]
```

**권한**: AUTHENTICATED (평가자)

---

#### 5.9.3 평가 상세 조회

**GET** `/api/assessments/{assessmentId}`

특정 평가의 상세 정보를 조회합니다.

**Path Parameters**
- `assessmentId` (Long): 평가 ID

**Request**: 없음

**Response** (200 OK)
```json
{
  "id": 1,
  "studentId": 1,
  "studentName": "김철수",
  "competencyId": 1,
  "competencyName": "의사소통",
  "score": 85,
  "assessmentDate": "2025-11-18",
  "assessorId": 100,
  "assessorName": "김교수",
  "assessmentType": "SELF",
  "notes": "의사소통 능력이 향상되었습니다."
}
```

**권한**: AUTHENTICATED

---

#### 5.9.4 평가 수정

**PUT** `/api/assessments/{assessmentId}`

평가 정보를 수정합니다.

**Path Parameters**
- `assessmentId` (Long): 평가 ID

**Request Body**
```json
{
  "score": 90,
  "notes": "수정된 비고"
}
```

**Response** (200 OK)
```json
{
  "id": 1,
  "score": 90,
  "updatedAt": "2025-11-18T11:00:00"
}
```

**권한**: 평가자만

---

#### 5.9.5 평가 삭제

**DELETE** `/api/assessments/{assessmentId}`

평가를 삭제합니다.

**Path Parameters**
- `assessmentId` (Long): 평가 ID

**Request**: 없음

**Response**: 204 No Content

**권한**: 평가자만

---

#### 5.9.6 학생별 모든 평가 조회

**GET** `/api/assessments/students/{studentId}`

특정 학생의 모든 역량 평가 내역을 조회합니다.

**Path Parameters**
- `studentId` (Long): 학생 ID

**Request**: 없음

**Response** (200 OK)
```json
[
  {
    "id": 1,
    "competencyId": 1,
    "competencyName": "의사소통",
    "score": 85,
    "assessmentDate": "2025-11-18"
  }
]
```

**권한**: AUTHENTICATED

---

#### 5.9.7 학생별 최신 평가 조회

**GET** `/api/assessments/students/{studentId}/latest`

각 역량당 최신 평가만 조회합니다.

**Path Parameters**
- `studentId` (Long): 학생 ID

**Request**: 없음

**Response** (200 OK): (5.9.6과 동일한 형식)

**권한**: AUTHENTICATED

---

#### 5.9.8 학생별 평가 조회 (페이지네이션)

**GET** `/api/assessments/students/{studentId}/page`

학생별 평가를 페이지네이션으로 조회합니다.

**Path Parameters**
- `studentId` (Long): 학생 ID

**Query Parameters**
- `page` (int, default: 0): 페이지 번호
- `size` (int, default: 10): 페이지 크기
- `sort` (String, default: "assessmentDate,desc"): 정렬 기준

**Request**: 없음

**Response** (200 OK)
```json
{
  "content": [...],
  "totalElements": 50,
  "totalPages": 5,
  "number": 0,
  "size": 10
}
```

**권한**: AUTHENTICATED

---

#### 5.9.9 학생 역량 평가 리포트 생성

**GET** `/api/assessments/students/{studentId}/report`

학생의 종합 역량 평가 리포트를 생성합니다.

**Path Parameters**
- `studentId` (Long): 학생 ID

**Request**: 없음

**Response** (200 OK)
```json
{
  "studentInfo": {
    "id": 1,
    "name": "김철수",
    "studentNum": 2024001,
    "department": "컴퓨터공학과"
  },
  "competencyScores": [
    {
      "competencyId": 1,
      "competencyName": "의사소통",
      "score": 85
    }
  ],
  "averageScore": 83.5,
  "strengths": ["의사소통", "문제해결"],
  "weaknesses": ["창의성"],
  "recommendations": [
    "창의성 향상을 위한 프로그램 참여 권장"
  ]
}
```

**권한**: AUTHENTICATED

---

#### 5.9.10 역량별 통계 조회

**GET** `/api/assessments/competencies/{competencyId}/statistics`

특정 역량에 대한 전체 통계를 조회합니다.

**Path Parameters**
- `competencyId` (Long): 역량 ID

**Request**: 없음

**Response** (200 OK)
```json
{
  "competencyId": 1,
  "competencyName": "의사소통",
  "averageScore": 78.5,
  "maxScore": 95,
  "minScore": 60,
  "assessmentCount": 100,
  "distributionByScore": {
    "60-69": 10,
    "70-79": 30,
    "80-89": 40,
    "90-100": 20
  }
}
```

**권한**: AUTHENTICATED

---

### 5.10 프로그램 추천 API (ProgramRecommendationController)

**Base URL**: `/api/programs`

#### 5.10.1 학생 맞춤형 프로그램 추천

**GET** `/api/programs/recommendations`

AI 기반 학생 맞춤형 프로그램을 추천합니다.

**Query Parameters**
- `studentId` (Long): 학생 ID (Student 테이블의 ID)
- `limit` (int, default: 10): 추천 개수

**Request**: 없음

**Response** (200 OK)
```json
[
  {
    "programId": 10,
    "title": "AI 기초 교육",
    "description": "인공지능 기초 이론 및 실습",
    "category": "기술교육",
    "recommendationScore": 0.95,
    "recommendationReason": "학생의 역량 진단 결과 AI 분야에 관심이 높고, 관련 프로그램 참여 이력이 있습니다."
  },
  {
    "programId": 15,
    "title": "창업 아이디어 경진대회",
    "description": "창업 아이디어 발굴 및 발표",
    "category": "창업",
    "recommendationScore": 0.87,
    "recommendationReason": "창의성 역량 향상에 도움이 됩니다."
  }
]
```

**권한**: PUBLIC

---

### 5.11 페이지 API (HomeController)

**Base URL**: `/` (HTML 페이지 반환)

#### 5.11.1 홈 페이지

**GET** `/`

메인 홈 페이지를 표시합니다.

**Query Parameters**
- `department` (String, optional): 학과 필터
- `college` (String, optional): 단과대 필터
- `category` (String, optional): 카테고리 필터

**Request**: 없음

**Response**: HTML View (`index`)

**권한**: PUBLIC

---

#### 5.11.2 프로그램 상세 페이지

**GET** `/programs/{programId}`

프로그램 상세 정보 페이지를 표시합니다. (조회수 증가)

**Path Parameters**
- `programId` (Integer): 프로그램 ID

**Request**: 없음

**Response**: HTML View (`program-detail`)

**권한**: PUBLIC

---

#### 5.11.3 프로그램 전체보기 페이지

**GET** `/programs`

프로그램 전체 목록을 표시합니다.

**Query Parameters**
- `department`, `college`, `category` (String, optional): 필터
- `search` (String, optional): 검색 키워드
- `recommended` (Boolean, optional): 추천 모드
- `studentId` (Long, optional): 추천 대상 학생 ID
- `page` (int, default: 0): 페이지 번호
- `size` (int, default: 12): 페이지 크기

**Request**: 없음

**Response**: HTML View (`programs`)

**권한**: PUBLIC

---

#### 5.11.4 로그인 페이지

**GET** `/login`

로그인 페이지를 표시합니다.

**Query Parameters**
- `error` (String, optional): 에러 메시지
- `message` (String, optional): 안내 메시지

**Request**: 없음

**Response**: HTML View (`login`)

**권한**: PUBLIC (이미 로그인한 경우 홈으로 리다이렉트)

---

#### 5.11.5 로그아웃

**GET, POST** `/logout`

로그아웃 처리를 수행합니다.

**Request**: 없음

**Response**: Redirect to `/`

**권한**: PUBLIC

---

#### 5.11.6 비밀번호 변경 페이지

**GET** `/password/change`

비밀번호 변경 페이지를 표시합니다.

**Request**: 없음

**Response**: HTML View (`password-change`)

**권한**: AUTHENTICATED

---

#### 5.11.7 비밀번호 찾기 페이지

**GET** `/password/reset`

비밀번호 찾기/재설정 페이지를 표시합니다.

**Request**: 없음

**Response**: HTML View (`password-reset`)

**권한**: PUBLIC

---

#### 5.11.8 알림 페이지

**GET** `/notifications`

알림 페이지를 표시합니다.

**Request**: 없음

**Response**: HTML View (`notifications`)

**권한**: AUTHENTICATED

---

## 6. 에러 코드

### 6.1 에러 코드 목록

| 에러 코드 | HTTP 상태 | 설명 |
|----------|----------|------|
| `AUTH_FAILED` | 401 | 인증 실패 |
| `UNAUTHORIZED` | 401 | 로그인 필요 |
| `FORBIDDEN` | 403 | 권한 없음 |
| `NOT_FOUND` | 404 | 리소스를 찾을 수 없음 |
| `ALREADY_EXISTS` | 400 | 이미 존재하는 리소스 |
| `INVALID_REQUEST` | 400 | 잘못된 요청 |
| `VALIDATION_ERROR` | 422 | 유효성 검증 실패 |
| `ACCOUNT_LOCKED` | 423 | 계정 잠금 |
| `INTERNAL_ERROR` | 500 | 서버 내부 오류 |

### 6.2 에러 응답 예시

```json
{
  "success": false,
  "message": "학번 또는 비밀번호가 일치하지 않습니다.",
  "errorCode": "AUTH_FAILED",
  "timestamp": "2025-11-18T10:30:00",
  "path": "/api/auth/login"
}
```

---

## 7. 버전 이력

| 버전 | 날짜 | 작성자 | 변경 내용 |
|------|------|--------|----------|
| 1.0 | 2025-11-18 | 개발팀 | 최초 작성 |

---

**문서 작성일**: 2025-11-18
**최종 검토자**: 개발팀
**승인일**: 2025-11-18
