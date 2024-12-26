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
    public String Cart(
            Model model,
            HttpSession session
    ) {
        List<CartDTO> carts = (List<CartDTO>) session.getAttribute("carts");
        if(carts == null) {
            carts = new ArrayList<>();
        }

        model.addAttribute("carts", carts);
        return "cart/cart2";
    }

    @PostMapping("/cart/delete")
    public String deleteCartItem(
            @RequestParam Long cartId,
            HttpSession session) {
        List<CartDTO> cartItems = (List<CartDTO>) session.getAttribute("carts");
        if (cartItems != null) {
            cartItems.removeIf(cart -> cart.getCartId().equals(cartId));
            session.setAttribute("carts", cartItems); // 세션 데이터 업데이트
        }
        return "redirect:/cart"; // 장바구니 페이지로 리다이렉트
    }

    @PostMapping("/cart/deleteAll")
    public String deleteAllCartItems(HttpSession session) {
        List<CartDTO> cartItems = (List<CartDTO>) session.getAttribute("carts");
        if (cartItems != null) {
            cartItems.clear();
        }
        return "redirect:/cart";

    }






}
