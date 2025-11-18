package com.scms.app.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 설문 대상자 엔티티 (SPECIFIC 유형용)
 */
@Entity
@Table(name = "survey_targets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyTarget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "survey_id", nullable = false)
    private Long surveyId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "has_responded", nullable = false)
    private Boolean hasResponded = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
