package com.nhnacademy.hello.dto.book;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class BookFormDTO {

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 100, message = "제목은 최대 100자까지 입력할 수 있습니다.")
    private String bookTitle;

    @NotBlank(message = "설명은 필수입니다.")
    @Size(min = 10, max = 10000, message = "설명은 10자 이상 10,000자 이하이어야 합니다.")
    private String bookDescription;

    @NotNull(message = "가격은 필수입니다.")
    @Positive(message = "가격은 양수이어야 합니다.")
    private Integer bookPrice;

    @NotNull(message = "포장 가능 여부는 필수입니다.")
    private Boolean bookWrappable;

    @NotBlank(message = "도서 상태는 필수입니다.")
    private String statusId;

    @NotBlank(message = "출판사 ID는 필수입니다.")
    private String publisherId;

    @NotBlank(message = "ISBN은 필수입니다.")
    @Pattern(regexp = "\\d{10}(\\d{3})?", message = "ISBN은 10자리 또는 13자리 숫자여야 합니다.")
    private String bookIsbn;

    @NotNull(message = "출판일은 필수입니다.")
    @PastOrPresent(message = "출판일은 과거 또는 현재 날짜여야 합니다.")
    private LocalDate bookPubDate;

    @NotNull(message = "원가는 필수입니다.")
    @Positive(message = "원가는 양수이어야 합니다.")
    private Integer bookOriginPrice;

    @NotNull(message = "수량은 필수입니다.")
    @PositiveOrZero(message = "수량은 0 이상이어야 합니다.")
    private Integer bookAmount;

    // 기본 생성자
    public BookFormDTO() {
    }

    // 모든 필드를 포함한 생성자
    public BookFormDTO(String bookTitle, String bookDescription, Integer bookPrice, Boolean bookWrappable,
                       String statusId, String publisherId, String bookIsbn, LocalDate bookPubDate,
                       Integer bookOriginPrice, Integer bookAmount) {
        this.bookTitle = bookTitle;
        this.bookDescription = bookDescription;
        this.bookPrice = bookPrice;
        this.bookWrappable = bookWrappable;
        this.statusId = statusId;
        this.publisherId = publisherId;
        this.bookIsbn = bookIsbn;
        this.bookPubDate = bookPubDate;
        this.bookOriginPrice = bookOriginPrice;
        this.bookAmount = bookAmount;
    }

    // Getters and Setters

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getBookDescription() {
        return bookDescription;
    }

    public void setBookDescription(String bookDescription) {
        this.bookDescription = bookDescription;
    }

    public Integer getBookPrice() {
        return bookPrice;
    }

    public void setBookPrice(Integer bookPrice) {
        this.bookPrice = bookPrice;
    }

    public Boolean getBookWrappable() {
        return bookWrappable;
    }

    public void setBookWrappable(Boolean bookWrappable) {
        this.bookWrappable = bookWrappable;
    }

    public String getStatusId() {
        return statusId;
    }

    public void setStatusId(String statusId) {
        this.statusId = statusId;
    }

    public String getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(String publisherId) {
        this.publisherId = publisherId;
    }

    public String getBookIsbn() {
        return bookIsbn;
    }

    public void setBookIsbn(String bookIsbn) {
        this.bookIsbn = bookIsbn;
    }

    public LocalDate getBookPubDate() {
        return bookPubDate;
    }

    public void setBookPubDate(LocalDate bookPubDate) {
        this.bookPubDate = bookPubDate;
    }

    public Integer getBookOriginPrice() {
        return bookOriginPrice;
    }

    public void setBookOriginPrice(Integer bookOriginPrice) {
        this.bookOriginPrice = bookOriginPrice;
    }

    public Integer getBookAmount() {
        return bookAmount;
    }

    public void setBookAmount(Integer bookAmount) {
        this.bookAmount = bookAmount;
    }
}