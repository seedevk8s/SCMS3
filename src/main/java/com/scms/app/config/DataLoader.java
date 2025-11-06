package com.scms.app.config;

import com.scms.app.model.User;
import com.scms.app.model.UserRole;
import com.scms.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;

/**
 * 애플리케이션 시작 시 초기 데이터 로드
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataLoader {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Bean
    public CommandLineRunner loadInitialData() {
        return args -> {
            // users 테이블이 비어있으면 초기 데이터 생성
            if (userRepository.count() == 0) {
                log.info("초기 사용자 데이터를 생성합니다...");
                createInitialUsers();
                log.info("초기 사용자 데이터 생성 완료!");
            } else {
                log.info("기존 사용자 데이터가 있습니다. 초기 데이터 생성을 건너뜁니다.");
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
        log.info("관리자 계정 생성: {} (학번: {})", admin.getName(), admin.getStudentNum());
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
        log.info("사용자 생성: {} (학번: {}, 초기 비밀번호: {})", name, studentNum, initialPassword);
    }
}
