package com.nhnacademy.hello.common.feignclient;

import com.nhnacademy.hello.dto.category.CategoryDTO;
import com.nhnacademy.hello.dto.category.FirstCategoryRequestDTO;
import com.nhnacademy.hello.dto.category.PagedCategoryDTO;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

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
    ResponseEntity<PagedCategoryDTO> createCategory(@RequestBody FirstCategoryRequestDTO category);

    /**
     * 특정 1차 카테고리에 2차 카테고리 삽입
     *
     * @param categoryId    부모 1차 카테고리의 ID
     * @param subCategoryId 삽입할 2차 카테고리의 ID
     * @return 생성된 2차 카테고리 정보
     */

    @PostMapping("/{categoryId}/subcategories/{subCategoryId}")
    ResponseEntity<PagedCategoryDTO> insertCategory(
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
     * 모든 카테고리에서 서브카테고리가 존재하는 카테고리들의 ID를 반환
     *
     * @return 서브카테고리가 있는 카테고리들의 ID 목록
     */
    @GetMapping("/ids")
    ResponseEntity<List<Long>> findCategoryIdsWithSubCategories();

    /**
     * 주어진 카테고리 ID와 그 하위 서브 카테고리들의 ID 목록을 반환합니다.
     *
     * @param categoryId 조회할 카테고리 ID
     * @return 카테고리 및 서브 카테고리들의 ID 목록
     */
    @GetMapping("/ids/{categoryId}")
    ResponseEntity<List<Long>> extractCategoryIds(@PathVariable Long categoryId);


    /**
     * 카테고리 목록을 페이징 처리하여 반환합니다.
     *
     * @param page 요청된 페이지 번호
     * @param size 한 페이지에 포함될 카테고리 수
     * @return 페이징된 카테고리 목록
     */
    @GetMapping("/paged")
    ResponseEntity<List<PagedCategoryDTO>> getAllPagedCategories(
            @RequestParam("page") int page,
            @RequestParam("size") int size
    );

    /**
     * 카테고리 목록을 페이징 처리하지 않고 전체 데이터를 반환하는 엔드포인트
     *
     * @return 전체 카테고리 목록
     */
    @GetMapping("/all")
    ResponseEntity<List<PagedCategoryDTO>> getAllCategories();

    /**
     * 전체 카테고리 수를 반환하는 엔드포인트.
     *
     * @return 전체 카테고리 수
     */
    @GetMapping("/total")
    ResponseEntity<Long> getTotal();

    /**
     * 카테고리 삭제
     *
     * @param categoryId 삭제할 카테고리의 ID
     * @return 응답 없음
     */
    @DeleteMapping("/{categoryId}")
    ResponseEntity<Void> deleteCategory(@PathVariable("categoryId") Long categoryId);
    
    // bookId로 해당 책의 카테고리 리스트 조회
    @GetMapping("/books/{bookId}")
    ResponseEntity<List<CategoryDTO>> getAllCategoriesByBookId(@PathVariable Long bookId);
}
