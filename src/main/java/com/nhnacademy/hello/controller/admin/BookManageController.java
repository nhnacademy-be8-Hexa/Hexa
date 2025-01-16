package com.nhnacademy.hello.controller.admin;

import com.nhnacademy.hello.common.feignclient.*;
import com.nhnacademy.hello.dto.book.*;
import com.nhnacademy.hello.dto.category.CategoryDTO;
import com.nhnacademy.hello.dto.category.PagedCategoryDTO;
import com.nhnacademy.hello.image.ImageStore;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/admin/bookManage")
public class BookManageController {
    private final BookAdapter bookAdapter;
    private final PublisherAdapter publisherAdapter;
    private final BookStatusAdapter bookStatusAdapter;
    private final ImageStore imageStore;
    private final CategoryAdapter categoryAdapter;
    private final AuthorAdapter authorAdapter;

    @Autowired
    public BookManageController(BookAdapter bookAdapter, PublisherAdapter publisherAdapter,
                                BookStatusAdapter bookStatusAdapter, ImageStore imageStore,
                                CategoryAdapter categoryAdapter, AuthorAdapter authorAdapter) {
        this.bookAdapter = bookAdapter;
        this.publisherAdapter = publisherAdapter;
        this.bookStatusAdapter = bookStatusAdapter;
        this.imageStore = imageStore;
        this.categoryAdapter = categoryAdapter;
        this.authorAdapter = authorAdapter;
    }

    @GetMapping
    public String bookList(
            // 검색 파라미터
            @RequestParam(value = "search", required = false) String search,

            // 페이징 파라미터 (size는 고정값 9)
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,

            // 정렬 파라미터
            @RequestParam(value = "sort", required = false, defaultValue = "title") String sort,

            // 추가적인 검색 및 정렬 파라미터
            @RequestParam(value = "categoryIds", required = false) List<Long> categoryIds,
            @RequestParam(value = "publisherName", required = false) String publisherName,
            @RequestParam(value = "authorName", required = false) String authorName,
            @RequestParam(value = "sortByView", required = false, defaultValue = "false") Boolean sortByView,
            @RequestParam(value = "sortBySellCount", required = false, defaultValue = "false") Boolean sortBySellCount,
            @RequestParam(value = "sortByLikeCount", required = false, defaultValue = "false") Boolean sortByLikeCount,
            @RequestParam(value = "latest", required = false, defaultValue = "false") Boolean latest,

            Model model
    ) {
        // 고정된 페이지 사이즈
        final int size = 9;

        // 페이지 번호 조정 (1 기반을 0 기반으로 변환)
        int adjustedPage = (page != null && page > 1) ? page - 1 : 0;

        // 도서 총계 가져오기 (필터링 조건을 반영)
        Long totalBooks = bookAdapter.getTotalBooks(
                search,
                categoryIds,
                publisherName,
                authorName
        ).getBody();

        // 전체 페이지 수 계산 (size가 9인 것을 전제로 함)
        int totalPages = (int) Math.ceil((double) totalBooks / size);

        // 현재 페이지가 총 페이지 수를 초과하지 않도록 조정
        if (page > totalPages && totalPages != 0) {
            adjustedPage = totalPages - 1;
            page = totalPages;
        }

        // 도서 목록 가져오기
        List<BookDTO> books = bookAdapter.getBooks(
                adjustedPage,
                size,
                sort,
                search,
                categoryIds,
                publisherName,
                authorName,
                sortByView,
                sortBySellCount,
                sortByLikeCount,
                latest,
                null,
                null,
                null
        );

        // 모델에 데이터 추가
        model.addAttribute("books", books);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);
        model.addAttribute("search", search);
        model.addAttribute("categoryIds", categoryIds);
        model.addAttribute("publisherName", publisherName);
        model.addAttribute("authorName", authorName);
        model.addAttribute("sortByView", sortByView);
        model.addAttribute("sortBySellCount", sortBySellCount);
        model.addAttribute("sortByLikeCount", sortByLikeCount);
        model.addAttribute("latest", latest);

