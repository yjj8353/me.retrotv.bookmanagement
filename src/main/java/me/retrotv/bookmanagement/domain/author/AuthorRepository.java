package me.retrotv.bookmanagement.domain.author;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 저자({@link Author}) 리포지토리 계층.
 * @version 1.0
 * @author yjj8353
 */
public interface AuthorRepository extends JpaRepository<Author, Long> {

    /**
     * 저자의 이름으로 한명의 저자 정보를 되돌려주는 함수.
     * @param name 저자명
     * @return 저자 한명의 정보가 담긴 {@link Optional} 객체
     */
    Optional<Author> findByName(String name);
}
