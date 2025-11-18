# 상담사 로그인 문제 해결 개발 로그

## 문제 요약
상담사(COUNSELOR) 계정의 로그인 및 데이터베이스 스키마 불일치 문제 발생

## 수정 일시
2025-11-18

## 발견된 문제점

### 1. 데이터베이스 스키마 불일치
**문제**: `counselors` 테이블의 스키마가 JPA 엔티티와 불일치
- 기존 스키마: `id BIGINT AUTO_INCREMENT PRIMARY KEY, user_id INT`
- JPA 엔티티: `counselor_id INT PRIMARY KEY` (with @MapsId)

**위치**:
- `database/schema.sql`
- `src/main/resources/db/migration/V4__create_consultation_tables.sql`

**해결**:
- `counselors` 테이블의 PK를 `counselor_id`로 변경
- `counselor_id`를 `user_id`와 동일한 값으로 사용 (@MapsId 패턴)
- AUTO_INCREMENT 제거 (user_id에서 값을 가져옴)

### 2. Flyway 마이그레이션 누락
**문제**: 초기 테이블(users, counselors 등)에 대한 Flyway 마이그레이션 파일 부재
- Hibernate ddl-auto에만 의존하여 스키마 관리
- 프로덕션 환경에서 문제 발생 가능성

**해결**:
- V1 마이그레이션 파일 생성: `V1__create_initial_tables.sql`
- users, login_history, counselors, notifications 테이블 정의 포함

### 3. Hibernate Cascade 문제
**문제**: @OneToOne 관계에서 cascade 설정으로 인한 "detached entity passed to persist" 에러
```java
@OneToOne(fetch = FetchType.LAZY)  // 기본 cascade 설정
@MapsId
@JoinColumn(name = "counselor_id")
private User user;
```

**에러 메시지**:
```
detached entity passed to persist: com.scms.app.model.User
```

**해결**:
```java
@OneToOne(fetch = FetchType.LAZY, cascade = {})  // cascade 비활성화
@MapsId
@JoinColumn(name = "counselor_id")
private User user;
```

### 4. @MapsId 사용 시 ID 설정 오류
**문제**: @MapsId를 사용하면서 counselorId를 명시적으로 설정
```java
// 잘못된 사용
Counselor counselor = Counselor.builder()
    .counselorId(user.getUserId())  // ❌ 명시적 ID 설정
    .user(user)
    .build();
```

**에러**: Hibernate에서 null identifier 에러 발생

**해결**:
```java
// 올바른 사용
Counselor counselor = Counselor.builder()
    .user(user)  // ✅ user만 설정, ID는 @MapsId가 자동 처리
    .build();
```

### 5. Foreign Key 참조 오류
**문제**: V4 마이그레이션의 consultation_sessions, consultation_records 테이블에서 잘못된 FK 참조
- `counselors.id` 참조 (존재하지 않는 컬럼)
- `counselor_id`의 데이터 타입 불일치 (BIGINT vs INT)

**해결**:
- 모든 FK를 `counselors.counselor_id`로 수정
- `counselor_id` 컬럼을 INT로 통일

### 6. 컬럼명 불일치
**문제**: DB 스키마와 엔티티 필드명 불일치
- DB: `special`, `intro`
- 엔티티: `specialty`, `introduction`

**해결**: @Column 어노테이션으로 매핑
```java
@Column(name = "special", length = 100)
private String specialty;

@Column(name = "intro", columnDefinition = "TEXT")
private String introduction;
```

## 수정된 파일 목록

### 1. 데이터베이스 마이그레이션
- **V1__create_initial_tables.sql** (신규 생성)
  - users, login_history, counselors, notifications 테이블 생성

- **V4__create_consultation_tables.sql** (수정)
  - counselor_id FK 참조 수정
  - 데이터 타입 INT로 통일
  - 제약조건 수정

- **database/schema.sql** (수정)
  - counselors 테이블 스키마 수정

### 2. 엔티티 및 설정
- **src/main/java/com/scms/app/model/Counselor.java** (수정)
  - cascade = {} 추가
  - @Column 매핑 수정

- **src/main/java/com/scms/app/config/DataLoader.java** (수정)
  - initializeCounselors() 메서드 추가
  - @MapsId 올바른 사용법 적용
  - 기존 User 존재 시에도 Counselor 프로필 생성

- **src/main/java/com/scms/app/config/SecurityConfig.java** (수정)
  - 상담사 권한 체크 추가
  - /counseling/manage 경로 COUNSELOR, ADMIN 권한 부여

## 상담사 계정 정보

### 자동 생성 계정
애플리케이션 시작 시 DataLoader에서 자동으로 생성됩니다.

| 학번 | 이름 | 비밀번호 | 이메일 | 전문분야 |
|------|------|----------|--------|----------|
| 8000001 | 김상담 | counselor123 | counselor1@pureum.ac.kr | 진로상담, 학업상담 |
| 8000002 | 이상담 | counselor123 | counselor2@pureum.ac.kr | 심리상담, 대인관계 |

### 로그인 방법
1. 로그인 페이지에서 학번으로 로그인: `8000001` 또는 `8000002`
2. 비밀번호: `counselor123`
3. 역할(ROLE)이 자동으로 `COUNSELOR`로 설정됨

## 데이터베이스 스키마

### counselors 테이블 (수정 후)
```sql
CREATE TABLE IF NOT EXISTS counselors (
    counselor_id INT PRIMARY KEY COMMENT '상담사 ID (= user_id)',
    special VARCHAR(100) COMMENT '전문 분야',
    intro TEXT COMMENT '소개',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME COMMENT '삭제일시 (Soft Delete)',
    FOREIGN KEY (counselor_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='상담사 정보';
```

