package com.nhnacademy.hello.controller.admin;

import com.nhnacademy.hello.common.feignclient.MemberAdapter;
import com.nhnacademy.hello.dto.member.MemberDTO;
import com.nhnacademy.hello.dto.member.MemberStatusDTO;
import com.nhnacademy.hello.dto.member.MemberUpdateDTO;
import com.nhnacademy.hello.dto.member.RatingDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Member;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/members")
public class MemberManageController {
    private final MemberAdapter memberAdapter;

    // 멤버 전체 조회 (페이징 및 검색 처리)
    @GetMapping
    public String getMembers(@RequestParam(defaultValue = "1") int page,
                             @RequestParam(defaultValue = "10") int pageSize,
                             @RequestParam(required = false) String search,
                             Model model) {
        try {
            // FeignClient를 통해 페이징 및 검색 조건으로 회원 목록 조회
            List<MemberDTO> members = memberAdapter.getMembers(page - 1, pageSize, search);

            // 전체 회원 수 조회 API 호출
            ResponseEntity<Long> response = memberAdapter.getMemberCount(search);
            long totalCount = response.getBody() != null ? response.getBody() : 0; // null일 경우 0으로 처리
            int totalPages = (int) Math.ceil((double) totalCount / pageSize);
            int currentPage = page > 0 ? page : 1; // 0 이하일 경우 기본값 설정

            model.addAttribute("members", members);
            model.addAttribute("currentPage", currentPage);
            model.addAttribute("pageSize", pageSize);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("search", search);
        } catch (Exception e) {
            model.addAttribute("error", "회원 데이터를 불러오는 중 오류가 발생했습니다.");
            model.addAttribute("members", List.of());
        }

        return "admin/memberManage"; // 멤버 관리 페이지
    }


    // 특정 회원 상세 조회
    @GetMapping("/{memberId}")
    public String getMemberDetail(@PathVariable String memberId, Model model) {
        MemberDTO member = memberAdapter.getMember(memberId);

        model.addAttribute("member",member);

        return "admin/memberDetail";
    }

    // MemberDTO를 MemberUpdateDTO로 변환
    private MemberUpdateDTO convertToUpdateDTO(MemberDTO member) {
        return new MemberUpdateDTO(
                null,  // 패스워드는 수정하지 않으므로 null로 설정
                member.memberName(),
                member.memberNumber(),
                member.memberEmail(),
                member.memberBirthAt(),
                member.rating() != null ? String.valueOf(member.rating().ratingId()) : null,
                member.memberStatus() != null ? String.valueOf(member.memberStatus().statusId()) : null
        );
    }

    @GetMapping("/update/{memberId}")
    public String getUpdateForm(@PathVariable String memberId, Model model) {
        try {
            MemberDTO member = memberAdapter.getMember(memberId);

            if (member == null) {
                model.addAttribute("error", "회원 정보를 찾을 수 없습니다.");
                return "admin/memberManage";
            }

            List<RatingDTO> ratings = memberAdapter.getRatings();
            List<MemberStatusDTO> memberStatuses = memberAdapter.getMemberStatus();

            // MemberUpdateDTO로 변환
            MemberUpdateDTO updateDTO = convertToUpdateDTO(member);

            model.addAttribute("member", updateDTO);  // UpdateDTO 전달
            model.addAttribute("ratings", ratings);
            model.addAttribute("memberStatuses", memberStatuses);

            return "admin/memberUpdateForm";
        } catch (Exception e) {
            model.addAttribute("error", "회원 정보를 불러오는 중 오류가 발생했습니다.");
            return "admin/memberManage";
        }
    }


    // 멤버 정보 수정 (등급, 상태 포함)
    @PostMapping("/{memberId}")
    public String updateMember(@PathVariable String memberId,
                               @ModelAttribute @Valid MemberUpdateDTO memberUpdateDTO,
                               Model model) {
        try {
            // 로그 추가
            System.out.println("Updating member: " + memberId + " with data: " + memberUpdateDTO);

            // 멤버 정보 수정
            memberAdapter.updateMember(memberId, memberUpdateDTO);

            // 상태 업데이트
            if (memberUpdateDTO.statusId() != null) {
                memberAdapter.updateMemberStatus(Long.parseLong(memberUpdateDTO.statusId()), memberUpdateDTO);
            }

            // 등급 업데이트
            if (memberUpdateDTO.ratingId() != null) {
                memberAdapter.updateRating(Long.parseLong(memberUpdateDTO.ratingId()), memberUpdateDTO);
            }

            return "redirect:/admin/members"; // 수정 성공 시 목록 페이지로 이동
        } catch (Exception e) {
            e.printStackTrace(); // 상세 예외 로그 확인
            model.addAttribute("error", "회원 정보를 저장하는 중 오류가 발생했습니다.");
            return "admin/memberUpdateForm";
        }
    }


    // 특정 회원 상태 변경 요청 (탈퇴 처리)
    @PutMapping("/{memberId}")
    @ResponseBody
    public ResponseEntity<Void> deactivateMember(@PathVariable String memberId) {
        try {
            // MemberUpdateDTO를 사용하여 상태를 "탈퇴"로 변경
            MemberUpdateDTO updateDTO = new MemberUpdateDTO(
                    null,    // 비밀번호
                    null,    // 이름
                    null,    // 연락처
                    null,    // 이메일
                    null,    // 생일
                    null,    // 등급
                    "3"      // 탈퇴 상태 ID
            );
            memberAdapter.updateMember(memberId, updateDTO); // PUT 요청 전송
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

}
