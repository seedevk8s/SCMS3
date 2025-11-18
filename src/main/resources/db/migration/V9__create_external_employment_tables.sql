-- V9: 외부취업가점 테이블 생성
-- 작성일: 2025-11-18
-- 설명: 학생들의 인턴십, 현장실습, 외부 프로젝트, 취업, 창업 등 외부 취업 활동을 관리하는 테이블

-- 외부취업 활동 테이블
CREATE TABLE external_employments (
    employment_id BIGSERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    employment_type VARCHAR(50) NOT NULL, -- INTERNSHIP, FIELD_TRAINING, PROJECT, JOB, STARTUP
    company_name VARCHAR(200) NOT NULL,
    position VARCHAR(100),
    department VARCHAR(100),
    start_date DATE NOT NULL,
    end_date DATE,
    duration_months INTEGER, -- 활동 기간(개월)
    description TEXT,
    work_content TEXT, -- 업무 내용
    skills_learned TEXT, -- 습득한 기술
    certificate_url VARCHAR(500), -- 증명서 첨부 파일 URL
    credits INTEGER NOT NULL DEFAULT 0, -- 획득 가점
    is_verified BOOLEAN DEFAULT FALSE, -- 관리자 승인 여부
    verified_by INTEGER, -- 승인한 관리자 ID
    verification_date TIMESTAMP, -- 승인 날짜
    rejection_reason TEXT, -- 거절 사유
    is_portfolio_linked BOOLEAN DEFAULT FALSE, -- 포트폴리오 연동 여부
    portfolio_item_id BIGINT, -- 연동된 포트폴리오 항목 ID
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,

    -- Foreign Keys
    CONSTRAINT fk_external_employment_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_external_employment_verifier FOREIGN KEY (verified_by) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_external_employment_portfolio FOREIGN KEY (portfolio_item_id) REFERENCES portfolio_items(item_id) ON DELETE SET NULL,

    -- Constraints
    CONSTRAINT chk_employment_type CHECK (employment_type IN ('INTERNSHIP', 'FIELD_TRAINING', 'PROJECT', 'JOB', 'STARTUP')),
    CONSTRAINT chk_credits_positive CHECK (credits >= 0),
    CONSTRAINT chk_date_order CHECK (end_date IS NULL OR start_date <= end_date)
);

-- 외부취업 가점 규칙 테이블
CREATE TABLE external_employment_credit_rules (
    rule_id SERIAL PRIMARY KEY,
    employment_type VARCHAR(50) NOT NULL,
    min_duration_months INTEGER, -- 최소 기간(개월)
    max_duration_months INTEGER, -- 최대 기간(개월)
    base_credits INTEGER NOT NULL, -- 기본 가점
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT chk_rule_employment_type CHECK (employment_type IN ('INTERNSHIP', 'FIELD_TRAINING', 'PROJECT', 'JOB', 'STARTUP')),
    CONSTRAINT chk_duration_order CHECK (max_duration_months IS NULL OR min_duration_months <= max_duration_months)
);

-- 인덱스 생성
CREATE INDEX idx_external_employments_user_id ON external_employments(user_id);
CREATE INDEX idx_external_employments_verified ON external_employments(is_verified);
CREATE INDEX idx_external_employments_type ON external_employments(employment_type);
CREATE INDEX idx_external_employments_created_at ON external_employments(created_at);
CREATE INDEX idx_external_employments_deleted_at ON external_employments(deleted_at);
CREATE INDEX idx_external_employment_rules_type ON external_employment_credit_rules(employment_type);
CREATE INDEX idx_external_employment_rules_active ON external_employment_credit_rules(is_active);

-- 기본 가점 규칙 삽입
-- 인턴십
INSERT INTO external_employment_credit_rules (employment_type, min_duration_months, max_duration_months, base_credits, description)
VALUES ('INTERNSHIP', 0, 2, 30, '인턴십 - 2개월 이하');

