package com.nhnacademy.hello.controller.advice;

import com.nhnacademy.hello.common.feignclient.CategoryAdapter;
import com.nhnacademy.hello.dto.category.CategoryDTO;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@Profile("!test")
public class GlobalControllerAdvice {

    @Autowired
    private CategoryAdapter categoryAdapter;

    @ModelAttribute
    public void addCategoriesToModel(Model model) {
        ResponseEntity<List<CategoryDTO>> categoryResponse = categoryAdapter.getCategories();
        if (categoryResponse.getStatusCode().is2xxSuccessful()) {
            List<CategoryDTO> categories = categoryResponse.getBody();
            model.addAttribute("categories", categories);
        } else {
            model.addAttribute("categories", List.of());
        }
    }
}
