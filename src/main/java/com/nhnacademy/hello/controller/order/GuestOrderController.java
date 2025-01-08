package com.nhnacademy.hello.controller.order;

import com.nhnacademy.hello.common.feignclient.DeliveryAdapter;
import com.nhnacademy.hello.common.feignclient.OrderAdapter;
import com.nhnacademy.hello.common.feignclient.OrderBookAdapter;
import com.nhnacademy.hello.dto.delivery.DeliveryDTO;
import com.nhnacademy.hello.dto.order.GuestOrderValidateRequestDTO;
import com.nhnacademy.hello.dto.order.OrderBookResponseDTO;
import com.nhnacademy.hello.dto.order.OrderDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.Objects;

@Controller
@RequiredArgsConstructor
public class GuestOrderController {

    private final OrderAdapter orderAdapter;
    private final OrderBookAdapter orderBookAdapter;
    private final PasswordEncoder passwordEncoder;
    private final DeliveryAdapter deliveryAdapter;

    @GetMapping("/guestOrder")
    public String guestOrderPage() {
        return "guest/guestOrderLogin";
    }

    @PostMapping("/guestOrder")
    public String checkGuestOrder(@RequestParam("orderId") Long orderId,
                                  @RequestParam("guestOrderPassword") String guestOrderPassword , Model model, RedirectAttributes redirectAttributes) {

        GuestOrderValidateRequestDTO guestOrderValidateRequestDTO = new GuestOrderValidateRequestDTO(orderId,passwordEncoder.encode(guestOrderPassword));
        String password = orderAdapter.getGuestOrderPassword(guestOrderValidateRequestDTO).getBody();
        boolean validate  = passwordEncoder.matches(guestOrderPassword,password);

        if (validate){
            orderAdapter.getOrderById(orderId);
            OrderBookResponseDTO[] orderBookLists = orderBookAdapter.getOrderBooksByOrderId(orderId);
            OrderDTO order  = orderAdapter.getOrderById(orderId).getBody();

            // 도서 총 금액
            long amountPrice = Arrays.stream(orderBookLists)
                    .filter(Objects::nonNull)  // null 값 필터링
                    .mapToInt(OrderBookResponseDTO::bookPrice)
                    .sum();

            // 결제 금액
            long paymentAmountBookPrice =  (order != null) ? order.orderPrice() : 0;

            String wrappingPaperName = (order != null && order.wrappingPaper() != null && order.wrappingPaper().wrappingPaperName() != null) ?   order.wrappingPaper().wrappingPaperName() : null;
            Integer wrappingPaperPrice = (order != null && order.wrappingPaper() != null && order.wrappingPaper().wrappingPaperPrice() != null) ?  order.wrappingPaper().wrappingPaperPrice() : 0;

            Integer deliveryCost = 0;
            try
            {
                DeliveryDTO delivery = deliveryAdapter.getDelivery(orderId);
                deliveryCost = delivery.deliveryAmount();
            } catch (Exception  e) {}

            long discountPrice = amountPrice-(paymentAmountBookPrice-wrappingPaperPrice-deliveryCost);

            model.addAttribute("order", order);
            model.addAttribute("orderBookLists", orderBookLists);
            model.addAttribute("amountPrice", amountPrice);
            model.addAttribute("discountPrice", discountPrice);
            model.addAttribute("paymentAmountBookPrice", paymentAmountBookPrice);
            model.addAttribute("wrappingPaperName", wrappingPaperName);
            model.addAttribute("wrappingPaperPrice", wrappingPaperPrice);
            model.addAttribute("deliveryCost", deliveryCost);
            return "guest/guestOrderInfo";
        }
        else {
            redirectAttributes.addFlashAttribute("error", "주문 정보가 올바르지 않습니다.");
            return "redirect:/guestOrder";
        }
    }
}
