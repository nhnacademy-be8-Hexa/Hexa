package com.nhnacademy.hello.common.feignclient;

import com.nhnacademy.hello.dto.category.CategoryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "hexa-gateway", contextId = "categoryAdapter", path = "/api/categories")
public interface CategoryAdapter {

    // 기존 메서드: 모든 카테고리 조회
    @GetMapping
    ResponseEntity<List<CategoryDTO>> getCategories();

    /**
     * 1차 카테고리 생성
     *
     * @param category 생성할 카테고리 정보
     * @return 생성된 카테고리 정보
     */
    @PostMapping
    ResponseEntity<CategoryDTO> createCategory(@RequestBody CategoryDTO category);

    /**
     * 특정 1차 카테고리에 2차 카테고리 삽입
     *
     * @param categoryId    부모 1차 카테고리의 ID
     * @param subCategoryId 삽입할 2차 카테고리의 ID
     * @return 생성된 2차 카테고리 정보
     */
    @PostMapping("/{categoryId}/subcategories/{subCategoryId}")
    ResponseEntity<CategoryDTO> insertSubCategory(
            @PathVariable("categoryId") Long categoryId,
            @PathVariable("subCategoryId") Long subCategoryId
    );

    /**
     * 특정 카테고리에 책 삽입
     *
     * @param categoryId 부모 카테고리의 ID
     * @param bookId     삽입할 책의 ID
     * @return 응답 없음
     */
    @PostMapping("/{categoryId}/books/{bookId}")
    ResponseEntity<Void> insertBook(
            @PathVariable("categoryId") Long categoryId,
            @PathVariable("bookId") Long bookId
    );

    /**
     * 카테고리 삭제
     *
     * @param categoryId 삭제할 카테고리의 ID
     * @return 응답 없음
     */
    @DeleteMapping("/{categoryId}")
    ResponseEntity<Void> deleteCategory(@PathVariable("categoryId") Long categoryId);
}
