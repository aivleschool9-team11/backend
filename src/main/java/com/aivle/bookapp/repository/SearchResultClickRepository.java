package com.aivle.bookapp.repository;

import com.aivle.bookapp.domain.SearchResultClick;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SearchResultClickRepository extends JpaRepository<SearchResultClick, Long>{
    List<SearchResultClick> findBySearchLogId(Long id);
}
