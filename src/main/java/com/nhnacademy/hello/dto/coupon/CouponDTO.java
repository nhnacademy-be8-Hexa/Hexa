package com.nhnacademy.hello.dto.coupon;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

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
}
