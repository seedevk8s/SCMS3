package com.scms.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 프로그램-역량 매핑 엔티티
 * 각 프로그램이 어떤 역량을 향상시키는지 매핑
 */
@Entity
@Table(name = "program_competencies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramCompetency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private Program program;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competency_id", nullable = false)
    private Competency competency;

    /**
     * 가중치 (1-10)
     * 이 프로그램이 해당 역량을 얼마나 향상시키는지
     * 10: 매우 큰 도움, 1: 약간 도움
     */
    @Column(nullable = false)
    private Integer weight;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
