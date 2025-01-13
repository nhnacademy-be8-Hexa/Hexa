package com.nhnacademy.hello.controller.coupon;

import com.nhnacademy.hello.common.feignclient.MemberAdapter;
import com.nhnacademy.hello.common.feignclient.coupon.CouponAdapter;
import com.nhnacademy.hello.common.feignclient.coupon.CouponMemberAdapter;
import com.nhnacademy.hello.common.util.AuthInfoUtils;
import com.nhnacademy.hello.dto.coupon.CouponDTO;
import com.nhnacademy.hello.dto.member.MemberDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@AllArgsConstructor
public class EventController {

    private final CouponMemberAdapter couponMemberAdapter;
    private final MemberAdapter memberAdapter;
    private final CouponAdapter couponAdapter;

    @GetMapping("/event")
    public String Event(Model model){
        return"/event/event";
    }

    @PostMapping("/coupon-get")
    public String issueCoupon(Model model) {

        // 먼저 로그인했는지 검사
        if(!AuthInfoUtils.isLogin()){
            // 로그인 하지 않았을 경우, 로그인 화면으로 이동
            return "redirect:/login";
        }

        MemberDTO member = memberAdapter.getMember(AuthInfoUtils.getUsername());

        List<CouponDTO> coupon = couponAdapter.getCouponByCouponName("test");

        for (CouponDTO couponDTO : coupon) {
            if (!couponMemberAdapter.isCouponAlreadyAssigned(couponDTO.couponId(), member.memberId())
                    && !couponMemberAdapter.checkCouponDuplicate(couponDTO.couponId())) {
                couponMemberAdapter.createMemberCoupon(member.memberId(), couponDTO.couponId());
                break; // 한 개만 발급되도록 루프 종료
            }
        }

        return "/event/event";
    }
}
