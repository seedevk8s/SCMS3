-- 초기 프로그램 데이터 (50개)
-- 모든 필터 옵션에 골고루 분산

-- 행정부서: 교수학습지원센터 (6개)
INSERT INTO programs (title, description, content, department, college, category, sub_category, application_start_date, application_end_date, max_participants, current_participants, thumbnail_url, status, hits, created_at) VALUES
('학습전략 워크샵', '효과적인 학습 방법을 배우는 워크샵', '학습전략과 시간관리 기법을 배웁니다', '교수학습지원센터', 'RISE사업단', '학습역량', '학습법', '2024-12-01 00:00:00', '2024-12-20 23:59:59', 30, 15, 'https://placehold.co/560x360/4A90E2/FFF?text=학습전략+워크샵', 'OPEN', 245, CURRENT_TIMESTAMP),
('글쓰기 클리닉', '학술적 글쓰기 능력 향상', '논문 및 보고서 작성법을 학습합니다', '교수학습지원센터', '간호대학', '학습역량', '글쓰기', '2024-12-03 00:00:00', '2024-12-25 23:59:59', 25, 8, 'https://placehold.co/560x360/50C878/FFF?text=글쓰기+클리닉', 'OPEN', 189, CURRENT_TIMESTAMP),
('발표스킬 향상 프로그램', '효과적인 프레젠테이션 기법', 'PPT 작성과 발표 스킬을 배웁니다', '교수학습지원센터', '교육대학원', '학습역량', '발표', '2024-11-25 00:00:00', '2024-12-15 23:59:59', 40, 35, 'https://placehold.co/560x360/FF6B6B/FFF?text=발표스킬+향상', 'OPEN', 312, CURRENT_TIMESTAMP),
('수학 기초 다지기', '대학 수학 기초 학습', '미적분과 선형대수 기초를 다집니다', '교수학습지원센터', '기계ICT융합공학부', '학습역량', '기초학습', '2024-12-05 00:00:00', '2024-12-30 23:59:59', 50, 12, 'https://placehold.co/560x360/9B59B6/FFF?text=수학+기초+다지기', 'OPEN', 156, CURRENT_TIMESTAMP),
('영어 프레젠테이션 특강', '영어로 발표하는 방법', '영어 프레젠테이션 스킬을 향상시킵니다', '교수학습지원센터', 'RIS지원센터', '학습역량', '외국어', '2024-12-10 00:00:00', '2025-01-10 23:59:59', 30, 5, 'https://placehold.co/560x360/F39C12/FFF?text=영어+프레젠테이션', 'OPEN', 98, CURRENT_TIMESTAMP),
('학습동아리 지원 프로그램', '스터디 그룹 운영 지원', '학습동아리 활동을 지원합니다', '교수학습지원센터', '약학대학', '학습역량', '동아리', '2024-11-20 00:00:00', '2024-12-31 23:59:59', NULL, 45, 'https://placehold.co/560x360/1ABC9C/FFF?text=학습동아리+지원', 'OPEN', 401, CURRENT_TIMESTAMP);

