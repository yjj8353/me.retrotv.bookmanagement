package me.retrotv.bookmanagement.domain.author;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
public class Author {
    
    /*
     * 식별을 위한 고유 ID 값 [PRIMARY KEY]
     */
    @Id
    @Column(name = "AUTHOR_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    /*
     * 저자명 [UNIQUE, NOT NULL]
     */
    @Column(name = "AUTHOR_NAME", nullable = false, unique = true, length = 30)
    private String name;

    /*
     * 등록 시간
     */
    @Column(name = "AUTHOR_REGIST_DATE", nullable = false)
    @CreationTimestamp
    private LocalDateTime registDate;

    /*
     * 수정 시간
     */
    @Column(name = "AUTHOR_UPDATE_DATE", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updateDate;

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
                        .id(this.id)
                        .name(this.name)
                        .build();
    }
}
