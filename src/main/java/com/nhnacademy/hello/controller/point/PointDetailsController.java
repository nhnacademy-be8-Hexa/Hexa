package com.nhnacademy.hello.controller.point;

import com.nhnacademy.hello.common.feignclient.MemberAdapter;
import com.nhnacademy.hello.common.feignclient.PointDetailsAdapter;
import com.nhnacademy.hello.common.util.AuthInfoUtils;
import com.nhnacademy.hello.dto.member.MemberDTO;
import com.nhnacademy.hello.dto.point.PointDetailsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class PointDetailsController {

    private final PointDetailsAdapter pointDetailsAdapter;
    private final MemberAdapter memberAdapter;

    /**
     * 포인트 내역 페이지를 반환하는 메소드
     *
     * @param model 뷰에 전달할 모델
     * @return 포인트 내역 페이지 뷰
     */
    @GetMapping("/mypage/points")
    public String pointsPage(Model model) {

        String username = AuthInfoUtils.getUsername();
        MemberDTO member = memberAdapter.getMember(username);
        Long sum = pointDetailsAdapter.sumPoint(username).getBody();
        List<PointDetailsDTO> pointDetails = pointDetailsAdapter.getPointDetails(username, 0, 10, "date,desc").getBody();

        model.addAttribute("member", member);
        model.addAttribute("sum", sum);
        model.addAttribute("pointDetails", pointDetails);

        return "member/points";
    }
}
