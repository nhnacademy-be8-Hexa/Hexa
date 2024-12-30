package com.nhnacademy.hello.controller.category;

import com.nhnacademy.hello.common.feignclient.CategoryAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryAdapter categoryAdapter;
}
