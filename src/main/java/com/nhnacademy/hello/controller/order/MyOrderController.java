package com.nhnacademy.hello.controller.order;

import com.nhnacademy.hello.common.feignclient.*;
import com.nhnacademy.hello.common.feignclient.coupon.CouponAdapter;
import com.nhnacademy.hello.common.util.AuthInfoUtils;
import com.nhnacademy.hello.dto.delivery.DeliveryDTO;
import com.nhnacademy.hello.dto.member.MemberDTO;
import com.nhnacademy.hello.dto.order.OrderBookResponseDTO;
import com.nhnacademy.hello.dto.order.OrderDTO;
import com.nhnacademy.hello.dto.returns.ReturnsDTO;
import com.nhnacademy.hello.dto.returns.ReturnsReasonDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

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
    private final ReturnsReasonAdapter returnsReasonAdapter;
    private final ReturnsAdapter returnsAdapter;
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

        List<OrderDTO> ordersList = orderAdapter.getOrdersByMemberId(
                member.memberId(),
                page-1,
                SIZE.intValue(),
                "orderId,desc"
        ).getBody();
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

        if (!result) {
            return "redirect:/?error=access_denied";
        }

        OrderBookResponseDTO[] orderBookLists = orderBookAdapter.getOrderBooksByOrderId(orderId);
        OrderDTO order = orderAdapter.getOrderById(orderId).getBody();

        // 도서 총 금액
        long amountPrice = Arrays.stream(orderBookLists)
                .filter(Objects::nonNull)  // null 값 필터링
                .mapToInt(OrderBookResponseDTO::bookPrice)
                .sum();

        // 결제 금액
        long paymentAmountBookPrice = (order != null) ? order.orderPrice() : 0;

        String wrappingPaperName = (order != null && order.wrappingPaper() != null && order.wrappingPaper().wrappingPaperName() != null) ? order.wrappingPaper().wrappingPaperName() : null;
        Integer wrappingPaperPrice = (order != null && order.wrappingPaper() != null && order.wrappingPaper().wrappingPaperPrice() != null) ? order.wrappingPaper().wrappingPaperPrice() : 0;

        Integer deliveryCost = 0;
        try {
            DeliveryDTO delivery = deliveryAdapter.getDelivery(orderId);
            deliveryCost = delivery.deliveryAmount();
        } catch (Exception e) {
            // 배달비가 없을 경우 기본값 유지
        }

        long discountPrice = amountPrice - (paymentAmountBookPrice - wrappingPaperPrice - deliveryCost);

        // 반품 정보 추가
        String returnsReason = null;
        String returnsDetail = null;
        if (order != null && (order.orderStatus().orderStatusId() == 4 || order.orderStatus().orderStatusId() == 6)) {
            ReturnsDTO returns = returnsAdapter.getReturnsByOrderId(orderId);  // returnsAdapter를 사용
            returnsReason = (returns != null && returns.returnsReason() != null) ? returns.returnsReason().returnsReason() : "정보 없음";
            returnsDetail = (returns != null && returns.returnsDetail() != null) ? returns.returnsDetail() : "정보 없음";
        }

        model.addAttribute("member", member);
        model.addAttribute("order", order);
        model.addAttribute("orderBookLists", orderBookLists);
        model.addAttribute("amountPrice", amountPrice);
        model.addAttribute("discountPrice", discountPrice);
        model.addAttribute("paymentAmountBookPrice", paymentAmountBookPrice);
        model.addAttribute("wrappingPaperName", wrappingPaperName);
        model.addAttribute("wrappingPaperPrice", wrappingPaperPrice);
        model.addAttribute("deliveryCost", deliveryCost);
        model.addAttribute("returnsReason", returnsReason);
        model.addAttribute("returnsDetail", returnsDetail);

        // 반품 사유 리스트
        List<ReturnsReasonDTO> returnsReasonList = returnsReasonAdapter.getAllReturnsReasons();
        model.addAttribute("returnsReasonList", returnsReasonList);

        return "member/orderBooks";
    }
    @GetMapping("/mypage/cancel-refunds")
    public String cancelRefundPage(
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size,
            Model model) {

        // 현재 로그인한 사용자 확인
        String username = AuthInfoUtils.getUsername();
        MemberDTO member = memberAdapter.getMember(username);

        // 상태 ID 설정: 반품(4), 반품 대기(6), 취소(5)
        Long refundStatusId = 4L;
        Long refundPendingStatusId = 6L;
        Long cancelStatusId = 5L;

        // API를 통해 사용자의 반품/반품 대기 주문 가져오기
        List<OrderDTO> refundList = orderAdapter.getOrdersByMemberId(
                        member.memberId(), page - 1, size, "orderId,desc"
                ).getBody().stream()
                .filter(order -> order.orderStatus().orderStatusId().equals(refundStatusId)
                        || order.orderStatus().orderStatusId().equals(refundPendingStatusId))
                .toList();

        long refundCount = refundList.size();
        long refundTotalPages = (refundCount + size - 1) / size;

        // API를 통해 사용자의 취소 주문 가져오기
        List<OrderDTO> cancelList = orderAdapter.getOrdersByMemberId(
                        member.memberId(), page - 1, size, "orderId,desc"
                ).getBody().stream()
                .filter(order -> order.orderStatus().orderStatusId().equals(cancelStatusId))
                .toList();

        long cancelCount = cancelList.size();
        long cancelTotalPages = (cancelCount + size - 1) / size;

        // 페이지 유효성 검증 및 빈 목록 처리
        if (page <= 0) {
            page = 1;
        }

        if (refundTotalPages == 0) {
            refundTotalPages = 1; // 최소 1 페이지는 존재하도록 설정
        }
        if (cancelTotalPages == 0) {
            cancelTotalPages = 1; // 최소 1 페이지는 존재하도록 설정
        }

        if (page > refundTotalPages) {
            page = (int) refundTotalPages; // 현재 페이지가 총 페이지 수를 초과하지 않도록 조정
        }
        if (page > cancelTotalPages) {
            page = (int) cancelTotalPages; // 현재 페이지가 총 페이지 수를 초과하지 않도록 조정
        }

        // 모델에 데이터 추가
        model.addAttribute("member", member);
        model.addAttribute("refundList", refundList);
        model.addAttribute("refundCurrentPage", page);
        model.addAttribute("refundTotalPages", refundTotalPages);

        model.addAttribute("cancelList", cancelList);
        model.addAttribute("cancelCurrentPage", page);
        model.addAttribute("cancelTotalPages", cancelTotalPages);

        return "member/orderCancelReturn";
    }
}
