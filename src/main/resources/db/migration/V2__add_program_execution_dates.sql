-- 프로그램 실행 날짜 필드 추가
-- programStartDate: 프로그램 시작일
-- programEndDate: 프로그램 종료일

ALTER TABLE programs
ADD COLUMN program_start_date DATETIME NOT NULL DEFAULT '2025-01-01 00:00:00' AFTER application_end_date,
ADD COLUMN program_end_date DATETIME NOT NULL DEFAULT '2025-01-15 00:00:00' AFTER program_start_date;

-- 기존 데이터의 프로그램 실행 날짜를 신청 마감일 기준으로 설정
-- program_start_date = application_end_date + 1일
-- program_end_date = program_start_date + 14일 (2주)
UPDATE programs
SET
    program_start_date = DATE_ADD(application_end_date, INTERVAL 1 DAY),
    program_end_date = DATE_ADD(DATE_ADD(application_end_date, INTERVAL 1 DAY), INTERVAL 14 DAY)
WHERE program_start_date = '2025-01-01 00:00:00';

-- DEFAULT 값 제거 (이후 INSERT는 명시적으로 값을 지정해야 함)
ALTER TABLE programs
ALTER COLUMN program_start_date DROP DEFAULT,
ALTER COLUMN program_end_date DROP DEFAULT;
