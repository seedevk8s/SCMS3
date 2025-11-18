-- ============================================
-- V8: Survey System Tables
-- 설문조사, 질문, 선택지, 응답, 답변 테이블 생성
-- ============================================

-- 설문조사 메인 테이블
CREATE TABLE IF NOT EXISTS surveys (
    survey_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '설문조사 ID',
    title VARCHAR(200) NOT NULL COMMENT '설문 제목',
    description TEXT COMMENT '설문 설명',
    start_date DATETIME NOT NULL COMMENT '시작일시',
    end_date DATETIME NOT NULL COMMENT '종료일시',
    is_anonymous BOOLEAN NOT NULL DEFAULT FALSE COMMENT '익명 여부',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '활성화 여부',
    target_type VARCHAR(20) NOT NULL DEFAULT 'ALL' COMMENT '대상 유형 (ALL, STUDENT, SPECIFIC)',
    max_responses INT COMMENT '최대 응답 수 (NULL이면 무제한)',
    allow_multiple_responses BOOLEAN NOT NULL DEFAULT FALSE COMMENT '중복 응답 허용 여부',
    show_results BOOLEAN NOT NULL DEFAULT FALSE COMMENT '응답자에게 결과 공개 여부',
    created_by INT NOT NULL COMMENT '생성자 ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    deleted_at DATETIME COMMENT '삭제일시 (Soft Delete)',
    INDEX idx_is_active (is_active),
    INDEX idx_start_date (start_date),
    INDEX idx_end_date (end_date),
    INDEX idx_created_by (created_by),
    INDEX idx_deleted_at (deleted_at),
    FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='설문조사';

-- 설문 질문 테이블
CREATE TABLE IF NOT EXISTS survey_questions (
    question_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '질문 ID',
    survey_id BIGINT NOT NULL COMMENT '설문조사 ID',
    question_type VARCHAR(30) NOT NULL COMMENT '질문 유형 (SINGLE_CHOICE, MULTIPLE_CHOICE, SHORT_TEXT, LONG_TEXT, SCALE)',
    question_text TEXT NOT NULL COMMENT '질문 내용',
    is_required BOOLEAN NOT NULL DEFAULT TRUE COMMENT '필수 응답 여부',
    display_order INT NOT NULL DEFAULT 0 COMMENT '표시 순서',
    scale_min INT COMMENT '척도 최소값 (SCALE 유형용)',
    scale_max INT COMMENT '척도 최대값 (SCALE 유형용)',
    scale_min_label VARCHAR(50) COMMENT '척도 최소값 레이블',
    scale_max_label VARCHAR(50) COMMENT '척도 최대값 레이블',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    INDEX idx_survey_id (survey_id),
    INDEX idx_question_type (question_type),
    INDEX idx_display_order (display_order),
    FOREIGN KEY (survey_id) REFERENCES surveys(survey_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='설문 질문';

-- 설문 질문 선택지 테이블 (객관식용)
CREATE TABLE IF NOT EXISTS survey_question_options (
    option_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '선택지 ID',
    question_id BIGINT NOT NULL COMMENT '질문 ID',
    option_text VARCHAR(500) NOT NULL COMMENT '선택지 내용',
    display_order INT NOT NULL DEFAULT 0 COMMENT '표시 순서',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    INDEX idx_question_id (question_id),
    INDEX idx_display_order (display_order),
    FOREIGN KEY (question_id) REFERENCES survey_questions(question_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='설문 질문 선택지';

-- 설문 응답 테이블
CREATE TABLE IF NOT EXISTS survey_responses (
    response_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '응답 ID',
    survey_id BIGINT NOT NULL COMMENT '설문조사 ID',
    user_id INT COMMENT '응답자 ID (익명이면 NULL)',
    submitted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '제출일시',
    ip_address VARCHAR(45) COMMENT '응답자 IP 주소',
    user_agent TEXT COMMENT '응답자 User Agent',
    INDEX idx_survey_id (survey_id),
    INDEX idx_user_id (user_id),
    INDEX idx_submitted_at (submitted_at),
    FOREIGN KEY (survey_id) REFERENCES surveys(survey_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='설문 응답';

-- 설문 답변 테이블
CREATE TABLE IF NOT EXISTS survey_answers (
    answer_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '답변 ID',
    response_id BIGINT NOT NULL COMMENT '응답 ID',
    question_id BIGINT NOT NULL COMMENT '질문 ID',
    option_id BIGINT COMMENT '선택지 ID (객관식용)',
    answer_text TEXT COMMENT '답변 텍스트 (주관식용)',
    answer_number INT COMMENT '답변 숫자 (척도형용)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    INDEX idx_response_id (response_id),
    INDEX idx_question_id (question_id),
    INDEX idx_option_id (option_id),
    FOREIGN KEY (response_id) REFERENCES survey_responses(response_id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES survey_questions(question_id) ON DELETE CASCADE,
    FOREIGN KEY (option_id) REFERENCES survey_question_options(option_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='설문 답변';

-- 설문 대상자 테이블 (SPECIFIC 유형용)
CREATE TABLE IF NOT EXISTS survey_targets (
    target_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '대상자 ID',
    survey_id BIGINT NOT NULL COMMENT '설문조사 ID',
    user_id INT NOT NULL COMMENT '대상 사용자 ID',
    has_responded BOOLEAN NOT NULL DEFAULT FALSE COMMENT '응답 완료 여부',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    INDEX idx_survey_id (survey_id),
    INDEX idx_user_id (user_id),
    INDEX idx_has_responded (has_responded),
    UNIQUE KEY uk_survey_user (survey_id, user_id),
    FOREIGN KEY (survey_id) REFERENCES surveys(survey_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='설문 대상자';
