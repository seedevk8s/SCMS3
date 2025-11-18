-- ============================================
-- V7: Enhance Portfolio Fields
-- 포트폴리오 테이블에 프로필 및 상세 정보 필드 추가
-- ============================================

-- 포트폴리오 테이블에 프로필 정보 추가
ALTER TABLE portfolios
ADD COLUMN profile_image_url VARCHAR(500) COMMENT '프로필 이미지 URL' AFTER template_type,
ADD COLUMN about_me TEXT COMMENT '자기소개' AFTER profile_image_url,
ADD COLUMN career_goal VARCHAR(500) COMMENT '경력 목표/희망 분야' AFTER about_me,
ADD COLUMN contact_email VARCHAR(100) COMMENT '연락처 이메일' AFTER career_goal,
ADD COLUMN contact_phone VARCHAR(20) COMMENT '연락처 전화번호' AFTER contact_email,
ADD COLUMN github_url VARCHAR(200) COMMENT 'GitHub URL' AFTER contact_phone,
ADD COLUMN blog_url VARCHAR(200) COMMENT '블로그 URL' AFTER github_url,
ADD COLUMN linkedin_url VARCHAR(200) COMMENT 'LinkedIn URL' AFTER blog_url,
ADD COLUMN website_url VARCHAR(200) COMMENT '개인 웹사이트 URL' AFTER linkedin_url,
ADD COLUMN skills TEXT COMMENT '기술 스택 (JSON 배열 또는 쉼표 구분)' AFTER website_url,
ADD COLUMN interests TEXT COMMENT '관심 분야 (JSON 배열 또는 쉼표 구분)' AFTER skills,
ADD COLUMN major VARCHAR(100) COMMENT '전공' AFTER interests,
ADD COLUMN grade INT COMMENT '학년' AFTER major,
ADD COLUMN gpa DECIMAL(3,2) COMMENT 'GPA' AFTER grade;

-- 인덱스 추가
CREATE INDEX idx_portfolios_contact_email ON portfolios(contact_email);
