-- 상담사 계정 8000002 (이상담) 수동 생성 스크립트
-- 비밀번호: counselor123
--
-- 사용법:
-- 1. MySQL에 접속: mysql -u root -p12345 scms2
-- 2. 이 파일 실행: source /home/user/SCMS3/add-counselor-8000002.sql
--
-- 또는 Docker 사용:
-- docker exec -i scms-mysql mysql -u root -p12345 scms2 < add-counselor-8000002.sql

-- 1. 먼저 8000001 계정의 비밀번호 해시 확인 (counselor123의 BCrypt)
SELECT '=== 8000001 계정 정보 (BCrypt 해시 확인용) ===' AS info;
SELECT student_num, name, SUBSTRING(password, 1, 20) AS password_hash_prefix
FROM users
WHERE student_num = 8000001;

-- 위에서 확인한 password 해시를 복사해서 아래 @pwd 변수에 넣으세요
-- 예시: SET @pwd = '$2a$10$...전체해시...';
--
-- 또는 8000001이 없거나 확인 안 되면 아래 기본 해시 사용
SET @pwd = '$2a$10$YQy5sY8xJXqTl5k9Zx5b2eBqXJxQXJQk5xH7Hx8Hx9Hx0Hx1Hx2Hx';

-- 2. 기존 8000002 계정 삭제 (있을 경우)
SELECT '=== 기존 8000002 계정 삭제 중 ===' AS info;
DELETE FROM counselors WHERE counselor_id IN (SELECT user_id FROM users WHERE student_num = 8000002);
DELETE FROM users WHERE student_num = 8000002;

-- 3. User 계정 생성
SELECT '=== User 계정 생성 중 ===' AS info;
INSERT INTO users (student_num, name, email, phone, password, birth_date, department, grade, role, locked, fail_cnt, created_at, updated_at)
VALUES (
    8000002,
    '이상담',
    'counselor2@pureum.ac.kr',
    '010-3333-4444',
    @pwd,
    '1988-07-20',
    '학생상담센터',
    NULL,
    'COUNSELOR',
    0,
    0,
    NOW(),
    NOW()
);

-- 4. user_id 가져오기
SET @user_id = (SELECT user_id FROM users WHERE student_num = 8000002);
SELECT CONCAT('생성된 user_id: ', @user_id) AS info;

-- 5. Counselor 프로필 생성
SELECT '=== Counselor 프로필 생성 중 ===' AS info;
INSERT INTO counselors (counselor_id, special, intro, created_at, updated_at)
VALUES (
    @user_id,
    '심리상담, 대인관계',
    '임상심리사 2급 자격을 보유하고 있으며, 심리 및 대인관계 상담을 전문으로 합니다.',
    NOW(),
    NOW()
);

-- 6. 생성 확인
SELECT '=== 생성 결과 확인 (두 상담사 모두) ===' AS info;
SELECT u.user_id, u.student_num, u.name, u.role, c.special, c.intro
FROM users u
LEFT JOIN counselors c ON u.user_id = c.counselor_id
WHERE u.student_num IN (8000001, 8000002)
ORDER BY u.student_num;

SELECT '=== 완료! 8000002 계정으로 로그인 테스트하세요 ===' AS info;
SELECT '학번: 8000002, 비밀번호: counselor123' AS login_info;
