package com.nhnacademy.hello.controller.order;

import com.nhnacademy.hello.common.feignclient.*;
import com.nhnacademy.hello.common.feignclient.coupon.CouponAdapter;
import com.nhnacademy.hello.common.util.AuthInfoUtils;
import com.nhnacademy.hello.dto.delivery.DeliveryDTO;
import com.nhnacademy.hello.dto.member.MemberDTO;
import com.nhnacademy.hello.dto.order.OrderBookResponseDTO;
import com.nhnacademy.hello.dto.order.OrderDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class MyOrderController {

    private final MemberAdapter memberAdapter;
    private final OrderAdapter orderAdapter;
    private final OrderBookAdapter orderBookAdapter;
    private final CouponAdapter couponAdapter;
    private final WrappingPaperAdapter wrappingPaperAdapter;
    private final DeliveryAdapter deliveryAdapter;
    private final Long SIZE = 10L;

    @GetMapping("/mypage/orders")
    public String ordersPage(@RequestParam(value = "page", required = false, defaultValue = "1") Integer page, Model model) {

        String username = AuthInfoUtils.getUsername();
        MemberDTO member = memberAdapter.getMember(username);

        Long totalCount = orderAdapter.countAllByMember_MemberId(member.memberId()).getBody();

        Long totalPage = (totalCount + SIZE - 1) / SIZE;
                //(long) Math.ceil( (double)(totalCount/SIZE) );

        if(page <=0 || totalPage<page){
            page=1;
        }

        List<OrderDTO> ordersList = orderAdapter.getOrdersByMemberId(page-1, member.memberId()).getBody();
        model.addAttribute("member", member);
        model.addAttribute("ordersList", ordersList);
        model.addAttribute("pageSize", SIZE);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPage);


        return "member/orders";
    }


    @GetMapping("/mypage/orders/{orderId}")
    public String ordersPage(@PathVariable long orderId, Model model) {

        String username = AuthInfoUtils.getUsername();
        MemberDTO member = memberAdapter.getMember(username);

        Boolean result = Optional.ofNullable(orderAdapter.existsOrderIdAndMember_MemberId(orderId, member.memberId()).getBody())
                .orElse(Boolean.FALSE);

        if(!result){
            return "redirect:/?error=access_denied";
        }

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


        model.addAttribute("member", member);
        model.addAttribute("order", order);
        model.addAttribute("orderBookLists", orderBookLists);
        model.addAttribute("amountPrice", amountPrice);
        model.addAttribute("discountPrice", discountPrice);
        model.addAttribute("paymentAmountBookPrice", paymentAmountBookPrice);
        model.addAttribute("wrappingPaperName", wrappingPaperName);
        model.addAttribute("wrappingPaperPrice", wrappingPaperPrice);
        model.addAttribute("deliveryCost", deliveryCost);


        return "member/orderBooks";
    }
}
