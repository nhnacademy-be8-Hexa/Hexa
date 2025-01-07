package com.nhnacademy.hello.controller.point;

import com.nhnacademy.hello.common.feignclient.PointPolicyAdapter;
import com.nhnacademy.hello.common.feignclient.MemberAdapter;
import com.nhnacademy.hello.common.util.AuthInfoUtils;
import com.nhnacademy.hello.dto.member.MemberDTO;
import com.nhnacademy.hello.dto.point.PointPolicyDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/point")
public class PointPolicyController {

    private final PointPolicyAdapter pointPolicyAdapter;
    private final MemberAdapter memberAdapter;

    /**
     * 포인트 정책 관리 페이지
     */
    @GetMapping
    public String adminPointPolicyPage(Model model) {
        if (!AuthInfoUtils.isLogin()) {
            return "redirect:/login";
        }

        MemberDTO memberDTO = memberAdapter.getMember(AuthInfoUtils.getUsername());

        if (!"ADMIN".equals(memberDTO.memberRole())) {
            return "redirect:/index";
        }

        List<PointPolicyDTO> policies = pointPolicyAdapter.getAllPointPolicies().getBody();

        model.addAttribute("member", memberDTO);
        model.addAttribute("policies", policies);

        return "admin/point"; // 관리 페이지로 이동
    }

    /**
     * 포인트 정책 생성 페이지
     */
    @GetMapping("/create")
    public String createPointPolicyForm(Model model) {
        model.addAttribute("pointPolicyDTO", new PointPolicyDTO("", 0)); // 기본값을 넣어준다
        return "admin/createPointPolicy";
    }

    /**
     * 포인트 정책 생성
     */
    @PostMapping
    public String createPointPolicy(@RequestParam("pointPolicyName") String pointPolicyName,
                                    @RequestParam("pointDelta") Integer pointDelta, Model model) {
        PointPolicyDTO pointPolicyDTO = new PointPolicyDTO(pointPolicyName, pointDelta);
        pointPolicyAdapter.createPointPolicy(pointPolicyDTO); // 포인트 정책 생성
        return "redirect:/admin/point"; // 생성 후 목록으로 리다이렉트
    }

    /**
     * 포인트 정책 수정 페이지
     */
    @GetMapping("/edit/{pointPolicyName}")
    public String editPointPolicyForm(@PathVariable String pointPolicyName, Model model) {
        PointPolicyDTO pointPolicy = pointPolicyAdapter.getAllPointPolicies().getBody()
                .stream()
                .filter(policy -> policy.pointPolicyName().equals(pointPolicyName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("해당 정책을 찾을 수 없습니다."));
        model.addAttribute("pointPolicyDTO", pointPolicy);
        return "admin/editPointPolicy";
    }

    /**
     * 포인트 정책 수정
     */
    @PostMapping("/edit")
    public String updatePointPolicy(@RequestParam("pointPolicyName") String pointPolicyName,
                                    @RequestParam("pointDelta") Integer pointDelta) {
        PointPolicyDTO updatedPolicyDTO = new PointPolicyDTO(pointPolicyName, pointDelta);
        pointPolicyAdapter.updatePointPolicy(updatedPolicyDTO);
        return "redirect:/admin/point"; // 수정 후 목록으로 리다이렉트
    }

    /**
     * 포인트 정책 삭제
     */
    @PostMapping("/delete/{pointPolicyName}")
    public String deletePointPolicy(@PathVariable String pointPolicyName) {
        pointPolicyAdapter.deletePointPolicy(pointPolicyName);
        return "redirect:/admin/point"; // 삭제 후 목록으로 리다이렉트
    }
}
