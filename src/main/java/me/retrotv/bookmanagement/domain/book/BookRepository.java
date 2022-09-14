package me.retrotv.bookmanagement.domain.book;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import me.retrotv.bookmanagement.domain.member.Member;

/**
 * 책({@link Book}) 리포지토리 계층.
 * @version 1.0
 * @author yjj8353
 */
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {
    Optional<Book> findByMemberAndIsbn(Member member, String isbn);
}
