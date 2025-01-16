package com.nhnacademy.hello.controller.coupon;

import com.nhnacademy.hello.common.feignclient.coupon.CouponAdapter;
import com.nhnacademy.hello.common.feignclient.coupon.CouponMemberAdapter;
import com.nhnacademy.hello.dto.coupon.CouponDTO;
import com.nhnacademy.hello.dto.coupon.CouponMemberDTO;
import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;

import java.util.*;

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
        List<Long> couponIds = new ArrayList<>(); // 멤버가 가진 쿠폰 아이디를 저장 할 리스트 선언

        for (CouponMemberDTO couponMemberDTO : couponMember) { // 멤버들이 가진 쿠폰들을 순회
            couponIds.add(couponMemberDTO.couponId()); // 쿠폰 객체에서 쿠폰 아이디만 불러와 저장
        }

        List<CouponDTO> couponTrue = couponAdapter.getCouponsByActive(couponIds, true); // 활성화된 쿠폰 조회
        List<CouponDTO> couponFalse = couponAdapter.getCouponsByActive(couponIds, false); // 비활성화된 쿠폰 조회

        Set<String> couponNameMember = new HashSet<>(); // 멤버가 가진 쿠폰 이름을 담을 리스트 // 어레이리시트를 해시셋으로 바꿈

        // 활성화된 쿠폰 + 비활성화된 쿠폰
        for (CouponDTO dto : couponTrue) {
            couponNameMember.add(dto.couponName());
        }
        for(CouponDTO dto : couponFalse){
            couponNameMember.add(dto.couponName());
        }

        List<CouponDTO> coupon = couponAdapter.getCouponByCouponName("겨울 쿠폰"); // 만들어진 쿠폰 중 멤버에게 나눠줄 쿠폰을 쿠폰 이름으로 찾기

        List<Long> allMemberCoupon = couponMemberAdapter.getAllCouponId(); // 멤버들에게 나눠준 모든 쿠폰 아이디
        Set<Long> allMemberCouponSet = new HashSet<>(allMemberCoupon);

        for (CouponDTO couponDTO : coupon) {
            if (couponNameMember.contains(couponDTO.couponName())) { // 멤버가 받은 적 없는 쿠폰이어야 함
                return "이미 발급받은 쿠폰입니다.";
            }
            if(!allMemberCouponSet.contains(couponDTO.couponId())){ // 다른 사람이 받은 쿠폰이 아니여야 함 // 너무 많은 페인 클라이언트 호출 -> 멤버쿠폰 테이블에 있는 아이디를 모두 뽑아와서 하는게 비교하게 바꿈
                couponMemberAdapter.createMemberCoupon(memberId, couponDTO.couponId());
                return "쿠폰을 발급 받았습니다!";
            }
        }
        return "쿠폰 발급에 실패했습니다. 잠시후에 다시 시도해주세요";
    }
}