INSERT INTO external_employment_credit_rules (employment_type, min_duration_months, max_duration_months, base_credits, description)
VALUES ('INTERNSHIP', 3, 5, 80, '인턴십 - 3개월 ~ 5개월');

INSERT INTO external_employment_credit_rules (employment_type, min_duration_months, max_duration_months, base_credits, description)
VALUES ('INTERNSHIP', 6, NULL, 150, '인턴십 - 6개월 이상');

-- 현장실습
INSERT INTO external_employment_credit_rules (employment_type, min_duration_months, max_duration_months, base_credits, description)
VALUES ('FIELD_TRAINING', 0, 2, 50, '현장실습 - 2개월 이하');

INSERT INTO external_employment_credit_rules (employment_type, min_duration_months, max_duration_months, base_credits, description)
VALUES ('FIELD_TRAINING', 3, 5, 100, '현장실습 - 3개월 ~ 5개월');

INSERT INTO external_employment_credit_rules (employment_type, min_duration_months, max_duration_months, base_credits, description)
VALUES ('FIELD_TRAINING', 6, NULL, 180, '현장실습 - 6개월 이상');

-- 외부 프로젝트
INSERT INTO external_employment_credit_rules (employment_type, min_duration_months, max_duration_months, base_credits, description)
VALUES ('PROJECT', 0, 2, 40, '외부 프로젝트 - 2개월 이하');

INSERT INTO external_employment_credit_rules (employment_type, min_duration_months, max_duration_months, base_credits, description)
VALUES ('PROJECT', 3, 5, 90, '외부 프로젝트 - 3개월 ~ 5개월');

INSERT INTO external_employment_credit_rules (employment_type, min_duration_months, max_duration_months, base_credits, description)
VALUES ('PROJECT', 6, NULL, 160, '외부 프로젝트 - 6개월 이상');

-- 취업
INSERT INTO external_employment_credit_rules (employment_type, min_duration_months, max_duration_months, base_credits, description)
VALUES ('JOB', 0, 5, 150, '취업 - 5개월 이하');

INSERT INTO external_employment_credit_rules (employment_type, min_duration_months, max_duration_months, base_credits, description)
VALUES ('JOB', 6, 11, 250, '취업 - 6개월 ~ 11개월');

INSERT INTO external_employment_credit_rules (employment_type, min_duration_months, max_duration_months, base_credits, description)
VALUES ('JOB', 12, NULL, 400, '취업 - 12개월 이상 (정규직)');

-- 창업
INSERT INTO external_employment_credit_rules (employment_type, min_duration_months, max_duration_months, base_credits, description)
VALUES ('STARTUP', 0, 5, 100, '창업 - 5개월 이하');

INSERT INTO external_employment_credit_rules (employment_type, min_duration_months, max_duration_months, base_credits, description)
VALUES ('STARTUP', 6, 11, 200, '창업 - 6개월 ~ 11개월');

INSERT INTO external_employment_credit_rules (employment_type, min_duration_months, max_duration_months, base_credits, description)
VALUES ('STARTUP', 12, NULL, 350, '창업 - 12개월 이상 (사업자 등록)');

-- 코멘트 추가
COMMENT ON TABLE external_employments IS '학생들의 외부 취업 활동 기록';
COMMENT ON COLUMN external_employments.employment_type IS '활동 유형: INTERNSHIP(인턴십), FIELD_TRAINING(현장실습), PROJECT(외부 프로젝트), JOB(취업), STARTUP(창업)';
COMMENT ON COLUMN external_employments.credits IS '획득한 가점';
COMMENT ON COLUMN external_employments.is_verified IS '관리자 승인 여부';
COMMENT ON COLUMN external_employments.is_portfolio_linked IS '포트폴리오와 연동되었는지 여부';
COMMENT ON TABLE external_employment_credit_rules IS '외부취업 활동 유형별 가점 규칙';
