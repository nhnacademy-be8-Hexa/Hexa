package com.nhnacademy.hello.dto.address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Length;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class AddressDTO {
    private Long addressId;

    @NotBlank
    @Setter
    @Length(max = 20)
    private String addressName;

    @NotBlank
    @Setter
    private String zoneCode;

    @NotBlank
    @Setter
    @Length(max = 400)
    private String address;

    @NotBlank
    @Setter
    @Length(max = 400)
    private String addressDetail;
}
