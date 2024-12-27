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

@FeignClient(name = "hexa-gateway", contextId = "memberAdapter", configuration = FeignConfig.class)
public interface MemberAdapter {

    // auth api
    // login request (id, password) 를 보내고 토큰을 받음
    @PostMapping("/api/auth/login")
    public String login(@RequestBody LoginRequest request);


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

    @GetMapping("/api/members")
    public List<MemberDTO> getMembers(
            @RequestParam(defaultValue  = "0") int page, @RequestParam(required = false) String search);

}
