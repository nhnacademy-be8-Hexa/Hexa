package com.nhnacademy.hello.dto.coupon;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.time.ZonedDateTime;

public record CouponRequestDTO(
        @NotNull Long couponPolicyId,
        @NotBlank @Length(max = 50) String couponName,
        @NotBlank @Length(max = 20) String couponTarget,
        Long couponTargetId,
        @Future ZonedDateTime couponDeadline
) {}

