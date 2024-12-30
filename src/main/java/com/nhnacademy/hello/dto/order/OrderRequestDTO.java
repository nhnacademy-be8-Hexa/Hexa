package com.nhnacademy.hello.dto.order;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record OrderRequestDTO(
        String memberId,

        @NotNull
        Integer orderPrice,

        Long wrappingPaperId,

        @NotNull
        Long orderStatusId,

        @NotNull
        @Length(max = 5)
        String zoneCode,

        @Length(max = 50)
        String address,

        @NotNull
        @Length(max = 100)
        String addressDetail
) {}
