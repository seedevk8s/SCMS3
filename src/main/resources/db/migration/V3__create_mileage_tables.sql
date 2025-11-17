-- CHAMP 마일리지 시스템 테이블 생성
-- V3: 마일리지 규칙 및 적립 내역 관리

-- ============================================
-- 마일리지 규칙 테이블
-- ============================================
CREATE TABLE IF NOT EXISTS mileage_rules (
    rule_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '규칙 ID',
    activity_type VARCHAR(50) NOT NULL COMMENT '활동 타입 (PROGRAM, COUNSELING, SURVEY, MANUAL)',
    activity_name VARCHAR(100) NOT NULL COMMENT '활동명',
    points INT NOT NULL COMMENT '지급 포인트',
    description VARCHAR(255) COMMENT '규칙 설명',
    is_active TINYINT(1) NOT NULL DEFAULT 1 COMMENT '활성화 여부',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    deleted_at DATETIME COMMENT '삭제일시 (Soft Delete)',
    INDEX idx_activity_type (activity_type),
    INDEX idx_is_active (is_active),
    INDEX idx_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='마일리지 규칙';

-- ============================================
-- 마일리지 적립 내역 테이블
-- ============================================
CREATE TABLE IF NOT EXISTS mileage_history (
    history_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '내역 ID',
    user_id INT NOT NULL COMMENT '사용자 ID',
    activity_type VARCHAR(50) NOT NULL COMMENT '활동 타입 (PROGRAM, COUNSELING, SURVEY, MANUAL)',
    activity_id BIGINT COMMENT '활동 ID (관련 테이블의 PK)',
    activity_name VARCHAR(255) NOT NULL COMMENT '활동명',
    points INT NOT NULL COMMENT '지급/차감 포인트 (양수: 지급, 음수: 차감)',
    description VARCHAR(500) COMMENT '상세 설명',
    awarded_by INT COMMENT '지급자 ID (수동 지급인 경우)',
    earned_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '적립일시',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    INDEX idx_user_id (user_id),
    INDEX idx_activity_type (activity_type),
    INDEX idx_earned_at (earned_at),
    INDEX idx_user_earned (user_id, earned_at),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (awarded_by) REFERENCES users(user_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='마일리지 적립 내역';

-- ============================================
-- 초기 마일리지 규칙 데이터
-- ============================================

-- 프로그램 참여 관련 마일리지
INSERT INTO mileage_rules (activity_type, activity_name, points, description, is_active) VALUES
('PROGRAM', '프로그램 참여 완료', 100, '프로그램 참여를 완료한 경우', 1),
('PROGRAM', '장기 프로그램 참여 완료', 200, '4주 이상 장기 프로그램 참여 완료', 1),
('PROGRAM', '우수 참여자', 150, '프로그램 우수 참여자로 선정된 경우', 1);

-- 상담 관련 마일리지 (추후 구현)
INSERT INTO mileage_rules (activity_type, activity_name, points, description, is_active) VALUES
('COUNSELING', '개인 상담 완료', 50, '개인 상담 1회 완료', 1),
('COUNSELING', '집단 상담 참여', 30, '집단 상담 1회 참여', 1);

-- 설문 관련 마일리지 (추후 구현)
INSERT INTO mileage_rules (activity_type, activity_name, points, description, is_active) VALUES
('SURVEY', '설문조사 완료', 20, '설문조사 응답 완료', 1),
('SURVEY', '만족도 조사 완료', 30, '프로그램 만족도 조사 완료', 1);

-- 기타 활동
INSERT INTO mileage_rules (activity_type, activity_name, points, description, is_active) VALUES
('MANUAL', '관리자 수동 지급', 0, '관리자가 수동으로 지급하는 마일리지 (포인트는 변동)', 1);
