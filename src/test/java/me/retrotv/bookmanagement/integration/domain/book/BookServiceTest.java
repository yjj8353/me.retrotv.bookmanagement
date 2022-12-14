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
    @DisplayName("??? ?????? ??????")
    void saveBook() {
        PublisherDTO publisherDTO = PublisherDTO.builder()
                                                .id(null)
                                                .name("?????????")
                                                .build();

        AuthorDTO authorDTO1 = AuthorDTO.builder()
                                        .id(null)
                                        .name("?????????")
                                        .build();

        AuthorDTO authorDTO2 = AuthorDTO.builder()
                                        .id(null)
                                        .name("?????????2")
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
                                           .title("????????? ????????? AWS??? ?????? ???????????? ??? ?????????")
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
    @DisplayName("??? ??????")
    class SearchBooks {

        @ParameterizedTest(name = "[{index}] ??????: {0}, ?????????: {1}, ????????????: {2}")
        @CsvSource(
            textBlock = """
                AWS , null  , null
                null, ?????????, null
                null, null  , ?????????
                AWS , ?????????, ?????????
            """
            , nullValues="null"
        )
        @DisplayName("??????/?????????/?????????????????? ?????? ??????")
        void success(String title, String authorName, String publisherName) throws Exception {
            BasicResult result = bookService.searchBooks(getBookDTOForSearchBooks(title, authorName, publisherName), PageRequest.of(0, 10));
            List<BookDTO> findBooks = result.getCatedData(result.getData());
            BookDTO book = findBooks.get(0);

            assertNull(result.getMessage());
            assertEquals(1, findBooks.size());
            assertEquals("????????? ????????? AWS??? ?????? ???????????? ??? ?????????", book.getTitle());
            assertEquals("9788965402602", book.getIsbn());
            assertEquals(2, book.getAuthorDTOs().size());
            assertEquals("?????????", book.getAuthorDTOs().get(0).getName());
            assertEquals("?????????2", book.getAuthorDTOs().get(1).getName());
            assertEquals("?????????", book.getPublisherDTO().getName());
            assertEquals(1, result.getCount());
        }
    }

    @Nested
    @DisplayName("??? ?????? ????????????")
    class UpdateBook {

        @Test
        @DisplayName("??????")
        void success() {
            List<AuthorDTO> authorDTOs = new ArrayList<>();
            authorDTOs.add(AuthorDTO.builder().name("?????????").build());
            BookDTO.Save bookDTO = BookDTO.Save.builder()
                                               .id(bookId)
                                               .title("????????? ?????? ?????? ?????????")
                                               .isbn("9791158393083")
                                               .publisherDTO(PublisherDTO.builder().name("????????????").build())
                                               .authorDTOs(authorDTOs)
                                               .imageDTO(ImageDTO.builder().build())
                                               .build();

            BasicResult result = bookService.updateBook(bookDTO);

            assertEquals("??? ????????? ?????? ???????????????.", result.getMessage());

            Optional<Book> book = bookRepository.findById(bookId);
            Book updatedBook = book.get();

            assertEquals("????????? ?????? ?????? ?????????", updatedBook.getTitle());
            assertEquals("9791158393083", updatedBook.getIsbn());
            assertEquals("????????????", updatedBook.getPublisher().getName());
            assertEquals("?????????", updatedBook.getBookAuthors().get(0).getAuthor().getName());
        }
    }

    @Nested
    @DisplayName("??? ??????")
    class DeleteBook {

        @Test
        @DisplayName("??? ??????")
        void success() {
            BasicResult result = bookService.deleteBook(bookId);
            
            assertEquals("??? ????????? ?????? ???????????????.", result.getMessage());

            Optional<Book> deletedBook = bookRepository.findById(bookId);
            boolean deletedResult = deletedBook.isPresent();

            assertFalse(deletedResult);
        }
    }

    @AfterEach
    @DisplayName("?????? Book Delete")
    void deleteAllBooks() {
        log.info("===== ?????? ??? ?????? =====>");

        List<Book> books = bookRepository.findAll();
        books.forEach(book -> bookRepository.delete(book));

        log.info("===== ?????? ??? ?????? ?????? =====>");
    }
}