        // 뷰 이름 반환 (예: "admin/bookManage")
        return "admin/bookManage";
    }

    /**
     * 도서 등록 폼 페이지 로드
     *
     * @param model 모델 객체
     * @return 도서 등록 페이지 뷰
     */
    @GetMapping("/add")
    public String showAddBookForm(Model model) {
        BookRequestDTO bookRequestDTO = new BookRequestDTO();
        model.addAttribute("bookRequestDTO", bookRequestDTO);

        // 출판사 목록 불러오기
        List<PublisherRequestDTO> publishers = publisherAdapter.getPublishers();
        model.addAttribute("publishers", publishers);

        // 카테고리 목록 불러오기
        ResponseEntity<List<CategoryDTO>> categoriesResponse = categoryAdapter.getCategories();
        if (categoriesResponse.getStatusCode().is2xxSuccessful()) {
            model.addAttribute("categories", categoriesResponse.getBody());
        } else {
            model.addAttribute("categories", List.of());
        }

        return "admin/bookCreateForm"; // Thymeleaf 템플릿 경로
    }

    /**
     * 도서 등록 처리 (POST /admin/bookManage/add)
     *
     * @param bookRequestDTO 도서 등록 정보
     * @return 도서 목록 페이지로 리다이렉트
     */
    @PostMapping("/add")
    public String addBook(@Valid @ModelAttribute("bookRequestDTO") BookRequestDTO bookRequestDTO,
                          BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            // 출판사 목록과 카테고리 목록을 다시 모델에 추가
            List<PublisherRequestDTO> publishers = publisherAdapter.getPublishers();
            model.addAttribute("publishers", publishers);

            ResponseEntity<List<CategoryDTO>> categoriesResponse = categoryAdapter.getCategories();
            if (categoriesResponse.getStatusCode().is2xxSuccessful()) {
                model.addAttribute("categories", categoriesResponse.getBody());
            } else {
                model.addAttribute("categories", List.of());
            }

            return "admin/bookCreateForm";
        }

        // 도서 등록 API 호출
        ResponseEntity<BookDTO> response = bookAdapter.createBook(bookRequestDTO);

        // 작가 등록 API 호출
        List<AuthorRequestDTO> authorRequestDTO = new ArrayList<>();
        List<String> authors = bookRequestDTO.authorName();

        for (String author : authors) {
            authorRequestDTO.add(new AuthorRequestDTO(author, response.getBody().bookId()));
        }

        for (AuthorRequestDTO authorRequestDTO1 : authorRequestDTO) {
            authorAdapter.createAuthor(authorRequestDTO1);
        }

        List<Long> categoryIds = bookRequestDTO.categoryIds();

        for (Long categoryId : categoryIds) {
            categoryAdapter.insertBook(categoryId, response.getBody().bookId());
        }

        bookAdapter.incrementBookAmountIncrease(response.getBody().bookId(), bookRequestDTO.bookAmount());

        if (response.getStatusCode().is2xxSuccessful()) {
            return "redirect:/admin/bookManage"; // 도서 목록 페이지로 리다이렉트
        } else {
            model.addAttribute("error", "도서 등록에 실패했습니다.");
            // 출판사 목록과 카테고리 목록을 다시 모델에 추가
            List<PublisherRequestDTO> publishers = publisherAdapter.getPublishers();
            model.addAttribute("publishers", publishers);

            ResponseEntity<List<CategoryDTO>> categoriesResponse = categoryAdapter.getCategories();
            if (categoriesResponse.getStatusCode().is2xxSuccessful()) {
                model.addAttribute("categories", categoriesResponse.getBody());
            } else {
                model.addAttribute("categories", List.of());
            }

            return "admin/bookCreateForm";
        }
    }

    // 도서 수정 폼
    @GetMapping("/edit/{bookId}")
    public String showEditBookForm(@PathVariable Long bookId, Model model) {
        BookDTO book = bookAdapter.getBook(bookId);
        if (book == null) {
            // 도서가 존재하지 않을 경우 처리 (예: 에러 페이지로 이동)
            return "redirect:/admin/bookManage/bookManage";
        }


        // BookDTO를 BookUpdateRequestDTO로 매핑
        BookUpdateRequestDTO bookUpdateRequestDTO = new BookUpdateRequestDTO(
                book.bookTitle(),
                book.bookDescription(),
                book.bookPrice(),
                book.bookWrappable(),
                null,
                book.bookStatus().bookStatusId().toString(),
                new ArrayList<>()
        );

        List<BookStatusRequestDTO> bookStatuses = bookStatusAdapter.getBookStatus(bookId.toString());

        model.addAttribute("bookUpdateRequestDTO", bookUpdateRequestDTO);
        model.addAttribute("isEdit", true);
        model.addAttribute("bookId", bookId);
        model.addAttribute("bookStatuses", bookStatuses);

        // 출판사 목록 추가 (선택 사항)
        List<PublisherRequestDTO> publishers = publisherAdapter.getPublishers();
        model.addAttribute("publishers", publishers);

        // 카테고리 목록 불러오기
        ResponseEntity<List<CategoryDTO>> categoriesResponse = categoryAdapter.getCategories();
        if (categoriesResponse.getStatusCode().is2xxSuccessful()) {
            model.addAttribute("categories", categoriesResponse.getBody());
        } else {
            model.addAttribute("categories", List.of());
        }

        List<PagedCategoryDTO> selectedCategories = categoryAdapter.getAllCategoriesByBookId(bookId).getBody();
        List<Long> selectedCategoryIds = selectedCategories != null ? selectedCategories.stream()
                .map(PagedCategoryDTO::getCategoryId)
                .toList() : List.of();
        model.addAttribute("selectedCategoryIds", selectedCategoryIds);

        return "admin/bookEditForm"; // 도서 수정 폼 템플릿
    }

    @PostMapping("/edit/{bookId}")
    public String updateBook(@PathVariable Long bookId,
                             @Valid @ModelAttribute("bookUpdateRequestDTO") BookUpdateRequestDTO bookUpdateRequestDTO,
                             BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", true);
            model.addAttribute("bookId", bookId);

            List<PublisherRequestDTO> publishers = publisherAdapter.getPublishers();
            model.addAttribute("publishers", publishers);

            List<BookStatusRequestDTO> bookStatuses = bookStatusAdapter.getBookStatus(bookId.toString());
            model.addAttribute("bookStatuses", bookStatuses);

            ResponseEntity<List<CategoryDTO>> categoriesResponse = categoryAdapter.getCategories();
            if (categoriesResponse.getStatusCode().is2xxSuccessful()) {
                model.addAttribute("categories", categoriesResponse.getBody());
            } else {
                model.addAttribute("categories", List.of());
            }

            List<PagedCategoryDTO> selectedCategories = categoryAdapter.getAllCategoriesByBookId(bookId).getBody();
            List<Long> selectedCategoryIds = selectedCategories != null ? selectedCategories.stream()
                    .map(PagedCategoryDTO::getCategoryId)
                    .toList() : List.of();
            model.addAttribute("selectedCategoryIds", selectedCategoryIds);

            return "admin/bookEditForm";
        }

        List<PagedCategoryDTO> currentCategories = categoryAdapter.getAllCategoriesByBookId(bookId).getBody();
        List<Long> currentCategoryIds = currentCategories != null ? currentCategories.stream()
                .map(PagedCategoryDTO::getCategoryId)
                .toList() : List.of();

        List<Long> updatedCategoryIds = Optional.ofNullable(bookUpdateRequestDTO.categoryIds())
                .orElse(List.of());


        // 추가해야 할 카테고리들 (현재 카테고리 목록에 없는 것들)
        List<Long> categoriesToAdd = updatedCategoryIds.stream()
                .filter(categoryId -> !currentCategoryIds.contains(categoryId))
                .toList();

        // 삭제해야 할 카테고리들 (현재 카테고리 목록에 있지만, DTO에 없는 것들)
        List<Long> categoriesToRemove = currentCategoryIds.stream()
                .filter(categoryId -> !updatedCategoryIds.contains(categoryId))
                .toList();

        // 카테고리 추가 로직 (추가해야 할 카테고리가 있을 때만)
        if (!categoriesToAdd.isEmpty()) {
            for (Long categoryId : categoriesToAdd) {
                categoryAdapter.insertBook(categoryId, bookId);
            }
        }

        // 카테고리 삭제 로직 (삭제해야 할 카테고리가 있을 때만)
        if (!categoriesToRemove.isEmpty()) {
            for (Long categoryId : categoriesToRemove) {
                categoryAdapter.deleteByCategoryIdAndBookId(categoryId, bookId);
            }
        }

        // 도서 수량 증가
        bookAdapter.incrementBookAmountIncrease(bookId, bookUpdateRequestDTO.bookAmount());
        
        bookAdapter.updateBook(bookId, bookUpdateRequestDTO);
        return "redirect:/admin/bookManage";
    }

    /**
     * 도서 썸네일 업로드 폼을 표시하는 GET 메서드
     *
     * @param bookId 도서 ID
     * @param model  모델
     * @return 썸네일 업로드 템플릿
     */
    @GetMapping("/thumbnail/{bookId}")
    public String showThumbnailUploadForm(@PathVariable Long bookId, Model model) {
        model.addAttribute("bookId", bookId);
        return "admin/thumbnailUpload"; // thumbnailUpload.html 템플릿
    }

    /**
     * 도서 썸네일 업로드를 처리하는 POST 메서드
     *
     * @param bookId    도서 ID
     * @param thumbnail 업로드할 이미지 파일
     * @param model     모델
     * @return 리다이렉트 URL 또는 다시 업로드 폼
     */
    @PostMapping("/thumbnail/{bookId}")
    public String uploadThumbnail(@PathVariable Long bookId,
                                  @RequestParam("thumbnail") MultipartFile thumbnail,
                                  Model model) {
        try {
            // 파일 이름 생성 (도서 ID를 포함하여 유니크하게)
            String fileName = "bookThumbnail_" + bookId;

            // 이미지 저장
            boolean success = imageStore.saveImages(List.of(thumbnail), fileName);
            if (!success) {
                model.addAttribute("error", "이미지 저장에 실패했습니다.");
                model.addAttribute("bookId", bookId);
                return "admin/thumbnailUpload";
            }

            // 저장된 이미지 URL 가져오기
            List<String> imageUrls = imageStore.getImage(fileName);
            if (imageUrls.isEmpty()) {
                model.addAttribute("error", "이미지 URL을 가져오는 데 실패했습니다.");
                model.addAttribute("bookId", bookId);
                return "admin/thumbnailUpload";
            }

            // 성공 메시지와 함께 도서 수정 페이지로 리다이렉트
            return "redirect:/admin/bookManage";
        } catch (Exception e) {
            model.addAttribute("error", "썸네일 업로드 중 오류가 발생했습니다: " + e.getMessage());
            model.addAttribute("bookId", bookId);
            return "admin/thumbnailUpload";
        }
    }
}
