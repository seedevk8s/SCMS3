-- 상담사 계정 추가 SQL
-- 학번: 8000001 / 비밀번호: counselor123

-- 기존 상담사 계정 확인
SELECT user_id, student_num, name, email, role FROM users WHERE role = 'COUNSELOR';

-- 상담사 계정이 없다면 아래 SQL 실행
-- 비밀번호: counselor123

-- 상담사 1
INSERT INTO users (student_num, name, email, phone, password, birth_date, department, role, grade, fail_count, locked, created_at)
VALUES
(8000001, '김상담', 'counselor1@pureum.ac.kr', '010-1111-2222',
 '$2b$10$7rV2FNOK4ANmgfrowkM8rOGg1.5t0y/N7dR88s5aj1gFYhTz6kRJa',
 '1985-03-15', '학생상담센터', 'COUNSELOR', NULL, 0, 0, NOW())
ON CONFLICT (student_num) DO UPDATE SET
password = '$2b$10$7rV2FNOK4ANmgfrowkM8rOGg1.5t0y/N7dR88s5aj1gFYhTz6kRJa';

-- 상담사 2
INSERT INTO users (student_num, name, email, phone, password, birth_date, department, role, grade, fail_count, locked, created_at)
VALUES
(8000002, '이상담', 'counselor2@pureum.ac.kr', '010-3333-4444',
 '$2b$10$7rV2FNOK4ANmgfrowkM8rOGg1.5t0y/N7dR88s5aj1gFYhTz6kRJa',
 '1988-07-20', '학생상담센터', 'COUNSELOR', NULL, 0, 0, NOW())
ON CONFLICT (student_num) DO UPDATE SET
password = '$2b$10$7rV2FNOK4ANmgfrowkM8rOGg1.5t0y/N7dR88s5aj1gFYhTz6kRJa';

-- Counselor 프로필 추가
INSERT INTO counselors (user_id, specialization, license, available)
SELECT user_id, '진로상담, 학업상담', '전문상담사 2급', 1
FROM users WHERE student_num = 8000001
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO counselors (user_id, specialization, license, available)
SELECT user_id, '심리상담, 대인관계', '임상심리사 2급', 1
FROM users WHERE student_num = 8000002
ON CONFLICT (user_id) DO NOTHING;

-- 확인
SELECT u.user_id, u.student_num, u.name, u.email, u.role, c.specialization, c.license
FROM users u
LEFT JOIN counselors c ON u.user_id = c.user_id
WHERE u.role = 'COUNSELOR';
