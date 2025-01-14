package com.nhnacademy.hello.controller.coupon;

import com.nhnacademy.hello.common.feignclient.coupon.CouponAdapter;
import com.nhnacademy.hello.common.feignclient.coupon.CouponMemberAdapter;
import com.nhnacademy.hello.dto.coupon.CouponDTO;
import com.nhnacademy.hello.dto.coupon.CouponMemberDTO;
import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@AllArgsConstructor
public class CouponConsumer {

    private final CouponMemberAdapter couponMemberAdapter;
    private final CouponAdapter couponAdapter;
    private final RabbitTemplate rabbitTemplate;

    public String couponGet() {

        Message message = rabbitTemplate.receive("hexa.coupon.queues");

        String memberId = new String(Objects.requireNonNull(message).getBody());

        List<CouponMemberDTO> couponMember = couponMemberAdapter.getMemberCoupons(memberId); // 멤버가 가진 쿠폰들
        List<String> couponNameMember = new ArrayList<>(); // 멤버가 가진 쿠폰 이름을 저장할 변수 선언

        for (CouponMemberDTO memberDTO : couponMember) { // 멤버가 가진 쿠폰 아이디 하나하나 돌면서 쿠폰 이름을 찾는 반복문
            CouponDTO couponIdMember = couponAdapter.getCouponById(memberDTO.couponId()); // 멤버가 가진 쿠폰 아이디로 쿠폰이름 찾기
            couponNameMember.add(couponIdMember.couponName()); // 멤버가 가진 쿠폰 이름 리스트에 넣기
        }

        List<CouponDTO> coupon = couponAdapter.getCouponByCouponName("겨울 쿠폰"); // 만들어진 쿠폰 중 멤버에게 나눠줄 쿠폰을 쿠폰 이름으로 찾기

        //boolean isCouponName = false;
        for (CouponDTO couponDTO : coupon) {
            for (String s : couponNameMember) {
                if (couponDTO.couponName().equals(s) && !couponMemberAdapter.checkCouponDuplicate(couponDTO.couponId())) { // 멤버가 가진 쿠폰 중 받으려는 쿠폰과 이름이 같은 쿠폰이 있는지 조사
                    return "이미 발급받은 쿠폰입니다.";
//                    isCouponName = true; // 있다면 참
                }
            }
            if(!couponMemberAdapter.checkCouponDuplicate(couponDTO.couponId())){ // 다른 사람이 받은 쿠폰이 아니고 멤버가 받은 적 없는 쿠폰이어야 함
                couponMemberAdapter.createMemberCoupon(memberId, couponDTO.couponId());
                return "쿠폰을 발급 받았습니다!";
            }
        }
        return "쿠폰 발급에 실패했습니다. 잠시후에 다시 시도해주세요";
    }
}