package com.scms.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 후기 작성/수정 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequest {

    private Integer rating; // 1-5

    private String content;

    private String imageUrl; // 선택 (향후 확장)

    /**
     * 유효성 검증
     */
    public void validate() {
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("평점은 1-5 사이의 값이어야 합니다.");
        }

        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("후기 내용을 입력해주세요.");
        }

        if (content.length() > 1000) {
            throw new IllegalArgumentException("후기는 1000자 이내로 작성해주세요.");
        }
    }
}
