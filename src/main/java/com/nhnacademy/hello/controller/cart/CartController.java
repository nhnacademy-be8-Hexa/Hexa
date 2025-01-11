package com.nhnacademy.hello.controller.cart;

import com.nhnacademy.hello.image.ImageStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class CartController {

    private final ImageStore imageStore;

    @GetMapping("/cart")
    public String Cart() {
        return "cart/cart";
    }

    @PostMapping("/cart/getBookImages")
    @ResponseBody
    public Map<String, String> getBookImages(@RequestBody List<String> bookIds) {
        Map<String, String> imagePaths = new HashMap<>();
        for (String bookId : bookIds) {
            String imageName = "bookThumbnail_" + bookId;
            List<String> pathList = imageStore.getImage(imageName);
            String imagePath = (pathList != null && !pathList.isEmpty()) ?
                    pathList.getFirst() : "/images/default-book.jpg"; // 기본 이미지 경로로 수정
            imagePaths.put(bookId, imagePath);
        }
        return imagePaths;
    }


}