-- 행정부서: 도서관 (5개)
INSERT INTO programs (title, description, content, department, college, category, sub_category, application_start_date, application_end_date, max_participants, current_participants, thumbnail_url, status, hits, created_at) VALUES
('독서 토론회', '책을 읽고 토론하는 프로그램', '매월 선정된 도서로 토론을 진행합니다', '도서관', 'RISE사업단', '학습역량', '독서', '2024-12-01 00:00:00', '2024-12-28 23:59:59', 20, 18, 'https://placehold.co/560x360/E74C3C/FFF?text=독서+토론회', 'OPEN', 267, CURRENT_TIMESTAMP),
('논문 검색 교육', '학술 데이터베이스 활용법', '효과적인 논문 검색 방법을 배웁니다', '도서관', '간호대학', '학습역량', '정보활용', '2024-12-05 00:00:00', '2024-12-22 23:59:59', 35, 10, 'https://placehold.co/560x360/3498DB/FFF?text=논문+검색+교육', 'OPEN', 134, CURRENT_TIMESTAMP),
('북카페 운영', '도서관 내 북카페 이용', '편안한 독서 공간을 제공합니다', '도서관', '교육대학원', '기타', '문화활동', '2024-11-15 00:00:00', '2025-02-28 23:59:59', NULL, 89, 'https://placehold.co/560x360/95A5A6/FFF?text=북카페+운영', 'OPEN', 523, CURRENT_TIMESTAMP),
('참고문헌 작성법', '논문 인용 및 참고문헌 정리', 'APA, MLA 양식을 배웁니다', '도서관', '기계ICT융합공학부', '학습역량', '글쓰기', '2024-12-08 00:00:00', '2024-12-30 23:59:59', 25, 7, 'https://placehold.co/560x360/2C3E50/FFF?text=참고문헌+작성법', 'OPEN', 112, CURRENT_TIMESTAMP),
('전자책 활용 세미나', 'e-Book 플랫폼 사용법', '전자책 대여 및 활용법을 안내합니다', '도서관', 'RIS지원센터', '학습역량', '정보활용', '2024-12-02 00:00:00', '2024-12-25 23:59:59', 30, 14, 'https://placehold.co/560x360/16A085/FFF?text=전자책+활용+세미나', 'OPEN', 187, CURRENT_TIMESTAMP);

-- 행정부서: 생활관 (5개)
INSERT INTO programs (title, description, content, department, college, category, sub_category, application_start_date, application_end_date, max_participants, current_participants, thumbnail_url, status, hits, created_at) VALUES
('기숙사 안전교육', '화재 및 안전사고 예방', '기숙사 생활 안전 수칙을 배웁니다', '생활관', 'RISE사업단', '기타', '안전교육', '2024-11-28 00:00:00', '2024-12-15 23:59:59', 100, 87, 'https://placehold.co/560x360/E67E22/FFF?text=기숙사+안전교육', 'OPEN', 356, CURRENT_TIMESTAMP),
('룸메이트 관계 개선', '기숙사 생활 갈등 해결', '원활한 공동생활을 위한 소통법을 배웁니다', '생활관', '간호대학', '심리상담', '관계', '2024-12-01 00:00:00', '2024-12-20 23:59:59', 40, 32, 'https://placehold.co/560x360/8E44AD/FFF?text=룸메이트+관계+개선', 'OPEN', 289, CURRENT_TIMESTAMP),
('기숙사 문화 프로그램', '층별 친목 도모 활동', '기숙사생 간 유대감을 형성합니다', '생활관', '교육대학원', '기타', '문화활동', '2024-12-10 00:00:00', '2025-01-10 23:59:59', 60, 15, 'https://placehold.co/560x360/27AE60/FFF?text=기숙사+문화+프로그램', 'OPEN', 145, CURRENT_TIMESTAMP),
('자취생 생활 가이드', '효율적인 자취 생활 팁', '요리, 청소, 예산 관리를 배웁니다', '생활관', '기계ICT융합공학부', '기타', '생활지도', '2024-12-05 00:00:00', '2024-12-28 23:59:59', 30, 21, 'https://placehold.co/560x360/D35400/FFF?text=자취생+생활+가이드', 'OPEN', 234, CURRENT_TIMESTAMP),
('기숙사 멘토링', '선배가 알려주는 기숙사 생활', '기숙사 생활 적응을 돕습니다', '생활관', 'RIS지원센터', '기타', '멘토링', '2024-11-22 00:00:00', '2024-12-31 23:59:59', 50, 38, 'https://placehold.co/560x360/2980B9/FFF?text=기숙사+멘토링', 'OPEN', 412, CURRENT_TIMESTAMP);

