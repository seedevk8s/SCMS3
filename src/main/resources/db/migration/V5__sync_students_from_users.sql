-- V5: User 테이블에서 Student 테이블로 데이터 동기화
-- 역량 평가 시스템을 위해 Student 테이블에 데이터 필요

-- 기존 User(STUDENT 역할) 데이터를 Student 테이블에 삽입
INSERT INTO students (student_id, name, email, phone, department, grade, status, created_at, updated_at)
SELECT
    CAST(u.student_num AS VARCHAR) AS student_id,
    u.name,
    u.email,
    u.phone,
    u.department,
    CAST(u.grade AS VARCHAR) AS grade,
    '재학' AS status,
    u.created_at,
    u.updated_at
FROM users u
WHERE u.role = 'STUDENT'
  AND NOT EXISTS (
    SELECT 1 FROM students s
    WHERE s.student_id = CAST(u.student_num AS VARCHAR)
  );
