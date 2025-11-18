package com.scms.app.service;

import com.scms.app.dto.*;
import com.scms.app.model.*;
import com.scms.app.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 포트폴리오 관리 Service
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final PortfolioItemRepository portfolioItemRepository;
    private final PortfolioViewRepository portfolioViewRepository;
    private final PortfolioShareRepository portfolioShareRepository;
    private final UserRepository userRepository;

    /**
     * 사용자의 모든 포트폴리오 조회
     */
    public List<PortfolioResponse> getPortfoliosByUserId(Integer userId) {
        List<Portfolio> portfolios = portfolioRepository.findByUserIdNotDeleted(userId);
        return portfolios.stream()
                .map(portfolio -> {
                    Long itemCount = portfolioItemRepository.countByPortfolioIdNotDeleted(portfolio.getPortfolioId());
                    Long viewCount = portfolioViewRepository.countByPortfolioId(portfolio.getPortfolioId());
                    return PortfolioResponse.from(portfolio, itemCount, viewCount);
                })
                .collect(Collectors.toList());
    }

    /**
     * 포트폴리오 ID로 조회
     */
    public Portfolio getPortfolio(Long portfolioId) {
        return portfolioRepository.findByIdNotDeleted(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("포트폴리오를 찾을 수 없습니다: ID " + portfolioId));
    }

    /**
     * 포트폴리오 상세 조회
     */
    public PortfolioDetailResponse getPortfolioDetail(Long portfolioId) {
        Portfolio portfolio = getPortfolio(portfolioId);

        // 항목들 조회
        List<PortfolioItem> items = portfolioItemRepository.findByPortfolioIdNotDeleted(portfolioId);
        List<PortfolioItemResponse> itemResponses = items.stream()
                .map(PortfolioItemResponse::from)
                .collect(Collectors.toList());

        // 통계 정보
        Long viewCount = portfolioViewRepository.countByPortfolioId(portfolioId);
        Boolean hasActiveShare = portfolioShareRepository.existsValidShareByPortfolioId(portfolioId);

        // 사용자 정보
        User user = userRepository.findById(portfolio.getUserId())
                .orElse(null);

        PortfolioDetailResponse response = PortfolioDetailResponse.from(portfolio);
        response.setItems(itemResponses);
        response.setTotalViews(viewCount);
        response.setItemCount((long) items.size());
        response.setHasActiveShare(hasActiveShare);
        if (user != null) {
            response.setUserName(user.getName());
        }

        return response;
    }

    /**
     * 포트폴리오 생성
     */
    @Transactional
    public Portfolio createPortfolio(Integer userId, PortfolioCreateRequest request) {
        // 사용자 존재 확인
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: ID " + userId));

        Portfolio portfolio = Portfolio.builder()
                .userId(userId)
                .title(request.getTitle())
                .description(request.getDescription())
                .visibility(request.getVisibility() != null ? request.getVisibility() : PortfolioVisibility.PRIVATE)
                .templateType(request.getTemplateType())
                // 프로필 정보
                .profileImageUrl(request.getProfileImageUrl())
                .aboutMe(request.getAboutMe())
                .careerGoal(request.getCareerGoal())
                // 연락처
                .contactEmail(request.getContactEmail())
                .contactPhone(request.getContactPhone())
                // SNS & 링크
                .githubUrl(request.getGithubUrl())
                .blogUrl(request.getBlogUrl())
                .linkedinUrl(request.getLinkedinUrl())
                .websiteUrl(request.getWebsiteUrl())
                // 역량
                .skills(request.getSkills())
                .interests(request.getInterests())
                // 학력
                .major(request.getMajor())
                .grade(request.getGrade())
                .gpa(request.getGpa())
                .build();

        Portfolio savedPortfolio = portfolioRepository.save(portfolio);
        log.info("포트폴리오 생성 완료: ID={}, 사용자 ID={}", savedPortfolio.getPortfolioId(), userId);

        return savedPortfolio;
    }

    /**
     * 포트폴리오 수정
     */
    @Transactional
    public Portfolio updatePortfolio(Long portfolioId, Integer userId, PortfolioUpdateRequest request) {
        Portfolio portfolio = portfolioRepository.findByIdAndUserIdNotDeleted(portfolioId, userId)
                .orElseThrow(() -> new IllegalArgumentException("포트폴리오를 찾을 수 없거나 수정 권한이 없습니다"));

        portfolio.setTitle(request.getTitle());
        portfolio.setDescription(request.getDescription());
        if (request.getVisibility() != null) {
            portfolio.setVisibility(request.getVisibility());
        }
        portfolio.setTemplateType(request.getTemplateType());

        // 프로필 정보
        portfolio.setProfileImageUrl(request.getProfileImageUrl());
        portfolio.setAboutMe(request.getAboutMe());
        portfolio.setCareerGoal(request.getCareerGoal());

        // 연락처
        portfolio.setContactEmail(request.getContactEmail());
        portfolio.setContactPhone(request.getContactPhone());

        // SNS & 링크
        portfolio.setGithubUrl(request.getGithubUrl());
        portfolio.setBlogUrl(request.getBlogUrl());
        portfolio.setLinkedinUrl(request.getLinkedinUrl());
        portfolio.setWebsiteUrl(request.getWebsiteUrl());

        // 역량
        portfolio.setSkills(request.getSkills());
        portfolio.setInterests(request.getInterests());

        // 학력
        portfolio.setMajor(request.getMajor());
        portfolio.setGrade(request.getGrade());
        portfolio.setGpa(request.getGpa());

        Portfolio updatedPortfolio = portfolioRepository.save(portfolio);
        log.info("포트폴리오 수정 완료: ID={}", portfolioId);

        return updatedPortfolio;
    }

    /**
     * 포트폴리오 삭제 (Soft Delete)
     */
    @Transactional
    public void deletePortfolio(Long portfolioId, Integer userId) {
        Portfolio portfolio = portfolioRepository.findByIdAndUserIdNotDeleted(portfolioId, userId)
                .orElseThrow(() -> new IllegalArgumentException("포트폴리오를 찾을 수 없거나 삭제 권한이 없습니다"));

        portfolio.delete();
        portfolioRepository.save(portfolio);
        log.info("포트폴리오 삭제 완료: ID={}", portfolioId);
    }

    /**
     * 포트폴리오 공개 범위 변경
     */
    @Transactional
    public Portfolio updateVisibility(Long portfolioId, Integer userId, PortfolioVisibility visibility) {
        Portfolio portfolio = portfolioRepository.findByIdAndUserIdNotDeleted(portfolioId, userId)
                .orElseThrow(() -> new IllegalArgumentException("포트폴리오를 찾을 수 없거나 수정 권한이 없습니다"));

        portfolio.setVisibility(visibility);
        Portfolio updatedPortfolio = portfolioRepository.save(portfolio);
        log.info("포트폴리오 공개 범위 변경 완료: ID={}, visibility={}", portfolioId, visibility);

        return updatedPortfolio;
    }

    /**
     * 공유 링크 생성
     */
    @Transactional
    public PortfolioShareResponse createShareLink(Long portfolioId, Integer userId, Integer expirationDays) {
        Portfolio portfolio = portfolioRepository.findByIdAndUserIdNotDeleted(portfolioId, userId)
                .orElseThrow(() -> new IllegalArgumentException("포트폴리오를 찾을 수 없거나 권한이 없습니다"));

        // 기존 활성 공유 링크가 있으면 재사용
        List<PortfolioShare> activeShares = portfolioShareRepository.findByPortfolioIdNotRevoked(portfolioId);
        if (!activeShares.isEmpty()) {
            PortfolioShare existingShare = activeShares.get(0);
            if (existingShare.isValid()) {
                return PortfolioShareResponse.from(existingShare);
            }
        }

        // 새 공유 링크 생성
        String shareToken = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        LocalDateTime expiresAt = expirationDays != null ? LocalDateTime.now().plusDays(expirationDays) : null;

        PortfolioShare share = PortfolioShare.builder()
                .portfolioId(portfolioId)
                .shareToken(shareToken)
                .expiresAt(expiresAt)
                .build();

        PortfolioShare savedShare = portfolioShareRepository.save(share);
        log.info("포트폴리오 공유 링크 생성 완료: ID={}, token={}", portfolioId, shareToken);

        return PortfolioShareResponse.from(savedShare);
    }

    /**
     * 공유 링크로 포트폴리오 조회
     */
    @Transactional
    public PortfolioDetailResponse getPortfolioByShareToken(String shareToken, String ipAddress, String userAgent) {
        PortfolioShare share = portfolioShareRepository.findByShareToken(shareToken)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 공유 링크입니다"));

        if (!share.isValid()) {
            throw new IllegalArgumentException("만료되었거나 취소된 공유 링크입니다");
        }

        // 공유 링크 조회수 증가
        share.incrementViewCount();
        portfolioShareRepository.save(share);

        // 조회 기록 저장
        PortfolioView view = PortfolioView.builder()
                .portfolioId(share.getPortfolioId())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .shareToken(shareToken)
                .build();
        portfolioViewRepository.save(view);

        // 포트폴리오 상세 정보 반환
        return getPortfolioDetail(share.getPortfolioId());
    }

    /**
     * 공유 링크 취소
     */
    @Transactional
    public void revokeShareLink(Long shareId, Integer userId) {
        PortfolioShare share = portfolioShareRepository.findById(shareId)
                .orElseThrow(() -> new IllegalArgumentException("공유 링크를 찾을 수 없습니다"));

        // 권한 확인
        Portfolio portfolio = getPortfolio(share.getPortfolioId());
        if (!portfolio.getUserId().equals(userId)) {
            throw new IllegalArgumentException("공유 링크를 취소할 권한이 없습니다");
        }

        share.revoke();
        portfolioShareRepository.save(share);
        log.info("포트폴리오 공유 링크 취소 완료: shareId={}", shareId);
    }

    /**
     * 포트폴리오 조회 기록
     */
    @Transactional
    public void recordView(Long portfolioId, Integer viewerUserId, String ipAddress, String userAgent) {
        PortfolioView view = PortfolioView.builder()
                .portfolioId(portfolioId)
                .viewerUserId(viewerUserId)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        portfolioViewRepository.save(view);
    }

    /**
     * 포트폴리오 검색
     */
    public List<PortfolioResponse> searchPortfolios(Integer userId, String keyword) {
        List<Portfolio> portfolios = portfolioRepository.searchByTitleNotDeleted(userId, keyword);
        return portfolios.stream()
                .map(portfolio -> {
                    Long itemCount = portfolioItemRepository.countByPortfolioIdNotDeleted(portfolio.getPortfolioId());
                    Long viewCount = portfolioViewRepository.countByPortfolioId(portfolio.getPortfolioId());
                    return PortfolioResponse.from(portfolio, itemCount, viewCount);
                })
                .collect(Collectors.toList());
    }

    /**
     * 포트폴리오 페이지네이션 조회
     */
    public Page<PortfolioResponse> getPortfoliosWithPagination(Integer userId, Pageable pageable) {
        Page<Portfolio> portfolios = portfolioRepository.findByUserIdNotDeletedWithPagination(userId, pageable);
        return portfolios.map(portfolio -> {
            Long itemCount = portfolioItemRepository.countByPortfolioIdNotDeleted(portfolio.getPortfolioId());
            Long viewCount = portfolioViewRepository.countByPortfolioId(portfolio.getPortfolioId());
            return PortfolioResponse.from(portfolio, itemCount, viewCount);
        });
    }

    /**
     * 사용자의 포트폴리오 수 조회
     */
    public Long countPortfoliosByUserId(Integer userId) {
        return portfolioRepository.countByUserIdNotDeleted(userId);
    }
}
