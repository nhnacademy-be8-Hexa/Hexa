package com.nhnacademy.hello.common.feignclient.tag;

import com.nhnacademy.hello.dto.tag.TagDTO;
import com.nhnacademy.hello.dto.tag.TagRequestDTO;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "hexa-gateway", contextId = "tagAdapter" ,path = "/api")
public interface TagAdapter {
    @PostMapping("/admin/tags")
    public ResponseEntity<TagDTO> createTag(
            @RequestBody @Valid TagRequestDTO requestDTO
    );

    @PutMapping("/admin/tags/{tagId}")
    public ResponseEntity<Void> updateTag(
            @RequestBody @Valid TagRequestDTO requestDTO,
            @PathVariable Long tagId
    );

    @GetMapping("/tags")
    public ResponseEntity<List<TagDTO>> getAllTags(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam("sort") String sort
            );

    @DeleteMapping("/admin/tags/{tagId}")
    public ResponseEntity<Void> deleteTag(
            @PathVariable Long tagId
    );


    // 전체 태그 수 조회 (검색 조건 포함)
    @GetMapping("admin/tags/count")
    public ResponseEntity<Long> getTotalTags();

    @GetMapping("/tags/{tagId}")
    public ResponseEntity<TagDTO> getTagById(@PathVariable Long tagId);

}