-- 행정부서: 학생상담센터 (6개)
INSERT INTO programs (title, description, content, department, college, category, sub_category, application_start_date, application_end_date, max_participants, current_participants, thumbnail_url, status, hits, created_at) VALUES
('스트레스 관리 워크샵', '건강한 스트레스 대처법', '명상과 이완 기법을 배웁니다', '학생상담센터', 'RISE사업단', '심리상담', '스트레스', '2024-12-01 00:00:00', '2024-12-25 23:59:59', 25, 20, 'https://placehold.co/560x360/5DADE2/FFF?text=스트레스+관리', 'OPEN', 298, CURRENT_TIMESTAMP),
('대인관계 상담', '친구 관계 고민 해결', '건강한 인간관계 형성법을 배웁니다', '학생상담센터', '간호대학', '심리상담', '관계', '2024-12-03 00:00:00', '2024-12-30 23:59:59', 20, 15, 'https://placehold.co/560x360/48C9B0/FFF?text=대인관계+상담', 'OPEN', 267, CURRENT_TIMESTAMP),
('우울감 극복 프로그램', '긍정적 마인드 형성', '우울감을 이겨내는 방법을 배웁니다', '학생상담센터', '교육대학원', '심리상담', '정서', '2024-12-05 00:00:00', '2024-12-28 23:59:59', 15, 12, 'https://placehold.co/560x360/AF7AC5/FFF?text=우울감+극복', 'OPEN', 189, CURRENT_TIMESTAMP),
('진로 탐색 상담', '나에게 맞는 진로 찾기', '진로 적성 검사 및 상담을 진행합니다', '학생상담센터', '기계ICT융합공학부', '진로지도', '진로탐색', '2024-12-08 00:00:00', '2025-01-15 23:59:59', 30, 8, 'https://placehold.co/560x360/F4D03F/FFF?text=진로+탐색+상담', 'OPEN', 156, CURRENT_TIMESTAMP),
('시험 불안 극복', '시험 스트레스 관리', '시험 불안을 줄이는 방법을 배웁니다', '학생상담센터', 'RIS지원센터', '심리상담', '스트레스', '2024-11-25 00:00:00', '2024-12-20 23:59:59', 20, 18, 'https://placehold.co/560x360/EC7063/FFF?text=시험+불안+극복', 'OPEN', 345, CURRENT_TIMESTAMP),
('집단 상담 프로그램', '또래 집단 심리 상담', '비슷한 고민을 가진 학생들이 모여 상담합니다', '학생상담센터', '약학대학', '심리상담', '집단상담', '2024-12-10 00:00:00', '2025-01-05 23:59:59', 12, 10, 'https://placehold.co/560x360/52BE80/FFF?text=집단+상담', 'OPEN', 223, CURRENT_TIMESTAMP);

-- 행정부서: 장애학생지원센터 (5개)
INSERT INTO programs (title, description, content, department, college, category, sub_category, application_start_date, application_end_date, max_participants, current_participants, thumbnail_url, status, hits, created_at) VALUES
('학습 보조기기 지원', '장애학생 학습 보조', '보조공학기기 사용법을 안내합니다', '장애학생지원센터', 'RISE사업단', '장애학생지원', '학습지원', '2024-11-20 00:00:00', '2025-02-28 23:59:59', NULL, 23, 'https://placehold.co/560x360/5499C7/FFF?text=학습+보조기기', 'OPEN', 178, CURRENT_TIMESTAMP),
('장애인식 개선 캠페인', '장애에 대한 이해 증진', '장애 인식 개선 활동을 진행합니다', '장애학생지원센터', '간호대학', '장애학생지원', '인식개선', '2024-12-01 00:00:00', '2024-12-31 23:59:59', NULL, 156, 'https://placehold.co/560x360/45B39D/FFF?text=장애인식+개선', 'OPEN', 289, CURRENT_TIMESTAMP),
('도우미 학생 교육', '장애학생 도우미 양성', '장애학생 지원 도우미를 교육합니다', '장애학생지원센터', '교육대학원', '장애학생지원', '도우미', '2024-12-05 00:00:00', '2024-12-25 23:59:59', 20, 17, 'https://placehold.co/560x360/A569BD/FFF?text=도우미+학생+교육', 'OPEN', 234, CURRENT_TIMESTAMP),
('편의시설 이용 안내', '캠퍼스 접근성 정보', '장애학생을 위한 편의시설을 안내합니다', '장애학생지원센터', '기계ICT융합공학부', '장애학생지원', '편의시설', '2024-11-15 00:00:00', '2025-03-31 23:59:59', NULL, 45, 'https://placehold.co/560x360/F5B041/FFF?text=편의시설+안내', 'OPEN', 412, CURRENT_TIMESTAMP),
('수어 통역 서비스', '청각장애 학생 지원', '수업 중 수어 통역을 제공합니다', '장애학생지원센터', 'RIS지원센터', '장애학생지원', '통역', '2024-11-01 00:00:00', '2025-02-28 23:59:59', NULL, 8, 'https://placehold.co/560x360/EB984E/FFF?text=수어+통역+서비스', 'OPEN', 145, CURRENT_TIMESTAMP);

