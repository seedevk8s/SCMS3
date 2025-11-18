package com.scms.app.config;

import com.scms.app.model.*;
import com.scms.app.repository.CounselorRepository;
import com.scms.app.repository.NotificationRepository;
import com.scms.app.repository.ProgramApplicationRepository;
import com.scms.app.repository.ProgramRepository;
import com.scms.app.repository.ProgramReviewRepository;
import com.scms.app.repository.UserRepository;
import com.scms.app.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 애플리케이션 시작 시 초기 데이터를 로드하는 클래스
 *
 * 동작:
 * 1. 사용자 데이터 생성 (학생 8명 + 관리자 1명)
 * 2. 프로그램 데이터 생성 (50개)
 * 3. 프로그램 신청 데이터 생성 (다양한 상태의 테스트 신청)
 * 4. 프로그램 후기 데이터 생성 (참여 완료자의 테스트 후기)
 *
 * 주의: 초기 데이터 로드 후에는 @Component를 주석처리하여 비활성화하세요.
 * (재시작 시 데이터가 중복 생성되는 것을 방지하기 위함)
 */
@Component
@org.springframework.core.annotation.Order(2)  // DatabaseMigration 이후 실행
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final ProgramRepository programRepository;
    private final UserRepository userRepository;
    private final CounselorRepository counselorRepository;
    private final ProgramApplicationRepository applicationRepository;
    private final ProgramReviewRepository reviewRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 1. 사용자 데이터 초기화
        initializeUsers();

        // 2. 프로그램 데이터 초기화
        initializePrograms();

        // 3. 테스트 신청 데이터 초기화
        initializeTestApplications();

        // 4. 테스트 후기 데이터 초기화
        initializeTestReviews();

        // 5. 테스트 알림 데이터 초기화
        initializeTestNotifications();
    }

    /**
     * 초기 사용자 데이터 생성
     * - 학생 8명 (1~4학년 각 2명)
     * - 관리자 1명
     */
    private void initializeUsers() {
        long userCount = userRepository.count();
        
        if (userCount > 0) {
            log.info("사용자 데이터가 이미 존재합니다 ({}명). 초기화를 건너뜁니다.", userCount);
            return;
        }

        log.info("초기 사용자 데이터를 생성합니다...");

        try {
            // 학생 데이터 생성 (8명)
            createStudent(2024001, "김철수", "chulsoo.kim@scms.ac.kr", "010-1234-5601", LocalDate.of(2003, 1, 1), "컴퓨터공학과", 1);
            createStudent(2024002, "이영희", "younghee.lee@scms.ac.kr", "010-1234-5602", LocalDate.of(2004, 2, 15), "경영학과", 1);
            createStudent(2023001, "박민수", "minsu.park@scms.ac.kr", "010-1234-5603", LocalDate.of(2002, 3, 10), "전자공학과", 2);
            createStudent(2023002, "최지은", "jieun.choi@scms.ac.kr", "010-1234-5604", LocalDate.of(2001, 8, 25), "영어영문학과", 2);
            createStudent(2022001, "정우진", "woojin.jung@scms.ac.kr", "010-1234-5605", LocalDate.of(2001, 6, 20), "기계공학과", 3);
            createStudent(2022002, "강하늘", "haneul.kang@scms.ac.kr", "010-1234-5606", LocalDate.of(1999, 11, 5), "화학공학과", 3);
            createStudent(2021001, "윤서현", "seohyun.yoon@scms.ac.kr", "010-1234-5607", LocalDate.of(2000, 4, 12), "간호학과", 4);
            createStudent(2021002, "임도윤", "doyun.lim@scms.ac.kr", "010-1234-5608", LocalDate.of(1999, 2, 28), "건축학과", 4);

            // 관리자 계정 생성
            createAdmin();

            // 상담사 계정 생성
            createCounselors();

            long afterCount = userRepository.count();
            log.info("✅ 초기 사용자 데이터 생성 완료: {}명", afterCount);

        } catch (Exception e) {
            log.error("초기 사용자 데이터 생성 중 오류 발생", e);
        }
    }

    /**
     * 학생 계정 생성
     */
    private void createStudent(int studentNum, String name, String email, String phone, 
                               LocalDate birthDate, String department, int grade) {
        // 초기 비밀번호: 생년월일 6자리 (YYMMDD)
        String rawPassword = String.format("%02d%02d%02d", 
            birthDate.getYear() % 100, 
            birthDate.getMonthValue(), 
            birthDate.getDayOfMonth());
        
        User student = User.builder()
                .studentNum(studentNum)
                .name(name)
                .email(email)
                .phone(phone)
                .password(passwordEncoder.encode(rawPassword))
                .birthDate(birthDate)
                .department(department)
                .grade(grade)
                .role(UserRole.STUDENT)
                .locked(false)
                .failCnt(0)
                .build();

        userRepository.save(student);
        log.info("학생 계정 생성: {} (학번: {}, 초기 비밀번호: {})", name, studentNum, rawPassword);
    }

    /**
     * 관리자 계정 생성
     */
    private void createAdmin() {
        User admin = User.builder()
                .studentNum(9999999)
                .name("관리자")
                .email("admin@scms.ac.kr")
                .phone("010-0000-0000")
                .password(passwordEncoder.encode("admin123"))
                .birthDate(LocalDate.of(1990, 1, 1))
                .department("행정부서")
                .grade(null)
                .role(UserRole.ADMIN)
                .locked(false)
                .failCnt(0)
                .build();

        userRepository.save(admin);
        log.info("관리자 계정 생성: 학번 9999999, 비밀번호: admin123");
    }

    /**
     * 상담사 계정 생성
     */
    private void createCounselors() {
        // 상담사 1: 김상담
        User counselor1 = User.builder()
                .studentNum(8000001)
                .name("김상담")
                .email("counselor1@pureum.ac.kr")
                .phone("010-1111-2222")
                .password(passwordEncoder.encode("counselor123"))
                .birthDate(LocalDate.of(1985, 3, 15))
                .department("학생상담센터")
                .grade(null)
                .role(UserRole.COUNSELOR)
                .locked(false)
                .failCnt(0)
                .build();

        User savedCounselor1 = userRepository.save(counselor1);

        // 상담사 1 프로필 생성
        Counselor counselorProfile1 = Counselor.builder()
                .counselorId(savedCounselor1.getUserId())
                .user(savedCounselor1)
                .specialty("진로상담, 학업상담")
                .introduction("전문상담사 2급 자격을 보유하고 있으며, 학생들의 진로와 학업 고민을 함께 해결합니다.")
                .build();

        counselorRepository.save(counselorProfile1);
        log.info("상담사 계정 생성: 김상담 (학번: 8000001, 비밀번호: counselor123)");

        // 상담사 2: 이상담
        User counselor2 = User.builder()
                .studentNum(8000002)
                .name("이상담")
                .email("counselor2@pureum.ac.kr")
                .phone("010-3333-4444")
                .password(passwordEncoder.encode("counselor123"))
                .birthDate(LocalDate.of(1988, 7, 20))
                .department("학생상담센터")
                .grade(null)
                .role(UserRole.COUNSELOR)
                .locked(false)
                .failCnt(0)
                .build();

        User savedCounselor2 = userRepository.save(counselor2);

        // 상담사 2 프로필 생성
        Counselor counselorProfile2 = Counselor.builder()
                .counselorId(savedCounselor2.getUserId())
                .user(savedCounselor2)
                .specialty("심리상담, 대인관계")
                .introduction("임상심리사 2급 자격을 보유하고 있으며, 심리 및 대인관계 상담을 전문으로 합니다.")
                .build();

        counselorRepository.save(counselorProfile2);
        log.info("상담사 계정 생성: 이상담 (학번: 8000002, 비밀번호: counselor123)");
    }

    /**
     * 초기 프로그램 데이터 생성
     */
    private void initializePrograms() {
        long count = programRepository.count();

        // 정확히 50개이고 새로운 다양한 상태의 샘플 데이터가 있으면 초기화 완료로 간주
        if (count == 50) {
            boolean hasUpdatedData = programRepository.findAll().stream()
                    .anyMatch(p -> p.getStatus() != null &&
                                   "OPEN".equals(p.getStatus().name()) &&
                                   p.getApplicationStartDate() != null &&
                                   p.getApplicationStartDate().getYear() == 2025);

            if (hasUpdatedData) {
                log.info("업데이트된 샘플 프로그램 데이터 50개가 이미 로드되어 있습니다. 초기화를 건너뜁니다.");
                return;
            }
        }

        // 기존 데이터 모두 삭제
        if (count > 0) {
            log.warn("기존 프로그램 데이터 {}개를 삭제하고 새로운 샘플 데이터로 초기화합니다...", count);
            programRepository.deleteAll();
            jdbcTemplate.execute("ALTER TABLE programs AUTO_INCREMENT = 1");
            log.info("기존 프로그램 데이터 삭제 완료");
        }

        log.info("초기 프로그램 데이터 50개를 로드합니다...");

        try {
            // data.sql 파일 읽기
            ClassPathResource resource = new ClassPathResource("data.sql");
            String sql = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))
                .lines()
                .filter(line -> !line.trim().startsWith("--"))
                .filter(line -> !line.trim().isEmpty())
                .collect(Collectors.joining("\n"));

            // SQL을 개별 INSERT 문으로 분리
            String[] statements = sql.split(";");

            int insertCount = 0;
            for (String statement : statements) {
                String trimmed = statement.trim();
                if (!trimmed.isEmpty()) {
                    try {
                        jdbcTemplate.execute(trimmed);
                        insertCount++;
                    } catch (Exception e) {
                        log.error("SQL 실행 실패: {}", e.getMessage());
                    }
                }
            }

            long afterCount = programRepository.count();
            log.info("✅ 초기 프로그램 데이터 로드 완료: {}개 INSERT 문 실행, {}개 프로그램 생성됨", insertCount, afterCount);

        } catch (Exception e) {
            log.error("초기 프로그램 데이터 로드 중 오류 발생", e);
        }
    }

    /**
     * 테스트용 프로그램 신청 데이터 생성
     * - 관리자 기능 테스트를 위한 다양한 상태의 신청 데이터 생성
     */
    private void initializeTestApplications() {
        long count = applicationRepository.count();

        if (count > 0) {
            log.info("프로그램 신청 데이터가 이미 존재합니다 ({}건). 초기화를 건너뜁니다.", count);
            return;
        }

        log.info("테스트용 프로그램 신청 데이터를 생성합니다...");

        try {
            // 첫 번째 OPEN 프로그램 찾기
            List<Program> openPrograms = programRepository.findAll().stream()
                    .filter(p -> p.getStatus() == ProgramStatus.OPEN)
                    .limit(3)
                    .collect(Collectors.toList());

            if (openPrograms.isEmpty()) {
                log.warn("OPEN 상태의 프로그램이 없어서 신청 데이터를 생성하지 않습니다.");
                return;
            }

            // 모든 학생 계정 조회
            List<User> students = userRepository.findAll().stream()
                    .filter(u -> u.getRole() == UserRole.STUDENT)
                    .collect(Collectors.toList());

            if (students.size() < 8) {
                log.warn("학생 계정이 부족하여 신청 데이터를 생성하지 않습니다.");
                return;
            }

            // 첫 번째 프로그램에 다양한 상태의 신청 생성
            Program program1 = openPrograms.get(0);

            // PENDING (대기 중) 신청 3건
            createApplication(program1, students.get(0), ApplicationStatus.PENDING, null);
            createApplication(program1, students.get(1), ApplicationStatus.PENDING, null);
            createApplication(program1, students.get(2), ApplicationStatus.PENDING, null);

            // APPROVED (승인됨) 신청 2건
            createApplication(program1, students.get(3), ApplicationStatus.APPROVED, null);
            createApplication(program1, students.get(4), ApplicationStatus.APPROVED, null);

            // REJECTED (거부됨) 신청 1건
            createApplication(program1, students.get(5), ApplicationStatus.REJECTED, "정원 초과로 인한 거부");

            // CANCELLED (취소됨) 신청 1건
            createApplication(program1, students.get(6), ApplicationStatus.CANCELLED, null);

            // COMPLETED (참여 완료) 신청 1건
            createApplication(program1, students.get(7), ApplicationStatus.COMPLETED, null);

            // 두 번째 프로그램에 신청 몇 건 추가
            if (openPrograms.size() > 1) {
                Program program2 = openPrograms.get(1);
                createApplication(program2, students.get(0), ApplicationStatus.PENDING, null);
                createApplication(program2, students.get(1), ApplicationStatus.COMPLETED, null); // 이영희 - 후기 작성 가능
            }

            // 세 번째 프로그램에 신청 몇 건 추가
            if (openPrograms.size() > 2) {
                Program program3 = openPrograms.get(2);
                createApplication(program3, students.get(0), ApplicationStatus.PENDING, null);
            }

            long afterCount = applicationRepository.count();
            log.info("✅ 테스트 신청 데이터 생성 완료: {}건", afterCount);
            log.info("📊 첫 번째 프로그램(ID: {})에 8건의 다양한 상태 신청 생성됨", program1.getProgramId());

        } catch (Exception e) {
            log.error("테스트 신청 데이터 생성 중 오류 발생", e);
        }
    }

    /**
     * 프로그램 신청 생성
     */
    private void createApplication(Program program, User user, ApplicationStatus status, String rejectionReason) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime appliedAt = now.minusDays(10); // 10일 전 신청

        ProgramApplication application = ProgramApplication.builder()
                .program(program)
                .user(user)
                .status(status)
                .appliedAt(appliedAt)
                .build();

        // 상태에 따라 추가 필드 설정
        switch (status) {
            case APPROVED:
                application.setApprovedAt(appliedAt.plusDays(1));
                break;
            case REJECTED:
                application.setRejectedAt(appliedAt.plusDays(1));
                application.setRejectionReason(rejectionReason);
                break;
            case CANCELLED:
                application.setCancelledAt(appliedAt.plusDays(2));
                break;
            case COMPLETED:
                application.setApprovedAt(appliedAt.plusDays(1));
                application.setCompletedAt(appliedAt.plusDays(8));
                break;
        }

        applicationRepository.save(application);
        log.debug("신청 생성: {} - {} ({})", user.getName(), program.getTitle(), status);
    }

    /**
     * 테스트용 프로그램 후기 데이터 생성
     * - 참여 완료(COMPLETED) 상태의 신청에 대해 다양한 평점의 후기 생성
     */
    private void initializeTestReviews() {
        long count = reviewRepository.count();

        if (count > 0) {
            log.info("프로그램 후기 데이터가 이미 존재합니다 ({}건). 초기화를 건너뜁니다.", count);
            return;
        }

        log.info("테스트용 프로그램 후기 데이터를 생성합니다...");

        try {
            // COMPLETED 상태의 신청 조회 (JOIN FETCH로 User와 Program 함께 로드)
            List<ProgramApplication> completedApplications =
                    applicationRepository.findByStatus(ApplicationStatus.COMPLETED);

            if (completedApplications.isEmpty()) {
                log.warn("COMPLETED 상태의 신청이 없어서 후기 데이터를 생성하지 않습니다.");
                return;
            }

            // 샘플 후기 내용
            String[] reviewContents = {
                "정말 유익한 프로그램이었습니다! 많은 것을 배울 수 있었고, 실무 경험도 쌓을 수 있어서 좋았습니다. 담당 멘토님도 친절하게 잘 가르쳐주셨고, 함께 참여한 동료들과도 좋은 관계를 맺을 수 있었습니다.",
                "기대했던 것보다 더 좋은 프로그램이었어요. 특히 실습 위주로 진행되어서 이해하기 쉬웠습니다. 다음에도 이런 기회가 있다면 꼭 참여하고 싶습니다!",
                "전반적으로 만족스러운 프로그램이었습니다. 다만 시간이 조금 짧아서 아쉬웠어요. 더 깊이 있는 내용을 다루면 좋을 것 같습니다.",
                "프로그램 내용은 좋았지만, 일정이 너무 빡빡해서 따라가기 힘들었습니다. 좀 더 여유있는 스케줄로 진행되면 더 좋을 것 같아요.",
                "기본적인 내용 위주로 진행되어 이미 관련 지식이 있는 사람에게는 다소 쉬울 수 있습니다. 하지만 초보자에게는 좋은 입문 기회가 될 것 같습니다.",
                "매우 만족합니다! 프로그램 구성도 체계적이고, 강사님의 설명도 명확했습니다. 실제로 프로젝트에 바로 적용할 수 있는 내용들이 많았어요.",
                "좋은 경험이었습니다. 특히 네트워킹 기회가 많아서 좋았고, 같은 관심사를 가진 사람들을 만날 수 있어서 의미있었습니다.",
                "프로그램 자체는 괜찮았으나, 준비물이나 사전 안내가 부족했던 점은 아쉬웠습니다. 그래도 전반적으로는 만족스러운 경험이었습니다."
            };

            int[] ratings = {5, 5, 4, 3, 3, 5, 4, 4};

            int reviewCount = 0;
            for (int i = 0; i < Math.min(completedApplications.size(), reviewContents.length); i++) {
                ProgramApplication application = completedApplications.get(i);
                createReview(application.getProgram(), application.getUser(),
                           ratings[i], reviewContents[i]);
                reviewCount++;
            }

            long afterCount = reviewRepository.count();
            log.info("✅ 테스트 후기 데이터 생성 완료: {}건", afterCount);
            log.info("📝 평균 평점: {}/5", String.format("%.1f",
                (double) (ratings[0] + ratings[1] + ratings[2] + ratings[3] + ratings[4] + ratings[5] + ratings[6] + ratings[7]) / Math.min(reviewCount, 8)));

        } catch (Exception e) {
            log.error("테스트 후기 데이터 생성 중 오류 발생", e);
        }
    }

    /**
     * 프로그램 후기 생성
     */
    private void createReview(Program program, User user, int rating, String content) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime createdAt = now.minusDays(5); // 5일 전 작성

        ProgramReview review = ProgramReview.builder()
                .program(program)
                .user(user)
                .rating(rating)
                .content(content)
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build();

        reviewRepository.save(review);
        log.debug("후기 생성: {} - {} ({}점)", user.getName(), program.getTitle(), rating);
    }

    /**
     * 테스트용 알림 데이터 생성
     * - 다양한 타입의 알림 생성
     */
    private void initializeTestNotifications() {
        long count = notificationRepository.count();

        if (count > 0) {
            log.info("알림 데이터가 이미 존재합니다 ({}건). 초기화를 건너뜁니다.", count);
            return;
        }

        log.info("테스트용 알림 데이터를 생성합니다...");

        try {
            // 모든 학생 계정 조회
            List<User> students = userRepository.findAll().stream()
                    .filter(u -> u.getRole() == UserRole.STUDENT)
                    .collect(Collectors.toList());

            if (students.isEmpty()) {
                log.warn("학생 계정이 없어서 알림 데이터를 생성하지 않습니다.");
                return;
            }

            // 첫 번째 OPEN 프로그램 찾기
            Program program = programRepository.findAll().stream()
                    .filter(p -> p.getStatus() == ProgramStatus.OPEN)
                    .findFirst()
                    .orElse(null);

            if (program == null) {
                log.warn("OPEN 상태의 프로그램이 없어서 알림 데이터를 생성하지 않습니다.");
                return;
            }

            // 첫 번째 학생(김철수)에게 다양한 알림 생성
            User student1 = students.get(0);
            String programUrl = "/programs/" + program.getProgramId();

            // 1. 신청 승인 알림 (읽지 않음)
            notificationService.createNotificationByType(
                    student1.getUserId(),
                    NotificationType.APPLICATION_APPROVED,
                    program.getTitle(),
                    programUrl
            );

            // 2. 마감 임박 알림 (읽음)
            Notification deadlineNotif = notificationService.createNotificationByType(
                    student1.getUserId(),
                    NotificationType.DEADLINE_APPROACHING,
                    program.getTitle(),
                    programUrl
            );
            deadlineNotif.markAsRead();
            notificationRepository.save(deadlineNotif);

            // 두 번째 학생(이영희)에게 알림 생성
            if (students.size() > 1) {
                User student2 = students.get(1);

                // 신청 거부 알림
                String content = "'" + program.getTitle() + "' 프로그램 신청이 거부되었습니다.\n사유: 정원 초과";
                notificationService.createNotification(
                        student2.getUserId(),
                        NotificationType.APPLICATION_REJECTED.getTitle(),
                        content,
                        NotificationType.APPLICATION_REJECTED,
                        programUrl
                );
            }

            // 세 번째 학생(박민수)에게 알림 생성
            if (students.size() > 2) {
                User student3 = students.get(2);

                // 신청 취소 알림 (읽음)
                Notification cancelNotif = notificationService.createNotificationByType(
                        student3.getUserId(),
                        NotificationType.APPLICATION_CANCELLED,
                        program.getTitle(),
                        programUrl
                );
                cancelNotif.markAsRead();
                notificationRepository.save(cancelNotif);
            }

            long afterCount = notificationRepository.count();
            log.info("✅ 테스트 알림 데이터 생성 완료: {}건", afterCount);
            log.info("📬 첫 번째 학생({})에게 {}건의 알림 생성됨 (읽지 않음: 1건, 읽음: 1건)",
                    student1.getName(), 2);

        } catch (Exception e) {
            log.error("테스트 알림 데이터 생성 중 오류 발생", e);
        }
    }
}
