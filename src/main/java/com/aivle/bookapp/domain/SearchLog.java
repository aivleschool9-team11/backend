package com.aivle.bookapp.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "search_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class SearchLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "검색 유형은 필수입니다.")
    @Column(name = "search_type", nullable = false)
    private String searchType;

    @NotBlank(message = "검색어는 필수입니다.")
    @Column(nullable = false)
    private String query;

    @Column(name = "matched_book_count", nullable = false)
    private Integer matchedBookCount;

    @Column(name = "duration_ms", nullable = false)
    private Long durationMs;

    @Column(name = "searched_at", nullable = false)
    private LocalDateTime searchedAt;
}