-- 행정부서: 취창업지원센터 (6개)
INSERT INTO programs (title, description, content, department, college, category, sub_category, application_start_date, application_end_date, max_participants, current_participants, thumbnail_url, status, hits, created_at) VALUES
('취업 특강 시리즈', '취업 전략 및 면접 준비', '대기업 취업 전략을 배웁니다', '취창업지원센터', 'RISE사업단', '진로지도', '취업', '2024-12-01 00:00:00', '2024-12-30 23:59:59', 100, 78, 'https://placehold.co/560x360/3498DB/FFF?text=취업+특강', 'OPEN', 534, CURRENT_TIMESTAMP),
('이력서 클리닉', '완벽한 이력서 작성', '취업 전문가의 이력서 첨삭을 받습니다', '취창업지원센터', '간호대학', '진로지도', '취업', '2024-12-05 00:00:00', '2025-01-15 23:59:59', 50, 42, 'https://placehold.co/560x360/1ABC9C/FFF?text=이력서+클리닉', 'OPEN', 389, CURRENT_TIMESTAMP),
('모의 면접 프로그램', '실전 면접 연습', '모의 면접을 통해 실전 감각을 익힙니다', '취창업지원센터', '교육대학원', '진로지도', '면접', '2024-12-08 00:00:00', '2025-01-20 23:59:59', 40, 35, 'https://placehold.co/560x360/9B59B6/FFF?text=모의+면접', 'OPEN', 456, CURRENT_TIMESTAMP),
('창업 아이디어 경진대회', '창업 아이템 발굴', '우수 창업 아이템에 시상합니다', '취창업지원센터', '기계ICT융합공학부', '진로지도', '창업', '2024-11-20 00:00:00', '2024-12-25 23:59:59', 30, 28, 'https://placehold.co/560x360/E74C3C/FFF?text=창업+경진대회', 'OPEN', 512, CURRENT_TIMESTAMP),
('기업 탐방 프로그램', '현장 견학 및 멘토링', '기업 현장을 방문하여 실무를 체험합니다', '취창업지원센터', 'RIS지원센터', '진로지도', '현장체험', '2024-12-10 00:00:00', '2025-01-10 23:59:59', 25, 20, 'https://placehold.co/560x360/F39C12/FFF?text=기업+탐방', 'OPEN', 367, CURRENT_TIMESTAMP),
('스타트업 창업 멘토링', '창업 전문가 1:1 상담', '창업 경험이 있는 선배의 조언을 받습니다', '취창업지원센터', '약학대학', '진로지도', '창업', '2024-12-01 00:00:00', '2024-12-31 23:59:59', 15, 12, 'https://placehold.co/560x360/16A085/FFF?text=창업+멘토링', 'OPEN', 289, CURRENT_TIMESTAMP);

