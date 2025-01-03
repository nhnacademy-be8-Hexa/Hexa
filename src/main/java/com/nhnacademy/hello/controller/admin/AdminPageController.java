package com.nhnacademy.hello.controller.admin;

import com.nhnacademy.hello.common.feignclient.AuthorAdapter;
import com.nhnacademy.hello.common.feignclient.BookAdapter;
import com.nhnacademy.hello.common.feignclient.MemberAdapter;
import com.nhnacademy.hello.common.util.AuthInfoUtils;
import com.nhnacademy.hello.dto.book.AuthorDTO;
import com.nhnacademy.hello.dto.book.BookDTO;
import com.nhnacademy.hello.dto.member.MemberDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class  AdminPageController {
    private final MemberAdapter memberAdapter;
    private final BookAdapter bookAdapter;
    private final AuthorAdapter authorAdapter;


    @GetMapping("/admin")
    public String adminPage(Model model){

        if(!AuthInfoUtils.isLogin()){
            return "redirect:/login";
        }
        // 현재 로그인된 사용자 정보 조회
        MemberDTO memberDTO = memberAdapter.getMember(AuthInfoUtils.getUsername());

        // 관리자인지 검사
        if(!"ADMIN".equals(memberDTO.memberRole())){
            return "redirect:/index";
        }

        model.addAttribute("member",memberDTO);

        // 가장 많이 방문한 도서 Top 5 조회
        List<BookDTO> mostVisitedBooks = bookAdapter.getBooks(
                0, 5, "", null, null, null, null, true, null, null, null
        );

        // 각 도서의 저자 리스트 가져오기
        Map<Long, List<AuthorDTO>> bookAuthorsMap = new HashMap<>();
        for (BookDTO book : mostVisitedBooks) {
            List<AuthorDTO> authors = bookAdapter.getAuthors(book.bookId());
            bookAuthorsMap.put(book.bookId(), authors);
        }

        model.addAttribute("mostVisitedBooks", mostVisitedBooks);
        model.addAttribute("bookAuthorsMap", bookAuthorsMap);


        return "admin/adminPage"; // 템플릿 파일 경로
    }

}