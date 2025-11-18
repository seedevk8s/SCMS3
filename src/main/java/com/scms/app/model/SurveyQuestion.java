package com.scms.app.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 설문 질문 엔티티
 */
@Entity
@Table(name = "survey_questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long questionId;

    @Column(name = "survey_id", nullable = false)
    private Long surveyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 30)
    private QuestionType questionType;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "is_required", nullable = false)
    private Boolean isRequired = true;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Column(name = "scale_min")
    private Integer scaleMin;

    @Column(name = "scale_max")
    private Integer scaleMax;

    @Column(name = "scale_min_label", length = 50)
    private String scaleMinLabel;

    @Column(name = "scale_max_label", length = 50)
    private String scaleMaxLabel;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * 객관식 질문인지 확인
     */
    public boolean isChoiceType() {
        return questionType == QuestionType.SINGLE_CHOICE ||
               questionType == QuestionType.MULTIPLE_CHOICE;
    }

    /**
     * 주관식 질문인지 확인
     */
    public boolean isTextType() {
        return questionType == QuestionType.SHORT_TEXT ||
               questionType == QuestionType.LONG_TEXT;
    }

    /**
     * 척도형 질문인지 확인
     */
    public boolean isScaleType() {
        return questionType == QuestionType.SCALE;
    }
}
