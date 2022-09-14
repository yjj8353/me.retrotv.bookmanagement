package me.retrotv.bookmanagement.domain.publisher;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 출판사({@link Publisher}) 리포지토리 계층.
 * @version 1.0
 * @author yjj8353
 */
public interface PublisherRepository extends JpaRepository<Publisher, Long> {

    /**
     * 출판사의 이름으로 하나의 출판사 정보를 되돌려주는 함수.
     * @param name 출판사명
     * @return 출판사 하나의 정보가 담긴 {@link Optional} 객체
     */
    Optional<Publisher> findByName(String name);
}
