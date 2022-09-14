package me.retrotv.bookmanagement.domain.publisher;

import java.util.List;

import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.retrotv.bookmanagement.domain.book.BookDTO;

/**
 * 출판사 데이터 전송 객체
 * @version 1.0
 * @author yjj8353
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublisherDTO {

    /*
     * 식별을 위한 고유 ID 값
     */
    private Long id;

    /*
    * 출판사명
    */
    @NotBlank(message = "출판사명은 필수 입니다.")
    private String name;

    /*
     * 책 정보
     */
    private List<BookDTO> bookDTOs;
}
