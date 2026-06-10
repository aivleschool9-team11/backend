package com.aivle.bookapp.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name ="book_tags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class BookTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "book_id", nullable = false)
    private Long bookId;

    @Column(name = "tag_id", nullable = false)
    private Long tagId;
}
