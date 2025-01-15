package com.nhnacademy.hello.controller.admin;

import com.nhnacademy.hello.common.feignclient.BookAdapter;
import com.nhnacademy.hello.common.feignclient.CategoryAdapter;
import com.nhnacademy.hello.dto.book.BookDTO;
import com.nhnacademy.hello.dto.category.CategoryDTO;
import com.nhnacademy.hello.dto.category.FirstCategoryRequestDTO;
import com.nhnacademy.hello.dto.category.PagedCategoryDTO;
import com.nhnacademy.hello.dto.category.SecondCategoryRequestDTO;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/categoryManage")
@Slf4j
public class CategoryManageController {
    private final CategoryAdapter categoryAdapter;
    private static final int PAGE_SIZE = 9;
    private final BookAdapter bookAdapter;

    public CategoryManageController(CategoryAdapter categoryAdapter, BookAdapter bookAdapter) {
        this.categoryAdapter = categoryAdapter;
        this.bookAdapter = bookAdapter;
    }

    @GetMapping
    public String categoryList(
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            Model model) {


        int adjustedPage = (page != null && page > 1) ? page - 1 : 0;

        Long totalCategories = categoryAdapter.getTotal().getBody();

        int totalPages = (int) Math.ceil((double) totalCategories / PAGE_SIZE);

        if (page > totalPages && totalPages != 0) {
            adjustedPage = totalPages - 1;
            page = totalPages;
        }

        List<Long> categoryIdsWithSubCategories = categoryAdapter.getCategories().getBody().stream()
                .filter(category -> !category.getSubCategories().isEmpty())
                .map(CategoryDTO::getCategoryId)
                .toList();

        model.addAttribute("categoryIdsWithSubCategories", categoryIdsWithSubCategories);

        // 2차 카테고리에 포함된 도서가 있을경우
        List<PagedCategoryDTO> pagedCategories =
                categoryAdapter.getAllPagedCategories(adjustedPage, PAGE_SIZE).getBody();
        model.addAttribute("pagedCategories", pagedCategories);


        List<PagedCategoryDTO> allCategories = categoryAdapter.getAllCategories().getBody();
        model.addAttribute("allCategories", allCategories);


        FirstCategoryRequestDTO firstCategoryRequestDTO = new FirstCategoryRequestDTO("");
        SecondCategoryRequestDTO secondCategoryRequestDTO = new SecondCategoryRequestDTO(null, null, null);


        model.addAttribute("firstCategoryRequestDTO", firstCategoryRequestDTO);
        model.addAttribute("secondCategoryRequestDTO", secondCategoryRequestDTO);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("size", PAGE_SIZE);


        return "admin/categoryManageForm";
    }


    @PostMapping
    public String createFirstCategory(
            @Valid @ModelAttribute("firstCategoryRequestDTO") FirstCategoryRequestDTO firstCategoryRequestDTO) {
        categoryAdapter.createCategory(firstCategoryRequestDTO);
        return "redirect:/admin/categoryManage";
    }

    
    @PostMapping("/add")
    public String createSecondCategory(
            @Valid @ModelAttribute("secondCategoryRequestDTO") SecondCategoryRequestDTO secondCategoryRequestDTO) {

        // 사용자가 선택한 카테고리와 연결 되어있는 모든 책들의 ID를 조회
        List<BookDTO> books =
                categoryAdapter.getAllBooksByCategoryId(secondCategoryRequestDTO.subCategoryId()).getBody();
        List<Long> bookIds = books.stream().map(BookDTO::bookId).toList();

        // 원래 부모 카테고리가 존재한다면, 해당 카테고리와 연결된 책들의 관계를 삭제
        if (Objects.nonNull(secondCategoryRequestDTO.parentCategoryId())) {
            categoryAdapter.deleteByCategoryIdAndBookIds(secondCategoryRequestDTO.parentCategoryId(), bookIds);
        }

        // 새로 지정된 부모 카테고리가 선택되었다면, 책들과 새 부모 카테고리 간의 관계를 생성
        if (!secondCategoryRequestDTO.categoryId().equals(0L)) {
            categoryAdapter.insertBooks(secondCategoryRequestDTO.categoryId(), bookIds);
        }

        categoryAdapter.insertCategory(secondCategoryRequestDTO.categoryId(), secondCategoryRequestDTO.subCategoryId());

        return "redirect:/admin/categoryManage";
    }

    @PostMapping("/{categoryId}")
    public String deleteCategory(@PathVariable Long categoryId) {
        categoryAdapter.deleteCategory(categoryId);
        return "redirect:/admin/categoryManage";
    }

}