package com.aivle.bookapp.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;

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
    @Size(max = 100, message = "제목은 100자를 넘을 h수 없습니다.")
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

    // 💡 Jackson이 필드를 직접 건드리지 못하게 설정
    @Transient
    @Builder.Default
    @Setter(AccessLevel.NONE)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<String> tags = new ArrayList<>();

    @Builder.Default
    @Column(nullable = false)
    private Integer likes = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 💡 입력은 이 메서드가 전담 (문자열, 리스트, JsonNode 모두 수용)
    @com.fasterxml.jackson.annotation.JsonProperty("tags")
    public void setTags(Object value) {
        if (value == null) {
            this.tags = new ArrayList<>();
            return;
        }

        if (value instanceof com.fasterxml.jackson.databind.JsonNode) {
            com.fasterxml.jackson.databind.JsonNode node = (com.fasterxml.jackson.databind.JsonNode) value;
            if (node.isArray()) {
                List<String> list = new ArrayList<>();
                node.forEach(n -> list.add(n.asText().trim()));
                this.tags = list.stream().filter(s -> !s.isEmpty()).collect(java.util.stream.Collectors.toList());
            } else {
                String text = node.asText();
                this.tags = parseStringTags(text);
            }
        } else if (value instanceof List) {
            this.tags = ((List<?>) value).stream()
                    .map(Object::toString)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(java.util.stream.Collectors.toList());
        } else if (value instanceof String) {
            this.tags = parseStringTags((String) value);
        }
    }

    private List<String> parseStringTags(String text) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }
        return java.util.Arrays.stream(text.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(java.util.stream.Collectors.toList());
    }

    @com.fasterxml.jackson.annotation.JsonProperty("tags")
    public List<String> getTags() {
        return this.tags;
    }

    @Transient
    private String embeddingJson;

    @Transient
    private Long embeddingDurationMs;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.likes == null) {
            this.likes = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}