package com.scms.app.service;

import com.scms.app.dto.ReviewRequest;
import com.scms.app.dto.ReviewResponse;
import com.scms.app.model.ApplicationStatus;
import com.scms.app.model.Program;
import com.scms.app.model.ProgramApplication;
import com.scms.app.model.ProgramReview;
import com.scms.app.model.User;
import com.scms.app.repository.ProgramApplicationRepository;
import com.scms.app.repository.ProgramRepository;
import com.scms.app.repository.ProgramReviewRepository;
import com.scms.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 프로그램 후기 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProgramReviewService {

    private final ProgramReviewRepository reviewRepository;
    private final ProgramRepository programRepository;
    private final UserRepository userRepository;
    private final ProgramApplicationRepository applicationRepository;

    /**
     * 후기 작성 (참여 완료한 사용자만 가능)
     */
    @Transactional
    public ReviewResponse createReview(Integer userId, Integer programId, ReviewRequest request) {
        // 유효성 검증
        request.validate();

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다."));

        // 프로그램 조회
        Program program = programRepository.findByIdNotDeleted(programId)
                .orElseThrow(() -> new IllegalStateException("프로그램을 찾을 수 없습니다."));

        // 참여 완료 여부 확인
        Optional<ProgramApplication> application = applicationRepository.findByUserIdAndProgramId(userId, programId);
        if (application.isEmpty() || application.get().getStatus() != ApplicationStatus.COMPLETED) {
            throw new IllegalStateException("참여 완료한 프로그램만 후기를 작성할 수 있습니다.");
        }

        // 중복 작성 확인
        if (reviewRepository.existsByUserIdAndProgramIdAndDeletedAtIsNull(userId, programId)) {
            throw new IllegalStateException("이미 후기를 작성한 프로그램입니다.");
        }

        // 후기 생성
        ProgramReview review = ProgramReview.builder()
                .program(program)
                .user(user)
                .rating(request.getRating())
                .content(request.getContent())
                .imageUrl(request.getImageUrl())
                .build();

        review = reviewRepository.save(review);
        log.info("후기 작성 완료: reviewId={}, userId={}, programId={}, rating={}",
                review.getReviewId(), userId, programId, request.getRating());

        return ReviewResponse.from(review, userId);
    }

    /**
     * 프로그램별 후기 목록 조회
     */
    public List<ReviewResponse> getReviewsByProgram(Integer programId, Integer currentUserId) {
        List<ProgramReview> reviews = reviewRepository.findByProgramIdAndDeletedAtIsNull(programId);

        return reviews.stream()
                .map(review -> ReviewResponse.from(review, currentUserId))
                .collect(Collectors.toList());
    }

    /**
     * 후기 수정 (본인만 가능)
     */
    @Transactional
    public ReviewResponse updateReview(Integer userId, Integer reviewId, ReviewRequest request) {
        // 유효성 검증
        request.validate();

        // 후기 조회 및 권한 확인
        ProgramReview review = reviewRepository.findByReviewIdAndUserId(reviewId, userId)
                .orElseThrow(() -> new IllegalStateException("수정 권한이 없거나 후기를 찾을 수 없습니다."));

        // 후기 수정
        review.setRating(request.getRating());
        review.setContent(request.getContent());
        review.setImageUrl(request.getImageUrl());

        review = reviewRepository.save(review);
        log.info("후기 수정 완료: reviewId={}, userId={}", reviewId, userId);

        return ReviewResponse.from(review, userId);
    }

    /**
     * 후기 삭제 (본인만 가능)
     */
    @Transactional
    public void deleteReview(Integer userId, Integer reviewId) {
        // 후기 조회 및 권한 확인
        ProgramReview review = reviewRepository.findByReviewIdAndUserId(reviewId, userId)
                .orElseThrow(() -> new IllegalStateException("삭제 권한이 없거나 후기를 찾을 수 없습니다."));

        // Soft Delete
        review.delete();
        reviewRepository.save(review);
        log.info("후기 삭제 완료: reviewId={}, userId={}", reviewId, userId);
    }

    /**
     * 프로그램 평균 평점 조회
     */
    public Double getAverageRating(Integer programId) {
        Double average = reviewRepository.getAverageRatingByProgramId(programId);
        return average != null ? Math.round(average * 10.0) / 10.0 : 0.0; // 소수점 1자리
    }

    /**
     * 프로그램 후기 개수 조회
     */
    public Long getReviewCount(Integer programId) {
        return reviewRepository.countByProgramIdAndDeletedAtIsNull(programId);
    }

    /**
     * 사용자가 후기를 작성할 수 있는지 확인
     */
    public boolean canWriteReview(Integer userId, Integer programId) {
        // 이미 작성했는지 확인
        if (reviewRepository.existsByUserIdAndProgramIdAndDeletedAtIsNull(userId, programId)) {
            return false;
        }

        // 참여 완료 여부 확인
        Optional<ProgramApplication> application = applicationRepository.findByUserIdAndProgramId(userId, programId);
        return application.isPresent() && application.get().getStatus() == ApplicationStatus.COMPLETED;
    }
}
