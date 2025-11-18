-- SCMS2 Database Schema
-- 푸름대학교 학생성장지원센터 데이터베이스 스키마

-- 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS scms2 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE scms2;

-- ============================================
-- 사용자 관련 테이블
-- ============================================

-- 사용자 테이블 (학생, 상담사, 관리자 통합)
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '사용자 ID',
    student_num INT NOT NULL UNIQUE COMMENT '학번',
    name VARCHAR(50) NOT NULL COMMENT '이름',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '이메일',
    phone VARCHAR(100) COMMENT '전화번호',
    password VARCHAR(255) COMMENT '비밀번호 (BCrypt 암호화)',
    birth_date DATE NOT NULL COMMENT '생년월일',
    department VARCHAR(100) COMMENT '학과',
    grade INT COMMENT '학년',
    role VARCHAR(20) NOT NULL DEFAULT 'STUDENT' COMMENT '역할 (STUDENT, COUNSELOR, ADMIN)',
    locked TINYINT(1) NOT NULL DEFAULT 0 COMMENT '계정 잠금 여부',
    fail_cnt INT NOT NULL DEFAULT 0 COMMENT '로그인 실패 횟수',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    deleted_at DATETIME COMMENT '삭제일시 (Soft Delete)',
    INDEX idx_student_num (student_num),
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자 정보';

-- 로그인 히스토리 테이블
CREATE TABLE IF NOT EXISTS login_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    user_id INT NOT NULL COMMENT '사용자 ID',
    login_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '로그인 시간',
    ip_address VARCHAR(50) COMMENT 'IP 주소',
    user_agent VARCHAR(255) COMMENT 'User Agent',
    success TINYINT(1) NOT NULL COMMENT '성공 여부',
    fail_reason VARCHAR(100) COMMENT '실패 사유',
    INDEX idx_user_id (user_id),
    INDEX idx_login_time (login_time),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='로그인 이력';

