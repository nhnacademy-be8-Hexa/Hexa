package com.nhnacademy.hello.common.feignclient;

import com.nhnacademy.hello.common.config.FeignConfig;
import com.nhnacademy.hello.dto.book.BookDTO;
import com.nhnacademy.hello.dto.member.LoginRequest;
import com.nhnacademy.hello.dto.member.MemberDTO;
import com.nhnacademy.hello.dto.member.MemberRequestDTO;
import com.nhnacademy.hello.dto.member.MemberUpdateDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@FeignClient(name = "hexa-gateway", contextId = "memberAdapter")
public interface MemberAdapter {


    // service api
    // member
    @GetMapping("/api/members/{memberId}")
    public MemberDTO getMember(@PathVariable String memberId);

    @PostMapping("/api/members")
    public ResponseEntity<MemberDTO> createMember(@RequestBody @Valid MemberRequestDTO memberRequestDto);

    @PutMapping("/api/members/{memberId}")
    public ResponseEntity<MemberDTO> updateMember(@PathVariable String memberId, @RequestBody @Valid MemberUpdateDTO updateDTO);

    @GetMapping("/api/members/{memberId}/liked-books")
    public ResponseEntity<List<BookDTO>> getLikedBooks(@PathVariable String memberId);

    @PutMapping("/api/members/{memberId}/login")
    public ResponseEntity<Void> loginMember(@PathVariable String memberId);

    // 회원 목록 조회 (페이징 및 검색)
    @GetMapping("/api/members")
    List<MemberDTO> getMembers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String search);

}
