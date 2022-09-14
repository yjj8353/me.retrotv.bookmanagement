package me.retrotv.bookmanagement.domain.book;

import java.util.Collection;

import javax.persistence.criteria.JoinType;

import org.springframework.data.jpa.domain.Specification;

import me.retrotv.bookmanagement.relation.BookAuthor;

/**
 * 책({@link Book}) 조회를 위한 사양 클래스
 */
public class BookSpecs {

    // 인스턴스화 방지
    private BookSpecs() {
        throw new UnsupportedOperationException("This is a specification class and cannot be instantiated");
    }

    /**
     * 검색 조건 중, '책 제목'이 포함되는 책을 조회하도록 사양을 조합하고 반환한다.
     * @param title 책 제목
     * @return 사양({@link Specification})의 조합
     */
    public static Specification<Book> equalTitle(String title) {
        return (root, query, builder) -> builder.like(root.get("title"), "%" + title + "%");
    }

    /**
     * 검색 조건 중, '저자명'이 포함되는 책을 조회하도록 사양을 조합하고 반환한다.
     * @param bookAuthors 책-저자 관계 객체의 배열
     * @return 사양({@link Specification})의 조합
     */
    public static Specification<Book> equalAuthorName(String authorName) {
        return (root, query, builder) ->
               builder.equal(root.join("bookAuthors", JoinType.INNER)
               					 .join("author", JoinType.INNER)
                                 .get("name"), authorName);
    }

    /**
     * 검색 조건 중, '저자명'이 포함되는 책을 조회하도록 사양을 조합하고 반환한다.
     * @param bookAuthors 책-저자 관계 객체의 배열
     * @return 사양({@link Specification})의 조합
     */
    public static Specification<Book> equalBookAuthor(Collection<BookAuthor> bookAuthors) {
        return (root, query, builder) -> root.join("bookAuthors", JoinType.INNER).in(bookAuthors);
    }

    /**
     * 검색 조건 중, '출판사명'이 포함되는 책을 조회하도록 사양을 조합하고 반환한다.
     * @param publisherName 출판사명
     * @return 사양({@link Specification})의 조합
     */
    public static Specification<Book> equalPublisherName(String publisherName) {
        return (root, query, builder) ->
               builder.equal(root.join("publisher", JoinType.INNER)
                                 .get("name"), publisherName);
    }

    /**
     * 검색 조건 중, '사용자의 이메일'이 포함되는 책을 조회하도록 사양을 조합하고 반환한다.
     * @param email 사용자의 이메일
     * @return 사양({@link Specification})의 조합
     */
    public static Specification<Book> equalMemberEmail(String email) {
        return (root, query, builder) ->
               builder.equal(root.join("member", JoinType.INNER)
                                 .get("email"), email);
    }
}
