package me.retrotv.bookmanagement.relation;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import me.retrotv.bookmanagement.domain.author.Author;
import me.retrotv.bookmanagement.domain.book.Book;

/**
 * 책({@link Book}) - 저자({@link Author}) 관계 엔티티의 리포지토리 계층.
 * @version 1.0
 * @author yjj8353
 */
public interface BookAuthorRepository extends JpaRepository<BookAuthor, BookAuthorId> {

    /**
     * 책({@link Book})의 ID 값을 이용해, 해당 ID를 가진 {@link BookAuthor} 객체의 배열을 반환하는 함수.
     * @param id {@link BookAuthor} 객체를 식별하기 위한 책({@link Book})의 ID 값
     * @return 해당 책의 ID 값을 가진 모든 {@link BookAuthor} 객체의 배열
     */
    List<BookAuthor> findByBookId(Long id);

    /**
     * 저자({@link Author})의 ID 값을 이용해, 해당 ID를 가진 {@link BookAuthor} 객체의 배열을 반환하는 함수.
     * @param id {@link BookAuthor} 객체를 식별하기 위한 저자({@link Author})의 ID 값
     * @return 해당 저자의 ID 값을 가진 모든 {@link BookAuthor} 객체의 배열
     */
    List<BookAuthor> findByAuthorId(Long id);

    /**
     * 책({@link Book})의 ID 값을 이용해, 해당 ID를 가진 {@link BookAuthor} 데이터를 모두 삭제하는 함수.
     * @param id {@link BookAuthor} 객체를 식별하기 위한 책({@link Book})의 ID 값
     */
    void deleteByBookId(Long id);

    /**
     * 저자({@link Author})의 ID 값을 이용해, 해당 ID를 가진 {@link BookAuthor} 데이터를 모두 삭제하는 함수.
     * @param id {@link BookAuthor} 객체를 식별하기 위한 저자({@link Author})의 ID 값
     */
    void deleteByAuthorId(Long id);
}
