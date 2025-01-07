package com.nhnacademy.hello.common.feignclient;

import com.nhnacademy.hello.common.config.FeignConfig;
import com.nhnacademy.hello.dto.book.BookDTO;
import com.nhnacademy.hello.dto.member.*;
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

    // 전체 회원 수 조회 (검색 조건 포함)
    @GetMapping("/api/members/count")
    public ResponseEntity<Long> getMemberCount(
            @RequestParam(required = false) String search);

    // 회원 등급 가져오기
    @GetMapping("/api/ratings")
    public List<RatingDTO> getRatings() ;

    // 회원 상태 가져오기
    @GetMapping("/api/memberStatus")
    public List<MemberStatusDTO> getMemberStatus();
}
