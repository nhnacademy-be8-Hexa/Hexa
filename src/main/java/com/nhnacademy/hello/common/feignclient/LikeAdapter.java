package com.nhnacademy.hello.common.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "hexa-gateway", contextId = "likeAdapter")
public interface LikeAdapter {

    /**
     * 새로운 좋아요 추가
     *
     * @param bookId   도서 ID
     * @param memberId 회원 ID
     * @return ResponseEntity<Void>
     */
    @PostMapping("/api/likes")
    ResponseEntity<Void> createLike(
            @RequestParam("bookId") Long bookId,
            @RequestParam("memberId") String memberId);

    /**
     * 특정 도서의 좋아요 합계 조회
     *
     * @param bookId 도서 ID
     * @return 좋아요 수
     */
    @GetMapping("/api/books/{bookId}/likes")
    ResponseEntity<Long> getLikeCount(@PathVariable("bookId") Long bookId);
}
