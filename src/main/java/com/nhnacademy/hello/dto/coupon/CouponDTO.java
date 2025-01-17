package com.nhnacademy.hello.dto.coupon;



import java.time.ZonedDateTime;


public record CouponDTO(Long couponId,
                        CouponPolicyDTO couponPolicy,
                        String couponName,
                        String couponTarget,
                        Long couponTargetId,
                        ZonedDateTime couponDeadline,
                        ZonedDateTime couponCreatedAt,
                        boolean couponIsActive,
                        ZonedDateTime couponUsedAt
) {
    public Object getCouponName() {
        return couponName;
    }

    public Object getCouponId() {
        return couponId;
    }
}
