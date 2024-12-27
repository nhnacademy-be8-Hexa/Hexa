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
        int totalQuantity = carts.stream().mapToInt(CartDTO::getCartAmount).sum();
        int totalPrice = carts.stream().mapToInt(cart -> cart.getCartAmount() * cart.getBook().getBookPrice()).sum();
        int deliveryFee = (totalPrice >= 30000) ? 0 : 3000;


        model.addAttribute("carts", carts);
        model.addAttribute("totalQuantity", totalQuantity);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("deliveryFee", deliveryFee);
        return "cart/cart";
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

    @GetMapping("/purchase")
    public String purchaseCartItem(
            @RequestParam List<Long> cartIds,
            Model model) {

        model.addAttribute("carts", cartIds);
        return "purchase/purchase";
    }







}
