-- 통합상담 시스템 테이블 생성
-- V4: 상담 세션, 상담 기록, 상담사 일정 관리

-- ============================================
-- 상담 세션 테이블 (상담 신청 및 예약)
-- ============================================
CREATE TABLE IF NOT EXISTS consultation_sessions (
    session_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '세션 ID',
    student_id INT NOT NULL COMMENT '학생 ID',
    counselor_id BIGINT COMMENT '배정된 상담사 ID',
    consultation_type VARCHAR(50) NOT NULL COMMENT '상담 유형 (CAREER, ACADEMIC, PSYCHOLOGICAL, RELATIONSHIP, OTHER)',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '상태 (PENDING, APPROVED, REJECTED, COMPLETED, CANCELLED)',
    requested_date DATE NOT NULL COMMENT '희망 상담 날짜',
    requested_time TIME COMMENT '희망 상담 시간',
    scheduled_datetime DATETIME COMMENT '확정된 상담 일시',
    title VARCHAR(200) NOT NULL COMMENT '상담 제목',
    content TEXT NOT NULL COMMENT '상담 신청 내용',
    rejection_reason VARCHAR(500) COMMENT '거부 사유',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '신청일시',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    deleted_at DATETIME COMMENT '삭제일시 (Soft Delete)',
    INDEX idx_student_id (student_id),
    INDEX idx_counselor_id (counselor_id),
    INDEX idx_status (status),
    INDEX idx_requested_date (requested_date),
    INDEX idx_deleted_at (deleted_at),
    FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (counselor_id) REFERENCES counselors(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='상담 세션 (신청 및 예약)';

-- ============================================
-- 상담 기록 테이블
-- ============================================
CREATE TABLE IF NOT EXISTS consultation_records (
    record_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '기록 ID',
    session_id BIGINT NOT NULL COMMENT '상담 세션 ID',
    counselor_id BIGINT NOT NULL COMMENT '상담사 ID',
    student_id INT NOT NULL COMMENT '학생 ID',
    consultation_date DATETIME NOT NULL COMMENT '실제 상담 일시',
    duration_minutes INT COMMENT '상담 시간 (분)',
    summary TEXT COMMENT '상담 요약',
    counselor_notes TEXT COMMENT '상담사 메모 (학생 비공개)',
    student_feedback TEXT COMMENT '학생 피드백',
    satisfaction_score INT COMMENT '만족도 (1-5)',
    follow_up_required TINYINT(1) DEFAULT 0 COMMENT '추가 상담 필요 여부',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    deleted_at DATETIME COMMENT '삭제일시 (Soft Delete)',
    INDEX idx_session_id (session_id),
    INDEX idx_counselor_id (counselor_id),
    INDEX idx_student_id (student_id),
    INDEX idx_consultation_date (consultation_date),
    INDEX idx_deleted_at (deleted_at),
    FOREIGN KEY (session_id) REFERENCES consultation_sessions(session_id) ON DELETE CASCADE,
    FOREIGN KEY (counselor_id) REFERENCES counselors(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='상담 기록';

-- ============================================
-- 상담사 일정 테이블
-- ============================================
CREATE TABLE IF NOT EXISTS counselor_schedules (
    schedule_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '일정 ID',
    counselor_id BIGINT NOT NULL COMMENT '상담사 ID',
    day_of_week INT NOT NULL COMMENT '요일 (1=월요일, 7=일요일)',
    start_time TIME NOT NULL COMMENT '시작 시간',
    end_time TIME NOT NULL COMMENT '종료 시간',
    is_available TINYINT(1) NOT NULL DEFAULT 1 COMMENT '가용 여부',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    deleted_at DATETIME COMMENT '삭제일시 (Soft Delete)',
    INDEX idx_counselor_id (counselor_id),
    INDEX idx_day_of_week (day_of_week),
    INDEX idx_is_available (is_available),
    INDEX idx_deleted_at (deleted_at),
    FOREIGN KEY (counselor_id) REFERENCES counselors(id) ON DELETE CASCADE,
    CONSTRAINT chk_day_of_week CHECK (day_of_week BETWEEN 1 AND 7),
    CONSTRAINT chk_satisfaction CHECK (satisfaction_score IS NULL OR (satisfaction_score BETWEEN 1 AND 5))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='상담사 근무 일정';

-- ============================================
-- 초기 샘플 데이터
-- ============================================

-- 샘플 상담사 (users 테이블에 상담사 역할 추가)
-- 비밀번호: "counselor123"
INSERT INTO users (student_num, name, email, phone, password, birth_date, department, role)
VALUES
(8000001, '김상담', 'counselor1@pureum.ac.kr', '010-1111-2222',
 '$2a$10$5FtU2vWqLR6xX7nO2YzS1OPfSTuBb8gP3JfXp2rUzPQ1Cz.ZCiQ.N',
 '1985-03-15', '학생상담센터', 'COUNSELOR'),
(8000002, '이상담', 'counselor2@pureum.ac.kr', '010-3333-4444',
 '$2a$10$5FtU2vWqLR6xX7nO2YzS1OPfSTuBb8gP3JfXp2rUzPQ1Cz.ZCiQ.N',
 '1988-07-20', '학생상담센터', 'COUNSELOR');

-- 상담사 프로필 정보
INSERT INTO counselors (user_id, specialization, license, available) VALUES
((SELECT user_id FROM users WHERE student_num = 8000001), '진로상담, 학업상담', '전문상담사 2급', 1),
((SELECT user_id FROM users WHERE student_num = 8000002), '심리상담, 대인관계', '임상심리사 2급', 1);

-- 상담사 근무 일정 (평일 오전 9시 ~ 오후 6시)
INSERT INTO counselor_schedules (counselor_id, day_of_week, start_time, end_time, is_available)
SELECT id, 1, '09:00:00', '18:00:00', 1 FROM counselors WHERE user_id = (SELECT user_id FROM users WHERE student_num = 8000001);
INSERT INTO counselor_schedules (counselor_id, day_of_week, start_time, end_time, is_available)
SELECT id, 2, '09:00:00', '18:00:00', 1 FROM counselors WHERE user_id = (SELECT user_id FROM users WHERE student_num = 8000001);
INSERT INTO counselor_schedules (counselor_id, day_of_week, start_time, end_time, is_available)
SELECT id, 3, '09:00:00', '18:00:00', 1 FROM counselors WHERE user_id = (SELECT user_id FROM users WHERE student_num = 8000001);
INSERT INTO counselor_schedules (counselor_id, day_of_week, start_time, end_time, is_available)
SELECT id, 4, '09:00:00', '18:00:00', 1 FROM counselors WHERE user_id = (SELECT user_id FROM users WHERE student_num = 8000001);
INSERT INTO counselor_schedules (counselor_id, day_of_week, start_time, end_time, is_available)
SELECT id, 5, '09:00:00', '18:00:00', 1 FROM counselors WHERE user_id = (SELECT user_id FROM users WHERE student_num = 8000001);

INSERT INTO counselor_schedules (counselor_id, day_of_week, start_time, end_time, is_available)
SELECT id, 1, '10:00:00', '19:00:00', 1 FROM counselors WHERE user_id = (SELECT user_id FROM users WHERE student_num = 8000002);
INSERT INTO counselor_schedules (counselor_id, day_of_week, start_time, end_time, is_available)
SELECT id, 2, '10:00:00', '19:00:00', 1 FROM counselors WHERE user_id = (SELECT user_id FROM users WHERE student_num = 8000002);
INSERT INTO counselor_schedules (counselor_id, day_of_week, start_time, end_time, is_available)
SELECT id, 3, '10:00:00', '19:00:00', 1 FROM counselors WHERE user_id = (SELECT user_id FROM users WHERE student_num = 8000002);
INSERT INTO counselor_schedules (counselor_id, day_of_week, start_time, end_time, is_available)
SELECT id, 4, '10:00:00', '19:00:00', 1 FROM counselors WHERE user_id = (SELECT user_id FROM users WHERE student_num = 8000002);
INSERT INTO counselor_schedules (counselor_id, day_of_week, start_time, end_time, is_available)
SELECT id, 5, '10:00:00', '19:00:00', 1 FROM counselors WHERE user_id = (SELECT user_id FROM users WHERE student_num = 8000002);

-- 샘플 상담 세션 (테스트용)
INSERT INTO consultation_sessions (student_id, counselor_id, consultation_type, status, requested_date, requested_time, title, content)
VALUES
((SELECT user_id FROM users WHERE student_num = 2024001),
 (SELECT id FROM counselors WHERE user_id = (SELECT user_id FROM users WHERE student_num = 8000001)),
 'CAREER', 'PENDING', '2025-12-01', '14:00:00', '진로 고민 상담', '졸업 후 진로에 대해 상담받고 싶습니다.'),
((SELECT user_id FROM users WHERE student_num = 2024002),
 (SELECT id FROM counselors WHERE user_id = (SELECT user_id FROM users WHERE student_num = 8000002)),
 'PSYCHOLOGICAL', 'APPROVED', '2025-12-02', '15:00:00', '학업 스트레스 상담', '학업 스트레스가 심해서 상담받고 싶습니다.');
