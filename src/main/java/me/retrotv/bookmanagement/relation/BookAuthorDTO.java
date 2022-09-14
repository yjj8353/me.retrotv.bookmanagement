package me.retrotv.bookmanagement.relation;

import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.retrotv.bookmanagement.domain.author.AuthorDTO;
import me.retrotv.bookmanagement.domain.book.BookDTO;

/**
 * 책(Book) - 저자(Author)의 ManyToMany 관계를 구현하기 위한 데이터 전송 객체
 * @version 1.0
 * @author yjj8353
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookAuthorDTO {

    /*
     * 책(Book) - 저자(Author) ManyToMany 관계를 형성하는 BookDTO 객체
     */
    @NotBlank
    private BookDTO bookDTO;

    /*
     * 책(Book) - 저자(Author) ManyToMany 관계를 형성하는 AuthorDTO 객체
     */
    @NotBlank
    private AuthorDTO authorDTO;
}
