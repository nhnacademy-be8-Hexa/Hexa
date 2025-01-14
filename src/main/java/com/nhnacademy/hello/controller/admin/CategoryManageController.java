package com.nhnacademy.hello.controller.admin;

import com.nhnacademy.hello.common.feignclient.CategoryAdapter;
import com.nhnacademy.hello.dto.category.FirstCategoryRequestDTO;
import com.nhnacademy.hello.dto.category.PagedCategoryDTO;
import com.nhnacademy.hello.dto.category.SecondCategoryRequestDTO;
import jakarta.validation.Valid;
import java.util.List;
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

        List<Long> categoryIdsWithSubCategories = categoryAdapter.getCategoryIdsWithSubCategories().getBody();
        model.addAttribute("categoryIdsWithSubCategories", categoryIdsWithSubCategories);

        List<PagedCategoryDTO> pagedCategories =
                categoryAdapter.getAllPagedCategories(adjustedPage, PAGE_SIZE).getBody();
        model.addAttribute("pagedCategories", pagedCategories);


        List<PagedCategoryDTO> allCategories = categoryAdapter.getAllCategories().getBody();
        model.addAttribute("allCategories", allCategories);

        FirstCategoryRequestDTO firstCategoryRequestDTO = new FirstCategoryRequestDTO("");
        SecondCategoryRequestDTO secondCategoryRequestDTO = new SecondCategoryRequestDTO(null, null);

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

}