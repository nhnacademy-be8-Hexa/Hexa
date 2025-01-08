package com.nhnacademy.hello.controller.admin;

import com.nhnacademy.hello.common.feignclient.CategoryAdapter;
import com.nhnacademy.hello.dto.category.CategoryDTO;
import com.nhnacademy.hello.dto.category.FirstCategoryRequestDTO;
import com.nhnacademy.hello.dto.category.PagedCategoryDTO;
import com.nhnacademy.hello.dto.category.SecondCategoryRequestDTO;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
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

    public CategoryManageController(CategoryAdapter categoryAdapter) {
        this.categoryAdapter = categoryAdapter;
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

        List<CategoryDTO> categories = categoryAdapter.getCategories().getBody();

        List<Long> categoriesWithSubCategories =
                findCategoriesIdsWithSubCategories(categories != null ? categories : Collections.emptyList());

        List<PagedCategoryDTO> pagedCategories =
                categoryAdapter.getAllPagedCategories(adjustedPage, PAGE_SIZE).getBody();
        List<PagedCategoryDTO> unPagedCategories = categoryAdapter.getAllUnPagedCategories().getBody();

        FirstCategoryRequestDTO firstCategoryRequestDTO = new FirstCategoryRequestDTO("");
        SecondCategoryRequestDTO secondCategoryRequestDTO = new SecondCategoryRequestDTO(null, null);

        model.addAttribute("unPagedCategories", unPagedCategories);
        model.addAttribute("pagedCategories", pagedCategories);
        model.addAttribute("categoriesWithSubCategories", categoriesWithSubCategories);
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
        categoryAdapter.insertCategory(secondCategoryRequestDTO.categoryId(), secondCategoryRequestDTO.subCategoryId());
        return "redirect:/admin/categoryManage";
    }

    @PostMapping("/{categoryId}")
    public String deleteCategory(@PathVariable Long categoryId) {
        categoryAdapter.deleteCategory(categoryId);
        return "redirect:/admin/categoryManage";
    }

    /**
     * 주어진 카테고리 목록에서 서브 카테고리가 존재하는 카테고리들의 ID를 반환하는 메서드.
     *
     * @param allCategories 모든 카테고리들의 리스트
     * @return 서브 카테고리가 존재하는 카테고리들의 ID를 담은 리스트
     */
    public List<Long> findCategoriesIdsWithSubCategories(List<CategoryDTO> allCategories) {
        return allCategories.stream()  // 스트림을 생성하여 allCategories 리스트에서 처리 시작

                // 서브 카테고리가 존재하는 카테고리만 필터링
                .filter(category -> !category.getSubCategories().isEmpty())  // 서브 카테고리가 비어있지 않으면 해당 카테고리만 필터링

                // 필터링된 카테고리에서 카테고리 ID를 추출
                .map(CategoryDTO::getCategoryId)  // 각 카테고리에서 ID만 추출

                // 최종 결과를 리스트로 수집하여 반환
                .collect(Collectors.toList());  // 카테고리 ID들을 List로 수집하여 반환
    }

}
