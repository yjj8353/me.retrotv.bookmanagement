package me.retrotv.bookmanagement.domain.publisher;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.retrotv.bookmanagement.domain.book.Book;
import me.retrotv.bookmanagement.domain.common.CommonEntity;

/**
 * 출판사 엔티티
 * @version 1.0
 * @author yjj8353
 */
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "PUBLISHER")
public class Publisher extends CommonEntity {

    /*
     * 출판사명 [UNIQUE, NOT NULL]
     */
    @NotEmpty
    @Column(name = "PUBLISHER_NAME", nullable = false, unique = true, length = 30)
    private String name;

    /*
     * 책 정보
     */
    @OneToMany(mappedBy = "publisher")
    private List<Book> books;

    /**
     * 출판사명 수정을 위한 함수
     * @param name 변경할 출판사명
     */
    public void updateName(String name) {
        this.name = name;
    }

    public PublisherDTO toDTO() {
        return PublisherDTO.builder()
                           .id(this.getId())
                           .name(this.name)
                           .build();
    }
}
