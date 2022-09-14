package me.retrotv.bookmanagement.relation;

import java.io.Serializable;
import java.util.Objects;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.Hibernate;

import lombok.Getter;
import lombok.Setter;
import me.retrotv.bookmanagement.domain.author.Author;
import me.retrotv.bookmanagement.domain.book.Book;

/**
 * 책({@link Book}) - 저자({@link Author}) 연관관계 객체에 대한 복합키 객체
 * @version 1.0
 * @author yjj8353
 * @serial
 */
@Getter
@Setter
public class BookAuthorId implements Serializable {
    
    /**
     * 두개의 복합키가 동일한 키인지 확인하고 해당 여부를 boolean 값으로 반환하는 함수.
     * @param obj 책-저자 복합키 객체
     * @return 동일키 여부
     */
    @Override
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        }

        if(obj == null || Hibernate.getClass(this) != Hibernate.getClass(obj)) {
            return false;
        }

        BookAuthorId bookAuthorId = (BookAuthorId) obj;

        // book의 id 값과 author의 id 값이 매개변수 obj에 담긴 book, author의 id 값과 동일하면 동일한 Object로 판단함.
        return (Objects.equals(book, bookAuthorId.getBook()) && Objects.equals(author, bookAuthorId.getAuthor()));
    }

    /**
     * 책({@link Book}) 객체와 저자({@link Author}) 객체의 ID를 조합하고 해시화 한 뒤, 해당 해시 값을 int형으로 반환하는 함수.
     * @return 해시 값
     */
    @Override
    public int hashCode() {
        HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
        hashCodeBuilder.append(book);
        hashCodeBuilder.append(author);
        
        return hashCodeBuilder.toHashCode();
    }

    /**
     * 책(Book) 객체의 ID
     */
    private Long book;

    /**
     * 저자(Author) 객체의 ID
     */
    private Long author;
}
