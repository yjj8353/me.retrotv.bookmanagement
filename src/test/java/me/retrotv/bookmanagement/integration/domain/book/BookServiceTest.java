package me.retrotv.bookmanagement.integration.domain.book;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;

import me.retrotv.bookmanagement.domain.author.Author;
import me.retrotv.bookmanagement.domain.author.AuthorDTO;
import me.retrotv.bookmanagement.domain.book.Book;
import me.retrotv.bookmanagement.domain.book.BookDTO;
import me.retrotv.bookmanagement.domain.book.BookRepository;
import me.retrotv.bookmanagement.domain.book.BookService;
import me.retrotv.bookmanagement.domain.image.ImageDTO;
import me.retrotv.bookmanagement.domain.member.MemberDTO;
import me.retrotv.bookmanagement.domain.publisher.Publisher;
import me.retrotv.bookmanagement.domain.publisher.PublisherDTO;
import me.retrotv.bookmanagement.relation.BookAuthor;
import me.retrotv.bookmanagement.relation.BookAuthorRepository;
import me.retrotv.bookmanagement.response.BasicResult;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
class BookServiceTest {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private Long bookId;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private BookService bookService;

    @Autowired
    private BookRepository bookRepository = Mockito.mock(BookRepository.class);

    @Autowired
    private BookAuthorRepository bookAuthorRespository = Mockito.mock(BookAuthorRepository.class);

    private BookDTO getBookDTOForSearchBooks(String title, String authorName, String publisherName) {
        AuthorDTO authorDTO = authorName != null ? AuthorDTO.builder().name(authorName).build() : AuthorDTO.builder().name("").build();
        List<AuthorDTO> authorDTOs = new ArrayList<>();
        
        if(authorDTO != null) {
            authorDTOs.add(authorDTO);
        }

        PublisherDTO publisherDTO = publisherName != null ? PublisherDTO.builder().name(publisherName).build() : PublisherDTO.builder().name("").build();

        String bookTitle = title != null ? title : "";
        
        return BookDTO.builder()
                      .authorDTOs(authorDTOs)
                      .publisherDTO(publisherDTO)
                      .title(bookTitle)
                      .build();
    }

    @BeforeEach
    @DisplayName("책 한권 저장")
    void saveBook() {
        PublisherDTO publisherDTO = PublisherDTO.builder()
                                                .id(null)
                                                .name("프리렉")
                                                .build();

        AuthorDTO authorDTO1 = AuthorDTO.builder()
                                        .id(null)
                                        .name("이동욱")
                                        .build();

        AuthorDTO authorDTO2 = AuthorDTO.builder()
                                        .id(null)
                                        .name("이동욱2")
                                        .build();

        List<AuthorDTO> authorDTOs = new ArrayList<AuthorDTO>();
        authorDTOs.add(authorDTO1);
        authorDTOs.add(authorDTO2);

        MemberDTO memberDTO = MemberDTO.builder()
                                       .username("bookservicetest")
                                       .email("bookservicetest@naver.com")
                                       .build();

        BookDTO.Save bookDTO = BookDTO.Save.builder()
                                           .id(null)
                                           .title("스프링 부트와 AWS로 혼자 구현하는 웹 서비스")
                                           .isbn("9788965402602")
                                           .publisherDTO(publisherDTO)
                                           .authorDTOs(authorDTOs)
                                           .memberDTO(memberDTO)
                                           .build();

        List<Author> authors = new ArrayList<Author>();

        bookDTO.getAuthorDTOs().forEach(authorDTO -> authors.add(modelMapper.map(authorDTO, Author.class)));
        
        Book book = modelMapper.map(bookDTO, Book.class);
        Publisher publisher = modelMapper.map(bookDTO.getPublisherDTO(), Publisher.class);

        book.updatePublisher(publisher);

        List<BookAuthor> bookAuthors = new ArrayList<BookAuthor>();

        authors.forEach(author -> {
            BookAuthor bookAuthor = BookAuthor.builder()
                                                .book(book)
                                                .author(author)
                                                .build();
            bookAuthors.add(bookAuthor);
        });

        book.updateBookAuthors(bookAuthors);

        bookAuthorRespository.saveAll(bookAuthors);
        Book savedBook = bookRepository.saveAndFlush(book);

        this.bookId = savedBook.getId();
    }

