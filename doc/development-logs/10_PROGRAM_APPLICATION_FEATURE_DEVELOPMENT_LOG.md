# 프로그램 신청 기능 개발 로그

## 📅 개발 일자
2025-11-14

## 📋 개요

프로그램 상세 페이지에서 학생들이 프로그램을 신청하고 취소할 수 있는 기능을 구현했습니다.

### 주요 기능
- ✅ 프로그램 신청 기능
- ✅ 프로그램 신청 취소 기능
- ✅ 나의 신청내역 조회 (탭 기능)
- ✅ 실시간 신청 상태 업데이트
- ✅ 중복 신청 방지
- ✅ 정원 관리 (참가자 수 자동 증감)
- ✅ 로그인 연동

---

## 구현 내용

### 1. Backend 구현

#### 1.1 Entity Layer

**ApplicationStatus.java** (Enum)
- PENDING: 대기
- APPROVED: 승인
- REJECTED: 거부
- CANCELLED: 취소
- COMPLETED: 참여완료

**ProgramApplication.java**
- 주요 필드:
  - program: 프로그램 (ManyToOne)
  - user: 신청 사용자 (ManyToOne)
  - status: 신청 상태
  - appliedAt: 신청일시
  - approvedAt, rejectedAt, cancelledAt, completedAt: 상태 변경 일시
  - rejectionReason: 거부 사유
  - notes: 메모
- 주요 메서드:
  - approve(): 신청 승인
  - reject(reason): 신청 거부
  - cancel(): 신청 취소
  - complete(): 참여 완료
  - isCancellable(): 취소 가능 여부 확인

#### 1.2 Repository Layer

**ProgramApplicationRepository.java**
- findByUserIdAndProgramId(): 사용자의 특정 프로그램 신청 조회
- findByUserId(): 사용자의 모든 신청 내역
- findByProgramId(): 프로그램별 신청 내역
- findByUserIdAndStatus(): 상태별 신청 조회
- existsActiveApplicationByUserAndProgram(): 활성 신청 존재 여부
- countApprovedApplicationsByProgramId(): 승인된 신청 개수

#### 1.3 Service Layer

**ProgramApplicationService.java**
- applyProgram(userId, programId): 프로그램 신청
  - 중복 신청 확인
  - 신청 가능 여부 확인 (기간, 정원)
  - 신청 생성
  - 참가자 수 증가
- cancelApplication(userId, applicationId): 신청 취소
  - 본인 신청 확인
  - 취소 가능 상태 확인
  - 참가자 수 감소
- getUserApplications(userId): 사용자 신청 내역 조회
- getUserApplicationForProgram(userId, programId): 특정 프로그램 신청 조회
- approveApplication(applicationId): 신청 승인 (관리자)
- rejectApplication(applicationId, reason): 신청 거부 (관리자)

#### 1.4 Controller Layer

**ProgramApplicationController.java**
- POST /api/programs/{programId}/apply: 프로그램 신청
- DELETE /api/programs/applications/{applicationId}: 신청 취소
- GET /api/programs/applications/my: 나의 모든 신청 내역
- GET /api/programs/{programId}/my-application: 특정 프로그램 신청 상태
- GET /api/programs/{programId}/applications: 프로그램별 신청 내역 (관리자)

#### 1.5 DTO Layer

**ProgramApplicationResponse.java**
- 프로그램 신청 정보를 클라이언트에 전달하는 DTO
- 프로그램 정보, 신청 상태, 날짜 정보 포함

---

### 2. Frontend 구현

#### 2.1 program-detail.html 수정

**주요 변경사항:**

1. **페이지 로드 시 신청 상태 확인**
   - 로그인 사용자는 자동으로 신청 상태 로드
   - 신청 버튼 텍스트 자동 업데이트

2. **나의 신청내역 탭**
   - 테이블 구조 변경 (신청일 컬럼 추가)
   - 실시간 데이터 로드
   - 상태별 색상 배지 표시 (대기/승인/거부/취소/참여완료)
   - 취소 가능한 경우 취소 버튼 표시

3. **신청 버튼**
   - 비로그인: "로그인이 필요합니다" → 로그인 페이지로 이동
   - 로그인 & 미신청: "신청하기" → 신청 API 호출
   - 로그인 & 신청완료: "신청 완료" → 버튼 비활성화

4. **JavaScript 함수**
   - loadApplicationStatus(): 신청 상태 로드
   - displayApplicationStatus(): 신청 내역 표시
   - displayNoApplication(): 신청 없음 표시
   - updateApplyButton(): 신청 버튼 상태 업데이트
   - applyProgram(): 프로그램 신청
   - cancelApplication(): 신청 취소

---

## 주요 비즈니스 로직

### 1. 신청 프로세스

```
1. 사용자가 "신청하기" 버튼 클릭
2. 로그인 여부 확인
3. 중복 신청 확인
4. 신청 가능 여부 확인 (기간, 정원)
5. 신청 생성 (상태: PENDING)
6. 프로그램 current_participants + 1
7. 성공 메시지 표시
8. 페이지 새로고침 (참가자 수 업데이트)
```

### 2. 취소 프로세스

```
1. 사용자가 "취소하기" 버튼 클릭
2. 취소 확인 다이얼로그
3. 본인 신청 확인
4. 취소 가능 상태 확인 (PENDING 또는 APPROVED)
5. 신청 상태 → CANCELLED
6. 프로그램 current_participants - 1
7. 성공 메시지 표시
8. 페이지 새로고침
```

