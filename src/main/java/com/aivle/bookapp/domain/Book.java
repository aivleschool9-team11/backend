package com.aivle.bookapp.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "도서 제목은 필수입니다.")
    @Size(max = 100, message = "제목은 100자를 넘을 수 없습니다.")
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "저자명은 필수입니다.")
    @Column(nullable = false)
    private String author;

    @NotBlank(message = "내용은 필수입니다.")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Size(max = 200, message = "한줄소개는 200자를 넘을 수 없습니다.")
    private String summary;

    private String copy;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    // === 요청/응답 전용 (DB 미저장). 태그는 BookTag 테이블에 저장됨. 추후 DTO로 분리 예정 ===
    @Transient
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @Transient
    private List<Float> embeddingJson;

    @Transient
    private Long embeddingDurationMs;

    @Builder.Default
    @Column(nullable = false)
    private Integer likes = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        // @Builder.Default 는 new Book()(Jackson 역직렬화) 경로엔 기본값을 적용하지 않아
        // likes 가 null 로 들어올 수 있으므로 여기서 방어한다.
        if (this.likes == null) {
            this.likes = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // === 요청/응답 전용 (DB 미저장) ===
    @Transient
    private Double similarityScore;
}