package com.nhnacademy.hello.controller.admin;

import com.nhnacademy.hello.common.feignclient.BookAdapter;
import com.nhnacademy.hello.common.feignclient.CategoryAdapter;
import com.nhnacademy.hello.dto.category.CategoryDTO;
import com.nhnacademy.hello.dto.category.FirstCategoryRequestDTO;
import com.nhnacademy.hello.dto.category.PagedCategoryDTO;
import com.nhnacademy.hello.dto.category.SecondCategoryRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class CategoryManageControllerTest {

    @Mock
    private CategoryAdapter categoryAdapter;

    @Mock
    private BookAdapter bookAdapter;

    @Mock
    private Model model;

    private CategoryManageController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new CategoryManageController(categoryAdapter, bookAdapter);
    }

    @Test
    @DisplayName("카테고리 목록 페이지 표시 - 성공")
    void testCategoryList() {
        // Given
        int page = 1;
        int totalCategories = 20;

        PagedCategoryDTO parentCategory = new PagedCategoryDTO();
        parentCategory.setCategoryId(1L);
        parentCategory.setCategoryName("ParentCategory");

        PagedCategoryDTO childCategory = new PagedCategoryDTO();
        childCategory.setCategoryId(2L);
        childCategory.setCategoryName("ChildCategory");
        childCategory.setParentCategory(parentCategory);

        List<PagedCategoryDTO> pagedCategories = List.of(parentCategory, childCategory);

        CategoryDTO category1 = new CategoryDTO();
        category1.setCategoryId(1L);
        category1.setCategoryName("Category1");

        CategoryDTO category2 = new CategoryDTO();
        category2.setCategoryId(2L);
        category2.setCategoryName("Category2");

        List<CategoryDTO> categories = List.of(category1, category2);

        // Mock 설정
        when(categoryAdapter.getTotal()).thenReturn(ResponseEntity.ok((long) totalCategories));
        when(categoryAdapter.getCategories()).thenReturn(ResponseEntity.ok(categories));
        // 수정: 새 메서드는 세 개의 파라미터(page, size, sort)를 받으므로 sort 값을 추가한다.
        when(categoryAdapter.getAllPagedCategories(anyInt(), eq(9), anyString()))
                .thenReturn(ResponseEntity.ok(pagedCategories));
        when(categoryAdapter.getAllCategories()).thenReturn(ResponseEntity.ok(pagedCategories));

        // When
        String viewName = controller.categoryList(page, model);

        // Then
        assertEquals("admin/categoryManageForm", viewName);
        verify(model).addAttribute(eq("currentPage"), eq(page));
        verify(model).addAttribute(eq("totalPages"), eq(3)); // 20 / 9 = 2.22 -> 총 3페이지
        verify(model).addAttribute(eq("size"), eq(9));
        verify(model).addAttribute(eq("pagedCategories"), eq(pagedCategories));
        verify(model).addAttribute(eq("allCategories"), eq(pagedCategories));
        verify(model).addAttribute(eq("categoryIdsWithSubCategories"), anyList());
    }


    @Test
    @DisplayName("1차 카테고리 생성 - 성공")
    void testCreateFirstCategory() {
        // Given
        FirstCategoryRequestDTO requestDTO = new FirstCategoryRequestDTO("NewCategory");

        // When
        String viewName = controller.createFirstCategory(requestDTO);

        // Then
        assertEquals("redirect:/admin/categoryManage", viewName);
        verify(categoryAdapter).createCategory(eq(requestDTO));
    }

    @Test
    @DisplayName("2차 카테고리 생성 - 성공")
    void testCreateSecondCategory() {
        // Given: parentCategoryId가 필요하지 않다면 null 전달 (또는 필요한 값을 전달)
        SecondCategoryRequestDTO requestDTO = new SecondCategoryRequestDTO(1L, 2L, null);

        // 새 2차 카테고리 생성 전에, 컨트롤러에서 categoryAdapter.getAllBooksByCategoryId(...)를 호출할 경우를 위해 모의 설정
        when(categoryAdapter.getAllBooksByCategoryId(anyLong()))
                .thenReturn(ResponseEntity.ok(Collections.emptyList()));

        // When
        String viewName = controller.createSecondCategory(requestDTO);

        // Then
        assertEquals("redirect:/admin/categoryManage", viewName);
        verify(categoryAdapter).insertCategory(eq(1L), eq(2L));
    }



    @Test
    @DisplayName("카테고리 삭제 - 성공")
    void testDeleteCategory() {
        // Given
        Long categoryId = 1L;

        // When
        String viewName = controller.deleteCategory(categoryId);

        // Then
        assertEquals("redirect:/admin/categoryManage", viewName);
        verify(categoryAdapter).deleteCategory(eq(categoryId));
    }
}