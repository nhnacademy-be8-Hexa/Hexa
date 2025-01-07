package com.nhnacademy.hello.dto.point;



import java.time.LocalDateTime;

public record PointDetailsDTO (
        Long pointDetailsId,
        MemberDTO member,
        Integer pointDetailsIncrement,
        String pointDetailsComment,
        LocalDateTime pointDetailsDatetime
){
    public record MemberDTO(String memberId) {}
}



