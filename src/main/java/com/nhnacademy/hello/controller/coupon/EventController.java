package com.nhnacademy.hello.controller.coupon;

import com.nhnacademy.hello.common.feignclient.MemberAdapter;
import com.nhnacademy.hello.common.feignclient.coupon.CouponAdapter;
import com.nhnacademy.hello.common.feignclient.coupon.CouponMemberAdapter;
import com.nhnacademy.hello.common.util.AuthInfoUtils;
import com.nhnacademy.hello.dto.coupon.CouponDTO;
import com.nhnacademy.hello.dto.member.MemberDTO;
import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
    private final RabbitTemplate rabbitTemplate;
    private final CouponConsumer couponGet;

    @GetMapping("/event")
    public String Event(Model model){
        return"event/event";
    }

    @PostMapping("/coupon-get")
    public String issueCoupon(Model model) {

        // 먼저 로그인했는지 검사
        if(!AuthInfoUtils.isLogin()){
            // 로그인 하지 않았을 경우, 로그인 화면으로 이동
            return "redirect:/login";
        }

        MemberDTO member = memberAdapter.getMember(AuthInfoUtils.getUsername());

        // 쿠폰 발급 요청 메시지 생성
        String couponRequestMessage = member.memberId();

        // 쿠폰 발급 요청을 메시지 큐에 전송
        rabbitTemplate.convertAndSend("hexa.coupon.exchanges","hexa.coupon.binding", couponRequestMessage);

        couponGet.couponGet();

        return "redirect:/event";
    }
}
