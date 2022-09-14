package me.retrotv.bookmanagement.domain.image;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 표지({@link Image}) 리포지토리 계층.
 * @version 1.0
 * @author yjj8353
 */
public interface ImageRepository extends JpaRepository<Image, Long>, JpaSpecificationExecutor<Image> {

}
