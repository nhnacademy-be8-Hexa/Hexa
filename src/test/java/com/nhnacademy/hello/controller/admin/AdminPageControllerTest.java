package com.nhnacademy.hello.controller.admin;

import com.nhnacademy.hello.common.feignclient.*;
import com.nhnacademy.hello.common.util.AuthInfoUtils;
import com.nhnacademy.hello.dto.book.BookDTO;
import com.nhnacademy.hello.dto.book.AuthorDTO;
import com.nhnacademy.hello.dto.member.MemberDTO;
import com.nhnacademy.hello.dto.order.*;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AdminPageControllerTest {

    @Mock
    private MemberAdapter memberAdapter;

    @Mock
    private BookAdapter bookAdapter;

    @Mock
    private OrderAdapter orderAdapter;

    @Mock
    private OrderBookAdapter orderBookAdapter;

    @Mock
    private Model model;

    @InjectMocks
    private AdminPageController adminPageController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adminPageController = spy(adminPageController); // spy를 사용하여 특정 메서드만 대체
    }

    @Test
    @DisplayName("로그인하지 않은 상태에서는 로그인 페이지로 리다이렉트")
    void adminPage_redirectsToLoginWhenNotLoggedIn() {
        try (MockedStatic<AuthInfoUtils> mockedAuthInfoUtils = mockStatic(AuthInfoUtils.class)) {
            // Mock 설정: 로그인하지 않은 상태
            mockedAuthInfoUtils.when(AuthInfoUtils::isLogin).thenReturn(false);

            // 테스트 실행
            String result = adminPageController.adminPage(1, 10, model);

            // 검증
            assertEquals("redirect:/login", result);
        }
    }

    @Test
    @DisplayName("로그인한 사용자가 ADMIN 권한이 아닌 경우 인덱스 페이지로 리다이렉트")
    void adminPage_redirectsToIndexWhenNotAdmin() {
        try (MockedStatic<AuthInfoUtils> mockedAuthInfoUtils = mockStatic(AuthInfoUtils.class)) {
            // Mock 설정: 로그인 상태와 USER 권한
            mockedAuthInfoUtils.when(AuthInfoUtils::isLogin).thenReturn(true);
            mockedAuthInfoUtils.when(AuthInfoUtils::getUsername).thenReturn("userId");

            MemberDTO nonAdminMember = new MemberDTO(
                    "userId", "Regular User", "12345", "user@example.com",
                    LocalDate.of(1990, 1, 1), LocalDate.of(2020, 1, 1),
                    LocalDateTime.now(), "USER", null, null
            );
            when(memberAdapter.getMember("userId")).thenReturn(nonAdminMember);

            // 테스트 실행
            String result = adminPageController.adminPage(1, 10, model);

            // 검증
            assertEquals("redirect:/index", result);
        }
    }

    @Test
    @DisplayName("로그인한 사용자가 ADMIN 권한을 가진 경우 정상 처리")
    void adminPage_processesForAdmin() {
        try (MockedStatic<AuthInfoUtils> mockedAuthInfoUtils = mockStatic(AuthInfoUtils.class)) {
            // 로그인 상태와 ADMIN 권한을 위한 Mock 설정
            mockedAuthInfoUtils.when(AuthInfoUtils::isLogin).thenReturn(true);
            mockedAuthInfoUtils.when(AuthInfoUtils::getUsername).thenReturn("adminId");

            MemberDTO adminMember = new MemberDTO(
                    "adminId", "Admin User", "12345", "admin@example.com",
                    LocalDate.of(1990, 1, 1), LocalDate.of(2020, 1, 1),
                    LocalDateTime.now(), "ADMIN", null, null
            );
            when(memberAdapter.getMember("adminId")).thenReturn(adminMember);

            // 테스트에 사용할 BookDTO 준비
            BookDTO book = new BookDTO(
                    1L, "Book Title", "Description", LocalDate.of(2020, 1, 1),
                    1234567890123L, 10000, 9000, true, 500, 100, 50L,
                    null, null, "path/to/image.jpg"
            );

            // getBooks 호출 시, 매개변수에 상관없이 stub이 적용되도록 any() 매처 사용
            when(bookAdapter.getBooks(
                    anyInt(),      // page
                    anyInt(),      // size
                    anyList(),     // sort (List<String>)
                    any(),         // search
                    any(),         // categoryIds
                    any(),         // publisherName
                    any(),         // authorName
                    any(),         // sortByLikeCount
                    any()          // sortByReviews
            )).thenReturn(List.of(book));

            AuthorDTO author = new AuthorDTO(1L, "Author Name");
            when(bookAdapter.getAuthors(book.bookId())).thenReturn(List.of(author));

            // getLikeCount 반환값 Mock 설정
            when(bookAdapter.getLikeCount(book.bookId())).thenReturn(ResponseEntity.ok(100L));

            // 테스트 실행 (페이지 번호 1, 사이즈 10 전달 → 내부에서 적절한 값 변환)
            String result = adminPageController.adminPage(1, 10, model);

            // 검증: 결과 뷰 이름 및 Model에 추가된 값 확인
            assertEquals("admin/adminPage", result);
            verify(model, times(1)).addAttribute(eq("member"), eq(adminMember));
            verify(model, times(1)).addAttribute(eq("mostVisitedBooks"), eq(List.of(book)));
        }
    }



    @Test
    @DisplayName("Valid Admin: Admin 페이지 로드 시 정상적으로 데이터 반환")
    void adminPage_withValidAdmin_returnsAdminPage() {
        // Model 객체를 Mock 처리 (필요한 경우)
        Model model = mock(Model.class);

        try (MockedStatic<AuthInfoUtils> mockedAuthInfoUtils = mockStatic(AuthInfoUtils.class)) {
            // 로그인 상태와 ADMIN 권한을 위한 Mock 설정
            mockedAuthInfoUtils.when(AuthInfoUtils::isLogin).thenReturn(true);
            mockedAuthInfoUtils.when(AuthInfoUtils::getUsername).thenReturn("adminId");

            MemberDTO adminMember = new MemberDTO(
                    "adminId", "Admin User", "12345", "admin@example.com",
                    LocalDate.of(1990, 1, 1), LocalDate.of(2020, 1, 1),
                    LocalDateTime.now(), "ADMIN", null, null
            );
            when(memberAdapter.getMember("adminId")).thenReturn(adminMember);

            // 테스트에 사용할 BookDTO 준비
            BookDTO book = new BookDTO(
                    1L,
                    "Book Title",
                    "Description",
                    LocalDate.of(2020, 1, 1),
                    1234567890123L,
                    10000,
                    9000,
                    true,
                    500,
                    100,
                    50L,
                    null,
                    null,
                    "path/to/image.jpg"
            );

            // 수정된 getBooks stub: 검색 파라미터에는 any()를 사용 (null 포함)
            when(bookAdapter.getBooks(
                    anyInt(),
                    anyInt(),
                    anyList(),
                    any(),        // any() 대신 anyString() 사용하면 null과 매칭되지 않음
                    any(),
                    any(),
                    any(),
                    any(),
                    any()
            )).thenReturn(List.of(book));

            AuthorDTO author = new AuthorDTO(1L, "Author Name");
            when(bookAdapter.getAuthors(book.bookId())).thenReturn(List.of(author));
            when(bookAdapter.getLikeCount(book.bookId())).thenReturn(ResponseEntity.ok(100L));

            OrderBookResponseDTO orderBookResponse = new OrderBookResponseDTO(
                    1L, 101L, "Effective Java", 2, 45000, null
            );
            when(orderBookAdapter.getOrderBooksByOrderId(anyLong()))
                    .thenReturn(new OrderBookResponseDTO[]{orderBookResponse});

            // 테스트 실행 (페이지 번호 1, 사이즈 10 전달)
            String result = adminPageController.adminPage(1, 10, model);

            // 검증
            assertEquals("admin/adminPage", result); // 컨트롤러에서 반환하는 뷰 이름
            verify(model, times(1)).addAttribute("member", adminMember);
            verify(model, times(1)).addAttribute("mostVisitedBooks", List.of(book));
            verify(model, times(1)).addAttribute(eq("bookAuthorsMap"), anyMap());
            verify(model, times(1)).addAttribute(eq("bookLikesMap"), anyMap());
        }
    }

    @Test
    @DisplayName("Valid Data: 주문 데이터를 Model에 추가")
    void addOrderDataToModel_withValidData_addsDataToModel() {
        // Mock 데이터 준비
        OrderDTO order = new OrderDTO(
                1L, 5000, LocalDateTime.now(),
                new WrappingPaperDTO(1L, "Gift Wrap", 200),
                new OrderStatusDTO(1L, "WAIT"),
                "12345", "Main St", "Apt 101",
                new OrderDTO.MemberDTO("memberId", "Member Name", "98765", "member@example.com")
        );

        MemberDTO fullMemberInfo = new MemberDTO(
                "memberId", "Full Member", "12345", "fullmember@example.com",
                LocalDate.of(1990, 1, 1), LocalDate.of(2020, 1, 1),
                LocalDateTime.now(), "USER", null, null
        );

        // Mock 설정: countOrdersByStatus 및 getOrderStatus
        when(orderAdapter.countOrdersByStatus(1L)).thenReturn(ResponseEntity.ok(1L));
        // 변경된 메서드 시그니처에 맞춰 sort 파라미터(여기서는 빈 문자열 "")를 추가
        when(orderAdapter.getOrderStatus(1L, 0, 10, "")).thenReturn(List.of(order));

        // Mock 설정: memberAdapter.getMember
        when(memberAdapter.getMember("memberId")).thenReturn(fullMemberInfo);

        // Mock 설정: enrichOrderDetails
        Map<String, Object> enrichedOrder = Map.of(
                "order", order,
                "member", new OrderDTO.MemberDTO(
                        fullMemberInfo.memberId(),
                        fullMemberInfo.memberName(),
                        fullMemberInfo.memberNumber(),
                        fullMemberInfo.memberEmail()
                )
        );
        doReturn(List.of(enrichedOrder)).when(adminPageController).enrichOrderDetails(List.of(order));

        // 테스트 실행
        adminPageController.addOrderDataToModel(1, 10, "waitOrders", 1L, model);

        // 검증
        verify(model, times(1)).addAttribute("waitOrders", List.of(enrichedOrder));
        verify(model, times(1)).addAttribute("waitOrdersCurrentPage", 1);
        verify(model, times(1)).addAttribute("waitOrdersTotalPages", 1);
    }


    @Test
    @DisplayName("Valid Orders: 주문 데이터 상세 정보 추가")
    void enrichOrderDetails_withValidOrders_enrichesDetails() {
        // Mock 데이터 준비
        OrderDTO order = new OrderDTO(
                1L, 5000, LocalDateTime.now(),
                new WrappingPaperDTO(1L, "Gift Wrap", 200),
                new OrderStatusDTO(1L, "WAIT"),
                "12345", "Main St", "Apt 101",
                new OrderDTO.MemberDTO("memberId", "Member Name", "98765", "member@example.com")
        );

        GuestOrderDTO guestOrder = new GuestOrderDTO(1L, "123456", "guest@example.com");
        when(orderAdapter.getGuestOrder(order.orderId())).thenReturn(guestOrder);

        OrderBookResponseDTO orderBookResponse = new OrderBookResponseDTO(
                1L, 101L, "Effective Java", 2, 45000, null
        );
        when(orderBookAdapter.getOrderBooksByOrderId(order.orderId())).thenReturn(new OrderBookResponseDTO[]{orderBookResponse});

        MemberDTO fullMemberInfo = new MemberDTO(
                "memberId", "Full Member", "12345", "fullmember@example.com",
                LocalDate.of(1990, 1, 1), LocalDate.of(2020, 1, 1),
                LocalDateTime.now(), "USER", null, null
        );
        when(memberAdapter.getMember("memberId")).thenReturn(fullMemberInfo);

        List<Map<String, Object>> enrichedOrders = adminPageController.enrichOrderDetails(List.of(order));

        assertEquals(1, enrichedOrders.size());
        Map<String, Object> orderDetails = enrichedOrders.get(0);
        assertEquals(order, orderDetails.get("order"));

        OrderDTO.MemberDTO member = (OrderDTO.MemberDTO) orderDetails.get("member");
        assertEquals("memberId", member.memberId());
        assertEquals("Full Member", member.memberName());
        assertEquals("12345", member.memberNumber());
        assertEquals("fullmember@example.com", member.memberEmail());
    }
    @Test
    @DisplayName("Guest Member: 비회원 주문 처리")
    void enrichOrderDetails_handlesGuestMember() {
        // Mock 데이터 준비: 비회원 시나리오
        OrderDTO order = new OrderDTO(
                1L, 5000, LocalDateTime.now(),
                new WrappingPaperDTO(1L, "Gift Wrap", 200),
                new OrderStatusDTO(1L, "WAIT"),
                "12345", "Main St", "Apt 101",
                null // 비회원인 경우 member가 null
        );

        GuestOrderDTO guestOrder = new GuestOrderDTO(1L, "123456", "guest@example.com");
        when(orderAdapter.getGuestOrder(order.orderId())).thenReturn(guestOrder);

        OrderBookResponseDTO orderBookResponse = new OrderBookResponseDTO(
                1L, 101L, "Effective Java", 2, 45000, null
        );
        when(orderBookAdapter.getOrderBooksByOrderId(order.orderId())).thenReturn(new OrderBookResponseDTO[]{orderBookResponse});

        // 테스트 실행
        List<Map<String, Object>> enrichedOrders = adminPageController.enrichOrderDetails(List.of(order));

        // 검증
        assertEquals(1, enrichedOrders.size());
        Map<String, Object> orderDetails = enrichedOrders.get(0);
        assertEquals(order, orderDetails.get("order"));

        OrderDTO.MemberDTO member = (OrderDTO.MemberDTO) orderDetails.get("member");
        assertEquals("비회원", member.memberId());
        assertEquals("Unknown", member.memberName());
        assertEquals("123456", member.memberNumber());
        assertEquals("guest@example.com", member.memberEmail());
    }

    @Test
    @DisplayName("Known Member: 회원 주문 처리")
    void enrichOrderDetails_handlesKnownMember() {
        // Mock 데이터 준비: 회원 시나리오
        OrderDTO order = new OrderDTO(
                1L, 5000, LocalDateTime.now(),
                new WrappingPaperDTO(1L, "Gift Wrap", 200),
                new OrderStatusDTO(1L, "WAIT"),
                "12345", "Main St", "Apt 101",
                new OrderDTO.MemberDTO("memberId", "Known Member", "98765", "member@example.com")
        );

        MemberDTO fullMemberInfo = new MemberDTO(
                "memberId", "Full Member", "12345", "fullmember@example.com",
                LocalDate.of(1990, 1, 1), LocalDate.of(2020, 1, 1),
                LocalDateTime.now(), "USER", null, null
        );
        when(memberAdapter.getMember("memberId")).thenReturn(fullMemberInfo);

        OrderBookResponseDTO orderBookResponse = new OrderBookResponseDTO(
                1L, 101L, "Effective Java", 2, 45000, null
        );
        when(orderBookAdapter.getOrderBooksByOrderId(order.orderId())).thenReturn(new OrderBookResponseDTO[]{orderBookResponse});

        // 테스트 실행
        List<Map<String, Object>> enrichedOrders = adminPageController.enrichOrderDetails(List.of(order));

        // 검증
        assertEquals(1, enrichedOrders.size());
        Map<String, Object> orderDetails = enrichedOrders.get(0);
        assertEquals(order, orderDetails.get("order"));

        OrderDTO.MemberDTO member = (OrderDTO.MemberDTO) orderDetails.get("member");
        assertEquals("memberId", member.memberId());
        assertEquals("Full Member", member.memberName());
        assertEquals("12345", member.memberNumber());
        assertEquals("fullmember@example.com", member.memberEmail());
    }

    @Test
    @DisplayName("Member Not Found: 회원 정보를 찾을 수 없는 경우 기본값 처리")
    void enrichOrderDetails_handlesMemberNotFound() {
        // Mock 데이터 준비: 회원 정보를 찾을 수 없는 시나리오
        OrderDTO order = new OrderDTO(
                1L, 5000, LocalDateTime.now(),
                new WrappingPaperDTO(1L, "Gift Wrap", 200),
                new OrderStatusDTO(1L, "WAIT"),
                "12345", "Main St", "Apt 101",
                new OrderDTO.MemberDTO("unknownId", "Unknown", "00000", "unknown@example.com")
        );

        // Mock FeignException.NotFound 생성
        Request request = Request.create(Request.HttpMethod.GET, "/member/unknownId", Map.of(), null, null, null);
        FeignException.NotFound notFoundException = new FeignException.NotFound("Member not found", request, null, null);

        when(memberAdapter.getMember("unknownId")).thenThrow(notFoundException);

        OrderBookResponseDTO orderBookResponse = new OrderBookResponseDTO(
                1L, 101L, "Effective Java", 2, 45000, null
        );
        when(orderBookAdapter.getOrderBooksByOrderId(order.orderId())).thenReturn(new OrderBookResponseDTO[]{orderBookResponse});

        // 테스트 실행
        List<Map<String, Object>> enrichedOrders = adminPageController.enrichOrderDetails(List.of(order));

        // 검증
        assertEquals(1, enrichedOrders.size());
        Map<String, Object> orderDetails = enrichedOrders.get(0);
        assertEquals(order, orderDetails.get("order"));

        OrderDTO.MemberDTO member = (OrderDTO.MemberDTO) orderDetails.get("member");
        assertEquals("Unknown", member.memberId());
        assertEquals("Unknown", member.memberName());
        assertEquals("Unknown", member.memberNumber());
        assertEquals("Unknown", member.memberEmail());
    }
}