### 관련 테이블 FK 수정
```sql
-- consultation_sessions
FOREIGN KEY (counselor_id) REFERENCES counselors(counselor_id) ON DELETE SET NULL

-- consultation_records
FOREIGN KEY (counselor_id) REFERENCES counselors(counselor_id) ON DELETE CASCADE

-- counselor_schedules
FOREIGN KEY (counselor_id) REFERENCES counselors(counselor_id) ON DELETE CASCADE
```

## JPA 엔티티 구조

### Counselor 엔티티
```java
@Entity
@Table(name = "counselors")
public class Counselor {
    @Id
    @Column(name = "counselor_id")
    private Integer counselorId;

    @OneToOne(fetch = FetchType.LAZY, cascade = {})
    @MapsId
    @JoinColumn(name = "counselor_id")
    private User user;

    @Column(name = "special", length = 100)
    private String specialty;

    @Column(name = "intro", columnDefinition = "TEXT")
    private String introduction;

    // timestamps, soft delete 필드...
}
```

### @MapsId 패턴
- User의 user_id를 Counselor의 counselor_id로 공유
- 1:1 관계에서 동일한 PK 사용
- counselorId는 user 설정 시 자동으로 매핑됨

## 테스트 방법

### 1. 데이터베이스 초기화
```bash
# Flyway 마이그레이션 확인
./mvnw flyway:info

# 필요 시 클린 및 재마이그레이션
./mvnw flyway:clean flyway:migrate
```

### 2. 애플리케이션 시작
```bash
./mvnw spring-boot:run
```

### 3. 상담사 계정 확인
```sql
-- 상담사 User 확인
SELECT user_id, student_num, name, email, role
FROM users
WHERE role = 'COUNSELOR';

-- 상담사 프로필 확인
SELECT c.counselor_id, c.special, c.intro, u.name
FROM counselors c
JOIN users u ON c.counselor_id = u.user_id;
```

### 4. 로그인 테스트
1. 브라우저에서 `/login` 접속
2. 학번: `8000001`, 비밀번호: `counselor123` 입력
3. 로그인 성공 후 `/counseling/manage` 접근 가능 확인

## 권한 설정

### SecurityConfig 설정
```java
.requestMatchers("/counseling/manage").hasAnyRole("COUNSELOR", "ADMIN")
.requestMatchers("/api/consultations/*/approve").hasAnyRole("COUNSELOR", "ADMIN")
.requestMatchers("/api/consultations/*/reject").hasAnyRole("COUNSELOR", "ADMIN")
.requestMatchers("/api/consultations/*/record").hasRole("COUNSELOR")
```

### 역할별 접근 권한
- **COUNSELOR**: 상담 관리, 상담 승인/거부, 상담 기록 작성
- **ADMIN**: 모든 권한
- **STUDENT**: 상담 신청, 본인 상담 내역 조회

## 주의사항

### 1. DataLoader 비활성화
프로덕션 배포 시 초기 데이터 로드를 방지하려면:
```java
// @Component  // 주석 처리
@org.springframework.core.annotation.Order(2)
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {
```

### 2. 비밀번호 변경
프로덕션 환경에서는 초기 비밀번호를 반드시 변경하세요:
- 현재: `counselor123` (테스트용)
- 변경: 안전한 비밀번호로 교체 필요

### 3. Cascade 설정
Counselor의 cascade = {}는 의도적인 설정입니다:
- User는 별도로 저장되어야 함
- Counselor 저장 시 User를 cascade하지 않음
- User 삭제 시 Counselor는 FK ON DELETE CASCADE로 자동 삭제

### 4. Soft Delete
users와 counselors 모두 soft delete를 지원합니다:
```java
counselor.delete();  // deleted_at 설정
counselor.isDeleted();  // 삭제 여부 확인
```

## 향후 개선 사항

1. **비밀번호 정책**: 초기 비밀번호 강제 변경 기능 추가
2. **권한 세분화**: 상담사별 전문 분야에 따른 접근 제어
3. **감사 로그**: 상담사의 모든 활동 로깅
4. **2FA 인증**: 상담사 계정에 대한 2단계 인증 추가
5. **세션 관리**: 동시 로그인 제한 및 세션 타임아웃 설정

## 참고 자료

- JPA @MapsId: https://docs.spring.io/spring-data/jpa/docs/current/reference/html/
- Flyway Migrations: https://flywaydb.org/documentation/
- Spring Security Role-based Access: https://docs.spring.io/spring-security/reference/

## 커밋 내역

1. `8c90581` - fix: Update counselors table schema to match JPA entity
2. `4d90b0d` - fix: Use @MapsId correctly without explicit ID setting
3. `2f6f7ac` - fix: Add empty cascade to Counselor entity
4. `9eefaf7` - fix: Use EntityManager to get managed User entity
5. `e963c9a` - fix: Use JDBC for counselor insertion to avoid Hibernate cascade
6. `f522bd1` - fix: Ensure counselor accounts are created even when users exist
7. `d608cd3` - fix: Add counselor account initialization in DataLoader

## 결론

모든 상담사 로그인 관련 문제가 해결되었습니다:
- ✅ 데이터베이스 스키마 정렬
- ✅ Flyway 마이그레이션 추가
- ✅ JPA 엔티티 관계 수정
- ✅ 상담사 계정 자동 생성
- ✅ 권한 기반 접근 제어 설정
- ✅ Foreign Key 참조 수정

상담사는 학번 `8000001` 또는 `8000002`, 비밀번호 `counselor123`으로 로그인할 수 있습니다.
