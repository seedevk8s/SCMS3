-- notifications 테이블 완전 재생성 스크립트
USE scms2;

-- 1. 기존 테이블 삭제 (외래키 때문에 안전하게 삭제)
DROP TABLE IF EXISTS notifications;

-- 2. 올바른 스키마로 테이블 재생성
CREATE TABLE notifications (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,  -- NOT NULL 제약조건
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. 테이블 확인
DESCRIBE notifications;

-- 4. content 컬럼 제약조건 확인
SELECT COLUMN_NAME, IS_NULLABLE, COLUMN_TYPE, COLUMN_DEFAULT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'scms2'
AND TABLE_NAME = 'notifications'
AND COLUMN_NAME = 'content';
