package com.aivle.bookapp.service;


import com.aivle.bookapp.domain.Book;
import com.aivle.bookapp.domain.BookEmbedding;
import com.aivle.bookapp.exception.BookNotFoundException;
import com.aivle.bookapp.repository.BookEmbeddingRepository;
import com.aivle.bookapp.repository.BookRepository;
import com.aivle.bookapp.repository.BookTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
//import com.aivle.bookapp.domain.SearchLog;
import com.aivle.bookapp.repository.BookEmbeddingRepository;
import java.util.AbstractMap;



import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    private final TagService tagService;
    private final BookEmbeddingService bookEmbeddingService;
    private final BookTagRepository bookTagRepository;
    private final BookEmbeddingRepository bookEmbeddingRepository;
    //private final SearchLogService searchLogService;

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
        if (tags != null && !tags.isEmpty()) {
            tagService.saveBookTags(saved.getId(), tags);
        }
        // 임베딩 저장
        if (embeddingJson != null && !embeddingJson.isBlank()){
            BookEmbedding embedding = BookEmbedding.builder()
                    .bookId(saved.getId())
                    .embeddingJson(embeddingJson)
                    .embeddingModel("text-embedding-3-small")
                    .embeddingDurationMs(embeddingDurationMs)
                    .embeddingUpdatedAt(LocalDateTime.now())
                    .build();
            bookEmbeddingService.save(embedding);
        }
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
            tagService.deleteByBookId(id);
            tagService.saveBookTags(id, tags);
        }
        // 임베딩 재저장
        if (embeddingJson != null && !embeddingJson.isBlank()) {
            BookEmbedding embedding = BookEmbedding.builder()
                    .bookId(id)
                    .embeddingJson(embeddingJson)
                    .embeddingModel("text-embedding-3-small")
                    .embeddingDurationMs(embeddingDurationMS)
                    .embeddingUpdatedAt(LocalDateTime.now())
                    .build();
            bookEmbeddingService.save(embedding);
        }
        return updated;
    }

    // 도서 삭제
    @Transactional
    public void deleteById(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new BookNotFoundException(id);
        }
        tagService.deleteByBookId(id);
        bookEmbeddingService.deleteByBookId(id);
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
        BookEmbedding embedding = BookEmbedding.builder()
                .bookId(id)
                .embeddingJson(embeddingJson)
                .embeddingModel("text-embedding-3-small")
                .embeddingDurationMs(embeddingDurationMs)
                .embeddingUpdatedAt(LocalDateTime.now())
                .build();
        bookEmbeddingService.save(embedding);
        return existing;
    }

    // 특정 태그에 속한 도서 목록 조회
    @Transactional(readOnly = true)
    public List<Book> findByTagName(String tagName) {
        return tagService.findByName(tagName).stream()
                .flatMap(tag -> bookTagRepository.findByTagId(tag.getId()).stream())
                .map(bookTag -> findById(bookTag.getBookId()))
                .collect(Collectors.toList());
    }

    // 키워드 검색 + 정렬
    @Transactional(readOnly = true)
    public List<Book> findAllWithFilter(String keyword, String sort, String tag){
        List<Book> result;
        if (tag != null && !tag.isEmpty()) {
            result = findByTagName(tag);
        } else if (keyword != null && !keyword.isBlank()) {
            result = bookRepository.findByTitleContainingOrAuthorContaining(keyword, keyword);
        } else {
            result = bookRepository.findAll();
        }
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
        List<BookEmbedding> allEmbeddings = bookEmbeddingRepository.findAll();

        // searchLogService.saveSearchLog() 호출 (searchType: "SEMANTIC")
        List<Book> results = allEmbeddings.stream()
                .map(bookEmbedding -> {
                    float[] vector = parseEmbeddingJson(bookEmbedding.getEmbeddingJson());
                    double score = cosineSimilarity(queryVector, vector);
                    return new AbstractMap.SimpleEntry<>(bookEmbedding.getBookId(), score);
                })
                .filter(entry -> entry.getValue() > 0)
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(topK)
                .map(entry -> findById(entry.getKey()))
                .collect(Collectors.toList());

        // 검색 로그 저장

        return List.of();
    }

    // embeddingJson 문자열을 float[]로 변환
    private float[] parseEmbeddingJson(String embeddingJson) {
        try {
            embeddingJson = embeddingJson.trim().replaceAll("[\\[\\]]", "");
            String[] parts = embeddingJson.split(",");
            float[] vector = new float[parts.length];
            for (int i = 0; i < parts.length; i++) {
                vector[i] = Float.parseFloat(parts[i].trim());
            }
            return vector;
        } catch (Exception e) {
            return new float[0];
        }
    }

    // 코사인 유사도 계산
    private double cosineSimilarity(float[] vectorA, float[] vectorB) {
        if (vectorA.length != vectorB.length) return 0;
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < vectorA.length; i++) {
            dot += vectorA[i] * vectorB[i];
            normA += vectorA[i] * vectorA[i];
            normB += vectorB[i] * vectorB[i];
        }
        if (normA == 0 || normB == 0) return 0;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}


