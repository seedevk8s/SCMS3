-- SCMS2 Database Schema
-- 학생 역량 관리 시스템 데이터베이스 스키마

-- 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS scms2 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE scms2;

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

-- 샘플 데이터 삽입
INSERT INTO students (student_id, name, email, phone, department, grade, status) VALUES
('2024001', '김철수', 'kim.cs@example.com', '010-1234-5678', '컴퓨터공학과', '1', '재학'),
('2024002', '이영희', 'lee.yh@example.com', '010-2345-6789', '소프트웨어학과', '2', '재학'),
('2023001', '박민수', 'park.ms@example.com', '010-3456-7890', '정보보안학과', '2', '재학'),
('2023002', '최지은', 'choi.je@example.com', '010-4567-8901', '컴퓨터공학과', '2', '휴학'),
('2022001', '정우진', 'jung.wj@example.com', '010-5678-9012', '인공지능학과', '3', '재학'),
('2022002', '강민지', 'kang.mj@example.com', '010-6789-0123', '데이터사이언스학과', '3', '재학'),
('2021001', '윤서준', 'yoon.sj@example.com', '010-7890-1234', '컴퓨터공학과', '4', '재학'),
('2021002', '임하늘', 'im.hn@example.com', '010-8901-2345', '소프트웨어학과', '4', '졸업');

-- 역량 카테고리 테이블 (추후 확장)
CREATE TABLE IF NOT EXISTS competency_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '역량 카테고리명',
    description TEXT COMMENT '설명',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='역량 카테고리';

-- 역량 테이블 (추후 확장)
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

-- 학생 역량 평가 테이블 (추후 확장)
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
