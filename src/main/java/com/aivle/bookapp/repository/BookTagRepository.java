package com.aivle.bookapp.repository;

import com.aivle.bookapp.domain.BookTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookTagRepository extends JpaRepository<BookTag, Long>{
}