    @Nested
    @DisplayName("책 조회")
    class SearchBooks {

        @ParameterizedTest(name = "[{index}] 제목: {0}, 저자명: {1}, 출판사명: {2}")
        @CsvSource(
            textBlock = """
                AWS , null  , null
                null, 이동욱, null
                null, null  , 프리렉
                AWS , 이동욱, 프리렉
            """
            , nullValues="null"
        )
        @DisplayName("제목/저자명/출판사명으로 조회 성공")
        void success(String title, String authorName, String publisherName) throws Exception {
            BasicResult result = bookService.searchBooks(getBookDTOForSearchBooks(title, authorName, publisherName), PageRequest.of(0, 10));
            List<BookDTO> findBooks = result.getCatedData(result.getData());
            BookDTO book = findBooks.get(0);

            assertNull(result.getMessage());
            assertEquals(1, findBooks.size());
            assertEquals("스프링 부트와 AWS로 혼자 구현하는 웹 서비스", book.getTitle());
            assertEquals("9788965402602", book.getIsbn());
            assertEquals(2, book.getAuthorDTOs().size());
            assertEquals("이동욱", book.getAuthorDTOs().get(0).getName());
            assertEquals("이동욱2", book.getAuthorDTOs().get(1).getName());
            assertEquals("프리렉", book.getPublisherDTO().getName());
            assertEquals(1, result.getCount());
        }
    }

    @Nested
    @DisplayName("책 정보 업데이트")
    class UpdateBook {

        @Test
        @DisplayName("성공")
        void success() {
            List<AuthorDTO> authorDTOs = new ArrayList<>();
            authorDTOs.add(AuthorDTO.builder().name("장정우").build());
            BookDTO.Save bookDTO = BookDTO.Save.builder()
                                               .id(bookId)
                                               .title("스프링 부트 핵심 가이드")
                                               .isbn("9791158393083")
                                               .publisherDTO(PublisherDTO.builder().name("위키북스").build())
                                               .authorDTOs(authorDTOs)
                                               .imageDTO(ImageDTO.builder().build())
                                               .build();

            BasicResult result = bookService.updateBook(bookDTO);

            assertEquals("책 수정이 완료 되었습니다.", result.getMessage());

            Optional<Book> book = bookRepository.findById(bookId);
            Book updatedBook = book.get();

            assertEquals("스프링 부트 핵심 가이드", updatedBook.getTitle());
            assertEquals("9791158393083", updatedBook.getIsbn());
            assertEquals("위키북스", updatedBook.getPublisher().getName());
            assertEquals("장정우", updatedBook.getBookAuthors().get(0).getAuthor().getName());
        }
    }

    @Nested
    @DisplayName("책 삭제")
    class DeleteBook {

        @Test
        @DisplayName("책 삭제")
        void success() {
            BasicResult result = bookService.deleteBook(bookId);
            
            assertEquals("책 삭제가 완료 되었습니다.", result.getMessage());

            Optional<Book> deletedBook = bookRepository.findById(bookId);
            boolean deletedResult = deletedBook.isPresent();

            assertFalse(deletedResult);
        }
    }

    @AfterEach
    @DisplayName("모든 Book Delete")
    void deleteAllBooks() {
        log.info("===== 모든 책 삭제 =====>");

        List<Book> books = bookRepository.findAll();
        books.forEach(book -> bookRepository.delete(book));

        log.info("===== 모든 책 삭제 완료 =====>");
    }
}
