-- 상담사 계정 8000002 (이상담) 추가 스크립트
-- BCrypt 해시: counselor123

-- 1. 기존 계정 확인 및 삭제 (있을 경우)
DELETE FROM counselors WHERE counselor_id IN (SELECT user_id FROM users WHERE student_num = 8000002);
DELETE FROM users WHERE student_num = 8000002;

-- 2. User 계정 생성
INSERT INTO users (student_num, name, email, phone, password, birth_date, department, grade, role, locked, fail_cnt, created_at, updated_at)
VALUES (
    8000002,
    '이상담',
    'counselor2@pureum.ac.kr',
    '010-3333-4444',
    '$2a$10$VQy5sY8xJXqTl5k9Zx5zJeK3F9H5F5F5F5F5F5F5F5F5F5F5F5F5F',  -- counselor123
    '1988-07-20',
    '학생상담센터',
    NULL,
    'COUNSELOR',
    FALSE,
    0,
    NOW(),
    NOW()
);

-- 3. Counselor 프로필 생성
INSERT INTO counselors (counselor_id, special, intro, created_at, updated_at)
VALUES (
    (SELECT user_id FROM users WHERE student_num = 8000002),
    '심리상담, 대인관계',
    '임상심리사 2급 자격을 보유하고 있으며, 심리 및 대인관계 상담을 전문으로 합니다.',
    NOW(),
    NOW()
);

-- 4. 생성 확인
SELECT u.user_id, u.student_num, u.name, u.role, c.special, c.intro
FROM users u
LEFT JOIN counselors c ON u.user_id = c.counselor_id
WHERE u.student_num = 8000002;
