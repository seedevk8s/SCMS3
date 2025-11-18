package com.scms.app.service;

import com.scms.app.dto.PortfolioItemRequest;
import com.scms.app.dto.PortfolioItemResponse;
import com.scms.app.model.Portfolio;
import com.scms.app.model.PortfolioItem;
import com.scms.app.model.ProgramApplication;
import com.scms.app.repository.PortfolioItemRepository;
import com.scms.app.repository.PortfolioRepository;
import com.scms.app.repository.ProgramApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 포트폴리오 항목 관리 Service
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class PortfolioItemService {

    private final PortfolioItemRepository portfolioItemRepository;
    private final PortfolioRepository portfolioRepository;
    private final ProgramApplicationRepository programApplicationRepository;

    /**
     * 포트폴리오의 모든 항목 조회
     */
    public List<PortfolioItemResponse> getPortfolioItems(Long portfolioId) {
        List<PortfolioItem> items = portfolioItemRepository.findByPortfolioIdNotDeleted(portfolioId);
        return items.stream()
                .map(PortfolioItemResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 항목 ID로 조회
     */
    public PortfolioItem getPortfolioItem(Long itemId) {
        return portfolioItemRepository.findByIdNotDeleted(itemId)
                .orElseThrow(() -> new IllegalArgumentException("포트폴리오 항목을 찾을 수 없습니다: ID " + itemId));
    }

    /**
     * 포트폴리오 항목 생성
     */
    @Transactional
    public PortfolioItem createPortfolioItem(Long portfolioId, Integer userId, PortfolioItemRequest request) {
        // 포트폴리오 존재 및 권한 확인
        Portfolio portfolio = portfolioRepository.findByIdAndUserIdNotDeleted(portfolioId, userId)
                .orElseThrow(() -> new IllegalArgumentException("포트폴리오를 찾을 수 없거나 권한이 없습니다"));

        // 프로그램 신청 ID가 있으면 중복 확인
        if (request.getProgramApplicationId() != null) {
            portfolioItemRepository.findByProgramApplicationIdNotDeleted(request.getProgramApplicationId())
                    .ifPresent(item -> {
                        throw new IllegalArgumentException("이미 포트폴리오에 추가된 프로그램입니다");
                    });
        }

        // display_order 자동 설정
        Integer displayOrder = request.getDisplayOrder();
        if (displayOrder == null) {
            Integer maxOrder = portfolioItemRepository.findMaxDisplayOrderByPortfolioId(portfolioId);
            displayOrder = maxOrder + 1;
        }

        PortfolioItem item = PortfolioItem.builder()
                .portfolioId(portfolioId)
                .itemType(request.getItemType())
                .title(request.getTitle())
                .description(request.getDescription())
                .organization(request.getOrganization())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .displayOrder(displayOrder)
                .programApplicationId(request.getProgramApplicationId())
                .build();

        PortfolioItem savedItem = portfolioItemRepository.save(item);
        log.info("포트폴리오 항목 생성 완료: portfolioId={}, itemId={}", portfolioId, savedItem.getItemId());

        return savedItem;
    }

    /**
     * 포트폴리오 항목 수정
     */
    @Transactional
    public PortfolioItem updatePortfolioItem(Long itemId, Integer userId, PortfolioItemRequest request) {
        PortfolioItem item = portfolioItemRepository.findByIdNotDeleted(itemId)
                .orElseThrow(() -> new IllegalArgumentException("포트폴리오 항목을 찾을 수 없습니다"));

        // 권한 확인
        Portfolio portfolio = portfolioRepository.findByIdAndUserIdNotDeleted(item.getPortfolioId(), userId)
                .orElseThrow(() -> new IllegalArgumentException("수정 권한이 없습니다"));

        item.setItemType(request.getItemType());
        item.setTitle(request.getTitle());
        item.setDescription(request.getDescription());
        item.setOrganization(request.getOrganization());
        item.setStartDate(request.getStartDate());
        item.setEndDate(request.getEndDate());
        if (request.getDisplayOrder() != null) {
            item.setDisplayOrder(request.getDisplayOrder());
        }

        PortfolioItem updatedItem = portfolioItemRepository.save(item);
        log.info("포트폴리오 항목 수정 완료: itemId={}", itemId);

        return updatedItem;
    }

    /**
     * 포트폴리오 항목 삭제 (Soft Delete)
     */
    @Transactional
    public void deletePortfolioItem(Long itemId, Integer userId) {
        PortfolioItem item = portfolioItemRepository.findByIdNotDeleted(itemId)
                .orElseThrow(() -> new IllegalArgumentException("포트폴리오 항목을 찾을 수 없습니다"));

        // 권한 확인
        Portfolio portfolio = portfolioRepository.findByIdAndUserIdNotDeleted(item.getPortfolioId(), userId)
                .orElseThrow(() -> new IllegalArgumentException("삭제 권한이 없습니다"));

        item.delete();
        portfolioItemRepository.save(item);
        log.info("포트폴리오 항목 삭제 완료: itemId={}", itemId);
    }

    /**
     * 항목 순서 변경
     */
    @Transactional
    public void reorderPortfolioItems(Long portfolioId, Integer userId, List<Long> itemIds) {
        // 권한 확인
        Portfolio portfolio = portfolioRepository.findByIdAndUserIdNotDeleted(portfolioId, userId)
                .orElseThrow(() -> new IllegalArgumentException("포트폴리오를 찾을 수 없거나 권한이 없습니다"));

        // 순서 업데이트
        for (int i = 0; i < itemIds.size(); i++) {
            Long itemId = itemIds.get(i);
            PortfolioItem item = portfolioItemRepository.findByIdAndPortfolioIdNotDeleted(itemId, portfolioId)
                    .orElseThrow(() -> new IllegalArgumentException("항목을 찾을 수 없습니다: ID " + itemId));

            item.setDisplayOrder(i);
            portfolioItemRepository.save(item);
        }

        log.info("포트폴리오 항목 순서 변경 완료: portfolioId={}", portfolioId);
    }

    /**
     * 프로그램 신청으로부터 포트폴리오 항목 자동 생성
     */
    @Transactional
    public PortfolioItem createFromProgramApplication(Long portfolioId, Integer userId, Integer applicationId) {
        // 권한 확인
        Portfolio portfolio = portfolioRepository.findByIdAndUserIdNotDeleted(portfolioId, userId)
                .orElseThrow(() -> new IllegalArgumentException("포트폴리오를 찾을 수 없거나 권한이 없습니다"));

        // 프로그램 신청 조회
        ProgramApplication application = programApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("프로그램 신청을 찾을 수 없습니다"));

        // 본인의 신청인지 확인
        if (!application.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 프로그램 신청만 추가할 수 있습니다");
        }

        // 이미 추가된 프로그램인지 확인
        portfolioItemRepository.findByProgramApplicationIdNotDeleted(Long.valueOf(applicationId))
                .ifPresent(item -> {
                    throw new IllegalArgumentException("이미 포트폴리오에 추가된 프로그램입니다");
                });

        // 다음 순서 계산
        Integer maxOrder = portfolioItemRepository.findMaxDisplayOrderByPortfolioId(portfolioId);
        Integer displayOrder = maxOrder + 1;

        // 항목 생성
        PortfolioItem item = PortfolioItem.builder()
                .portfolioId(portfolioId)
                .itemType(determineItemTypeFromApplication(application))
                .title(application.getProgram().getTitle())
                .description(application.getProgram().getDescription())
                .organization(application.getProgram().getDepartment())
                .startDate(application.getProgram().getProgramStartDate().toLocalDate())
                .endDate(application.getProgram().getProgramEndDate().toLocalDate())
                .displayOrder(displayOrder)
                .programApplicationId(Long.valueOf(applicationId))
                .build();

        PortfolioItem savedItem = portfolioItemRepository.save(item);
        log.info("프로그램 신청으로부터 포트폴리오 항목 생성 완료: applicationId={}, itemId={}", applicationId, savedItem.getItemId());

        return savedItem;
    }

    /**
     * 프로그램 신청의 카테고리에 따라 항목 유형 결정
     */
    private com.scms.app.model.PortfolioItemType determineItemTypeFromApplication(ProgramApplication application) {
        String category = application.getProgram().getCategory();

        if (category == null) {
            return com.scms.app.model.PortfolioItemType.ACTIVITY;
        }

        // 카테고리에 따라 항목 유형 매핑
        if (category.contains("프로젝트") || category.contains("project")) {
            return com.scms.app.model.PortfolioItemType.PROJECT;
        } else if (category.contains("자격증") || category.contains("certificate")) {
            return com.scms.app.model.PortfolioItemType.CERTIFICATION;
        } else if (category.contains("교육") || category.contains("강좌") || category.contains("course")) {
            return com.scms.app.model.PortfolioItemType.COURSE;
        } else if (category.contains("수상") || category.contains("award")) {
            return com.scms.app.model.PortfolioItemType.ACHIEVEMENT;
        }

        return com.scms.app.model.PortfolioItemType.ACTIVITY;
    }
}
