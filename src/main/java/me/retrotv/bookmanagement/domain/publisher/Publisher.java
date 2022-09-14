package me.retrotv.bookmanagement.domain.publisher;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.retrotv.bookmanagement.domain.book.Book;

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
public class Publisher {
    
    /*
     * 식별을 위한 고유 ID 값 [PRIMARY KEY]
     */
    @Id
    @Column(name = "PUBLISHER_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

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

    /*
     * 등록 시간
     */
    @Column(name = "PUBLISHER_REGIST_DATE", nullable = false)
    @CreationTimestamp
    private LocalDateTime registDate;

    /*
     * 수정 시간
     */
    @Column(name = "PUBLISHER_UPDATE_DATE", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updateDate;

    /**
     * 출판사명 수정을 위한 함수
     * @param name 변경할 출판사명
     */
    public void updateName(String name) {
        this.name = name;
    }

    public PublisherDTO toDTO() {
        return PublisherDTO.builder()
                           .id(this.id)
                           .name(this.name)
                           .build();
    }
}
