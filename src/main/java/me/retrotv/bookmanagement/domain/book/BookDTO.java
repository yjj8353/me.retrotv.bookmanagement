package me.retrotv.bookmanagement.domain.book;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.retrotv.bookmanagement.domain.author.AuthorDTO;
import me.retrotv.bookmanagement.domain.image.ImageDTO;
import me.retrotv.bookmanagement.domain.member.MemberDTO;
import me.retrotv.bookmanagement.domain.publisher.PublisherDTO;
import me.retrotv.bookmanagement.relation.BookAuthorDTO;
import me.retrotv.bookmanagement.validator.ISBN;

/**
 * 책 데이터 전송 객체 (조회용)
 * @version 1.0
 * @author yjj8353
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookDTO {

    /*
     * 식별을 위한 고유 ID 값
     */
    private Long id;

    /*
     * 제목
     */
    private String title;

    /*
     * 국제 표준 도서 번호
     */
    @ISBN
    private String isbn;

    /*
     * 저자 정보
     */
    @JsonProperty("authors")
    private List<AuthorDTO> authorDTOs;

    /*
     * 책-저자 연관관계 정보
     */
    private List<BookAuthorDTO> bookAuthorDTOs;
    
    /*
     * 출판사 정보
     */
    @JsonProperty("publisher")
    private PublisherDTO publisherDTO;

    /*
     * 이미지 정보
     */
    @JsonProperty("image")
    private ImageDTO imageDTO;

    /*
     * 사용자 정보
     */
    @JsonProperty("member")
    private MemberDTO memberDTO;

    /**
     * 책 데이터 전송 객체 (추가 및 수정용)
     * @version 1.0
     * @author yjj8353
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Save {
        private Long id;
        private String title;

        @ISBN
        private String isbn;

        @JsonProperty("publisher")
        private PublisherDTO publisherDTO;

        @JsonProperty("authors")
        private List<AuthorDTO> authorDTOs;

        @JsonProperty("image")
        private ImageDTO imageDTO;

        @JsonProperty("member")
        private MemberDTO memberDTO;
    }
}