### 3. 신청 상태 흐름

```
PENDING (대기)
  ↓
  ├→ APPROVED (승인) → COMPLETED (참여완료)
  ├→ REJECTED (거부)
  └→ CANCELLED (취소)
```

---

## 데이터베이스 스키마

### program_applications 테이블

```sql
CREATE TABLE program_applications (
    application_id INT PRIMARY KEY AUTO_INCREMENT,
    program_id INT NOT NULL,
    user_id INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    applied_at DATETIME NOT NULL,
    approved_at DATETIME,
    rejected_at DATETIME,
    cancelled_at DATETIME,
    completed_at DATETIME,
    rejection_reason TEXT,
    notes TEXT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    deleted_at DATETIME,
    FOREIGN KEY (program_id) REFERENCES programs(program_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);
```

---

## 테스트 시나리오

### 1. 비로그인 사용자
- [x] 상세 페이지 접근 가능
- [x] "로그인이 필요합니다" 버튼 표시
- [x] 버튼 클릭 시 로그인 페이지로 이동 확인 다이얼로그
- [x] 나의 신청내역 탭: "로그인 후 확인 가능" 메시지

### 2. 로그인 사용자 (미신청)
- [x] "신청하기" 버튼 표시
- [x] 버튼 클릭 시 확인 다이얼로그
- [x] 신청 완료 후 "신청 완료" 버튼으로 변경
- [x] 참가자 수 +1 증가
- [x] 나의 신청내역 탭에 신청 정보 표시

### 3. 로그인 사용자 (신청완료)
- [x] "신청 완료" 버튼 (비활성화)
- [x] 나의 신청내역 탭에 신청 정보 표시
- [x] 상태별 색상 배지 표시
- [x] 취소 가능한 경우 "취소하기" 버튼 표시

### 4. 신청 취소
- [x] "취소하기" 버튼 클릭
- [x] 취소 확인 다이얼로그
- [x] 취소 완료 후 "신청하기" 버튼으로 변경
- [x] 참가자 수 -1 감소
- [x] 나의 신청내역: 상태 "취소"로 변경

### 5. 중복 신청 방지
- [x] 이미 신청한 프로그램 재신청 시 에러 메시지

### 6. 정원 관리
- [x] 정원 초과 시 신청 불가
- [x] 정원 가득 찬 경우 "신청 마감" 표시

---

## 에러 처리

### API 에러 응답

1. **로그인 필요 (401)**
   ```json
   {"error": "로그인이 필요합니다."}
   ```

2. **중복 신청 (409)**
   ```json
   {"error": "이미 신청한 프로그램입니다."}
   ```

3. **신청 불가 (409)**
   ```json
   {"error": "현재 신청할 수 없는 프로그램입니다."}
   ```

4. **정원 마감 (409)**
   ```json
   {"error": "프로그램 정원이 마감되었습니다."}
   ```

5. **권한 없음 (409)**
   ```json
   {"error": "본인의 신청만 취소할 수 있습니다."}
   ```

6. **서버 오류 (500)**
   ```json
   {"error": "서버 오류가 발생했습니다."}
   ```

---

## 향후 개선사항

### 1. 승인 프로세스 구현
- [ ] 관리자 페이지에서 신청 승인/거부 기능
- [ ] 승인 알림 이메일 발송
- [ ] 대량 승인 기능

### 2. 대기열 시스템
- [ ] 정원 초과 시 대기열 등록
- [ ] 취소 발생 시 대기열 자동 승인

### 3. 마일리지 연동
- [ ] 프로그램 참여 완료 시 마일리지 자동 적립
- [ ] 취소 시 패널티 마일리지

### 4. 출석 관리
- [ ] QR 코드 출석 체크
- [ ] 출석률에 따른 참여 완료 처리

### 5. 알림 기능
- [ ] 신청 상태 변경 시 알림
- [ ] 프로그램 시작 D-1 알림
- [ ] 신청 마감 임박 알림

---

## 파일 목록

### Backend
- src/main/java/com/scms/app/model/ApplicationStatus.java (NEW)
- src/main/java/com/scms/app/model/ProgramApplication.java (NEW)
- src/main/java/com/scms/app/repository/ProgramApplicationRepository.java (NEW)
- src/main/java/com/scms/app/service/ProgramApplicationService.java (NEW)
- src/main/java/com/scms/app/controller/ProgramApplicationController.java (NEW)
- src/main/java/com/scms/app/dto/ProgramApplicationResponse.java (NEW)

### Frontend
- src/main/resources/templates/program-detail.html (MODIFIED)

### Documentation
- doc/10_PROGRAM_APPLICATION_FEATURE_DEVELOPMENT_LOG.md (NEW)

---

## 요약

프로그램 신청 기능을 완전히 구현하여 학생들이 비교과 프로그램에 신청하고 관리할 수 있게 되었습니다.

**핵심 성과:**
1. ✅ 완전한 신청/취소 기능 구현
2. ✅ 실시간 신청 상태 확인
3. ✅ 중복 신청 및 정원 관리
4. ✅ 사용자 친화적인 UI/UX
5. ✅ 전체 프로젝트 핵심 기능 완성

이제 학생들은 프로그램 상세 페이지에서 바로 신청할 수 있으며, 나의 신청내역을 실시간으로 확인하고 관리할 수 있습니다.

다음 단계로는 **관리자 승인 기능**, **마일리지 연동**, **알림 시스템** 구현이 필요합니다.
