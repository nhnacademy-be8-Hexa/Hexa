package com.nhnacademy.hello.common.feignclient;

import com.nhnacademy.hello.dto.member.LoginRequest;
import com.nhnacademy.hello.dto.member.MemberDTO;
import com.nhnacademy.hello.dto.member.MemberRequestDTO;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;


@FeignClient(name = "hexa-gateway")
public interface HexaGateway {

    // auth api
    // login request (id, password) 를 보내고 토큰을 받음
    @PostMapping("/api/auth/login")
    public String login(@RequestBody LoginRequest request);


    // service api
    // member
    // Get members list with pagination and search
    @GetMapping("/api/members")
    List<MemberDTO> getMembers(@RequestParam(defaultValue = "0") int page, @RequestParam(required = false) String search);

    // Get a specific member by ID
    @GetMapping("/api/members/{memberId}")
    MemberDTO getMember(@PathVariable("memberId") String memberId);

    // Create a new member
    @PostMapping("/api/members")
    ResponseEntity<MemberDTO> createMember(@RequestBody MemberRequestDTO memberRequestDto);

    // Update an existing member
    @PatchMapping("/api/members/{memberId}")
    public ResponseEntity<MemberDTO> updateMember(@PathVariable String memberId, @RequestBody @Valid MemberDTO memberRequestDto);



    }
