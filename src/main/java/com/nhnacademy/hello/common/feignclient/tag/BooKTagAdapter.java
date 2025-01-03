package com.nhnacademy.hello.common.feignclient.tag;

import com.nhnacademy.hello.dto.book.BookDTO;
import com.nhnacademy.hello.dto.tag.TagDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "hexa-gateway", contextId = "bookTagAdapter")
public interface BooKTagAdapter {

    @PostMapping("/admin/books/{bookId}/tags/{tagId}")
    public ResponseEntity<Void> addBookTag(@PathVariable Long bookId, @PathVariable Long tagId);

    @GetMapping("/books/{bookId}/tags")
    public ResponseEntity<List<TagDTO>> getTagsByBook(@PathVariable Long bookId);

    @GetMapping("/tags/{tagId}/books")
    public ResponseEntity<List<BookDTO>> getBooksByTag(@PathVariable Long tagId,
                                                       @RequestParam("page") int page,
                                                       @RequestParam("size") int size,
                                                       @RequestParam("sort") String sort);

    @DeleteMapping("/admin/books/{bookId}/tags/{tagId}")
    public ResponseEntity<Void> deleteBookTag(@PathVariable Long bookId, @PathVariable Long tagId);

}
