package com.scms.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

/**
 * Spring Security 설정
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * SecurityContext를 HttpSession에 저장하기 위한 Repository
     */
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    /**
     * Security Filter Chain 설정
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (개발 단계, 추후 활성화 필요)
                .csrf(csrf -> csrf.disable())

                // SecurityContext를 세션에 저장하도록 설정
                .securityContext(context -> context
                        .securityContextRepository(securityContextRepository())
                )

                // 요청 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 인증 없이 접근 가능한 경로
                        .requestMatchers(
                                "/",
                                "/login",
                                "/password/**",
                                "/help",
                                "/api/auth/login",
                                "/api/auth/logout",
                                "/api/auth/password/change",
                                "/api/auth/password/reset",
                                "/api/auth/me",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/webjars/**",
                                "/error"
                        ).permitAll()

                        // 관리자 전용 경로
                        .requestMatchers("/api/users/**").hasRole("ADMIN")

                        // 나머지는 인증된 사용자만 접근 가능
                        .anyRequest().authenticated()
                )

                // 폼 로그인 비활성화 (커스텀 REST API 로그인 사용)
                .formLogin(form -> form.disable())

                // HTTP Basic 인증 비활성화
                .httpBasic(basic -> basic.disable())

                // 예외 처리 - 인증 실패 시 로그인 페이지로 리다이렉트
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            // AJAX 요청인지 확인
                            String ajaxHeader = request.getHeader("X-Requested-With");
                            if ("XMLHttpRequest".equals(ajaxHeader)) {
                                // AJAX 요청이면 401 반환
                                response.sendError(401, "Unauthorized");
                            } else {
                                // 일반 요청이면 로그인 페이지로 리다이렉트
                                response.sendRedirect("/login");
                            }
                        })
                )

                // 로그아웃 설정
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .clearAuthentication(true)
                        .permitAll()
                );

        return http.build();
    }

    /**
     * Password Encoder Bean
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
