-- Flyway 실패 기록 삭제 스크립트
-- MySQL 워크벤치나 터미널에서 실행

USE scms2;

-- 1. 현재 상태 확인
SELECT * FROM flyway_schema_history ORDER BY installed_rank;

-- 2. 실패한 V2 레코드 삭제
DELETE FROM flyway_schema_history WHERE version = '2' AND success = 0;

-- 3. 확인
SELECT * FROM flyway_schema_history ORDER BY installed_rank;

-- 완료! 이제 application.yml에서 flyway.enabled = true로 변경 후 재시작
