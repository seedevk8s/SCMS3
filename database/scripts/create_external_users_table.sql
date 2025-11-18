-- =========================================
-- 스크립트명: 외부회원 테이블 생성
-- 작성일: 2025-11-18
-- 설명: external_users 테이블 생성 및 초기 데이터 삽입
-- =========================================

-- 1. 기존 테이블 확인
SELECT '외부회원 테이블 생성을 시작합니다...' AS message;

-- 2. external_users 테이블 생성
CREATE TABLE IF NOT EXISTS external_users (
    -- 기본 정보
    user_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '사용자 ID',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '이메일 (로그인 ID)',
    password VARCHAR(255) NOT NULL COMMENT '비밀번호 (BCrypt 암호화)',
    name VARCHAR(50) NOT NULL COMMENT '이름',
    phone VARCHAR(20) COMMENT '전화번호',
    birth_date DATE NOT NULL COMMENT '생년월일',

    -- 추가 정보
    address VARCHAR(200) COMMENT '주소',
    gender ENUM('M', 'F', 'OTHER') COMMENT '성별',

    -- 계정 상태
    status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') DEFAULT 'ACTIVE' COMMENT '계정 상태',
    locked BOOLEAN DEFAULT FALSE COMMENT '계정 잠금 여부',
    fail_cnt INT DEFAULT 0 COMMENT '로그인 실패 횟수',

    -- 이메일 인증
    email_verified BOOLEAN DEFAULT FALSE COMMENT '이메일 인증 여부',
    email_verify_token VARCHAR(255) COMMENT '이메일 인증 토큰',
    email_verified_at DATETIME COMMENT '이메일 인증 일시',

    -- 약관 동의
    agree_terms BOOLEAN DEFAULT FALSE COMMENT '이용약관 동의',
    agree_privacy BOOLEAN DEFAULT FALSE COMMENT '개인정보 처리방침 동의',
    agree_marketing BOOLEAN DEFAULT FALSE COMMENT '마케팅 수신 동의 (선택)',

    -- 타임스탬프
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    deleted_at DATETIME COMMENT '삭제일시 (Soft Delete)',
    last_login_at DATETIME COMMENT '마지막 로그인 일시',

    -- 인덱스
    INDEX idx_email (email),
    INDEX idx_created_at (created_at),
    INDEX idx_status (status),
    INDEX idx_email_verified (email_verified)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='외부 회원 테이블';

-- 3. 테이블 생성 확인
SELECT '외부회원 테이블이 생성되었습니다.' AS message;
DESCRIBE external_users;

-- 4. 초기 테스트 데이터 삽입 (비밀번호: password123!)
INSERT INTO external_users (
    email,
    password,
    name,
    phone,
    birth_date,
    address,
    gender,
    email_verified,
    agree_terms,
    agree_privacy,
    agree_marketing
) VALUES
(
    'external1@example.com',
    '$2a$10$rGG3WJZ8qVqVH.qVqVH.qOX8qVqVH.qVqVH.qVqVH.qVqVH.qVqVHe',  -- password123!
    '김외부',
    '010-1111-2222',
    '1990-01-15',
    '서울시 강남구',
    'M',
    TRUE,
    TRUE,
    TRUE,
    TRUE
),
(
    'external2@example.com',
    '$2a$10$rGG3WJZ8qVqVH.qVqVH.qOX8qVqVH.qVqVH.qVqVH.qVqVH.qVqVHe',  -- password123!
    '이외부',
    '010-2222-3333',
    '1995-05-20',
    '서울시 서초구',
    'F',
    TRUE,
    TRUE,
    TRUE,
    FALSE
),
(
    'external3@example.com',
    '$2a$10$rGG3WJZ8qVqVH.qVqVH.qOX8qVqVH.qVqVH.qVqVH.qVqVH.qVqVHe',  -- password123!
    '박외부',
    '010-3333-4444',
    '1988-12-30',
    '경기도 성남시',
    'OTHER',
    FALSE,
    TRUE,
    TRUE,
    FALSE
);

-- 5. 데이터 삽입 확인
SELECT '테스트 데이터가 삽입되었습니다.' AS message;
SELECT user_id, email, name, phone, birth_date, email_verified, created_at
FROM external_users;

-- 6. 완료 메시지
SELECT '외부회원 테이블 생성 및 초기화 완료!' AS message;
