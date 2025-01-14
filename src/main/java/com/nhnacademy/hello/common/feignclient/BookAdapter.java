package com.nhnacademy.hello.common.feignclient;

import com.nhnacademy.hello.dto.book.AuthorDTO;
import com.nhnacademy.hello.dto.book.BookDTO;
import com.nhnacademy.hello.dto.book.BookRequestDTO;
import com.nhnacademy.hello.dto.book.BookUpdateRequestDTO;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "hexa-gateway", contextId = "bookAdapter")
public interface BookAdapter {

    // 통합된 도서 목록 조회
    @GetMapping("/api/books")
    public List<BookDTO> getBooks(
            // 페이징
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam("sort") String sort,

            //도서 제목으로 검색
            @RequestParam(required = false) String search,
            //카테고리(아이디)로 검색
            @RequestParam(required = false) List<Long> categoryIds,
            //출판사명으로 검색
            @RequestParam(required = false) String publisherName,
            //작가명으로 검색
            @RequestParam(required = false) String authorName,
            //조회수에 의한 정렬
            @RequestParam(required = false) Boolean sortByView,
            //판매수에 의한 정렬
            @RequestParam(required = false) Boolean sortBySellCount,
            //좋아요수에 의한 정렬
            @RequestParam(required = false) Boolean sortByLikeCount,
            //출간일 최신순으로 정렬
            @RequestParam(required = false) Boolean latest,
            //도서명 내림차순
            @RequestParam(required = false) Boolean sortByBookTitleDesc,
            //도서명 오름차순
            @RequestParam(required = false) Boolean sortByBookTitleAsc,
            //리뷰순
            @RequestParam(required = false) Boolean sortByReviews
    );

    // 도서 아이디리스트를 이용한 도서 목록 조회
    @GetMapping("/api/books/ids")
    public List<BookDTO> getBooksByIds(
            @RequestParam List<Long> bookIds
    );

    // 도서 생성
    @PostMapping("/api/books")
    public ResponseEntity<BookDTO> createBook(
            @RequestBody @Valid BookRequestDTO bookRequestDTO);

    // 도서 아이디로 조회
    @GetMapping("/api/books/{bookId}")
    public BookDTO getBook(@PathVariable Long bookId);

    // 도서 수정
    @PutMapping("/api/books/{bookId}")
    public BookDTO updateBook(
            @PathVariable Long bookId,
            @RequestBody @Valid BookUpdateRequestDTO bookRequestDTO);

    // 도서 조회수 증가
    @PutMapping("/api/books/{bookId}/view")
    public ResponseEntity<Void> incrementBookView(@PathVariable Long bookId);

    // 도서 판매량 증가
    @PutMapping("/api/books/{bookId}/sell-count")
    public ResponseEntity<Void> incrementBookSellCount(@PathVariable Long bookId, @RequestParam int quantity);

    // 도서 삭제
    @DeleteMapping("/api/books/{bookId}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long bookId);

    // 도서 작가 목록 조회
    @GetMapping("/api/books/{bookId}/authors")
    public List<AuthorDTO> getAuthors(@PathVariable Long bookId);

    // 책 수량 증가
    @GetMapping("/api/books/{bookId}/amount-increase")
    public ResponseEntity<Void> incrementBookAmountIncrease(
            @PathVariable Long bookId, @RequestParam int quantity);

    // 좋아요 추가
    @PostMapping("/api/likes")
    public ResponseEntity<Void> createLike(
            @RequestParam Long bookId,
            @RequestParam String memberId);

    // 좋아요 수 조회
    @GetMapping("/api/books/{bookId}/likes")
    public ResponseEntity<Long> getLikeCount(
            @PathVariable Long bookId);

    // 도서 총계 조회(페이징용) - 필터링 조건을 반영하도록 수정
    @GetMapping("/api/books/total")
    public ResponseEntity<Long> getTotalBooks(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "categoryIds", required = false) List<Long> categoryIds,
            @RequestParam(value = "publisherName", required = false) String publisherName,
            @RequestParam(value = "authorName", required = false) String authorName
    );

}
