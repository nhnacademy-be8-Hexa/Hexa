package com.nhnacademy.hello.controller.admin;

import com.nhnacademy.hello.common.feignclient.ReviewAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/review")
public class ReviewManageController {
    private final ReviewAdapter reviewAdapter;

    // 리뷰 전체 조회
//    @GetMapping
//    public String getReviewsFromAdmin(@RequestParam("page") int page, @RequestParam("size") int size, @RequestParam("sort") String sort)

    // 특정 리뷰 상세 조회
    // 특정 리뷰 수정(차단 여부)
}
