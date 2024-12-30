package com.nhnacademy.hello.dto.category;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
public class CategoryDTO {
    private Long categoryId;
    private String categoryName;
    @Getter
    private List<CategoryDTO> subCategories = new ArrayList<>();
}
