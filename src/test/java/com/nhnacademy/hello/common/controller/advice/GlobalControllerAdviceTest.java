package com.nhnacademy.hello.common.controller.advice;

import com.nhnacademy.hello.common.feignclient.CategoryAdapter;
import com.nhnacademy.hello.controller.advice.GlobalControllerAdvice;
import com.nhnacademy.hello.dto.category.CategoryDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

class GlobalControllerAdviceTest {

    @Mock
    private CategoryAdapter categoryAdapter;

    @Mock
    private Model model;

    @InjectMocks
    private GlobalControllerAdvice globalControllerAdvice;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("addCategoriesToModel - 성공적으로 카테고리를 모델에 추가")
    void testAddCategoriesToModel_Success() {
        // given
        List<CategoryDTO> mockCategories = new ArrayList<>();
        CategoryDTO category1 = new CategoryDTO();
        category1.setCategoryId(1L);
        category1.setCategoryName("Category 1");

        CategoryDTO category2 = new CategoryDTO();
        category2.setCategoryId(2L);
        category2.setCategoryName("Category 2");

        mockCategories.add(category1);
        mockCategories.add(category2);
        ResponseEntity<List<CategoryDTO>> response = new ResponseEntity<>(mockCategories, HttpStatus.OK);
        when(categoryAdapter.getCategories()).thenReturn(response);

        // when
        globalControllerAdvice.addCategoriesToModel(model);

        // then
        verify(model, times(1)).addAttribute("categories", mockCategories);
    }

    @Test
    @DisplayName("addCategoriesToModel - 실패 시 빈 리스트를 모델에 추가")
    void testAddCategoriesToModel_Failure() {
        // given
        ResponseEntity<List<CategoryDTO>> response = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        when(categoryAdapter.getCategories()).thenReturn(response);

        // when
        globalControllerAdvice.addCategoriesToModel(model);

        // then
        verify(model, times(1)).addAttribute("categories", List.of());
    }
}