-- 상담사 테이블
-- @MapsId 사용: counselor_id = user_id (1:1 관계)
CREATE TABLE IF NOT EXISTS counselors (
    counselor_id INT PRIMARY KEY COMMENT '상담사 ID (= user_id)',
    special VARCHAR(100) COMMENT '전문 분야',
    intro TEXT COMMENT '소개',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME COMMENT '삭제일시 (Soft Delete)',
    FOREIGN KEY (counselor_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='상담사 정보';

-- 외부회원 테이블 (이메일 기반 로그인)
CREATE TABLE IF NOT EXISTS external_users (
    user_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '사용자 ID',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '이메일 (로그인 ID)',
    password VARCHAR(255) NOT NULL COMMENT '비밀번호 (BCrypt 암호화)',
    name VARCHAR(50) NOT NULL COMMENT '이름',
    phone VARCHAR(20) COMMENT '전화번호',
    birth_date DATE NOT NULL COMMENT '생년월일',
    address VARCHAR(200) COMMENT '주소',
    gender VARCHAR(10) COMMENT '성별 (M, F, OTHER)',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '계정 상태 (ACTIVE, INACTIVE, SUSPENDED)',
    locked TINYINT(1) NOT NULL DEFAULT 0 COMMENT '계정 잠금 여부',
    fail_cnt INT NOT NULL DEFAULT 0 COMMENT '로그인 실패 횟수',
    email_verified TINYINT(1) NOT NULL DEFAULT 0 COMMENT '이메일 인증 여부',
    email_verify_token VARCHAR(255) COMMENT '이메일 인증 토큰',
    email_verified_at DATETIME COMMENT '이메일 인증 일시',
    agree_terms TINYINT(1) NOT NULL DEFAULT 0 COMMENT '이용약관 동의',
    agree_privacy TINYINT(1) NOT NULL DEFAULT 0 COMMENT '개인정보 처리방침 동의',
    agree_marketing TINYINT(1) DEFAULT 0 COMMENT '마케팅 수신 동의 (선택)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    deleted_at DATETIME COMMENT '삭제일시 (Soft Delete)',
    last_login_at DATETIME COMMENT '마지막 로그인 일시',
    INDEX idx_email (email),
    INDEX idx_created_at (created_at),
    INDEX idx_status (status),
    INDEX idx_email_verified (email_verified)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='외부 회원';

-- ============================================
-- 학생 정보 테이블 (기존 - 추후 users 테이블로 통합 고려)
-- ============================================

-- 학생 테이블
CREATE TABLE IF NOT EXISTS students (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(50) NOT NULL UNIQUE COMMENT '학번',
    name VARCHAR(100) NOT NULL COMMENT '이름',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '이메일',
    phone VARCHAR(20) COMMENT '전화번호',
    department VARCHAR(100) COMMENT '학과',
    grade VARCHAR(20) COMMENT '학년',
    status VARCHAR(20) DEFAULT '재학' COMMENT '상태 (재학, 휴학, 졸업, 자퇴)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    INDEX idx_student_id (student_id),
    INDEX idx_email (email),
    INDEX idx_department (department)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='학생 정보';

-- ============================================
-- 역량 관리 테이블
-- ============================================

-- 역량 카테고리 테이블
CREATE TABLE IF NOT EXISTS competency_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '역량 카테고리명',
    description TEXT COMMENT '설명',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='역량 카테고리';

-- 역량 테이블
CREATE TABLE IF NOT EXISTS competencies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL COMMENT '역량명',
    description TEXT COMMENT '설명',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES competency_categories(id) ON DELETE CASCADE,
    INDEX idx_category_id (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='역량';

-- 학생 역량 평가 테이블
CREATE TABLE IF NOT EXISTS student_competency_assessments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    competency_id BIGINT NOT NULL,
    score INT NOT NULL COMMENT '점수 (0-100)',
    assessment_date DATE NOT NULL COMMENT '평가일',
    assessor VARCHAR(100) COMMENT '평가자',
    notes TEXT COMMENT '비고',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (competency_id) REFERENCES competencies(id) ON DELETE CASCADE,
    INDEX idx_student_id (student_id),
    INDEX idx_competency_id (competency_id),
    INDEX idx_assessment_date (assessment_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='학생 역량 평가';

-- ============================================
-- 초기 샘플 데이터
-- ============================================

-- 학생 샘플 데이터 (students 테이블)
INSERT INTO students (student_id, name, email, phone, department, grade, status) VALUES
('2024001', '김철수', 'kim.cs@example.com', '010-1234-5678', '컴퓨터공학과', '1', '재학'),
('2024002', '이영희', 'lee.yh@example.com', '010-2345-6789', '소프트웨어학과', '2', '재학'),
('2023001', '박민수', 'park.ms@example.com', '010-3456-7890', '정보보안학과', '2', '재학'),
('2023002', '최지은', 'choi.je@example.com', '010-4567-8901', '컴퓨터공학과', '2', '휴학'),
('2022001', '정우진', 'jung.wj@example.com', '010-5678-9012', '인공지능학과', '3', '재학'),
('2022002', '강민지', 'kang.mj@example.com', '010-6789-0123', '데이터사이언스학과', '3', '재학'),
('2021001', '윤서준', 'yoon.sj@example.com', '010-7890-1234', '컴퓨터공학과', '4', '재학'),
('2021002', '임하늘', 'im.hn@example.com', '010-8901-2345', '소프트웨어학과', '4', '졸업');

-- 사용자 샘플 데이터 (users 테이블)
-- 초기 비밀번호는 생년월일 6자리 (yyMMdd)입니다.
-- 예: 2003년 1월 1일 생 → 비밀번호 "030101"
-- BCrypt로 암호화된 비밀번호가 저장됩니다.

-- 비밀번호: "030101" (2003년 1월 1일)
INSERT INTO users (student_num, name, email, phone, password, birth_date, department, grade, role) VALUES
(2024001, '김철수', 'kim.cs@example.com', '010-1234-5678',
 '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36xVIPfHukJ5d3BAFH9vv.S',
 '2003-01-01', '컴퓨터공학과', 1, 'STUDENT');

-- 비밀번호: "040215" (2004년 2월 15일)
INSERT INTO users (student_num, name, email, phone, password, birth_date, department, grade, role) VALUES
(2024002, '이영희', 'lee.yh@example.com', '010-2345-6789',
 '$2a$10$7eqFTfQvA8R7gBxNn8wuXeKxDVwQMfBw6O9.3QrTz5nQ0YqK7Wv.C',
 '2004-02-15', '소프트웨어학과', 2, 'STUDENT');

-- 비밀번호: "020310" (2002년 3월 10일)
INSERT INTO users (student_num, name, email, phone, password, birth_date, department, grade, role) VALUES
(2023001, '박민수', 'park.ms@example.com', '010-3456-7890',
 '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
 '2002-03-10', '정보보안학과', 2, 'STUDENT');

-- 비밀번호: "020505" (2002년 5월 5일)
INSERT INTO users (student_num, name, email, phone, password, birth_date, department, grade, role) VALUES
(2023002, '최지은', 'choi.je@example.com', '010-4567-8901',
 '$2a$10$YRgkVp3h.7sVUKLR4U9rSOVWwdRp9FBc3pPQbR7Tk8fCQxMN7Yv.W',
 '2002-05-05', '컴퓨터공학과', 2, 'STUDENT');

-- 비밀번호: "010620" (2001년 6월 20일)
INSERT INTO users (student_num, name, email, phone, password, birth_date, department, grade, role) VALUES
(2022001, '정우진', 'jung.wj@example.com', '010-5678-9012',
 '$2a$10$3FrR9vqKQN2xS5tL8WzO7ONcPQrYz5dM9FbTk8nQxMN7Yv.WZeL.K',
 '2001-06-20', '인공지능학과', 3, 'STUDENT');

-- 비밀번호: "010815" (2001년 8월 15일)
INSERT INTO users (student_num, name, email, phone, password, birth_date, department, grade, role) VALUES
(2022002, '강민지', 'kang.mj@example.com', '010-6789-0123',
 '$2a$10$8TqQ9wQKRN3xT6uM9XzP8OPdQRsZz6eN0GcUl9oRyNO8Zw.XZfM.L',
 '2001-08-15', '데이터사이언스학과', 3, 'STUDENT');

-- 비밀번호: "000911" (2000년 9월 11일)
INSERT INTO users (student_num, name, email, phone, password, birth_date, department, grade, role) VALUES
(2021001, '윤서준', 'yoon.sj@example.com', '010-7890-1234',
 '$2a$10$9UsR0xRLSO4yU7vN0YzQ9OQeRStAa7fO1HdVm0pSzOP9Ax.YAgN.M',
 '2000-09-11', '컴퓨터공학과', 4, 'STUDENT');

-- 비밀번호: "001225" (2000년 12월 25일)
INSERT INTO users (student_num, name, email, phone, password, birth_date, department, grade, role) VALUES
(2021002, '임하늘', 'im.hn@example.com', '010-8901-2345',
 '$2a$10$0VtS1ySmTP5zV8wO1ZzR0OPfSTuBb8gP2IeWn1qTzPQ0By.ZBhO.N',
 '2000-12-25', '소프트웨어학과', 4, 'STUDENT');

-- 관리자 계정
-- 학번: 9999999, 비밀번호: "admin123"
INSERT INTO users (student_num, name, email, phone, password, birth_date, department, role) VALUES
(9999999, '시스템관리자', 'admin@pureum.ac.kr', '010-0000-0000',
 '$2a$10$VQbrqrzS8Zx.XWlM2ZzS0OqfUTvCc9hR3JfXo2rUzQR1Cy.aCiP.O',
 '1990-01-01', 'IT관리팀', 'ADMIN');
