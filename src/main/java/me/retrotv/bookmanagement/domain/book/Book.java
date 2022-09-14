package me.retrotv.bookmanagement.domain.book;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.retrotv.bookmanagement.domain.author.AuthorDTO;
import me.retrotv.bookmanagement.domain.image.Image;
import me.retrotv.bookmanagement.domain.member.Member;
import me.retrotv.bookmanagement.domain.publisher.Publisher;
import me.retrotv.bookmanagement.relation.BookAuthor;

/**
 * 책 엔티티
 * @version 1.0
 * @author yjj8353
 */
@Getter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "BOOK")
public class Book {
    
    /*
     * 식별을 위한 고유 ID 값 [PRIMARY KEY]
     */
    @Id
    @Column(name = "BOOK_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    /*
     * 제목 [NOT NULL]
     */
    @Column(name = "BOOK_TITLE", nullable = false)
    private String title;
    
    /*
     * 국제 표준 도서 번호
     */
    @Column(name = "BOOK_ISBN", length = 13)
    private String isbn;
    
    /*
     * 저자 정보
     */
    @OneToMany(mappedBy = "book", cascade = { CascadeType.REMOVE, CascadeType.PERSIST }) // CascadeType -> 설정된 엔티티가 삭제/수정/저장 될 때, 해당 엔티티도 동일한 동작을 실행 함.
    private List<BookAuthor> bookAuthors;

    /*
     * 출판사 정보
     */
    @JoinColumn(name = "PUBLISHER_ID")
    @ManyToOne(cascade = CascadeType.PERSIST)
    private Publisher publisher;

    /*
     * 이미지 정보
     */
    @JoinColumn(name = "IMAGE_ID")
    @OneToOne(cascade = CascadeType.ALL)
    private Image image;

    /*
     * 등록자 정보
     */
    @JoinColumn(name = "MEMBER_ID")
    @OneToOne
    private Member member;

    /*
     * 등록 시간
     */
    @Column(name = "BOOK_REGIST_DATE", nullable = false)
    @CreationTimestamp
    private LocalDateTime registDate;

    /*
     * 수정 시간
     */
    @Column(name = "BOOK_UPDATE_DATE", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updateDate;

    /**
     * 출판사 정보 수정을 위한 함수
     * @param publisher 변경할 Publisher 객체
     * @see me.retrotv.bookmanagement.domain.publisher.Publisher
     */
    public void updatePublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    /**
     * 저자 정보 수정을 위한 함수
     * @param bookAuthors 변경할 BookAuthor 객체 배열
     * @see me.retrotv.bookmanagement.domain.author.Author
     * @see me.retrotv.bookmanagement.relation.BookAuthor
     */
    public void updateBookAuthors(List<BookAuthor> bookAuthors) {
        if(this.bookAuthors == null) {
            this.bookAuthors = new ArrayList<>();
        }

        this.bookAuthors = bookAuthors;
    }

    /**
     * 표지 정보 수정을 위한 함수
     * @param image 변경할 Image 객체
     * @see me.retrotv.bookmanagement.domain.image.Image
     */
    public void updateImage(Image image) {
        this.image = image;
    }

    /**
     * 제목 수정을 위한 함수
     * @param title 변경할 책 제목
     */
    public void updateTitle(String title) {
        this.title = title;
    }

    /**
     * 국제 표준 도서 번호 수정을 위한 함수
     * @param isbn 변경할 국제 표준 도서 번호
     */
    public void updateIsbn(String isbn) {
        this.isbn = isbn;
    }

    /**
     * 책을 저장하는 사용자 수정을 위한 함수
     * @param member 저장한 사용자 객체
     */
    public void updateMember(Member member) {
        this.member = member;
    }

    public BookDTO toDTO() {
        List<AuthorDTO> authorDTOs = new ArrayList<>();
        if(this.bookAuthors != null) {
            this.bookAuthors.forEach(bookAuthor -> authorDTOs.add(bookAuthor.getAuthor().toDTO()));
        }

        return BookDTO.builder()
                      .id(this.id)
                      .title(this.title)
                      .isbn(this.isbn)
                      .authorDTOs(authorDTOs)
                      .publisherDTO(this.publisher != null ? this.publisher.toDTO() : null)
                      .imageDTO(this.image != null ? this.image.toDTO() : null)
                      .build();
    }
}
