package com.nhnacademy.hello.controller.coupon;

import com.nhnacademy.hello.common.feignclient.MemberAdapter;
import com.nhnacademy.hello.common.feignclient.coupon.CouponAdapter;
import com.nhnacademy.hello.common.feignclient.coupon.CouponMemberAdapter;
import com.nhnacademy.hello.common.util.AuthInfoUtils;
import com.nhnacademy.hello.dto.coupon.CouponDTO;
import com.nhnacademy.hello.dto.member.MemberDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class EventController {

    private CouponMemberAdapter couponMemberAdapter;
    private MemberAdapter memberAdapter;

    @GetMapping("/event")
    public String Event(Model model){
        return"/event/event";
    }

    @PostMapping("/admin/coupon")
    public String issueCoupon(Model model) {

        // 먼저 로그인했는지 검사
        if(!AuthInfoUtils.isLogin()){
            // 로그인 하지 않았을 경우, 로그인 화면으로 이동
            return "redirect:/login";
        }

        // 현재 로그인된 아이디를 이용해서 api 로부터 멤버 정보 받아옴
        MemberDTO member = memberAdapter.getMember(AuthInfoUtils.getUsername());

        couponMemberAdapter.createMemberCoupon(member.memberId(),  1l);

        return "/event/event"; // 결과를 다시 보여줄 뷰
    }
}
