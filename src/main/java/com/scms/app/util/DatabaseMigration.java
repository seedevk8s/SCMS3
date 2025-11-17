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
            // 1. notifications 테이블 생성
            createNotificationsTableIfNotExists();

            // 2. program_start_date, program_end_date 컬럼 추가
            addProgramExecutionDateColumns();

            log.info("✅ 데이터베이스 마이그레이션 완료!");
        } catch (Exception e) {
            log.error("데이터베이스 마이그레이션 실패: {}", e.getMessage(), e);
            // 실패해도 애플리케이션은 계속 실행
        }
    }

    /**
     * notifications 테이블 생성 및 스키마 수정
     * 문제 해결을 위해 테이블을 강제로 재생성
     */
    private void createNotificationsTableIfNotExists() {
        try {
            boolean tableExists = checkTableExists("notifications");

            if (!tableExists) {
                log.info("notifications 테이블을 생성합니다...");
                createNotificationsTable();
            } else {
                log.info("notifications 테이블이 이미 존재합니다. 스키마를 확인합니다...");

                // content 컬럼이 NULL 허용인지 확인
                if (isContentColumnNullable()) {
                    log.warn("⚠️  notifications 테이블의 content 컬럼이 NULL 허용으로 되어 있습니다.");
                    log.warn("⚠️  테이블을 삭제하고 올바른 스키마로 재생성합니다...");

                    // 테이블 삭제 및 재생성
                    jdbcTemplate.execute("DROP TABLE IF EXISTS notifications");
                    log.info("✅ 기존 notifications 테이블 삭제 완료");

                    createNotificationsTable();
                } else {
                    log.info("✅ notifications 테이블 스키마가 올바릅니다.");
                }
            }
        } catch (Exception e) {
            log.error("notifications 테이블 생성 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * notifications 테이블 생성
     */
    private void createNotificationsTable() {
        String createTableSql = """
            CREATE TABLE notifications (
                notification_id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT NOT NULL,
                title VARCHAR(200) NOT NULL,
                content TEXT NOT NULL,
                type VARCHAR(50) NOT NULL,
                is_read BOOLEAN NOT NULL DEFAULT FALSE,
                related_url VARCHAR(500),
                created_at DATETIME NOT NULL,
                read_at DATETIME,
                deleted_at DATETIME,
                CONSTRAINT fk_notification_user FOREIGN KEY (user_id)
                    REFERENCES users(user_id) ON DELETE CASCADE,
                INDEX idx_user_id (user_id),
                INDEX idx_is_read (is_read),
                INDEX idx_deleted_at (deleted_at),
                INDEX idx_created_at (created_at)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """;

        jdbcTemplate.execute(createTableSql);
        log.info("✅ notifications 테이블 생성 완료 (content: TEXT NOT NULL)");
    }

    /**
     * content 컬럼이 NULL 허용인지 확인
     */
    private boolean isContentColumnNullable() {
        try {
            String checkNullableSql = """
                SELECT IS_NULLABLE
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = 'scms2'
                AND TABLE_NAME = 'notifications'
                AND COLUMN_NAME = 'content'
                """;

            String isNullable = jdbcTemplate.queryForObject(checkNullableSql, String.class);
            return "YES".equals(isNullable);
        } catch (Exception e) {
            log.warn("content 컬럼 확인 실패: {}", e.getMessage());
            return false;
        }
    }


    /**
     * programs 테이블에 실행 날짜 컬럼 추가
     */
    private void addProgramExecutionDateColumns() {
        try {
            boolean hasStartDate = checkColumnExists("programs", "program_start_date");
            boolean hasEndDate = checkColumnExists("programs", "program_end_date");

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
            } else {
                log.info("프로그램 실행 날짜 컬럼이 이미 존재합니다.");
            }
        } catch (Exception e) {
            log.error("프로그램 실행 날짜 컬럼 추가 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 테이블 존재 여부 확인
     */
    private boolean checkTableExists(String tableName) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.TABLES " +
                "WHERE TABLE_SCHEMA = 'scms2' " +
                "AND TABLE_NAME = ?",
                Integer.class,
                tableName
            );
            return count != null && count > 0;
        } catch (Exception e) {
            log.warn("테이블 존재 확인 실패 ({}): {}", tableName, e.getMessage());
            return false;
        }
    }

    /**
     * 컬럼 존재 여부 확인
     */
    private boolean checkColumnExists(String tableName, String columnName) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.COLUMNS " +
                "WHERE TABLE_SCHEMA = 'scms2' " +
                "AND TABLE_NAME = ? " +
                "AND COLUMN_NAME = ?",
                Integer.class,
                tableName,
                columnName
            );
            return count != null && count > 0;
        } catch (Exception e) {
            log.warn("컬럼 존재 확인 실패 ({}.{}): {}", tableName, columnName, e.getMessage());
            return false;
        }
    }
}
