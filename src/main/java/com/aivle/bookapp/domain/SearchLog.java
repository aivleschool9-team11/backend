package com.aivle.bookapp.domain;

import jakarta.persistence.*;
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

    @Column(name = "search_type", nullable = false)
    private String searchType;

    @Column(nullable = false)
    private String query;

    @Column(name = "matched_book_count", nullable = false)
    private Integer matchedBookCount;

    @Column(name = "duration_ms", nullable = false)
    private Long durationMs;

    @Column(name = "searched_at", nullable = false)
    private LocalDateTime searchedAt;
}
