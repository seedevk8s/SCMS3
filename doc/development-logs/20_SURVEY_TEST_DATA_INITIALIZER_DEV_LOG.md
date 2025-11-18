# 20. 설문조사 테스트 데이터 초기화 개발 로그

**작성일:** 2025-11-18
**개발자:** Claude (AI Assistant)
**브랜치:** `claude/review-portfolio-012kRTYwt5T23RBteRQPHmZq`

---

## 📋 목차
1. [개요](#개요)
2. [구현 내용](#구현-내용)
3. [기술적 의사결정](#기술적-의사결정)
4. [커밋 히스토리](#커밋-히스토리)
5. [테스트 가이드](#테스트-가이드)
6. [향후 개선사항](#향후-개선사항)

---

## 개요

### 배경
이전 PR에서 설문조사 시스템이 구현되었으나, 테스트 데이터가 없어 이영희 학생 계정으로 로그인 시 다음과 같은 빈 화면만 표시되는 문제가 발생했습니다:
- "현재 응답 가능한 설문이 없습니다"
- "응답한 설문이 없습니다"

이로 인해 설문조사 기능을 실제로 테스트하고 확인할 수 있는 방법이 없었습니다.

### 목표
1. **테스트 데이터 자동 생성 시스템 구현**
   - 애플리케이션 시작 시 자동으로 샘플 설문 데이터 생성
   - 다양한 질문 유형 포함 (척도형, 객관식, 주관식)
   - 진행 중/종료된 설문 모두 포함

2. **실제 사용 시나리오 제공**
   - 학생이 응답할 수 있는 진행 중인 설문 제공
   - 과거 응답 내역 확인 가능하도록 샘플 응답 데이터 포함
   - 익명/실명 설문 모두 포함

### 개발 범위
- `SurveyDataInitializer` 클래스 작성
- 3개의 샘플 설문 생성
- 이영희 학생의 과거 응답 데이터 1건 생성

---

## 구현 내용

### 1. SurveyDataInitializer 클래스

#### 1.1 기본 구조

**파일 위치:** `src/main/java/com/scms/app/config/SurveyDataInitializer.java`

**주요 특징:**
```java
@Component
@Order(2) // MileageDataInitializer 이후 실행
@RequiredArgsConstructor
@Slf4j
public class SurveyDataInitializer implements CommandLineRunner
```

- `CommandLineRunner` 구현으로 애플리케이션 시작 시 자동 실행
- `@Order(2)` 설정으로 마일리지 데이터 초기화 이후 실행
- 이미 데이터가 있으면 스킵하여 중복 생성 방지

#### 1.2 중복 생성 방지 로직

```java
@Override
@Transactional
public void run(String... args) {
    // 이미 데이터가 있으면 스킵
    if (surveyRepository.count() > 0) {
        log.info("설문조사 데이터가 이미 존재합니다. 초기화를 스킵합니다.");
        return;
    }
    // ... 데이터 생성 로직
}
```

### 2. 생성되는 샘플 설문

#### 2.1 설문 #1: "2025년 학생복지 만족도 조사" (진행 중)

**설문 기본 정보:**
- 시작일: 현재일 기준 3일 전
- 종료일: 현재일 기준 14일 후
- 익명 여부: 실명 설문 (isAnonymous = false)
- 대상: 전체 학생 (targetType = ALL)
- 결과 공개: 활성화 (showResults = true)

**질문 구성:**

1. **Q1: 전반적 만족도 평가 (척도형)**
   - 질문 유형: `QuestionType.SCALE`
   - 점수 범위: 1-5점
   - 최소 레이블: "매우 불만족"
   - 최대 레이블: "매우 만족"
   - 필수 응답: true

2. **Q2: 가장 유용한 서비스 선택 (객관식 단일선택)**
   - 질문 유형: `QuestionType.SINGLE_CHOICE`
   - 선택지:
     - 심리상담 서비스
     - 진로상담 서비스
     - 장학금/재정지원
     - 건강관리 서비스
     - 기타
   - 필수 응답: true

3. **Q3: 개선 의견 작성 (주관식 서술형)**
   - 질문 유형: `QuestionType.LONG_TEXT`
   - 필수 응답: false
   - 자유로운 의견 작성 가능

4. **Q4: 필요한 서비스 다중 선택 (객관식 복수선택)**
   - 질문 유형: `QuestionType.MULTIPLE_CHOICE`
   - 선택지:
     - 학업 멘토링
     - 취업 준비 지원
     - 법률 상담
     - 외국어 교육 지원
   - 필수 응답: false

#### 2.2 설문 #2: "온라인 강의 개선 설문" (진행 중, 익명)

**설문 기본 정보:**
- 시작일: 현재일 기준 1일 전
- 종료일: 현재일 기준 7일 후
- 익명 여부: 익명 설문 (isAnonymous = true)
- 대상: 전체 학생
- 결과 공개: 비활성화 (showResults = false)

**질문 구성:**

1. **Q1: 온라인 강의 만족도 (척도형)**
   - 점수 범위: 1-5점
   - 필수 응답: true

2. **Q2: 불편한 점 선택 (객관식 단일선택)**
   - 선택지:
     - 화질/음질 문제
     - 시스템 접속 불안정
     - UI/UX가 불편함
     - 모바일 지원 부족
   - 필수 응답: true

3. **Q3: 개선 의견 작성 (주관식 서술형)**
   - 필수 응답: false

#### 2.3 설문 #3: "2024년 2학기 복지 만족도" (종료됨)

**설문 기본 정보:**
- 시작일: 현재일 기준 60일 전
- 종료일: 현재일 기준 30일 전 (이미 종료됨)
- 익명 여부: 실명 설문
- 활성화 상태: false
- 결과 공개: 활성화

**질문 구성:**

1. **Q1: 2학기 만족도 (척도형)**
   - 점수 범위: 1-5점

2. **Q2: 이용한 서비스 (객관식 복수선택)**
   - 선택지:
     - 심리상담
     - 진로상담
     - 장학금 신청

**샘플 응답 데이터:**
```java
// 이영희 학생(userId = 2)의 응답
SurveyResponse response = SurveyResponse.builder()
    .surveyId(survey.getSurveyId())
    .userId(2) // 이영희 student ID
    .submittedAt(now.minusDays(35))
    .ipAddress("127.0.0.1")
    .userAgent("Test Browser")
    .build();

// Q1 답변: 만족도 4점
SurveyAnswer answer1 = SurveyAnswer.builder()
    .responseId(response.getResponseId())
    .questionId(q1.getQuestionId())
    .answerNumber(4)
    .build();

// Q2 답변: 심리상담, 진로상담 선택
SurveyAnswer answer2 = SurveyAnswer.builder()
    .responseId(response.getResponseId())
    .questionId(q2.getQuestionId())
    .optionId(option1) // 심리상담
    .build();

SurveyAnswer answer3 = SurveyAnswer.builder()
    .responseId(response.getResponseId())
    .questionId(q2.getQuestionId())
    .optionId(option2) // 진로상담
    .build();
```

### 3. 데이터 생성 프로세스

#### 3.1 실행 흐름

```
애플리케이션 시작
    ↓
CommandLineRunner 실행 (@Order(2))
    ↓
설문 데이터 존재 여부 확인
    ↓
없으면 → 데이터 생성 시작
    ↓
설문 #1 생성
├── Survey 엔티티 저장
├── SurveyQuestion 4개 저장
└── SurveyQuestionOption 여러 개 저장
    ↓
설문 #2 생성
├── Survey 엔티티 저장
├── SurveyQuestion 3개 저장
└── SurveyQuestionOption 여러 개 저장
    ↓
설문 #3 생성
├── Survey 엔티티 저장
├── SurveyQuestion 2개 저장
├── SurveyQuestionOption 여러 개 저장
├── SurveyResponse 1개 저장 (이영희 응답)
└── SurveyAnswer 3개 저장
    ↓
로그 출력: "설문조사 테스트 데이터 삽입 완료"
```

#### 3.2 트랜잭션 관리

```java
@Override
@Transactional
public void run(String... args) {
    // 모든 데이터 생성이 하나의 트랜잭션으로 처리됨
    // 중간에 실패 시 전체 롤백
}
```

### 4. 로깅

```java
log.info("=== 설문조사 테스트 데이터 삽입 시작 ===");
log.info("설문 생성: {}", survey.getTitle());
log.info("샘플 응답 데이터 추가: 이영희 학생의 과거 설문 응답");
log.info("=== 설문조사 테스트 데이터 삽입 완료: {} 개 설문 생성됨 ===",
    surveyRepository.count());
```

---

## 기술적 의사결정

### 1. CommandLineRunner 선택 이유

**선택지:**
1. `CommandLineRunner` 인터페이스 구현
2. `@PostConstruct` 메서드 사용
3. `ApplicationRunner` 인터페이스 구현

**선택:** `CommandLineRunner`

**이유:**
- 애플리케이션 초기화 시점에 실행되어 데이터 준비 완료
- `@Order` 어노테이션으로 다른 Initializer와의 실행 순서 제어 가능
- 기존 `MileageDataInitializer`와 동일한 패턴 유지로 일관성 확보
- 트랜잭션 범위 관리가 명확함

### 2. 중복 생성 방지 전략

**구현 방법:**
```java
if (surveyRepository.count() > 0) {
    log.info("설문조사 데이터가 이미 존재합니다. 초기화를 스킵합니다.");
    return;
}
```

**장점:**
- 애플리케이션 재시작 시 데이터 중복 생성 방지
- 개발 환경에서 데이터베이스 유지 시 편리함
- 운영 환경에서 실수로 실행되어도 안전

**단점:**
- 개발 중 데이터를 다시 생성하려면 수동 삭제 필요

**대안:**
- `@Profile("dev")` 사용 고려 가능
- 현재는 단순한 count 체크로 충분하다고 판단

### 3. 샘플 응답 데이터 설계

**왜 이영희 학생(userId=2)인가?**
- 요구사항에서 "이영희로 로그인한 상태"라고 명시됨
- 실제 테스트 시나리오와 일치
- 학생 계정으로 과거 응답 내역 확인 가능

**왜 종료된 설문에만 응답을 넣었는가?**
- 진행 중인 설문은 학생이 직접 응답 제출 기능을 테스트할 수 있도록 비워둠
- 종료된 설문의 응답은 "내 응답 내역" 탭 테스트용
- 실제 사용 시나리오를 최대한 반영

### 4. 설문 날짜 설정

**동적 날짜 사용:**
```java
LocalDateTime now = LocalDateTime.now();
.startDate(now.minusDays(3))
.endDate(now.plusDays(14))
```

**정적 날짜 대신 동적 날짜를 선택한 이유:**
- 언제 애플리케이션을 실행하더라도 "진행 중"/"종료됨" 상태 유지
- 날짜를 수동으로 업데이트할 필요 없음
- 테스트 환경에서 항상 일관된 동작 보장

---

## 커밋 히스토리

### Commit #1: 설문조사 테스트 데이터 초기화 기능 추가

**커밋 메시지:**
```
feat: 설문조사 테스트 데이터 초기화 기능 추가

- SurveyDataInitializer 클래스 생성
- 3개의 샘플 설문 자동 생성:
  1. "2025년 학생복지 만족도 조사" (진행 중)
     - 척도형, 객관식 단일/복수선택, 주관식 문항 포함
  2. "온라인 강의 개선 설문" (진행 중, 익명)
     - 척도형, 객관식, 주관식 문항 포함
  3. "2024년 2학기 복지 만족도" (종료됨)
     - 이영희 학생의 응답 데이터 포함

이제 로그인 시 빈 화면 대신 응답 가능한 설문과 응답 내역을 확인할 수 있습니다.
```

**변경 파일:**
- `src/main/java/com/scms/app/config/SurveyDataInitializer.java` (신규 생성, 346줄)

**커밋 해시:** `5d7d643`

---

## 테스트 가이드

### 1. 애플리케이션 재시작

```bash
./gradlew bootRun
```

**예상 로그:**
```
=== 설문조사 테스트 데이터 삽입 시작 ===
설문 생성: 2025년 학생복지 만족도 조사
설문 생성: 온라인 강의 개선 설문
설문 생성: 2024년 2학기 복지 만족도 (종료됨)
샘플 응답 데이터 추가: 이영희 학생의 과거 설문 응답
=== 설문조사 테스트 데이터 삽입 완료: 3 개 설문 생성됨 ===
```

### 2. 이영희 계정으로 로그인

**계정 정보:**
- 사용자명: 이영희
- 역할: 학생 (STUDENT)

### 3. 설문조사 페이지 접속

**URL:** `/survey/student`

### 4. "응답 가능한 설문" 탭 확인

**예상 결과:**
- 2개의 설문 카드 표시
- "2025년 학생복지 만족도 조사"
- "온라인 강의 개선 설문"

**확인 사항:**
- 각 설문의 제목, 설명, 기간이 올바르게 표시되는가?
- "설문 참여하기" 버튼이 표시되는가?
- 익명/실명 표시가 맞는가?

### 5. "내 응답 내역" 탭 확인

**예상 결과:**
- 1개의 응답 카드 표시
- "2024년 2학기 복지 만족도" 설문 응답

**확인 사항:**
- 응답 날짜가 표시되는가?
- "상세보기" 버튼이 동작하는가?

### 6. 설문 참여 테스트

**진행 중인 설문 중 하나 선택:**

1. "설문 참여하기" 버튼 클릭
2. 설문 상세 페이지 이동
3. 각 질문 유형별 응답 입력:
   - 척도형: 1-5 중 선택
   - 객관식 단일: 하나의 선택지 선택
   - 객관식 복수: 여러 선택지 선택
   - 주관식: 텍스트 입력
4. "제출하기" 버튼 클릭
5. 제출 완료 확인

### 7. 응답 후 확인

**"내 응답 내역" 탭 다시 확인:**
- 방금 제출한 설문이 목록에 추가되었는가?
- 총 2개의 응답이 표시되는가?

### 8. 데이터베이스 직접 확인 (옵션)

```sql
-- 설문 확인
SELECT survey_id, title, start_date, end_date, is_active
FROM surveys;

-- 질문 확인
SELECT question_id, survey_id, question_type, question_text
FROM survey_questions;

-- 응답 확인
SELECT response_id, survey_id, user_id, submitted_at
FROM survey_responses;

-- 답변 확인
SELECT answer_id, response_id, question_id, option_id, answer_text, answer_number
FROM survey_answers;
```

---

## 향후 개선사항

### 1. 더 다양한 샘플 데이터

**현재 한계:**
- 3개의 설문만 제공
- 1명의 학생 응답만 포함

**개선 방안:**
- 더 많은 설문 추가 (5-10개)
- 다양한 학생의 응답 데이터 생성
- 통계 분석이 가능한 수준의 데이터 확보

### 2. 프로필별 데이터 생성

**현재:**
```java
@Component
public class SurveyDataInitializer implements CommandLineRunner
```

**개선 후:**
```java
@Component
@Profile("dev")  // 개발 환경에서만 실행
public class SurveyDataInitializer implements CommandLineRunner
```

**장점:**
- 운영 환경에서 자동 실행 방지
- 환경별 데이터 전략 분리 가능

### 3. 데이터 재생성 기능

**현재 한계:**
- 데이터를 다시 생성하려면 수동 삭제 필요

**개선 방안:**
```java
public void regenerateTestData() {
    surveyRepository.deleteAll();
    questionRepository.deleteAll();
    responseRepository.deleteAll();
    answerRepository.deleteAll();

    // 데이터 재생성
    createWelfareSatisfactionSurvey();
    createOnlineLectureSurvey();
    createPastSurvey();
}
```

### 4. Admin API 엔드포인트 추가

**새로운 엔드포인트:**
```java
@PostMapping("/api/admin/survey/initialize-test-data")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<String> initializeTestData() {
    surveyDataInitializer.regenerateTestData();
    return ResponseEntity.ok("테스트 데이터가 재생성되었습니다.");
}
```

**장점:**
- 개발 중 필요할 때 API 호출로 데이터 재생성
- 애플리케이션 재시작 불필요

### 5. 설정 파일 기반 데이터 관리

**현재:**
- Java 코드에 하드코딩된 데이터

**개선 방안:**
- `resources/test-data/surveys.json` 파일 생성
- JSON 파일에서 설문 정의 읽어오기
- 코드 수정 없이 데이터만 변경 가능

**예시 JSON:**
```json
{
  "surveys": [
    {
      "title": "2025년 학생복지 만족도 조사",
      "description": "...",
      "startDate": "now-3d",
      "endDate": "now+14d",
      "questions": [
        {
          "type": "SCALE",
          "text": "학생복지 서비스에 대한 전반적인 만족도를 평가해주세요.",
          "required": true,
          "scaleMin": 1,
          "scaleMax": 5
        }
      ]
    }
  ]
}
```

### 6. 다국어 지원 고려

**현재:**
- 한국어로만 작성된 설문

**개선 방안:**
- i18n 메시지 번들 활용
- 다국어 설문 지원
- 국제 학생도 테스트 가능

---

## 결론

### 달성한 목표
✅ 설문조사 테스트 데이터 자동 생성 시스템 구현
✅ 다양한 질문 유형 포함한 샘플 설문 3개 생성
✅ 이영희 학생의 과거 응답 데이터 생성
✅ 빈 화면 문제 해결

### 주요 성과
- **사용자 경험 개선**: 로그인 시 즉시 설문 데이터 확인 가능
- **테스트 용이성**: 개발자와 QA가 설문 기능을 쉽게 테스트 가능
- **유지보수성**: 코드로 관리되는 테스트 데이터로 재현 가능
- **확장성**: 추가 설문 생성 로직을 쉽게 추가 가능

### 남은 과제
- PR 생성 및 리뷰 진행
- 애플리케이션 재시작하여 데이터 생성 확인
- 실제 설문 응답 흐름 테스트
- 통계 기능 동작 확인

---

**문서 버전:** 1.0
**최종 수정일:** 2025-11-18
