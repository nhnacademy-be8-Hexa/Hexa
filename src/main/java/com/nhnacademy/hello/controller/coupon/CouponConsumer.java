package com.nhnacademy.hello.controller.coupon;

import com.nhnacademy.hello.common.feignclient.coupon.CouponAdapter;
import com.nhnacademy.hello.common.feignclient.coupon.CouponMemberAdapter;
import com.nhnacademy.hello.dto.coupon.CouponDTO;
import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@AllArgsConstructor
public class CouponConsumer {

    private final CouponMemberAdapter couponMemberAdapter;
    private final CouponAdapter couponAdapter;
    private final RabbitTemplate rabbitTemplate;

    public void couponGet() {

        Message message = rabbitTemplate.receive("hexa.coupon.queues");

        String memberId = new String(Objects.requireNonNull(message).getBody());

        List<CouponDTO> coupon = couponAdapter.getCouponByCouponName("겨울 쿠폰");

        for (CouponDTO couponDTO : coupon) {
            if (!couponMemberAdapter.isCouponAlreadyAssigned(couponDTO.couponId(), memberId)
                    && !couponMemberAdapter.checkCouponDuplicate(couponDTO.couponId())) {
                couponMemberAdapter.createMemberCoupon(memberId, couponDTO.couponId());
                break; // 한 개만 발급되도록 루프 종료
            }
        }

    }
}