package com.scms.app.config;

import com.scms.app.model.Program;
import com.scms.app.model.ProgramStatus;
import com.scms.app.model.User;
import com.scms.app.model.UserRole;
import com.scms.app.repository.ProgramRepository;
import com.scms.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 애플리케이션 시작 시 초기 데이터 로드
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataLoader {

    private final UserRepository userRepository;
    private final ProgramRepository programRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Bean
    public CommandLineRunner loadInitialData() {
        return args -> {
            // users 테이블이 비어있으면 초기 데이터 생성
            if (userRepository.count() == 0) {
                log.info("=================================================");
                log.info("초기 사용자 데이터를 생성합니다...");
                log.info("=================================================");
                createInitialUsers();
                log.info("=================================================");
                log.info("초기 사용자 데이터 생성 완료!");
                log.info("=================================================");
            } else {
                log.info("기존 사용자 데이터가 있습니다. 초기 데이터 생성을 건너뜁니다.");
            }

            // programs 테이블이 비어있으면 샘플 데이터 생성
            if (programRepository.count() == 0) {
                log.info("=================================================");
                log.info("샘플 프로그램 데이터를 생성합니다...");
                log.info("=================================================");
                createSamplePrograms();
                log.info("=================================================");
                log.info("샘플 프로그램 데이터 8개 생성 완료!");
                log.info("=================================================");
            } else {
                log.info("기존 프로그램 데이터가 있습니다. 샘플 데이터 생성을 건너뜁니다.");
            }
        };
    }

    private void createInitialUsers() {
        // 학생 계정 생성
        // 초기 비밀번호는 생년월일 6자리 (yyMMdd)

        createUser(2024001, "김철수", "kim.cs@example.com", "010-1234-5678",
                LocalDate.of(2003, 1, 1), "컴퓨터공학과", 1, UserRole.STUDENT);

        createUser(2024002, "이영희", "lee.yh@example.com", "010-2345-6789",
                LocalDate.of(2004, 2, 15), "소프트웨어학과", 2, UserRole.STUDENT);

        createUser(2023001, "박민수", "park.ms@example.com", "010-3456-7890",
                LocalDate.of(2002, 3, 10), "정보보안학과", 2, UserRole.STUDENT);

        createUser(2023002, "최지은", "choi.je@example.com", "010-4567-8901",
                LocalDate.of(2002, 5, 5), "컴퓨터공학과", 2, UserRole.STUDENT);

        createUser(2022001, "정우진", "jung.wj@example.com", "010-5678-9012",
                LocalDate.of(2001, 6, 20), "인공지능학과", 3, UserRole.STUDENT);

        createUser(2022002, "강민지", "kang.mj@example.com", "010-6789-0123",
                LocalDate.of(2001, 8, 15), "데이터사이언스학과", 3, UserRole.STUDENT);

        createUser(2021001, "윤서준", "yoon.sj@example.com", "010-7890-1234",
                LocalDate.of(2000, 9, 11), "컴퓨터공학과", 4, UserRole.STUDENT);

        createUser(2021002, "임하늘", "im.hn@example.com", "010-8901-2345",
                LocalDate.of(2000, 12, 25), "소프트웨어학과", 4, UserRole.STUDENT);

        // 관리자 계정 생성
        // 학번: 9999999, 비밀번호: "admin123"
        User admin = User.builder()
                .studentNum(9999999)
                .name("시스템관리자")
                .email("admin@pureum.ac.kr")
                .phone("010-0000-0000")
                .password(passwordEncoder.encode("admin123"))
                .birthDate(LocalDate.of(1990, 1, 1))
                .department("IT관리팀")
                .role(UserRole.ADMIN)
                .locked(false)
                .failCnt(0)
                .build();
        userRepository.save(admin);
        log.info("✅ 관리자 계정 생성: {} (학번: {}, 비밀번호: admin123)", admin.getName(), admin.getStudentNum());
    }

    /**
     * 사용자 생성 헬퍼 메서드
     * 초기 비밀번호는 생년월일 6자리 (yyMMdd)
     */
    private void createUser(int studentNum, String name, String email, String phone,
                            LocalDate birthDate, String department, int grade, UserRole role) {
        // 생년월일에서 6자리 비밀번호 생성 (yyMMdd)
        String initialPassword = String.format("%02d%02d%02d",
                birthDate.getYear() % 100,
                birthDate.getMonthValue(),
                birthDate.getDayOfMonth());

        User user = User.builder()
                .studentNum(studentNum)
                .name(name)
                .email(email)
                .phone(phone)
                .password(passwordEncoder.encode(initialPassword))
                .birthDate(birthDate)
                .department(department)
                .grade(grade)
                .role(role)
                .locked(false)
                .failCnt(0)
                .build();

        userRepository.save(user);
        log.info("✅ 학생 계정 생성: {} (학번: {}, 초기 비밀번호: {})", name, studentNum, initialPassword);
    }

    /**
     * 샘플 프로그램 데이터 생성
     */
    private void createSamplePrograms() {
        LocalDateTime now = LocalDateTime.now();

        // 1. 입박 - 내일 마감
        createProgram(
            "[진전] 전공설계 포트폴리오 특강",
            "(4주차)나만의 마스터 입사지원서 제작과 면접준비",
            "전공설계 포트폴리오 제작 특강 4주차 과정입니다.",
            "취창업지원센터",
            "공과대학",
            "진로지도",
            "(4주차)나만의 마스터 입사지원서 제작과 면접준비",
            now.minusDays(23),
            now.plusDays(1),
            null, 0, 160, ProgramStatus.OPEN
        );

        // 2. D-3
        createProgram(
            "2025-2 나스너(WOW-더플 대인관계능력 향상 프로그램)",
            "7회기",
            "대인관계능력 향상을 위한 집단 프로그램입니다.",
            "(진전)학생상담센터",
            "전체",
            "인성역량",
            "7회기",
            now.minusDays(63),
            now.plusDays(3),
            null, 6, 172, ProgramStatus.OPEN
        );

        // 3. D-4
        createProgram(
            "2025년 2학기 면접스피치&이미지메이킹 과정",
            "면접스피치&이미지메이킹 2회차",
            "취업을 위한 면접 준비 및 이미지 메이킹 과정입니다.",
            "대학일자리본부 대학일자리본...",
            "전체",
            "취업역량",
            "면접스피치&이미지메이킹 2회차",
            now.minusDays(39),
            now.plusDays(4),
            55, 20, 198, ProgramStatus.OPEN
        );

        // 4. D-4
        createProgram(
            "동기역량강화 집단 학습컨설팅(2025-2학기)",
            "동기역량강화 집단 학습컨설팅(2025-2학기)",
            "학습동기 향상을 위한 집단 컨설팅 프로그램입니다.",
            "교수학습지원센터",
            "전체",
            "학습역량",
            "동기역량강화 집단 학습컨설팅(2025-2학기)",
            now.minusDays(4),
            now.plusDays(4),
            8, 5, 31, ProgramStatus.OPEN
        );

        // 5. 마감
        createProgram(
            "세대공감 '인생책 함께읽기'",
            "세대공감 '인생책 함께읽기'",
            "세대 간 소통을 위한 독서 프로그램입니다.",
            "학생복지센터 학생복지센터 - ...",
            "전체",
            "인성역량",
            "세대공감 '인생책 함께읽기'",
            now.minusDays(11),
            now.minusDays(1),
            null, 15, 94, ProgramStatus.CLOSED
        );

        // 6. 모집완료
        createProgram(
            "우리 몸의 닮 조절을 위한 인슐린 작용 이해",
            "우리 몸의 닮 조절을 위한 인슐린 작용 이해",
            "인슐린 작용 메커니즘에 대한 전공 심화 프로그램입니다.",
            "약학대학 약학과(2+4) - 약학과...",
            "약학대학",
            "전공심화",
            "우리 몸의 닮 조절을 위한 인슐린 작용 이해",
            now.minusDays(37),
            now.plusDays(21),
            30, 30, 90, ProgramStatus.FULL
        );

        // 7. D-24
        createProgram(
            "2025학년도 2학기 우석챔프 시즌제 장학금",
            "2025학년도 2학기 우석챔프 시즌제 장학금",
            "비교과 활동 참여 시 장학금을 지급하는 프로그램입니다.",
            "역량개발인증센터 역량개발인...",
            "전체",
            "마일리지",
            "2025학년도 2학기 우석챔프 시즌제 장학금",
            now.minusDays(60),
            now.plusDays(24),
            null, 160, 713, ProgramStatus.OPEN
        );

        // 8. D-35
        createProgram(
            "[전주] 2025학년도 1학기 찾아가는 Academic Advising",
            "[전주] 2025학년도 1학기 찾아가는 Academic Advising",
            "전공 선택 및 학업 설계를 위한 상담 프로그램입니다.",
            "전공설계지원센터",
            "전체",
            "학습역량",
            "[전주] 2025학년도 1학기 찾아가는 Academic Advising",
            now.minusDays(246),
            now.plusDays(35),
            null, 432, 398, ProgramStatus.OPEN
        );
    }

    /**
     * 프로그램 생성 헬퍼 메서드
     */
    private void createProgram(String title, String description, String content,
                               String department, String college, String category, String subCategory,
                               LocalDateTime applicationStartDate, LocalDateTime applicationEndDate,
                               Integer maxParticipants, int currentParticipants, int hits,
                               ProgramStatus status) {
        Program program = Program.builder()
            .title(title)
            .description(description)
            .content(content)
            .department(department)
            .college(college)
            .category(category)
            .subCategory(subCategory)
            .applicationStartDate(applicationStartDate)
            .applicationEndDate(applicationEndDate)
            .maxParticipants(maxParticipants)
            .currentParticipants(currentParticipants)
            .hits(hits)
            .status(status)
            .build();

        programRepository.save(program);
        log.info("✅ 프로그램 생성: {} (신청 마감: {})", title, applicationEndDate.toLocalDate());
    }
}
