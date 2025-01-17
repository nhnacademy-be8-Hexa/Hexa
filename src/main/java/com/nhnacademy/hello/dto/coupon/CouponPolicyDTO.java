package com.nhnacademy.hello.dto.coupon;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.hibernate.validator.constraints.Length;

import java.time.ZonedDateTime;

public record CouponPolicyDTO(
        long couponPolicyId,
        String couponPolicyName,
        int minPurchaseAmount,
        String discountType,
        int discountValue,
        int maxDiscountAmount,
        boolean isDeleted,
        String eventType,
        ZonedDateTime createdAt
) {
    public Object getCouponPolicyId() {
        return couponPolicyId;
    }

    public String getCouponPolicyName() {
        return couponPolicyName;
    }


    public Object getEventType() {
        return eventType;
    }
}