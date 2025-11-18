package com.scms.app;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashTest {

    @Test
    public void generateAdminPasswordHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String plainPassword = "admin123";
        String hashedPassword = encoder.encode(plainPassword);

        System.out.println("=".repeat(80));
        System.out.println("평문 비밀번호: " + plainPassword);
        System.out.println("BCrypt 해시: " + hashedPassword);
        System.out.println("=".repeat(80));

        // 검증
        boolean matches = encoder.matches(plainPassword, hashedPassword);
        System.out.println("검증 결과: " + matches);

        // schema.sql의 기존 해시와 비교
        String existingHash = "$2a$10$VQbrqrzS8Zx.XWlM2ZzS0OqfUTvCc9hR3JfXo2rUzQR1Cy.aCiP.O";
        boolean matchesExisting = encoder.matches(plainPassword, existingHash);
        System.out.println("기존 해시와 매칭: " + matchesExisting);
        System.out.println("=".repeat(80));
    }

    @Test
    public void generateCounselorPasswordHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String plainPassword = "counselor123";
        String hashedPassword = encoder.encode(plainPassword);

        System.out.println("=".repeat(80));
        System.out.println("평문 비밀번호: " + plainPassword);
        System.out.println("BCrypt 해시: " + hashedPassword);
        System.out.println("=".repeat(80));

        // 검증
        boolean matches = encoder.matches(plainPassword, hashedPassword);
        System.out.println("검증 결과: " + matches);
        System.out.println("=".repeat(80));
    }
}
