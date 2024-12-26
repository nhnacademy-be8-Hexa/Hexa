package com.nhnacademy.hello.common.feignclient;

import com.nhnacademy.hello.dto.book.BookDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "hexa-gateway", path = "/api/books")
public interface BookAdapter {

    // 통합된 도서 목록 조회
    @GetMapping
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
            @RequestParam(required = false) Boolean latest
    );

    // 도서 생성
    @PostMapping
    public ResponseEntity<BookDTO> createBook(
            @RequestBody @Valid BookRequestDTO bookRequestDTO);

    // 도서 아이디로 조회
    @GetMapping("/{bookId}")
    public BookDTO getBook(@PathVariable Long bookId);

    // 도서 수정
    @PutMapping("/{bookId}")
    public BookDTO updateBook(
            @PathVariable Long bookId,
            @RequestBody @Valid BookUpdateRequestDTO bookRequestDTO);

    // 도서 조회수 증가
    @PutMapping("/{bookId}/view")
    public ResponseEntity<Void> incrementBookView(@PathVariable Long bookId);

    // 도서 판매량 증가
    @PutMapping("/{bookId}/sell-count")
    public ResponseEntity<Void> incrementBookSellCount(@PathVariable Long bookId, @RequestParam int quantity);

    // 도서 삭제
    @DeleteMapping("/{bookId}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long bookId);

    // 도서 작가 목록 조회
    @GetMapping("/{bookId}/authors")
    public List<Author> getAuthors(@PathVariable Long bookId);

    // 책 수량 증가
    @GetMapping("/{bookId}/amount-increase")
    public ResponseEntity<Void> incrementBookAmountIncrease(
            @PathVariable Long bookId, @RequestParam int quantity);

}
