package com.aivle.bookapp.service;


import com.aivle.bookapp.domain.Book;
import com.aivle.bookapp.exception.BookNotFoundException;
import com.aivle.bookapp.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;

    // 전체 도서 목록 조회
    @Transactional(readOnly = true)
    public List<Book> findAll(){
        return bookRepository.findAll();
    }

    // 특정 도서 단건 조회
    @Transactional(readOnly = true)
    public Book findById(Long id){
        return bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    // 새 도서 등록 + 태그 저장 + 임베딩 저장
    @Transactional
    public Book create(Book book, List<String> tags, String embeddingJson, Long embeddingDurationMs){
        Book saved = bookRepository.save(book);

        // 태그 저장
        // 임베딩 저장
        return saved;
    }


    // 도서 부분 수정
    @Transactional
    public Book update(Long id, Book book, List<String> tags, String embeddingJson, Long embeddingDurationMS){
        Book existing = findById(id);

        if (book.getTitle() != null) existing.setTitle(book.getTitle());
        if (book.getAuthor() != null) existing.setAuthor(book.getAuthor());
        if (book.getSummary() != null) existing.setSummary(book.getSummary());
        if (book.getContent() != null) existing.setContent(book.getContent());
        if (book.getCopy() != null) existing.setCopy(book.getCopy());
        if (book.getCoverImageUrl() != null) existing.setCoverImageUrl(book.getCoverImageUrl());

        Book updated = bookRepository.save(existing);

        // 태그 재저장
        if (tags != null) {

        }
        // 임베딩 재저장
        if (embeddingJson != null && !embeddingJson.isBlank()){

        }
        return updated;
    }

    // 도서 삭제
    @Transactional
    public void deleteById(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new BookNotFoundException(id);
        }
        bookRepository.deleteById(id);
    }

    // 좋아요 증가/감소
    @Transactional
    public Book updateLikes(Long id, int likes) {
        Book existing = findById(id);
        int current = existing.getLikes() == null ? 0: existing.getLikes();
        existing.setLikes(current + likes);
        return bookRepository.save(existing);
    }

    // AI 표지 저장
    @Transactional
    public Book updateCover(Long id, String coverImageUrl) {
        Book existing = findById(id);
        existing.setCoverImageUrl(coverImageUrl);
        return bookRepository.save(existing);
    }

    // 임베딩 백필
    @Transactional
    public Book updateEmbedding(Long id, String embeddingJson, Long embeddingDurationMs) {
        Book existing = findById(id);
        //bookEmbeddingService.save(id, embeddingJson, embeddingDurationMs);
        return existing;
    }

    // 특정 태그에 속한 도서 목록 조회
    @Transactional(readOnly = true)
    public List<Book> findByTagName(String tagName){
        // TagRepository 받으면 구현
        return List.of();
    }

    // 키워드 검색 + 정렬
    @Transactional(readOnly = true)
    public List<Book> findAllWithFilter(String keyword, String sort){
        List<Book> result = (keyword == null || keyword.isBlank())
                ? bookRepository.findAll()
                : bookRepository.findByTitleContainingOrAuthorContaining(keyword, keyword);
        if ("newest".equals(sort)) result.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        else if ("oldest".equals(sort)) result.sort((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()));
        else if ("title".equals(sort)) result.sort((a, b) -> a.getTitle().compareTo(b.getTitle()));
        else if ("author".equals(sort)) result.sort((a, b) -> a.getAuthor().compareTo(b.getAuthor()));
        else if ("likes".equals(sort)) result.sort((a, b) -> (b.getLikes() == null ? 0 : b.getLikes()) - (a.getLikes() == null ? 0 : a.getLikes()));

        // searchLogService.saveSearchLog() 호출
        return result;
    }

    // AI 의미 검색 + 코사인 유사도 계산
    @Transactional(readOnly = true)
    public List<Book> semanticSearch(float[] queryVector, String query, int topK){
        // bookEmbeddingService에서 전체 임베딩 조회 후 코사인 유사도 계산
        // searchLogService.saveSearchLog() 호출 (searchType: "SEMANTIC")
        return List.of();
    }
}
