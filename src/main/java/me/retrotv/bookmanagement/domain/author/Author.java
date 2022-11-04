package me.retrotv.bookmanagement.domain.author;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.retrotv.bookmanagement.domain.common.CommonEntity;
import me.retrotv.bookmanagement.relation.BookAuthor;

/**
 * 저자 엔티티
 * @version 1.0
 * @author yjj8353
 */
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "AUTHOR")
public class Author extends CommonEntity {
    
    /*
     * 저자명 [UNIQUE, NOT NULL]
     */
    @Column(name = "AUTHOR_NAME", nullable = false, unique = true, length = 30)
    private String name;

    /*
     * 책 정보
     */
    @OneToMany(mappedBy = "author")
    private List<BookAuthor> bookAuthors;

    /**
     * 저자명 수정을 위한 함수
     * @param name 변경할 저자명
     */
    public void updateName(String name) {
        this.name = name;
    }

    public AuthorDTO toDTO() {
        return AuthorDTO.builder()
                        .id(this.getId())
                        .name(this.name)
                        .build();
    }
}
