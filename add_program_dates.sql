-- 프로그램 실행 날짜 컬럼 추가
USE scms2;

-- 컬럼 추가
ALTER TABLE programs
ADD COLUMN program_start_date DATETIME NULL AFTER application_end_date,
ADD COLUMN program_end_date DATETIME NULL AFTER program_start_date;

-- 기존 데이터 업데이트
UPDATE programs
SET
    program_start_date = DATE_ADD(application_end_date, INTERVAL 1 DAY),
    program_end_date = DATE_ADD(DATE_ADD(application_end_date, INTERVAL 1 DAY), INTERVAL 14 DAY);

-- NOT NULL 제약 조건 추가
ALTER TABLE programs
MODIFY COLUMN program_start_date DATETIME NOT NULL,
MODIFY COLUMN program_end_date DATETIME NOT NULL;

-- 확인
SELECT COUNT(*) as total_programs,
       COUNT(program_start_date) as programs_with_start_date,
       COUNT(program_end_date) as programs_with_end_date
FROM programs;
