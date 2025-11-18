-- 상담사 계정 즉시 추가 (애플리케이션 재시작 없이)
-- 비밀번호: counselor123

-- 기존 상담사 계정 확인
SELECT user_id, student_num, name, email, role FROM users WHERE role = 'COUNSELOR';

-- 상담사 1 추가
INSERT INTO users (student_num, name, email, phone, password, birth_date, department, role, grade, fail_cnt, locked, created_at, updated_at)
VALUES
(8000001, '김상담', 'counselor1@pureum.ac.kr', '010-1111-2222',
 '$2a$10$7rV2FNOK4ANmgfrowkM8rOGg1.5t0y/N7dR88s5aj1gFYhTz6kRJa',
 '1985-03-15', '학생상담센터', 'COUNSELOR', NULL, 0, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE
password = '$2a$10$7rV2FNOK4ANmgfrowkM8rOGg1.5t0y/N7dR88s5aj1gFYhTz6kRJa';

-- 상담사 2 추가
INSERT INTO users (student_num, name, email, phone, password, birth_date, department, role, grade, fail_cnt, locked, created_at, updated_at)
VALUES
(8000002, '이상담', 'counselor2@pureum.ac.kr', '010-3333-4444',
 '$2a$10$7rV2FNOK4ANmgfrowkM8rOGg1.5t0y/N7dR88s5aj1gFYhTz6kRJa',
 '1988-07-20', '학생상담센터', 'COUNSELOR', NULL, 0, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE
password = '$2a$10$7rV2FNOK4ANmgfrowkM8rOGg1.5t0y/N7dR88s5aj1gFYhTz6kRJa';

-- Counselor 프로필 추가
INSERT INTO counselors (counselor_id, user_id, special, intro, created_at, updated_at)
SELECT user_id, user_id, '진로상담, 학업상담', '전문상담사 2급 자격을 보유하고 있으며, 학생들의 진로와 학업 고민을 함께 해결합니다.', NOW(), NOW()
FROM users WHERE student_num = 8000001
ON DUPLICATE KEY UPDATE
special = '진로상담, 학업상담';

INSERT INTO counselors (counselor_id, user_id, special, intro, created_at, updated_at)
SELECT user_id, user_id, '심리상담, 대인관계', '임상심리사 2급 자격을 보유하고 있으며, 심리 및 대인관계 상담을 전문으로 합니다.', NOW(), NOW()
FROM users WHERE student_num = 8000002
ON DUPLICATE KEY UPDATE
special = '심리상담, 대인관계';

-- 확인
SELECT u.user_id, u.student_num, u.name, u.email, u.role, c.special
FROM users u
LEFT JOIN counselors c ON u.user_id = c.counselor_id
WHERE u.role = 'COUNSELOR';
