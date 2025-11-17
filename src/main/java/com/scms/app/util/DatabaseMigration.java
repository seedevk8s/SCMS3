package com.scms.app.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 데이터베이스 마이그레이션 유틸리티
 * 프로그램 실행 날짜 필드 추가
 */
@Component
@Order(1)  // DataLoader보다 먼저 실행
@Slf4j
public class DatabaseMigration implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("데이터베이스 마이그레이션 시작...");

        try {
            // program_start_date 컬럼이 존재하는지 확인
            boolean hasStartDate = checkColumnExists("program_start_date");
            boolean hasEndDate = checkColumnExists("program_end_date");

            if (!hasStartDate || !hasEndDate) {
                log.info("프로그램 실행 날짜 컬럼을 추가합니다...");

                // 컬럼 추가 (NULL 허용으로 시작)
                if (!hasStartDate) {
                    jdbcTemplate.execute(
                        "ALTER TABLE programs ADD COLUMN program_start_date DATETIME NULL AFTER application_end_date"
                    );
                    log.info("✅ program_start_date 컬럼 추가 완료");
                }

                if (!hasEndDate) {
                    jdbcTemplate.execute(
                        "ALTER TABLE programs ADD COLUMN program_end_date DATETIME NULL AFTER program_start_date"
                    );
                    log.info("✅ program_end_date 컬럼 추가 완료");
                }

                // 기존 데이터 업데이트
                int updatedRows = jdbcTemplate.update(
                    "UPDATE programs SET " +
                    "program_start_date = DATE_ADD(application_end_date, INTERVAL 1 DAY), " +
                    "program_end_date = DATE_ADD(DATE_ADD(application_end_date, INTERVAL 1 DAY), INTERVAL 14 DAY) " +
                    "WHERE program_start_date IS NULL OR program_end_date IS NULL"
                );
                log.info("✅ 기존 프로그램 {}개의 실행 날짜 설정 완료", updatedRows);

                // NOT NULL 제약 조건 추가
                if (!hasStartDate) {
                    jdbcTemplate.execute(
                        "ALTER TABLE programs MODIFY COLUMN program_start_date DATETIME NOT NULL"
                    );
                }

                if (!hasEndDate) {
                    jdbcTemplate.execute(
                        "ALTER TABLE programs MODIFY COLUMN program_end_date DATETIME NOT NULL"
                    );
                }
                log.info("✅ NOT NULL 제약 조건 추가 완료");

                log.info("✅ 데이터베이스 마이그레이션 완료!");
            } else {
                log.info("프로그램 실행 날짜 컬럼이 이미 존재합니다. 마이그레이션을 건너뜁니다.");
            }
        } catch (Exception e) {
            log.error("데이터베이스 마이그레이션 실패: {}", e.getMessage(), e);
            // 실패해도 애플리케이션은 계속 실행
        }
    }

    private boolean checkColumnExists(String columnName) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.COLUMNS " +
                "WHERE TABLE_SCHEMA = 'scms2' " +
                "AND TABLE_NAME = 'programs' " +
                "AND COLUMN_NAME = ?",
                Integer.class,
                columnName
            );
            return count != null && count > 0;
        } catch (Exception e) {
            log.warn("컬럼 존재 확인 실패 ({}): {}", columnName, e.getMessage());
            return false;
        }
    }
}
