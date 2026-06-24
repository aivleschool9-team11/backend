package com.aivle.bookapp.repository;

import com.aivle.bookapp.domain.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
    // 제목 또는 저자 키워드 검색
    List<Book> findByTitleContainingOrAuthorContaining(String title, String author);

    // pgvector 유사도 검색
    @Query(value= """
        SELECT * FROM books
        WHERE embedding IS NOT NULL
        ORDER BY embedding <=> CAST(:queryVector AS vector)
        LIMIT :topK
    """, nativeQuery = true)
    List<Book> findSimilarBooks(
            @Param("queryVector") String queryVector,
            @Param("topK") int topK
    );

    // 유사도 점수만 따로 조회
    @Query(value = """
        SELECT 1 - (embedding <=> CAST(:queryVector AS vector)) AS score
        FROM books
        WHERE id = :bookId
    """, nativeQuery = true)
    Double findSimilarityScore(
            @Param("queryVector") String queryVector,
            @Param("bookId") Long bookId
    );
}
