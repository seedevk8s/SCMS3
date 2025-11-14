package com.scms.app.dto;

import com.scms.app.model.ProgramReview;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 후기 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {

    private Integer reviewId;
    private Integer programId;
    private String programTitle;
    private Integer userId;
    private String userName;
    private String userStudentNum;
    private Integer rating;
    private String content;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isEditable; // 현재 사용자가 수정 가능한지 여부

    /**
     * Entity를 DTO로 변환
     */
    public static ReviewResponse from(ProgramReview review) {
        return ReviewResponse.builder()
                .reviewId(review.getReviewId())
                .programId(review.getProgram().getProgramId())
                .programTitle(review.getProgram().getTitle())
                .userId(review.getUser().getUserId())
                .userName(review.getUser().getName())
                .userStudentNum(review.getUser().getStudentNum())
                .rating(review.getRating())
                .content(review.getContent())
                .imageUrl(review.getImageUrl())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .isEditable(false) // 기본값, Service에서 설정
                .build();
    }

    /**
     * Entity를 DTO로 변환 (수정 가능 여부 포함)
     */
    public static ReviewResponse from(ProgramReview review, Integer currentUserId) {
        ReviewResponse response = from(review);
        response.setIsEditable(review.isEditableBy(currentUserId));
        return response;
    }
}
