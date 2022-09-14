package me.retrotv.bookmanagement.relation;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.springframework.data.domain.Persistable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.retrotv.bookmanagement.domain.author.Author;
import me.retrotv.bookmanagement.domain.book.Book;

/**
 * 책(Book) - 저자(Author)의 ManyToMany 관계를 구현하기 위한 엔티티
 * @version 1.0
 * @author yjj8353
 */
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(BookAuthorId.class)
public class BookAuthor implements Persistable<String> {

    /**
     * <p>* Persistable 인터페이스 로부터 상속받음.</p>
     * <p>이 엔티티 객체가 새로 생성된 객체인지 확인하고 결과를 boolean 형태로 반환하는 함수.</p>
     * <p>BookAuthor 객체는 항상 생성만 하므로 강제로 true 값을 반환한다.</p>
     * @return true 값
     */
    @Override
    public boolean isNew() {

        // BookAuthor는 매번 새로 생성되므로 isNew()가 항상 true가 리턴 되도록 함.
        return true;
    }

    /**
     * <p>* Persistable 인터페이스 로부터 상속받음.</p>
     * <p>이 엔티티 객체의 ID 값을 반환하는 함수.</p>
     * <p>사용되지 않으므로 항상 null을 반환한다.</p>
     */
    @Override
    public String getId() {
        return null;
    }

    /*
     * 책(Book) - 저자(Author) ManyToMany 관계를 형성하는 Book 객체
     */
    @Id
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "BOOK_ID")
    private Book book;

    /*
     * 책(Book) - 저자(Author) ManyToMany 관계를 형성하는 Author 객체
     */
    @Id
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "AUTHOR_ID")
    private Author author;
}
