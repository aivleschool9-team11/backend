package com.aivle.bookapp.repository;

import com.aivle.bookapp.domain.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
    // 제목 또는 저자 키워드 검색
    List<Book> findByTitleContainingOrAuthorContaining(String title, String author);
}
