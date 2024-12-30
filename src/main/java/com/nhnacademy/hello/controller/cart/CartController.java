package com.nhnacademy.hello.controller.cart;

import com.nhnacademy.hello.dto.cart.CartDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class CartController {

    @GetMapping("/cart")
    public String Cart() {
        return "cart/cart";
    }



    @GetMapping("/purchase")
    public String purchaseCartItem(
            @RequestParam List<Long> bookIds,
            Model model) {

        if (bookIds == null || bookIds.isEmpty()) {
            throw new IllegalArgumentException("cartIds가 전달되지 않았습니다.");
        }

        System.out.println("선택된 IDs: " + bookIds);
        model.addAttribute("carts", bookIds);
        return "purchase/purchase";
    }







}
