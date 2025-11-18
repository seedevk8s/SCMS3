package com.scms.app.service;

import com.scms.app.dto.CompetencyCategoryRequest;
import com.scms.app.dto.CompetencyCategoryResponse;
import com.scms.app.dto.CompetencyRequest;
import com.scms.app.dto.CompetencyResponse;
import com.scms.app.model.Competency;
import com.scms.app.model.CompetencyCategory;
import com.scms.app.repository.CompetencyCategoryRepository;
import com.scms.app.repository.CompetencyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 역량 관리 Service
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class CompetencyService {

    private final CompetencyCategoryRepository categoryRepository;
    private final CompetencyRepository competencyRepository;

    // ==================== 역량 카테고리 관리 ====================

    /**
     * 모든 카테고리 조회
     */
    public List<CompetencyCategoryResponse> getAllCategories() {
        log.debug("모든 역량 카테고리 조회");
        return categoryRepository.findAllByOrderByCreatedAtAsc().stream()
                .map(CompetencyCategoryResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 모든 카테고리 조회 (역량 목록 포함)
     */
    public List<CompetencyCategoryResponse> getAllCategoriesWithCompetencies() {
        log.debug("모든 역량 카테고리 조회 (역량 포함)");
        return categoryRepository.findAllWithCompetencies().stream()
                .map(CompetencyCategoryResponse::fromWithCompetencies)
                .collect(Collectors.toList());
    }

    /**
     * 카테고리 ID로 조회
     */
    public CompetencyCategoryResponse getCategoryById(Long id) {
        log.debug("역량 카테고리 조회: ID {}", id);
        CompetencyCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다: ID " + id));
        return CompetencyCategoryResponse.from(category);
    }

    /**
     * 카테고리 ID로 조회 (역량 목록 포함)
     */
    public CompetencyCategoryResponse getCategoryByIdWithCompetencies(Long id) {
        log.debug("역량 카테고리 조회 (역량 포함): ID {}", id);
        CompetencyCategory category = categoryRepository.findByIdWithCompetencies(id)
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다: ID " + id));
        return CompetencyCategoryResponse.fromWithCompetencies(category);
    }

    /**
     * 카테고리 생성
     */
    @Transactional
    public CompetencyCategoryResponse createCategory(CompetencyCategoryRequest request) {
        log.info("역량 카테고리 생성: {}", request.getName());

        // 중복 체크
        if (categoryRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("이미 존재하는 카테고리명입니다: " + request.getName());
        }

        CompetencyCategory category = CompetencyCategory.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        CompetencyCategory saved = categoryRepository.save(category);
        log.info("역량 카테고리 생성 완료: ID {}", saved.getId());

        return CompetencyCategoryResponse.from(saved);
    }

    /**
     * 카테고리 수정
     */
    @Transactional
    public CompetencyCategoryResponse updateCategory(Long id, CompetencyCategoryRequest request) {
        log.info("역량 카테고리 수정: ID {}", id);

        CompetencyCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다: ID " + id));

        // 이름 중복 체크 (다른 카테고리와)
        if (!category.getName().equals(request.getName()) &&
            categoryRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("이미 존재하는 카테고리명입니다: " + request.getName());
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        CompetencyCategory updated = categoryRepository.save(category);
        log.info("역량 카테고리 수정 완료: ID {}", updated.getId());

        return CompetencyCategoryResponse.from(updated);
    }

    /**
     * 카테고리 삭제
     */
    @Transactional
    public void deleteCategory(Long id) {
        log.info("역량 카테고리 삭제: ID {}", id);

        CompetencyCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다: ID " + id));

        // 연관된 역량이 있는지 체크
        long competencyCount = competencyRepository.countByCategoryId(id);
        if (competencyCount > 0) {
            throw new IllegalStateException(
                "연관된 역량이 " + competencyCount + "개 존재합니다. 먼저 역량을 삭제하거나 다른 카테고리로 이동해주세요."
            );
        }

        categoryRepository.delete(category);
        log.info("역량 카테고리 삭제 완료: ID {}", id);
    }

    // ==================== 역량 관리 ====================

    /**
     * 모든 역량 조회
     */
    public List<CompetencyResponse> getAllCompetencies() {
        log.debug("모든 역량 조회");
        return competencyRepository.findAllWithCategory().stream()
                .map(CompetencyResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 카테고리별 역량 조회
     */
    public List<CompetencyResponse> getCompetenciesByCategory(Long categoryId) {
        log.debug("카테고리별 역량 조회: 카테고리 ID {}", categoryId);

        // 카테고리 존재 여부 확인
        if (!categoryRepository.existsById(categoryId)) {
            throw new IllegalArgumentException("카테고리를 찾을 수 없습니다: ID " + categoryId);
        }

        return competencyRepository.findByCategoryIdOrderByCreatedAtAsc(categoryId).stream()
                .map(CompetencyResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 역량 ID로 조회
     */
    public CompetencyResponse getCompetencyById(Long id) {
        log.debug("역량 조회: ID {}", id);
        Competency competency = competencyRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new IllegalArgumentException("역량을 찾을 수 없습니다: ID " + id));
        return CompetencyResponse.from(competency);
    }

    /**
     * 역량 생성
     */
    @Transactional
    public CompetencyResponse createCompetency(CompetencyRequest request) {
        log.info("역량 생성: {}", request.getName());

        // 카테고리 존재 여부 확인
        CompetencyCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다: ID " + request.getCategoryId()));

        // 중복 체크
        if (competencyRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("이미 존재하는 역량명입니다: " + request.getName());
        }

        Competency competency = Competency.builder()
                .category(category)
                .name(request.getName())
                .description(request.getDescription())
                .build();

        Competency saved = competencyRepository.save(competency);
        log.info("역량 생성 완료: ID {}", saved.getId());

        return CompetencyResponse.from(saved);
    }

    /**
     * 역량 수정
     */
    @Transactional
    public CompetencyResponse updateCompetency(Long id, CompetencyRequest request) {
        log.info("역량 수정: ID {}", id);

        Competency competency = competencyRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new IllegalArgumentException("역량을 찾을 수 없습니다: ID " + id));

        // 카테고리 변경 시 카테고리 존재 여부 확인
        if (!competency.getCategory().getId().equals(request.getCategoryId())) {
            CompetencyCategory newCategory = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다: ID " + request.getCategoryId()));
            competency.setCategory(newCategory);
        }

        // 이름 중복 체크 (다른 역량과)
        if (!competency.getName().equals(request.getName()) &&
            competencyRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("이미 존재하는 역량명입니다: " + request.getName());
        }

        competency.setName(request.getName());
        competency.setDescription(request.getDescription());

        Competency updated = competencyRepository.save(competency);
        log.info("역량 수정 완료: ID {}", updated.getId());

        return CompetencyResponse.from(updated);
    }

    /**
     * 역량 삭제
     */
    @Transactional
    public void deleteCompetency(Long id) {
        log.info("역량 삭제: ID {}", id);

        Competency competency = competencyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("역량을 찾을 수 없습니다: ID " + id));

        competencyRepository.delete(competency);
        log.info("역량 삭제 완료: ID {}", id);
    }

    /**
     * 역량명으로 검색
     */
    public List<CompetencyResponse> searchCompetenciesByName(String keyword) {
        log.debug("역량 검색: 키워드 {}", keyword);
        return competencyRepository.findByNameContaining(keyword).stream()
                .map(CompetencyResponse::from)
                .collect(Collectors.toList());
    }
}
