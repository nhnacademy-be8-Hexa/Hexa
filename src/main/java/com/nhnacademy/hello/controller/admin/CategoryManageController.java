package com.nhnacademy.hello.controller.admin;

import com.nhnacademy.hello.common.feignclient.CategoryAdapter;
import com.nhnacademy.hello.dto.category.CategoryDTO;
import com.nhnacademy.hello.dto.category.FirstCategoryRequestDTO;
import com.nhnacademy.hello.dto.category.PagedCategoryDTO;
import com.nhnacademy.hello.dto.category.SecondCategoryRequestDTO;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Objects;
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

    public CategoryManageController(CategoryAdapter categoryAdapter) {
        this.categoryAdapter = categoryAdapter;
    }

    @GetMapping
    public String categoryList(
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            Model model) {
        final int size = 9;

        int adjustedPage = (page != null && page > 1) ? page - 1 : 0;

        Long totalCategories = categoryAdapter.getTotal().getBody();

        int totalPages = (int) Math.ceil((double) totalCategories / size);

        if (page > totalPages && totalPages != 0) {
            adjustedPage = totalPages - 1;
            page = totalPages;
        }

        List<CategoryDTO> categories = categoryAdapter.getCategories().getBody();
        List<Long> categoriesWithSubCategories = findCategoriesIdsWithSubCategories(Objects.requireNonNull(categories));
        List<PagedCategoryDTO> pagedCategories = categoryAdapter.getAllPagedCategories(adjustedPage, size).getBody();
        List<PagedCategoryDTO> unPagedCategories = categoryAdapter.getAllUnPagedCategories().getBody();
        FirstCategoryRequestDTO firstCategoryRequestDTO = new FirstCategoryRequestDTO("");
        SecondCategoryRequestDTO secondCategoryRequestDTO = new SecondCategoryRequestDTO(null, null);
        
        model.addAttribute("unPagedCategories", unPagedCategories);
        model.addAttribute("categoriesWithSubCategories", categoriesWithSubCategories);
        model.addAttribute("firstCategoryRequestDTO", firstCategoryRequestDTO);
        model.addAttribute("secondCategoryRequestDTO", secondCategoryRequestDTO);
        model.addAttribute("pagedCategories", pagedCategories);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("size", size);

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

    public List<Long> findCategoriesIdsWithSubCategories(List<CategoryDTO> allCategories) {
        return allCategories.stream()
                .filter(category -> !category.getSubCategories().isEmpty())
                .map(CategoryDTO::getCategoryId)
                .collect(Collectors.toList());
    }

}