-- 행정부서: 평생교육원 (5개)
INSERT INTO programs (title, description, content, department, college, category, sub_category, application_start_date, application_end_date, max_participants, current_participants, thumbnail_url, status, hits, created_at) VALUES
('자격증 취득 과정', '각종 자격증 준비반', '전산, 외국어 자격증 준비를 지원합니다', '평생교육원', 'RISE사업단', '학습역량', '자격증', '2024-12-01 00:00:00', '2025-02-28 23:59:59', 50, 38, 'https://placehold.co/560x360/2ECC71/FFF?text=자격증+취득', 'OPEN', 423, CURRENT_TIMESTAMP),
('직무능력 향상 과정', '실무 능력 강화', '직장에서 필요한 실무 스킬을 배웁니다', '평생교육원', '간호대학', '진로지도', '직무교육', '2024-12-05 00:00:00', '2025-01-31 23:59:59', 40, 25, 'https://placehold.co/560x360/3498DB/FFF?text=직무능력+향상', 'OPEN', 312, CURRENT_TIMESTAMP),
('코딩 부트캠프', '프로그래밍 기초부터 실전까지', 'Python, Java 프로그래밍을 배웁니다', '평생교육원', '기계ICT융합공학부', '학습역량', '코딩', '2024-12-10 00:00:00', '2025-02-10 23:59:59', 30, 28, 'https://placehold.co/560x360/9B59B6/FFF?text=코딩+부트캠프', 'OPEN', 589, CURRENT_TIMESTAMP),
('외국어 회화 수업', '영어, 중국어 회화', '실생활 회화를 집중적으로 학습합니다', '평생교육원', '교육대학원', '학습역량', '외국어', '2024-11-25 00:00:00', '2025-01-25 23:59:59', 25, 22, 'https://placehold.co/560x360/E67E22/FFF?text=외국어+회화', 'OPEN', 378, CURRENT_TIMESTAMP),
('재무설계 과정', '개인 재무 관리', '합리적인 재무 계획을 세우는 법을 배웁니다', '평생교육원', 'RIS지원센터', '기타', '생활지도', '2024-12-08 00:00:00', '2025-01-15 23:59:59', 35, 18, 'https://placehold.co/560x360/34495E/FFF?text=재무설계', 'OPEN', 267, CURRENT_TIMESTAMP);

-- 행정부서: 학생처 (6개)
INSERT INTO programs (title, description, content, department, college, category, sub_category, application_start_date, application_end_date, max_participants, current_participants, thumbnail_url, status, hits, created_at) VALUES
('신입생 오리엔테이션', '대학 생활 적응 프로그램', '대학 생활 전반을 안내합니다', '학생처', 'RISE사업단', '기타', '오리엔테이션', '2024-11-15 00:00:00', '2024-12-10 23:59:59', 200, 187, 'https://placehold.co/560x360/1ABC9C/FFF?text=신입생+OT', 'OPEN', 623, CURRENT_TIMESTAMP),
('동아리 박람회', '동아리 소개 및 가입', '다양한 동아리를 소개합니다', '학생처', '간호대학', '기타', '동아리', '2024-12-01 00:00:00', '2024-12-20 23:59:59', NULL, 234, 'https://placehold.co/560x360/E74C3C/FFF?text=동아리+박람회', 'OPEN', 512, CURRENT_TIMESTAMP),
('학생 리더십 캠프', '학생회 임원 역량 강화', '리더십과 조직 운영을 배웁니다', '학생처', '교육대학원', '기타', '리더십', '2024-12-15 00:00:00', '2025-01-05 23:59:59', 50, 42, 'https://placehold.co/560x360/F39C12/FFF?text=리더십+캠프', 'OPEN', 389, CURRENT_TIMESTAMP),
('봉사활동 프로그램', '지역사회 봉사 활동', '다양한 봉사 활동 기회를 제공합니다', '학생처', '기계ICT융합공학부', '기타', '봉사', '2024-11-20 00:00:00', '2025-02-28 23:59:59', NULL, 156, 'https://placehold.co/560x360/27AE60/FFF?text=봉사활동', 'OPEN', 445, CURRENT_TIMESTAMP),
('캠퍼스 문화 축제', '학교 축제 기획 및 참여', '학생 주도 문화 행사를 진행합니다', '학생처', 'RIS지원센터', '기타', '문화활동', '2024-12-05 00:00:00', '2024-12-22 23:59:59', NULL, 312, 'https://placehold.co/560x360/9B59B6/FFF?text=문화+축제', 'OPEN', 678, CURRENT_TIMESTAMP),
('학생 권리 구제', '학생 고충 상담', '학생의 권리를 보호하고 고충을 해결합니다', '학생처', '약학대학', '기타', '상담', '2024-11-01 00:00:00', '2025-03-31 23:59:59', NULL, 23, 'https://placehold.co/560x360/95A5A6/FFF?text=권리+구제', 'OPEN', 234, CURRENT_TIMESTAMP);

