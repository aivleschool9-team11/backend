package com.aivle.bookapp.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "book_embeddings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class BookEmbedding {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "book_id", nullable = false, unique = true)
    private Long bookId;

    @Column(name = "embedding_json", columnDefinition = "TEXT", nullable = false)
    private String embeddingJson;

    @Column(name = "embedding_model", nullable = false)
    private String embeddingModel;

    @Column(name = "embedding_duration_ms", nullable = false)
    private Long embeddingDurationMs;

    @Column(name = "embedding_updated_at", nullable = false)
    private LocalDateTime embeddingUpdatedAt;
}
