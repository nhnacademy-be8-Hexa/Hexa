package com.nhnacademy.hello.dto.category;

import lombok.Data;

@Data
public class PagedCategoryDTO {
    private Long categoryId;
    private String categoryName;
    private PagedCategoryDTO parentCategory;
}
