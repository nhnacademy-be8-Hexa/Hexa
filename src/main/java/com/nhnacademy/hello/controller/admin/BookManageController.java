package com.nhnacademy.hello.controller.admin;

import com.nhnacademy.hello.common.feignclient.BookAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;

@RequiredArgsConstructor
@Controller
public class BookManageController{

    private final BookAdapter bookAdapter;


}
