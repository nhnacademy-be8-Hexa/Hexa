package com.nhnacademy.hello.controller.admin;

import com.nhnacademy.hello.common.feignclient.*;
import com.nhnacademy.hello.dto.book.*;
import com.nhnacademy.hello.dto.category.CategoryDTO;
import com.nhnacademy.hello.dto.category.PagedCategoryDTO;
import com.nhnacademy.hello.image.ImageStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class BookManageControllerTest {

    @Mock
    private BookAdapter bookAdapter;

    @Mock
    private PublisherAdapter publisherAdapter;

    @Mock
    private BookStatusAdapter bookStatusAdapter;

    @Mock
    private ImageStore imageStore;

    @Mock
    private CategoryAdapter categoryAdapter;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private MultipartFile thumbnail;

    @Mock
    private AuthorAdapter authorAdapter;

    @InjectMocks
    private BookManageController bookManageController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    @DisplayName("도서 목록 조회 - 기본")
    void testBookList() {
        // 총 도서 수: 20개 (따라서 페이지 수는 ceil(20/9)=3)
        when(bookAdapter.getTotalBooks(any(), any(), any(), any()))
                .thenReturn(ResponseEntity.ok(20L));

        // getBooks 모의 설정:
        // - page가 1로 넘어오면 내부에서는 adjustedPage=0, size=9
        // - 정렬 조건이 "title"으로 입력되면 sorting 리스트는 ["title", "bookId,desc"]가 됨.
        // 나머지 파라미터는 null로 넘어갑니다.
        when(bookAdapter.getBooks(
                eq(0),                   // adjustedPage: 0
                eq(9),                   // size: 9
                eq(List.of("title", "bookId,desc")), // 정렬 조건 리스트
                eq(null),                // search가 null
                isNull(),                // categoryIds
                isNull(),                // publisherName
                isNull(),                // authorName
                isNull(),                // sortByLikeCount
                isNull()                 // sortByReviews
        )).thenReturn(List.of(new BookDTO(
                1L,
                "Test Book",
                "Description",
                LocalDate.now(),
                1234567890123L,
                10000,
                9000,
                true,
                500,
                50,
                30L,
                new PublisherRequestDTO(1L, "Publisher Name"),
                new BookStatusRequestDTO(1L, "Available"),
                "path/to/image.jpg"
        )));

        // 컨트롤러의 bookList 메서드는 (search, page, sort, model) 4개의 인자를 받습니다.
        // 여기서는 search=null, page=1, sort="title"로 호출합니다.
        String viewName = bookManageController.bookList(null, 1, "title", model);

        // 검증: 컨트롤러는 "admin/bookManage" 뷰를 반환하고,
        // model에 "books", "currentPage", "totalPages", "size", "sort", "search" 등을 추가합니다.
        assertEquals("admin/bookManage", viewName);
        verify(model).addAttribute(eq("books"), anyList());
        verify(model).addAttribute(eq("currentPage"), eq(1));
        verify(model).addAttribute(eq("totalPages"), eq(3));
        verify(model).addAttribute(eq("size"), eq(9));
        verify(model).addAttribute(eq("sort"), eq("title"));
        verify(model).addAttribute(eq("search"), eq(null));
    }



    @Test
    @DisplayName("도서 목록 조회 - 필터와 정렬")
    void testBookListWithFilters() {
        // 컨트롤러는 다음과 같이 동작합니다.
        // - search 값: "Test"
        // - page 값: 2 → 내부적으로 adjustedPage는 1 (2-1)
        // - sort 값: "author" → 정렬 리스트는 ["author", "bookId,desc"]
        // - size는 고정 9
        // - 총 도서 수가 10개이면 totalPages = ceil(10/9) = 2

        // 총 도서 수 모의 설정 (getTotalBooks는 검색어 "Test"와 null인 필터 조건을 사용)
        when(bookAdapter.getTotalBooks(
                eq("Test"),
                isNull(),
                isNull(),
                isNull()
        )).thenReturn(ResponseEntity.ok(10L));

        // 기대 정렬 리스트
        List<String> expectedSorting = List.of("author", "bookId,desc");

        // 도서 목록 모의 설정
        when(bookAdapter.getBooks(
                eq(1),                    // adjustedPage: 2 -> 1
                eq(9),                    // 고정 사이즈 9
                eq(expectedSorting),      // ["author", "bookId,desc"]
                eq("Test"),               // 검색어
                isNull(),                 // categoryIds
                isNull(),                 // publisherName
                isNull(),                 // authorName
                isNull(),                 // sortByLikeCount
                isNull()                  // sortByReviews
        )).thenReturn(List.of(new BookDTO(
                1L,
                "Filtered Book",
                "Filtered Description",
                LocalDate.now(),
                1234567890123L,
                10000,
                9000,
                true,
                500,
                50,
                30L,
                new PublisherRequestDTO(2L, "Filtered Publisher"),
                new BookStatusRequestDTO(2L, "Unavailable"),
                "path/to/filtered-image.jpg"
        )));

        // When
        // 컨트롤러 메서드는 (search, page, sort, model) 4개의 인자를 받습니다.
        String viewName = bookManageController.bookList("Test", 2, "author", model);

        // Then
        assertEquals("admin/bookManage", viewName);
        verify(model).addAttribute(eq("books"), anyList());
        verify(model).addAttribute("currentPage", 2);
        verify(model).addAttribute("totalPages", 2);  // 10개의 도서를 9로 나누면 2페이지
        verify(model).addAttribute("size", 9);
        verify(model).addAttribute("sort", "author");
        verify(model).addAttribute("search", "Test");
    }



    @Test
    @DisplayName("도서 등록 폼 표시")
    void testShowAddBookForm() {
        when(publisherAdapter.getPublishers()).thenReturn(List.of(new PublisherRequestDTO(1L, "Publisher Name")));
        when(categoryAdapter.getCategories()).thenReturn(ResponseEntity.ok(List.of(new CategoryDTO())));

        String viewName = bookManageController.showAddBookForm(model);

        assertEquals("admin/bookCreateForm", viewName);
        verify(model).addAttribute(eq("bookRequestDTO"), any(BookRequestDTO.class));
        verify(model).addAttribute(eq("publishers"), anyList());
    }

    @Test
    @DisplayName("도서 등록 - 유효하지 않은 요청")
    void testAddBook_InvalidRequest() {
        when(bindingResult.hasErrors()).thenReturn(true);
        when(publisherAdapter.getPublishers()).thenReturn(List.of(new PublisherRequestDTO(1L, "Publisher Name")));
        when(categoryAdapter.getCategories()).thenReturn(ResponseEntity.ok(List.of(new CategoryDTO())));

        String viewName = bookManageController.addBook(new BookRequestDTO(), bindingResult, model);

        assertEquals("admin/bookCreateForm", viewName);
        verify(model).addAttribute(eq("publishers"), anyList());
        verify(model).addAttribute(eq("categories"), anyList());
    }

    @Test
    @DisplayName("도서 등록 - 유효한 요청")
    void testAddBook_ValidRequest() {
        when(bindingResult.hasErrors()).thenReturn(false);

        when(bookAdapter.createBook(any())).thenReturn(ResponseEntity.ok(new BookDTO(
                1L,
                "Test Book",
                "Description",
                LocalDate.now(),
                1234567890123L,
                10000,
                9000,
                true,
                500,
                50,
                30L,
                new PublisherRequestDTO(1L, "Publisher Name"),
                new BookStatusRequestDTO(1L, "Available"),
                "path/to/image.jpg"
        )));

        doAnswer(invocation -> null).when(bookAdapter).incrementBookAmountIncrease(anyLong(), anyInt());

        String viewName = bookManageController.addBook(new BookRequestDTO(), bindingResult, model);

        assertEquals("redirect:/admin/bookManage", viewName);
    }
    @Test
    @DisplayName("도서 등록 - 유효하지 않은 요청 (BindingResult 에러)")
    void testAddBook_InvalidBindingResult() {
        // BindingResult에 에러가 있다고 설정
        when(bindingResult.hasErrors()).thenReturn(true);

        // Mock 데이터 설정
        when(publisherAdapter.getPublishers()).thenReturn(List.of(new PublisherRequestDTO(1L, "Publisher Name")));
        when(categoryAdapter.getCategories()).thenReturn(ResponseEntity.ok(List.of(new CategoryDTO())));

        // 테스트 실행
        String viewName = bookManageController.addBook(new BookRequestDTO(), bindingResult, model);

        // 결과 검증
        assertEquals("admin/bookCreateForm", viewName);
        verify(model).addAttribute(eq("publishers"), anyList());
        verify(model).addAttribute(eq("categories"), anyList());
    }

    @Test
    @DisplayName("도서 등록 - 성공")
    void testAddBook_Success() {
        // BindingResult에 에러가 없다고 설정
        when(bindingResult.hasErrors()).thenReturn(false);

        // Mock 데이터 설정
        BookDTO mockBook = new BookDTO(
                1L,
                "Test Book",
                "Description",
                LocalDate.now(),
                1234567890123L,
                10000,
                9000,
                true,
                500,
                50,
                30L,
                new PublisherRequestDTO(1L, "Publisher Name"),
                new BookStatusRequestDTO(1L, "Available"),
                "path/to/image.jpg"
        );
        when(bookAdapter.createBook(any())).thenReturn(ResponseEntity.ok(mockBook));
        doAnswer(invocation -> null).when(categoryAdapter).insertBook(anyLong(), anyLong());
        doAnswer(invocation -> null).when(bookAdapter).incrementBookAmountIncrease(anyLong(), anyInt());

        // 테스트 실행
        // 기존 생성자 호출에서는 작가 목록(authorName)이 누락되었으므로,
        // 마지막 인자로 예시 작가 목록(예: List.of("Author1"))을 추가합니다.
        BookRequestDTO request = new BookRequestDTO(
                "Valid Book",
                "Valid Description",
                LocalDate.now(),
                1234567890123L,
                10000,
                9000,
                true,
                "1",
                "1",
                List.of(1L, 2L),
                10,
                List.of("Author1")  // 누락된 마지막 인자 추가
        );
        String viewName = bookManageController.addBook(request, bindingResult, model);

        // 결과 검증
        assertEquals("redirect:/admin/bookManage", viewName);
        verify(bookAdapter).createBook(any());
        verify(categoryAdapter, times(2)).insertBook(anyLong(), eq(1L));
        verify(bookAdapter).incrementBookAmountIncrease(eq(1L), eq(10));
    }


    @Test
    @DisplayName("도서 등록 - API 호출 실패")
    void testAddBook_ApiCallFails() {
        // BindingResult에 에러가 없다고 설정
        when(bindingResult.hasErrors()).thenReturn(false);

        // Mock 데이터 설정: 도서 생성 API 실패 응답
        BookDTO mockBookDTO = new BookDTO(
                1L,
                "Test Book",
                "Description",
                LocalDate.now(),
                1234567890123L,
                10000,
                9000,
                true,
                500,
                50,
                30L,
                new PublisherRequestDTO(1L, "Publisher Name"),
                new BookStatusRequestDTO(1L, "Available"),
                "path/to/image.jpg"
        );
        when(bookAdapter.createBook(any()))
                .thenReturn(ResponseEntity.status(500).body(mockBookDTO)); // 실패 응답

        when(publisherAdapter.getPublishers())
                .thenReturn(List.of(new PublisherRequestDTO(1L, "Publisher Name")));
        when(categoryAdapter.getCategories())
                .thenReturn(ResponseEntity.ok(List.of(new CategoryDTO())));

        // authorAdapter.createAuthor에 대한 Stub 설정
        // (API 호출 시 AuthorRequestDTO를 받아, 적당한 AuthorDTO를 반환하도록 처리)
        when(authorAdapter.createAuthor(any(AuthorRequestDTO.class)))
                .thenReturn(new AuthorDTO(1L, "Author Name"));

        // 테스트 요청 객체 생성 (마지막 인자: 작가 목록)
        BookRequestDTO request = new BookRequestDTO(
                "Valid Book",
                "Valid Description",
                LocalDate.now(),
                1234567890123L,
                10000,
                9000,
                true,
                "1",
                "1",
                List.of(1L, 2L),
                10,
                List.of("Author1")
        );

        // 테스트 실행
        String viewName = bookManageController.addBook(request, bindingResult, model);

        // 결과 검증
        assertEquals("admin/bookCreateForm", viewName); // 실패 시 반환될 뷰 확인
        verify(model).addAttribute(eq("error"), eq("도서 등록에 실패했습니다.")); // 오류 메시지 확인
        verify(model).addAttribute(eq("publishers"), anyList()); // 출판사 목록이 모델에 포함되는지 확인
        verify(model).addAttribute(eq("categories"), anyList()); // 카테고리 목록이 모델에 포함되는지 확인
    }

    @Test
    @DisplayName("도서 등록 - 카테고리 추가 실패")
    void testAddBook_CategoryInsertFails() {
        // BindingResult에 에러가 없다고 설정
        when(bindingResult.hasErrors()).thenReturn(false);

        // Mock 데이터 설정
        BookDTO mockBook = new BookDTO(
                1L,
                "Test Book",
                "Description",
                LocalDate.now(),
                1234567890123L,
                10000,
                9000,
                true,
                500,
                50,
                30L,
                new PublisherRequestDTO(1L, "Publisher Name"),
                new BookStatusRequestDTO(1L, "Available"),
                "path/to/image.jpg"
        );
        when(bookAdapter.createBook(any())).thenReturn(ResponseEntity.ok(mockBook));

        // 카테고리 추가 시 예외 발생하도록 설정
        doThrow(new RuntimeException("Category insert failed"))
                .when(categoryAdapter).insertBook(anyLong(), anyLong());

        // 테스트 실행
        // 작가 목록(authorName) 파라미터를 추가하여 총 12개 인자를 전달합니다.
        BookRequestDTO request = new BookRequestDTO(
                "Valid Book",
                "Valid Description",
                LocalDate.now(),
                1234567890123L,
                10000,
                9000,
                true,
                "1",
                "1",
                List.of(1L, 2L),
                10,
                List.of("Author1")
        );

        // 예외가 발생하더라도 컨트롤러에서 처리하도록 테스트
        try {
            String viewName = bookManageController.addBook(request, bindingResult, model);

            // 결과 검증 (예외가 잡혀서 컨트롤러가 에러 처리를 한 경우)
            assertEquals("admin/bookCreateForm", viewName); // 예외 발생 시 반환될 뷰 확인
            verify(model).addAttribute(eq("error"), eq("도서 등록에 실패했습니다."));
        } catch (RuntimeException e) {
            // 예외 발생 시 메시지 확인 (컨트롤러에 예외가 전파되는 경우)
            assertEquals("Category insert failed", e.getMessage());
        }
    }


    @Test
    @DisplayName("도서 수정 폼 표시")
    void testShowEditBookForm() {
        when(bookAdapter.getBook(anyLong())).thenReturn(new BookDTO(
                1L,
                "Test Book",
                "Description",
                LocalDate.now(),
                1234567890123L,
                10000,
                9000,
                true,
                500,
                50,
                30L,
                new PublisherRequestDTO(1L, "Publisher Name"),
                new BookStatusRequestDTO(1L, "Available"),
                "path/to/image.jpg"
        ));
        when(bookStatusAdapter.getBookStatus(any())).thenReturn(List.of(new BookStatusRequestDTO(1L, "Available")));
        when(publisherAdapter.getPublishers()).thenReturn(List.of(new PublisherRequestDTO(1L, "Publisher Name")));
        when(categoryAdapter.getCategories()).thenReturn(ResponseEntity.ok(List.of(new CategoryDTO())));
        when(categoryAdapter.getAllCategoriesByBookId(anyLong())).thenReturn(ResponseEntity.ok(List.of(new PagedCategoryDTO())));

        String viewName = bookManageController.showEditBookForm(1L, model);

        assertEquals("admin/bookEditForm", viewName);
        verify(model).addAttribute(eq("isEdit"), eq(true));
        verify(model).addAttribute(eq("bookId"), eq(1L));
    }

    @Test
    @DisplayName("도서 수정 - 유효하지 않은 요청")
    void testUpdateBook_InvalidRequest() {
        when(bindingResult.hasErrors()).thenReturn(true);

        when(publisherAdapter.getPublishers())
                .thenReturn(List.of(new PublisherRequestDTO(1L, "Publisher Name")));
        when(bookStatusAdapter.getBookStatus(any()))
                .thenReturn(List.of(new BookStatusRequestDTO(1L, "Available")));
        when(categoryAdapter.getCategories())
                .thenReturn(ResponseEntity.ok(List.of(new CategoryDTO())));
        when(categoryAdapter.getAllCategoriesByBookId(anyLong()))
                .thenReturn(ResponseEntity.ok(List.of(new PagedCategoryDTO())));

        // 수정: DTO의 7번째 인자를 포함하도록 변경 (bookAmount 필드 추가)
        BookUpdateRequestDTO bookUpdateRequestDTO = new BookUpdateRequestDTO(
                "Invalid Title",         // bookTitle
                "Short",                 // bookDescription (너무 짧아서 유효하지 않음)
                -500,                    // bookPrice (음수여서 유효하지 않음)
                true,                    // bookWrappable
                -5,                      // bookAmount (양수가 아니므로 유효하지 않음)
                "1",                     // statusId
                List.of(1L, 2L)          // categoryIds
        );

        String viewName = bookManageController.updateBook(1L, bookUpdateRequestDTO, bindingResult, model);

        assertEquals("admin/bookEditForm", viewName);
        verify(model).addAttribute(eq("isEdit"), eq(true));
        verify(model).addAttribute(eq("bookId"), eq(1L));
        verify(model).addAttribute(eq("publishers"), anyList());
        verify(model).addAttribute(eq("categories"), anyList());
    }


    @Test
    @DisplayName("도서 수정 - 성공적으로 수정됨")
    void testUpdateBook_Success() {
        // Mock 데이터 설정
        Long bookId = 1L;

        // BookUpdateRequestDTO 생성 시 7개 필드 순서:
        // 1. bookTitle
        // 2. bookDescription
        // 3. bookPrice
        // 4. bookWrappable
        // 5. bookAmount
        // 6. statusId
        // 7. categoryIds
        BookUpdateRequestDTO requestDTO = new BookUpdateRequestDTO(
                "Updated Book",           // bookTitle
                "Updated Description",    // bookDescription
                15000,                    // bookPrice
                true,                     // bookWrappable
                20,                       // bookAmount (추가된 필드, 양수 값)
                "2",                      // statusId
                List.of(1L, 3L)           // categoryIds (추가할 카테고리는 3번, 기존에 있었던 1번 유지)
        );

        // 기존 카테고리 목록 설정 (수정 전 도서에 연결된 카테고리)
        PagedCategoryDTO category1 = new PagedCategoryDTO();
        category1.setCategoryId(1L);
        category1.setCategoryName("Category1");

        PagedCategoryDTO category2 = new PagedCategoryDTO();
        category2.setCategoryId(2L);
        category2.setCategoryName("Category2");

        List<PagedCategoryDTO> existingCategories = List.of(category1, category2);

        // Mock 설정
        when(categoryAdapter.getAllCategoriesByBookId(bookId))
                .thenReturn(ResponseEntity.ok(existingCategories));
        when(bindingResult.hasErrors()).thenReturn(false);

        // 반환값이 있는 메서드에 대한 Mock 설정
        when(categoryAdapter.insertBook(anyLong(), anyLong()))
                .thenReturn(ResponseEntity.ok().build());
        when(categoryAdapter.deleteByCategoryIdAndBookId(anyLong(), anyLong()))
                .thenReturn(ResponseEntity.ok().build());

        // void 메서드인 bookAdapter.updateBook의 동작 검증을 위해 doAnswer 사용
        doAnswer(invocation -> {
            Long passedBookId = invocation.getArgument(0);
            BookUpdateRequestDTO passedRequest = invocation.getArgument(1);

            // 전달된 매개변수 검증
            assertEquals(bookId, passedBookId);
            assertEquals(requestDTO, passedRequest);

            return null;
        }).when(bookAdapter).updateBook(eq(bookId), eq(requestDTO));

        // 테스트 실행
        String viewName = bookManageController.updateBook(bookId, requestDTO, bindingResult, model);

        // 결과 검증 : 성공 시 리다이렉트가 정상적으로 발생해야 함.
        assertEquals("redirect:/admin/bookManage", viewName);

        // Mock 호출 검증
        // 기존 연결된 카테고리: {1, 2} → 요청된 카테고리: {1, 3}
        // 즉, 카테고리 2는 삭제되고, 카테고리 3은 신규 추가됨.
        verify(categoryAdapter).insertBook(3L, bookId);     // 추가된 카테고리
        verify(categoryAdapter).deleteByCategoryIdAndBookId(2L, bookId); // 제거된 카테고리
        verify(bookAdapter).updateBook(bookId, requestDTO);   // 도서 수정 호출
    }

    @Test
    @DisplayName("도서 수정 - 카테고리 업데이트")
    void testUpdateBook_CategoryUpdate() {
        // Given: Mock 데이터 준비
        Long bookId = 1L;

        // 현재 카테고리 (기존 카테고리 목록)
        PagedCategoryDTO category1 = new PagedCategoryDTO();
        category1.setCategoryId(1L);
        category1.setCategoryName("Fiction");

        PagedCategoryDTO category2 = new PagedCategoryDTO();
        category2.setCategoryId(2L);
        category2.setCategoryName("Mystery");

        List<PagedCategoryDTO> currentCategories = List.of(category1, category2);

        // 업데이트된 카테고리 (사용자가 선택한 새 카테고리 목록)
        List<Long> updatedCategoryIds = List.of(2L, 3L); // 기존 2번 유지, 새로 3번 추가

        // BookUpdateRequestDTO 설정
        // 새로 추가된 필드 bookAmount(예: 20)를 포함하여 총 7개의 인자를 전달합니다.
        BookUpdateRequestDTO bookUpdateRequestDTO = new BookUpdateRequestDTO(
                "Updated Book Title",     // bookTitle
                "Updated Description",      // bookDescription
                12000,                      // bookPrice
                true,                       // bookWrappable
                20,                         // bookAmount (추가된 필드)
                "1",                      // statusId
                updatedCategoryIds          // categoryIds
        );

        // Mock: 현재 카테고리 목록 반환
        when(categoryAdapter.getAllCategoriesByBookId(bookId))
                .thenReturn(ResponseEntity.ok(currentCategories));

        // BindingResult 에러 없음 설정
        when(bindingResult.hasErrors()).thenReturn(false);

        // Mock: 카테고리 추가/삭제 API 호출
        when(categoryAdapter.insertBook(anyLong(), eq(bookId)))
                .thenReturn(ResponseEntity.ok().build());
        when(categoryAdapter.deleteByCategoryIdAndBookId(anyLong(), eq(bookId)))
                .thenReturn(ResponseEntity.ok().build());

        // Mock: 도서 업데이트 API 호출
        doAnswer(invocation -> {
            Long passedBookId = invocation.getArgument(0);
            BookUpdateRequestDTO passedRequest = invocation.getArgument(1);
            assertEquals(bookId, passedBookId);
            assertEquals(bookUpdateRequestDTO, passedRequest);
            return null;
        }).when(bookAdapter).updateBook(eq(bookId), any(BookUpdateRequestDTO.class));

        // When: 테스트 실행
        String viewName = bookManageController.updateBook(bookId, bookUpdateRequestDTO, bindingResult, model);

        // Then: 결과 검증
        assertEquals("redirect:/admin/bookManage", viewName); // 성공 시 리다이렉트 확인

        // 추가된 카테고리 검증 (3번 카테고리 추가)
        verify(categoryAdapter).insertBook(eq(3L), eq(bookId));

        // 삭제된 카테고리 검증 (1번 카테고리 삭제)
        verify(categoryAdapter).deleteByCategoryIdAndBookId(eq(1L), eq(bookId));

        // 유지된 카테고리 검증 (2번 카테고리 유지)
        verify(categoryAdapter, never()).deleteByCategoryIdAndBookId(eq(2L), eq(bookId));

        // 도서 업데이트 API 호출 검증
        verify(bookAdapter).updateBook(eq(bookId), eq(bookUpdateRequestDTO));
    }


    @Test
    @DisplayName("썸네일 업로드 성공")
    void testUploadThumbnail_Success() {
        when(imageStore.saveImages(anyList(), anyString())).thenReturn(true);
        when(imageStore.getImage(anyString())).thenReturn(List.of("path/to/image.jpg"));

        String viewName = bookManageController.uploadThumbnail(1L, thumbnail, model);

        assertEquals("redirect:/admin/bookManage", viewName);
    }

    @Test
    @DisplayName("썸네일 업로드 실패")
    void testUploadThumbnail_Failure() {
        when(imageStore.saveImages(anyList(), anyString())).thenReturn(false);

        String viewName = bookManageController.uploadThumbnail(1L, thumbnail, model);

        assertEquals("admin/thumbnailUpload", viewName);
        verify(model).addAttribute(eq("error"), anyString());
    }

    @Test
    @DisplayName("썸네일 업로드 폼 표시 - 성공적으로 렌더링됨")
    void testShowThumbnailUploadForm() {
        // Given: Mock 데이터 설정
        Long bookId = 1L;

        // When: 메서드 호출
        String viewName = bookManageController.showThumbnailUploadForm(bookId, model);

        // Then: 결과 검증
        assertEquals("admin/thumbnailUpload", viewName); // 올바른 뷰 이름 반환 확인
        verify(model).addAttribute("bookId", bookId); // Model에 bookId가 추가되었는지 검증
    }

    @Test
    @DisplayName("썸네일 업로드 실패 - 저장 오류")
    void testUploadThumbnail_SaveError() {
        when(imageStore.saveImages(anyList(), anyString())).thenReturn(false);

        String viewName = bookManageController.uploadThumbnail(1L, thumbnail, model);

        assertEquals("admin/thumbnailUpload", viewName);
        verify(model).addAttribute(eq("error"), eq("이미지 저장에 실패했습니다."));
    }

    @Test
    @DisplayName("도서 수정 폼 - 도서 없음")
    void testShowEditBookForm_BookNotFound() {
        when(bookAdapter.getBook(anyLong())).thenReturn(null);

        String viewName = bookManageController.showEditBookForm(1L, model);

        assertEquals("redirect:/admin/bookManage/bookManage", viewName);
    }
}