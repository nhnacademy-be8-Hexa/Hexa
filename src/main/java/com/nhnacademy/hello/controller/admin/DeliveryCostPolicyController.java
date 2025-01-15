package com.nhnacademy.hello.controller.admin;

import com.nhnacademy.hello.common.feignclient.DeliveryCostPolicyAdapter;
import com.nhnacademy.hello.common.util.AuthInfoUtils;
import com.nhnacademy.hello.dto.delivery.DeliveryCostPolicyDTO;
import com.nhnacademy.hello.dto.delivery.DeliveryCostPolicyRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/admin/delivery-cost-policy")
public class DeliveryCostPolicyController {
    private final DeliveryCostPolicyAdapter deliveryCostPolicyAdapter;

    @GetMapping
    public String deliveryCostPolicyPage(
            Model model,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "5") Integer size
            ) {
        // 현재 페이지를 0부터 시작하도록 조정
        int adjustedPage = (page != null && page > 1) ? page - 1 : 0;

        // 전체 데이터 개수 가져오기
        Long total = deliveryCostPolicyAdapter.getCount().getBody();

        // 전체 페이지 수 계산
        int totalPages = (int) Math.ceil((double) total / size);

        // 페이지 번호가 총 페이지 수를 초과할 경우 마지막 페이지로 조정
        if (page > totalPages && totalPages != 0) {
            adjustedPage = totalPages - 1;
            page = totalPages;
        }

        // 데이터 리스트 가져오기
        List<DeliveryCostPolicyDTO> policyList =
                deliveryCostPolicyAdapter.getAll(adjustedPage, size, "deliveryCostPolicyId,desc").getBody();

        // Model에 페이징 및 데이터 추가
        model.addAttribute("policyList", policyList);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", total);

        return "admin/deliveryCostPolicyManage";
    }

    @PostMapping
    public ResponseEntity<?> deliveryCostPolicySave(
            @RequestBody requestDto requestDto
    ) {
        DeliveryCostPolicyRequest request = new DeliveryCostPolicyRequest(
                requestDto.deliveryCost(),
                requestDto.freeMinimumAmount,
                AuthInfoUtils.getUsername()
        );
        deliveryCostPolicyAdapter.create(request);

        return ResponseEntity.ok().build();
    }

    private record requestDto(
            int deliveryCost,
            int freeMinimumAmount
    ){}

}