-- 행정부서: 학습역량강화사업단 (6개)
INSERT INTO programs (title, description, content, department, college, category, sub_category, application_start_date, application_end_date, max_participants, current_participants, thumbnail_url, status, hits, created_at) VALUES
('기초학력 진단평가', '학업 능력 진단 및 피드백', '기초학력을 진단하고 맞춤 학습을 제공합니다', '학습역량강화사업단', 'RISE사업단', '학습역량', '진단평가', '2024-11-20 00:00:00', '2024-12-15 23:59:59', 100, 89, 'https://placehold.co/560x360/3498DB/FFF?text=기초학력+진단', 'OPEN', 456, CURRENT_TIMESTAMP),
('학습 컨설팅', '1:1 학습 상담', '학습 전략을 개인별로 컨설팅합니다', '학습역량강화사업단', '간호대학', '학습역량', '컨설팅', '2024-12-01 00:00:00', '2025-01-31 23:59:59', 50, 38, 'https://placehold.co/560x360/16A085/FFF?text=학습+컨설팅', 'OPEN', 367, CURRENT_TIMESTAMP),
('튜터링 프로그램', '선배가 가르치는 학습 멘토링', '어려운 과목을 선배에게 배웁니다', '학습역량강화사업단', '교육대학원', '학습역량', '튜터링', '2024-12-05 00:00:00', '2025-02-28 23:59:59', 40, 35, 'https://placehold.co/560x360/E67E22/FFF?text=튜터링', 'OPEN', 512, CURRENT_TIMESTAMP),
('학점 UP 프로젝트', '학점 향상 집중 관리', '낮은 학점을 개선하기 위한 집중 프로그램', '학습역량강화사업단', '기계ICT융합공학부', '학습역량', '학점관리', '2024-12-08 00:00:00', '2025-01-20 23:59:59', 30, 27, 'https://placehold.co/560x360/E74C3C/FFF?text=학점UP', 'OPEN', 389, CURRENT_TIMESTAMP),
('독서 인증제', '독서 활동 장려', '독서를 하고 인증하면 마일리지를 적립합니다', '학습역량강화사업단', 'RIS지원센터', '학습역량', '독서', '2024-11-15 00:00:00', '2025-02-28 23:59:59', NULL, 234, 'https://placehold.co/560x360/27AE60/FFF?text=독서+인증제', 'OPEN', 534, CURRENT_TIMESTAMP),
('학습법 특강', '효율적인 공부 방법', '과목별 학습 전략을 배웁니다', '학습역량강화사업단', '약학대학', '학습역량', '학습법', '2024-12-10 00:00:00', '2025-01-15 23:59:59', 60, 48, 'https://placehold.co/560x360/9B59B6/FFF?text=학습법+특강', 'OPEN', 478, CURRENT_TIMESTAMP);
