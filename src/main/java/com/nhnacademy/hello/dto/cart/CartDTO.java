package com.nhnacademy.hello.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CartDTO {
    private Long cartId;
    private MemberProjection member;
    private BookProjection book;
    private Integer cartAmount;

    public interface MemberProjection {
        String getMemberId();
    }
    public interface BookProjection {
        Long getBookId();
        String getBookTitle();
        Integer getBookPrice();
    }
}
