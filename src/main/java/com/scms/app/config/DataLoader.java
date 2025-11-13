package com.scms.app.config;

import com.scms.app.repository.ProgramRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * 애플리케이션 시작 시 초기 데이터를 로드하는 클래스
 *
 * 동작:
 * 1. 샘플 데이터 50개가 이미 있으면 → 건너뜀
 * 2. 기존 데이터가 있으면 → 모두 삭제 후 새 샘플 데이터 50개 삽입
 * 3. 데이터가 없으면 → 샘플 데이터 50개 삽입
 *
 * 이렇게 하면 필터링이 안 되는 구 데이터를 깨끗하게 정리하고
 * 모든 필터 옵션에 맞는 새 데이터로 초기화됩니다.
 *
 * 주의: 초기 데이터 로드 후에는 @Component를 주석처리하여 비활성화하세요.
 * (재시작 시 데이터가 삭제되는 것을 방지하기 위함)
 */
@Component  // 썸네일 URL 추가를 위해 재활성화
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final ProgramRepository programRepository;

    @Override
    public void run(String... args) throws Exception {
        long count = programRepository.count();

        // 정확히 50개이고 썸네일이 있는 샘플 데이터가 있으면 초기화 완료로 간주
        if (count == 50) {
            // 샘플 데이터 중 하나가 존재하고 thumbnailUrl이 있는지 확인
            boolean hasSampleDataWithThumbnail = programRepository.findAll().stream()
                    .anyMatch(p -> ("학습전략 워크샵".equals(p.getTitle()) ||
                                   "취업 특강 시리즈".equals(p.getTitle())) &&
                                   p.getThumbnailUrl() != null && !p.getThumbnailUrl().isEmpty());

            if (hasSampleDataWithThumbnail) {
                log.info("샘플 데이터 50개가 이미 로드되어 있습니다. 초기화를 건너뜁니다.");
                return;
            } else {
                log.warn("기존 데이터에 썸네일 URL이 없습니다. 데이터를 새로 초기화합니다.");
            }
        }

        // 기존 데이터 모두 삭제 (필터링 안 되는 구 데이터 제거)
        if (count > 0) {
            log.warn("기존 프로그램 데이터 {}개를 삭제하고 새로운 샘플 데이터로 초기화합니다...", count);
            programRepository.deleteAll();
            jdbcTemplate.execute("ALTER TABLE programs AUTO_INCREMENT = 1");
            log.info("기존 데이터 삭제 완료");
        }

        log.info("초기 프로그램 데이터 50개를 로드합니다...");

        try {
            // data.sql 파일 읽기 (주석 제거)
            ClassPathResource resource = new ClassPathResource("data.sql");
            String sql = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))
                .lines()
                .filter(line -> !line.trim().startsWith("--"))  // 주석 라인 제거
                .filter(line -> !line.trim().isEmpty())         // 빈 라인 제거
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
                        log.debug("SQL 실행 성공 ({}번째)", insertCount);
                    } catch (Exception e) {
                        log.error("SQL 실행 실패: {}", e.getMessage());
                    }
                }
            }

            long afterCount = programRepository.count();
            log.info("✅ 초기 데이터 로드 완료: {}개 INSERT 문 실행, {}개 프로그램 생성됨", insertCount, afterCount);

        } catch (Exception e) {
            log.error("초기 데이터 로드 중 오류 발생", e);
        }
    }
}
