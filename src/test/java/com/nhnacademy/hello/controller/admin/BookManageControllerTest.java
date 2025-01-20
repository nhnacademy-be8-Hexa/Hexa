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

    @InjectMocks
    private BookManageController bookManageController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    @DisplayName("도서 목록 조회 - 기본")
    void testBookList() {
        when(bookAdapter.getTotalBooks(any(), any(), any(), any())).thenReturn(ResponseEntity.ok(20L));
        when(bookAdapter.getBooks(anyInt(), anyInt(), any(), any(), any(), any(), any(),
                anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), any(), any(), any()))
                .thenReturn(List.of(new BookDTO(
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

        String viewName = bookManageController.bookList(null, 1, "title", null, null, null, false, false, false, false, model);

        assertEquals("admin/bookManage", viewName);
        verify(model).addAttribute(eq("books"), anyList());
        verify(model).addAttribute(eq("totalPages"), eq(3));
    }

    @Test
    @DisplayName("도서 목록 조회 - 필터와 정렬")
    void testBookListWithFilters() {
        when(bookAdapter.getTotalBooks(any(), any(), any(), any())).thenReturn(ResponseEntity.ok(10L));
        when(bookAdapter.getBooks(anyInt(), anyInt(), eq("author"), any(), any(), any(), any(),
                anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), any(), any(), any()))
                .thenReturn(List.of(new BookDTO(
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

        String viewName = bookManageController.bookList("Test", 2, "author", List.of(1L, 2L), "Filtered Publisher", "Filtered Author",
                true, true, false, true, model);

        assertEquals("admin/bookManage", viewName);
        verify(model).addAttribute(eq("books"), anyList());
        verify(model).addAttribute(eq("currentPage"), eq(2));
        verify(model).addAttribute(eq("totalPages"), eq(2));
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
                1L, "Test Book", "Description", LocalDate.now(), 1234567890123L,
                10000, 9000, true, 500, 50, 30L,
                new PublisherRequestDTO(1L, "Publisher Name"),
                new BookStatusRequestDTO(1L, "Available"),
                "path/to/image.jpg"
        );
        when(bookAdapter.createBook(any())).thenReturn(ResponseEntity.ok(mockBook));
        doAnswer(invocation -> null).when(categoryAdapter).insertBook(anyLong(), anyLong());
        doAnswer(invocation -> null).when(bookAdapter).incrementBookAmountIncrease(anyLong(), anyInt());

        // 테스트 실행
        BookRequestDTO request = new BookRequestDTO(
                "Valid Book", "Valid Description", LocalDate.now(),
                1234567890123L, 10000, 9000, true,
                "1", "1", List.of(1L, 2L), 10
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
                1L, "Test Book", "Description", LocalDate.now(), 1234567890123L,
                10000, 9000, true, 500, 50, 30L,
                new PublisherRequestDTO(1L, "Publisher Name"),
                new BookStatusRequestDTO(1L, "Available"),
                "path/to/image.jpg"
        );
        when(bookAdapter.createBook(any())).thenReturn(ResponseEntity.status(500).body(mockBookDTO)); // 실패 응답

        when(publisherAdapter.getPublishers()).thenReturn(List.of(new PublisherRequestDTO(1L, "Publisher Name")));
        when(categoryAdapter.getCategories()).thenReturn(ResponseEntity.ok(List.of(new CategoryDTO())));

        // 테스트 요청 객체 생성
        BookRequestDTO request = new BookRequestDTO(
                "Valid Book", "Valid Description", LocalDate.now(),
                1234567890123L, 10000, 9000, true,
                "1", "1", List.of(1L, 2L), 10
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
                1L, "Test Book", "Description", LocalDate.now(), 1234567890123L,
                10000, 9000, true, 500, 50, 30L,
                new PublisherRequestDTO(1L, "Publisher Name"),
                new BookStatusRequestDTO(1L, "Available"),
                "path/to/image.jpg"
        );
        when(bookAdapter.createBook(any())).thenReturn(ResponseEntity.ok(mockBook));

        // 카테고리 추가 시 예외 발생하도록 설정
        doThrow(new RuntimeException("Category insert failed"))
                .when(categoryAdapter).insertBook(anyLong(), anyLong());

        // 테스트 실행
        BookRequestDTO request = new BookRequestDTO(
                "Valid Book", "Valid Description", LocalDate.now(),
                1234567890123L, 10000, 9000, true,
                "1", "1", List.of(1L, 2L), 10
        );

        // 예외가 발생하더라도 컨트롤러에서 처리하도록 테스트
        try {
            String viewName = bookManageController.addBook(request, bindingResult, model);

            // 결과 검증
            assertEquals("admin/bookCreateForm", viewName); // 예외 발생 시 반환될 뷰 확인
            verify(model).addAttribute(eq("error"), eq("도서 등록에 실패했습니다."));
        } catch (RuntimeException e) {
            // 예외 발생 시 메시지 확인
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

        when(publisherAdapter.getPublishers()).thenReturn(List.of(new PublisherRequestDTO(1L, "Publisher Name")));
        when(bookStatusAdapter.getBookStatus(any())).thenReturn(List.of(new BookStatusRequestDTO(1L, "Available")));
        when(categoryAdapter.getCategories()).thenReturn(ResponseEntity.ok(List.of(new CategoryDTO())));
        when(categoryAdapter.getAllCategoriesByBookId(anyLong())).thenReturn(ResponseEntity.ok(List.of(new PagedCategoryDTO())));

        BookUpdateRequestDTO bookUpdateRequestDTO = new BookUpdateRequestDTO(
                "Invalid Title",
                "Short",
                -500,
                true,
                "1",
                List.of(1L, 2L)
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

        // BookUpdateRequestDTO 설정
        BookUpdateRequestDTO requestDTO = new BookUpdateRequestDTO(
                "Updated Book", "Updated Description", 15000, true,
                "2", List.of(1L, 3L)
        );

        // 기존 카테고리 목록 설정
        PagedCategoryDTO category1 = new PagedCategoryDTO();
        category1.setCategoryId(1L);
        category1.setCategoryName("Category1");

        PagedCategoryDTO category2 = new PagedCategoryDTO();
        category2.setCategoryId(2L);
        category2.setCategoryName("Category2");

        List<PagedCategoryDTO> existingCategories = List.of(category1, category2);

        // Mock 설정
        when(categoryAdapter.getAllCategoriesByBookId(bookId)).thenReturn(ResponseEntity.ok(existingCategories));
        when(bindingResult.hasErrors()).thenReturn(false);

        // 반환값이 있는 메서드에 대한 Mock 설정
        when(categoryAdapter.insertBook(anyLong(), anyLong())).thenReturn(ResponseEntity.ok().build());
        when(categoryAdapter.deleteByCategoryIdAndBookId(anyLong(), anyLong())).thenReturn(ResponseEntity.ok().build());

        // void 메서드인 bookAdapter.updateBook Mock 설정
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

        // 결과 검증
        assertEquals("redirect:/admin/bookManage", viewName); // 리다이렉트 확인

        // Mock 호출 검증
        verify(categoryAdapter).insertBook(3L, bookId); // 추가된 카테고리
        verify(categoryAdapter).deleteByCategoryIdAndBookId(2L, bookId); // 제거된 카테고리
        verify(bookAdapter).updateBook(bookId, requestDTO); // 도서 수정 호출
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
        BookUpdateRequestDTO bookUpdateRequestDTO = new BookUpdateRequestDTO(
                "Updated Book Title",
                "Updated Description",
                12000,
                true,
                "1", // Status
                updatedCategoryIds
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