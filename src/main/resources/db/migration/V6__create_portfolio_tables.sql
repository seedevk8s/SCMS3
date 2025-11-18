-- ============================================
-- V6: Portfolio System Tables
-- 포트폴리오, 포트폴리오 항목, 파일, 공유, 조회 통계 테이블 생성
-- ============================================

-- 포트폴리오 메인 테이블
CREATE TABLE IF NOT EXISTS portfolios (
    portfolio_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '포트폴리오 ID',
    user_id INT NOT NULL COMMENT '사용자 ID',
    title VARCHAR(200) NOT NULL COMMENT '포트폴리오 제목',
    description TEXT COMMENT '포트폴리오 설명',
    visibility VARCHAR(20) NOT NULL DEFAULT 'PRIVATE' COMMENT '공개 범위 (PRIVATE, PUBLIC)',
    template_type VARCHAR(50) COMMENT '템플릿 유형',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    deleted_at DATETIME COMMENT '삭제일시 (Soft Delete)',
    INDEX idx_user_id (user_id),
    INDEX idx_visibility (visibility),
    INDEX idx_deleted_at (deleted_at),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='포트폴리오';

-- 포트폴리오 항목 테이블
CREATE TABLE IF NOT EXISTS portfolio_items (
    item_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '항목 ID',
    portfolio_id BIGINT NOT NULL COMMENT '포트폴리오 ID',
    item_type VARCHAR(50) NOT NULL COMMENT '항목 유형 (PROJECT, ACHIEVEMENT, CERTIFICATION, ACTIVITY, COURSE)',
    title VARCHAR(200) NOT NULL COMMENT '항목 제목',
    description TEXT COMMENT '항목 설명',
    organization VARCHAR(200) COMMENT '기관/조직',
    start_date DATE COMMENT '시작일',
    end_date DATE COMMENT '종료일',
    display_order INT NOT NULL DEFAULT 0 COMMENT '표시 순서',
    program_application_id BIGINT COMMENT '연결된 프로그램 신청 ID (자동 가져오기용)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    deleted_at DATETIME COMMENT '삭제일시 (Soft Delete)',
    INDEX idx_portfolio_id (portfolio_id),
    INDEX idx_item_type (item_type),
    INDEX idx_display_order (display_order),
    INDEX idx_deleted_at (deleted_at),
    INDEX idx_program_application_id (program_application_id),
    FOREIGN KEY (portfolio_id) REFERENCES portfolios(portfolio_id) ON DELETE CASCADE,
    FOREIGN KEY (program_application_id) REFERENCES program_applications(application_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='포트폴리오 항목';

-- 포트폴리오 파일 테이블
CREATE TABLE IF NOT EXISTS portfolio_files (
    file_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '파일 ID',
    portfolio_item_id BIGINT NOT NULL COMMENT '포트폴리오 항목 ID',
    original_file_name VARCHAR(255) NOT NULL COMMENT '원본 파일명',
    stored_file_name VARCHAR(255) NOT NULL COMMENT '저장된 파일명 (UUID)',
    file_path VARCHAR(500) NOT NULL COMMENT '파일 경로',
    file_size BIGINT NOT NULL COMMENT '파일 크기 (bytes)',
    file_type VARCHAR(100) COMMENT 'MIME 타입',
    uploaded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '업로드일시',
    deleted_at DATETIME COMMENT '삭제일시 (Soft Delete)',
    INDEX idx_portfolio_item_id (portfolio_item_id),
    INDEX idx_deleted_at (deleted_at),
    FOREIGN KEY (portfolio_item_id) REFERENCES portfolio_items(item_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='포트폴리오 파일';

-- 포트폴리오 공유 테이블
CREATE TABLE IF NOT EXISTS portfolio_shares (
    share_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '공유 ID',
    portfolio_id BIGINT NOT NULL COMMENT '포트폴리오 ID',
    share_token VARCHAR(100) NOT NULL UNIQUE COMMENT '공유 토큰 (UUID)',
    expires_at DATETIME COMMENT '만료일시',
    view_count INT NOT NULL DEFAULT 0 COMMENT '조회 횟수',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    revoked_at DATETIME COMMENT '취소일시',
    INDEX idx_portfolio_id (portfolio_id),
    INDEX idx_share_token (share_token),
    INDEX idx_expires_at (expires_at),
    FOREIGN KEY (portfolio_id) REFERENCES portfolios(portfolio_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='포트폴리오 공유';

-- 포트폴리오 조회 통계 테이블
CREATE TABLE IF NOT EXISTS portfolio_views (
    view_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '조회 ID',
    portfolio_id BIGINT NOT NULL COMMENT '포트폴리오 ID',
    viewer_user_id INT COMMENT '조회자 사용자 ID (로그인한 경우)',
    ip_address VARCHAR(50) COMMENT 'IP 주소',
    user_agent VARCHAR(255) COMMENT 'User Agent',
    share_token VARCHAR(100) COMMENT '공유 토큰 (공유 링크로 접근한 경우)',
    viewed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '조회일시',
    INDEX idx_portfolio_id (portfolio_id),
    INDEX idx_viewer_user_id (viewer_user_id),
    INDEX idx_viewed_at (viewed_at),
    INDEX idx_share_token (share_token),
    FOREIGN KEY (portfolio_id) REFERENCES portfolios(portfolio_id) ON DELETE CASCADE,
    FOREIGN KEY (viewer_user_id) REFERENCES users(user_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='포트폴리오 조회 통계';
