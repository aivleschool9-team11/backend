package com.aivle.bookapp.service;

import com.aivle.bookapp.domain.BookEmbedding;
import com.aivle.bookapp.exception.BookEmbeddingNotFoundException;
import com.aivle.bookapp.repository.BookEmbeddingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookEmbeddingService {
    private final BookEmbeddingRepository bookEmbeddingRepository;

    @Transactional(readOnly = true)
    public BookEmbedding findById(Long bookId) {
        return bookEmbeddingRepository.findById(bookId).orElseThrow(() -> new BookEmbeddingNotFoundException(bookId));
    }

    @Transactional(readOnly = true)
    public BookEmbedding findByBookId(Long bookId) {
        return bookEmbeddingRepository.findByBookId(bookId);
    }

    @Transactional
    public BookEmbedding save(BookEmbedding emb) {
        return bookEmbeddingRepository.save(emb);
    }

    @Transactional
    public void deleteByBookId(Long bookId) {
        // 정리용 삭제이므로 idempotent: 임베딩이 없으면 조용히 통과
        // (임베딩 없는 책 삭제/임베딩 최초 저장 시 404 나던 버그 수정)
        if (bookEmbeddingRepository.existsByBookId(bookId)) {
            bookEmbeddingRepository.deleteByBookId(bookId);
        }
    }



}
