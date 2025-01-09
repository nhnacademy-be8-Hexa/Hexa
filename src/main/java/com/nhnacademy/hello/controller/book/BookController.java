package com.nhnacademy.hello.controller.book;

import com.nhnacademy.hello.common.feignclient.BookAdapter;
import com.nhnacademy.hello.common.feignclient.PointDetailsAdapter;
import com.nhnacademy.hello.common.feignclient.PointPolicyAdapter;
import com.nhnacademy.hello.common.feignclient.ReviewAdapter;
import com.nhnacademy.hello.common.util.AuthInfoUtils;
import com.nhnacademy.hello.common.util.SetImagePathsUtils;
import com.nhnacademy.hello.common.feignclient.tag.BooKTagAdapter;
import com.nhnacademy.hello.common.feignclient.tag.TagAdapter;
import com.nhnacademy.hello.dto.book.AuthorDTO;
import com.nhnacademy.hello.dto.book.BookDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.nhnacademy.hello.dto.point.CreatePointDetailDTO;
import com.nhnacademy.hello.dto.point.PointDetailsDTO;
import com.nhnacademy.hello.dto.point.PointPolicyDTO;
import com.nhnacademy.hello.dto.review.ReviewDTO;
import com.nhnacademy.hello.dto.review.ReviewRequestDTO;
import com.nhnacademy.hello.dto.tag.TagDTO;
import com.nhnacademy.hello.image.ImageStore;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class BookController {

    private final BookAdapter bookAdapter;
    private final ReviewAdapter reviewAdapter;
    private final ImageStore imageStore;
    private final SetImagePathsUtils setImagePathsUtils;
    private final TagAdapter tagAdapter;
    private final BooKTagAdapter bookTagAdapter;
    private final PointDetailsAdapter pointDetailsAdapter;
    private final PointPolicyAdapter pointPolicyAdapter;

    @GetMapping("/book/{bookId}")
    public String bookDetail(
            @PathVariable("bookId") Long bookId,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model
    ) {
        // 책 정보 전달
        BookDTO book = bookAdapter.getBook(bookId);
        model.addAttribute("book", book);

        // 책의 저자 리스트 전달
        List<String> authors = bookAdapter.getAuthors(bookId)
                .stream().map(AuthorDTO::authorName).toList();
        model.addAttribute("authors", authors);


        // 조회수 증가
        bookAdapter.incrementBookView(bookId);

        // 리뷰 갯수
        Long totalCount = reviewAdapter.getTotalReviewsFromBook(bookId).getBody();
        totalCount = totalCount != null ? totalCount : 0L;
        model.addAttribute("totalCount", totalCount);

        // 총 페이지 수 계산
        int totalPages = (int) Math.ceil((double) totalCount / size);
        model.addAttribute("totalPages", totalPages);

        // 현재 페이지 및 페이지 크기
        page = page == 0 ? 0 : page - 1;
        model.addAttribute("page", page);
        model.addAttribute("size", size);

        // 리뷰 목록 가져오기
        List<ReviewDTO> reviews = reviewAdapter.getReviewsFromBook(bookId, page, size).getBody();
        reviews = reviews != null ? reviews : List.of();
        model.addAttribute("reviews", reviews);

        // 책 섬네일
        String imageName = "bookThumbnail_" + book.bookId();
        List<String> imagePaths = imageStore.getImage(imageName);
        String imagePath = (imagePaths != null && !imagePaths.isEmpty()) ?
                imagePaths.get(0) : "/images/default-book.jpg"; // 기본 이미지 경로로 수정
        model.addAttribute("thumbnailImage", imagePath);

        // 리뷰 작성 폼을 위한 ReviewRequestDTO 추가
        String memberId = AuthInfoUtils.getUsername();
        ReviewRequestDTO reviewRequestDTO = new ReviewRequestDTO("", BigDecimal.ZERO);
        boolean isOrder = false;
        boolean isReview = false;
        if (Objects.nonNull(memberId)) {
            isOrder = Boolean.TRUE.equals(reviewAdapter.checkOrderBook(memberId, book.bookId()).getBody());
            isReview = Boolean.TRUE.equals(reviewAdapter.checkReviews(memberId, book.bookId()).getBody());
        }
        model.addAttribute("isOrder", isOrder);
        model.addAttribute("isReview", isReview);
        model.addAttribute("reviewRequestDTO", reviewRequestDTO);
        model.addAttribute("memberId", memberId);
        //책의 태그
        List<TagDTO> assignedTags = bookTagAdapter.getTagsByBook(bookId).getBody();
        if (assignedTags == null) {
            assignedTags = List.of();
        }


        model.addAttribute("assignedTags", assignedTags);
        model.addAttribute("bookId", bookId);

        return "book/bookDetail";

    }

    @PostMapping("/book/{bookId}/reviews/{reviewId}")
    public String bookReviewUpdate(
            @PathVariable Long bookId,
            @ModelAttribute("reviewRequestDTO") @Valid ReviewRequestDTO reviewRequestDTO,
            BindingResult bindingResult,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @PathVariable Long reviewId,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            // 검증 오류 발생 시, 필요한 모델 속성 재설정
            BookDTO book = bookAdapter.getBook(bookId);
            model.addAttribute("book", book);

            List<String> authors = bookAdapter.getAuthors(bookId)
                    .stream().map(AuthorDTO::authorName).toList();
            model.addAttribute("authors", authors);

            ResponseEntity<Long> likeCountResponse = bookAdapter.getLikeCount(bookId);
            Long likeCount = likeCountResponse.getBody();
            model.addAttribute("likeCount", likeCount != null ? likeCount : 0L);

            ResponseEntity<List<ReviewDTO>> reviewsResponse = reviewAdapter.getReviewsFromBook(bookId, page, size);
            List<ReviewDTO> reviews = reviewsResponse.getBody();
            reviews = reviews != null ? reviews : List.of();
            model.addAttribute("reviews", reviews);

            ResponseEntity<Long> totalReviewsResponse = reviewAdapter.getTotalReviewsFromBook(bookId);
            Long totalCount = totalReviewsResponse.getBody();
            totalCount = totalCount != null ? totalCount : 0L;
            model.addAttribute("totalCount", totalCount);

            // 총 페이지 수 계산
            int totalPages = (int) Math.ceil((double) totalCount / size);
            model.addAttribute("totalPages", totalPages);

            String imageName = "bookThumbnail_" + book.bookId();
            List<String> imagePaths = imageStore.getImage(imageName);
            String imagePath = (imagePaths != null && !imagePaths.isEmpty()) ?
                    imagePaths.get(0) : "/images/default-book.jpg";
            model.addAttribute("thumbnailImage", imagePath);

            // 현재 사용자 ID 가져오기
            String memberId = AuthInfoUtils.getUsername();
            model.addAttribute("memberId", memberId);

            return "book/bookDetail";
        }

        String memberId = AuthInfoUtils.getUsername();
        ResponseEntity<Void> updateReviewResponse = reviewAdapter.updateReview(reviewRequestDTO, reviewId);

        if (updateReviewResponse.getStatusCode().is2xxSuccessful()) {
            // 리뷰 수정 성공 후
            return "redirect:/book/" + bookId + "?success=true&page=" + page + "&size=" + size;
        } else {
            // 리뷰 작성 실패 시, 오류 메시지 추가 및 모델 재설정
            model.addAttribute("submissionError", "리뷰 작성에 실패했습니다. 다시 시도해주세요.");

            // 리뷰 목록 및 기타 모델 속성 재설정
            BookDTO book = bookAdapter.getBook(bookId);
            model.addAttribute("book", book);

            List<String> authors = bookAdapter.getAuthors(bookId)
                    .stream().map(AuthorDTO::authorName).toList();
            model.addAttribute("authors", authors);

            ResponseEntity<Long> likeCountResponse = bookAdapter.getLikeCount(bookId);
            Long likeCount = likeCountResponse.getBody();
            model.addAttribute("likeCount", likeCount != null ? likeCount : 0L);

            ResponseEntity<List<ReviewDTO>> reviewsResponse = reviewAdapter.getReviewsFromBook(bookId, page, size);
            List<ReviewDTO> reviews = reviewsResponse.getBody();
            reviews = reviews != null ? reviews : List.of();
            model.addAttribute("reviews", reviews);

            ResponseEntity<Long> totalReviewsResponse = reviewAdapter.getTotalReviewsFromBook(bookId);
            Long totalCount = totalReviewsResponse.getBody();
            totalCount = totalCount != null ? totalCount : 0L;
            model.addAttribute("totalCount", totalCount);

            // 총 페이지 수 계산
            int totalPages = (int) Math.ceil((double) totalCount / size);
            model.addAttribute("totalPages", totalPages);

            String imageName = "bookThumbnail_" + book.bookId();
            List<String> imagePaths = imageStore.getImage(imageName);
            String imagePath = (imagePaths != null && !imagePaths.isEmpty()) ?
                    imagePaths.get(0) : "/images/default-book.jpg";
            model.addAttribute("thumbnailImage", imagePath);

            // 리뷰 작성 폼을 위한 ReviewRequestDTO 재설정
            model.addAttribute("reviewRequestDTO", reviewRequestDTO);
            model.addAttribute("memberId", memberId);

            return "book/bookDetail";
        }
    }

    @PostMapping("/book/{bookId}/reviews")
    public String submitReview(
            @PathVariable("bookId") Long bookId,
            @ModelAttribute("reviewRequestDTO") @Valid ReviewRequestDTO reviewRequestDTO,
            BindingResult bindingResult,
            @RequestParam(value = "imagesIncluded", defaultValue = "false") boolean imagesIncluded, // 이미지 포함 여부 추가
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            // 검증 오류 발생 시, 필요한 모델 속성 재설정
            BookDTO book = bookAdapter.getBook(bookId);
            model.addAttribute("book", book);

            List<String> authors = bookAdapter.getAuthors(bookId)
                    .stream().map(AuthorDTO::authorName).toList();
            model.addAttribute("authors", authors);

            ResponseEntity<Long> likeCountResponse = bookAdapter.getLikeCount(bookId);
            Long likeCount = likeCountResponse.getBody();
            model.addAttribute("likeCount", likeCount != null ? likeCount : 0L);

            ResponseEntity<List<ReviewDTO>> reviewsResponse = reviewAdapter.getReviewsFromBook(bookId, page, size);
            List<ReviewDTO> reviews = reviewsResponse.getBody();
            reviews = reviews != null ? reviews : List.of();
            model.addAttribute("reviews", reviews);

            ResponseEntity<Long> totalReviewsResponse = reviewAdapter.getTotalReviewsFromBook(bookId);
            Long totalCount = totalReviewsResponse.getBody();
            totalCount = totalCount != null ? totalCount : 0L;
            model.addAttribute("totalCount", totalCount);

            // 총 페이지 수 계산
            int totalPages = (int) Math.ceil((double) totalCount / size);
            model.addAttribute("totalPages", totalPages);

            String imageName = "bookThumbnail_" + book.bookId();
            List<String> imagePaths = imageStore.getImage(imageName);
            String imagePath = (imagePaths != null && !imagePaths.isEmpty()) ?
                    imagePaths.get(0) : "/images/default-book.jpg";
            model.addAttribute("thumbnailImage", imagePath);

            // 현재 사용자 ID 가져오기
            String memberId = AuthInfoUtils.getUsername();
            model.addAttribute("memberId", memberId);

            return "book/bookDetail";
        }

        // 현재 사용자 ID 가져오기
        String memberId = AuthInfoUtils.getUsername();

        // 리뷰 생성
        ResponseEntity<Void> createReviewResponse = reviewAdapter.createReview(
                reviewRequestDTO,
                memberId,
                bookId
        );

        if (createReviewResponse.getStatusCode().is2xxSuccessful()) {
            // 이미지가 포함된 경우 추가 로직 실행
            if (imagesIncluded) {
                System.out.println("check");
                // 여기에 포인트 추가 등의 로직을 구현할 수 있습니다.
                // 예: userService.addPoints(memberId, 10);
                List<PointPolicyDTO> policies =  pointPolicyAdapter.getAllPointPolicies().getBody();
                PointPolicyDTO pointPolicyDTO =  policies.stream()
                        .filter(f -> f.pointPolicyName().contains("사진 o"))
                        .collect(Collectors.toList()).getFirst();
                CreatePointDetailDTO pointDetailDTO = new CreatePointDetailDTO(
                        pointPolicyDTO.pointDelta(),
                        pointPolicyDTO.pointPolicyName()
                );
                pointDetailsAdapter.createPointDetails(memberId, pointDetailDTO);
            }

            // 리뷰 작성 성공 후 리다이렉트
            return "redirect:/book/" + bookId + "?success=true&page=" + page + "&size=" + size;
        } else {
            // 리뷰 작성 실패 시, 오류 메시지 추가 및 모델 재설정
            model.addAttribute("submissionError", "리뷰 작성에 실패했습니다. 다시 시도해주세요.");

            // 리뷰 목록 및 기타 모델 속성 재설정
            BookDTO book = bookAdapter.getBook(bookId);
            model.addAttribute("book", book);

            List<String> authors = bookAdapter.getAuthors(bookId)
                    .stream().map(AuthorDTO::authorName).toList();
            model.addAttribute("authors", authors);

            ResponseEntity<Long> likeCountResponse = bookAdapter.getLikeCount(bookId);
            Long likeCount = likeCountResponse.getBody();
            model.addAttribute("likeCount", likeCount != null ? likeCount : 0L);

            ResponseEntity<List<ReviewDTO>> reviewsResponse = reviewAdapter.getReviewsFromBook(bookId, page, size);
            List<ReviewDTO> reviews = reviewsResponse.getBody();
            reviews = reviews != null ? reviews : List.of();
            model.addAttribute("reviews", reviews);

            ResponseEntity<Long> totalReviewsResponse = reviewAdapter.getTotalReviewsFromBook(bookId);
            Long totalCount = totalReviewsResponse.getBody();
            totalCount = totalCount != null ? totalCount : 0L;
            model.addAttribute("totalCount", totalCount);

            // 총 페이지 수 계산
            int totalPages = (int) Math.ceil((double) totalCount / size);
            model.addAttribute("totalPages", totalPages);

            String imageName = "bookThumbnail_" + book.bookId();
            List<String> imagePaths = imageStore.getImage(imageName);
            String imagePath = (imagePaths != null && !imagePaths.isEmpty()) ?
                    imagePaths.get(0) : "/images/default-book.jpg";
            model.addAttribute("thumbnailImage", imagePath);

            // 리뷰 작성 폼을 위한 ReviewRequestDTO 재설정
            model.addAttribute("reviewRequestDTO", reviewRequestDTO);
            model.addAttribute("memberId", memberId);

            return "book/bookDetail";
        }
    }
}
