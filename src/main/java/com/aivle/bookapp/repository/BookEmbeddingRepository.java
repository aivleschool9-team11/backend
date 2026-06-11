package com.aivle.bookapp.repository;

import com.aivle.bookapp.domain.BookEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookEmbeddingRepository extends JpaRepository<BookEmbedding, Long> {
    BookEmbedding findByBookId(Long bookId);
    boolean existsByBookId(Long bookId);

    @Modifying
    @Query("DELETE FROM BookEmbedding b WHERE b.bookId = :bookId")
    void deleteByBookId(@Param("bookId") Long bookId);
}