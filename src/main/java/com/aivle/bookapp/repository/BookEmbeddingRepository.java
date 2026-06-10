package com.aivle.bookapp.repository;

import com.aivle.bookapp.domain.BookEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookEmbeddingRepository extends JpaRepository<BookEmbedding, Long> {
    BookEmbedding findByBookId(Long bookId);
    void deleteByBookId(Long bookId);
    boolean existsByBookId(Long bookId);
}
