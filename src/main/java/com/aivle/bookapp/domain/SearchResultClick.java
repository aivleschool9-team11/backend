package com.aivle.bookapp.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "search_result_clicks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class SearchResultClick {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "search_log_id", nullable = false)
    private Long searchLogId;

    @Column(name = "book_id", nullable = false)
    private Long bookId;

    @Column(name = "rank_position", nullable = false)
    private Integer rankPosition;

    @Column(name = "similarity_score", nullable = false)
    private Float similarityScore;

    @Column(name = "clicked_at")
    private LocalDateTime clickedAt;
}
