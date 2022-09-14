package me.retrotv.bookmanagement.unit.domain.book;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration.AccessLevel;
import org.springframework.http.HttpStatus;

import me.retrotv.bookmanagement.domain.author.Author;
import me.retrotv.bookmanagement.domain.author.AuthorDTO;
import me.retrotv.bookmanagement.domain.author.AuthorRepository;
import me.retrotv.bookmanagement.domain.book.Book;
import me.retrotv.bookmanagement.domain.book.BookDTO;
import me.retrotv.bookmanagement.domain.book.BookRepository;
import me.retrotv.bookmanagement.domain.book.BookService;
import me.retrotv.bookmanagement.domain.image.Image;
import me.retrotv.bookmanagement.domain.image.ImageDTO;
import me.retrotv.bookmanagement.domain.image.ImageRepository;
import me.retrotv.bookmanagement.domain.member.MemberRepository;
import me.retrotv.bookmanagement.domain.publisher.Publisher;
import me.retrotv.bookmanagement.domain.publisher.PublisherDTO;
import me.retrotv.bookmanagement.domain.publisher.PublisherRepository;
import me.retrotv.bookmanagement.relation.BookAuthor;
import me.retrotv.bookmanagement.relation.BookAuthorRepository;
import me.retrotv.bookmanagement.response.BasicResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;

@DisplayName("Book Service 계층 단위 테스트")
class BookServiceTest {
    private AuthorRepository authorRepository = Mockito.mock(AuthorRepository.class);
    private BookRepository bookRepository = Mockito.mock(BookRepository.class);
    private BookAuthorRepository bookAuthorRespository = Mockito.mock(BookAuthorRepository.class);
    private PublisherRepository publisherRepository = Mockito.mock(PublisherRepository.class);
    private ImageRepository imageRepository = Mockito.mock(ImageRepository.class);
    private MemberRepository memberRepository = Mockito.mock(MemberRepository.class);
    private ModelMapper modelMapper;
    private BookService bookService;

    private BookDTO getFindBookDTO() {
        PublisherDTO publisherDTO = PublisherDTO.builder()
                                                .id(1L)
                                                .name("프리렉")
                                                .build();

        AuthorDTO authorDTO = AuthorDTO.builder()
                                        .id(1L)
                                        .name("이동욱")
                                        .build();
        List<AuthorDTO> authorDTOs = new ArrayList<AuthorDTO>();
        authorDTOs.add(authorDTO);

        ImageDTO imageDTO = ImageDTO.builder()
                                    .id(1L)
                                    .name("XL")
                                    .proxyName("XL.jpeg")
                                    .format("jpeg")
                                    .path("src/test/java/me/retrotv/bookmanagement/unit/domain/image/")
                                    .build();

        BookDTO bookDTO = BookDTO.builder()
                                 .id(1L)
                                 .title("스프링 부트와 AWS로 혼자 구현하는 웹 서비스")
                                 .isbn("9788965402602")
                                 .publisherDTO(publisherDTO)
                                 .authorDTOs(authorDTOs)
                                 .imageDTO(imageDTO)
                                 .build();

        return bookDTO;
    }

    private BookDTO.Save getBookDTOSave() {
        PublisherDTO publisherDTO = PublisherDTO.builder()
                                                .id(null)
                                                .name("프리렉")
                                                .build();

        AuthorDTO authorDTO = AuthorDTO.builder()
                                        .id(null)
                                        .name("이동욱")
                                        .build();
        List<AuthorDTO> authorDTOs = new ArrayList<AuthorDTO>();
        authorDTOs.add(authorDTO);

        ImageDTO imageDTO = ImageDTO.builder()
                                    .id(1L)
                                    .name("XL")
                                    .proxyName("XL.jpeg")
                                    .format("jpeg")
                                    .path("src/test/java/me/retrotv/bookmanagement/unit/domain/image/")
                                    .build();

        BookDTO.Save bookDTO = BookDTO.Save.builder()
                                           .id(null)
                                           .title("스프링 부트와 AWS로 혼자 구현하는 웹 서비스")
                                           .isbn("9788965402602")
                                           .publisherDTO(publisherDTO)
                                           .authorDTOs(authorDTOs)
                                           .imageDTO(imageDTO)
                                           .build();

        return bookDTO;
    }

    private BookDTO.Save getModifyBookDTOSave() {
        PublisherDTO publisherDTO = PublisherDTO.builder()
                                                .id(null)
                                                .name("새출판사")
                                                .build();

        AuthorDTO authorDTO = AuthorDTO.builder()
                                        .id(null)
                                        .name("새저자")
                                        .build();
        List<AuthorDTO> authorDTOs = new ArrayList<AuthorDTO>();
        authorDTOs.add(authorDTO);

        ImageDTO imageDTO = ImageDTO.builder()
                                    .id(2L)
                                    .name("XL2")
                                    .proxyName("XL2.jpeg")
                                    .format("jpeg")
                                    .path("src/test/java/me/retrotv/bookmanagement/unit/domain/image/")
                                    .build();

        BookDTO.Save bookDTO = BookDTO.Save.builder()
                                           .id(1L)
                                           .title("새로운 책 제목")
                                           .isbn("1111111111111")
                                           .publisherDTO(publisherDTO)
                                           .authorDTOs(authorDTOs)
                                           .imageDTO(imageDTO)
                                           .build();

        return bookDTO;
    }

    @BeforeEach
    void setup() {

        // unit test에서 ModelMapper 설정이 변경된 Bean이 @Autowired로 주입되지 않으므로 직접 설정해야 함. 
        modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                   .setFieldAccessLevel(AccessLevel.PRIVATE)
                   .setFieldMatchingEnabled(true);

        bookService = new BookService(modelMapper,
                                      authorRepository,
                                      bookRepository,
                                      bookAuthorRespository, 
                                      publisherRepository,
                                      imageRepository,
                                      memberRepository);
    }

    @Nested
    @DisplayName("책 저장")
    class SaveBook {

        @Test
        @DisplayName("성공 - 저자/출판사 중복 없음")
        void successAuthorAndPublisherNotDuplicate() {

            /* give */
            BookDTO.Save bookDTO = getBookDTOSave();
            Book book = modelMapper.map(bookDTO, Book.class);
            List<Author> authors = new ArrayList<>();

            bookDTO.getAuthorDTOs().forEach(authorDTO -> {
                Mockito.when(authorRepository.findByName(authorDTO.getName())).thenReturn(Optional.ofNullable(null));

                /* when */
                authorRepository.findByName(authorDTO.getName())
                            .ifPresentOrElse(
                                authors::add,
                                () -> authors.add(modelMapper.map(authorDTO, Author.class))
                            );
            });

            /* then */
            assertEquals(1, authors.size());
            Mockito.verify(authorRepository, times(1)).findByName(anyString());

            /* give */
            Mockito.when(publisherRepository.findByName(bookDTO.getPublisherDTO().getName())).thenReturn(Optional.ofNullable(null));
            
            /* when */
            Optional<Publisher> findPublisher = publisherRepository.findByName(bookDTO.getPublisherDTO().getName());
            Publisher publisher = null;

            if(!findPublisher.isPresent()) {
                publisher = modelMapper.map(bookDTO.getPublisherDTO(), Publisher.class);

                /* then */
                assertEquals(bookDTO.getPublisherDTO().getName(), modelMapper.map(bookDTO.getPublisherDTO(), Publisher.class).getName());
            }

            book.updatePublisher(publisher);
            
            /* give */
            Image image = null;
            Long imageId = bookDTO.getImageDTO().getId();

            if(imageId != null) {
                Mockito.when(imageRepository.findById(imageId)).thenReturn(Optional.of(Image.builder()
                                                                                            .id(1L)
                                                                                            .name("XL")
                                                                                            .proxyName("XL.jpeg")
                                                                                            .format("jpeg")
                                                                                            .path("src/test/java/me/retrotv/bookmanagement/unit/domain/image/")
                                                                                            .build()));

                /* when */
                image = imageRepository.findById(imageId).orElse(null);
                
                /* then */
                assertEquals(bookDTO.getImageDTO().getName(), image.getName());
            }

            book.updateImage(image);

            List<BookAuthor> bookAuthors = new ArrayList<>();

            authors.forEach(author ->
                bookAuthors.add(
                    BookAuthor.builder()
                              .book(book)
                              .author(author)
                              .build()
                )
            );

            book.updateBookAuthors(bookAuthors);

            assertEquals("스프링 부트와 AWS로 혼자 구현하는 웹 서비스", book.getTitle());
            assertEquals("9788965402602", book.getIsbn());
            assertEquals("이동욱", book.getBookAuthors().get(0).getAuthor().getName());
            assertEquals("프리렉", book.getPublisher().getName());
            assertEquals("XL", book.getImage().getName());
            assertEquals("jpeg", book.getImage().getFormat());

            BasicResult result = bookService.saveBook(bookDTO);
            assertEquals("책 저장이 완료 되었습니다.", result.getMessage());
        }

        @Test
        @DisplayName("성공 - 저자/출판사 중복")
        void successAuthorAndPublisherDuplicate() throws Exception {
            BookDTO.Save bookDTO = getBookDTOSave();
            List<Author> authors = new ArrayList<>();

            bookDTO.getAuthorDTOs().forEach(authorDTO -> {
                Mockito.when(authorRepository.findByName(authorDTO.getName())).thenReturn(Optional.of(Author.builder().id(1L).name("이동욱").build()));
                authorRepository.findByName(authorDTO.getName())
                            .ifPresentOrElse(
                                authors::add,
                                () -> authors.add(modelMapper.map(authorDTO, Author.class))
                            );
            });

            assertEquals(1, authors.size());
            Mockito.verify(authorRepository, times(1)).findByName(anyString());

            Book book = modelMapper.map(bookDTO, Book.class);

            Mockito.when(publisherRepository.findByName(bookDTO.getPublisherDTO().getName())).thenReturn(Optional.of(Publisher.builder().id(1L).name("프리렉").build()));
            Optional<Publisher> findPublisher = publisherRepository.findByName(bookDTO.getPublisherDTO().getName());
            Publisher publisher = null;

            if(findPublisher.isPresent()) {
                publisher = findPublisher.get();
                assertEquals(bookDTO.getPublisherDTO().getName(), publisher.getName());
            }

            book.updatePublisher(publisher);

            Image image = null;
            Long imageId = bookDTO.getImageDTO().getId();

            if(imageId != null) {
                Mockito.when(imageRepository.findById(imageId)).thenReturn(Optional.of(Image.builder()
                                                                                            .id(1L)
                                                                                            .name("XL")
                                                                                            .proxyName("XL.jpeg")
                                                                                            .format("jpeg")
                                                                                            .path("src/test/java/me/retrotv/bookmanagement/unit/domain/image/")
                                                                                            .build()));

                image = imageRepository.findById(imageId).orElse(null);
            }

            book.updateImage(image);

            List<BookAuthor> bookAuthors = new ArrayList<>();

            authors.forEach(author -> {
                BookAuthor bookAuthor = BookAuthor.builder()
                                                  .book(book)
                                                  .author(author)
                                                  .build();
                bookAuthors.add(bookAuthor);    
            });

            book.updateBookAuthors(bookAuthors);

            assertEquals("스프링 부트와 AWS로 혼자 구현하는 웹 서비스", book.getTitle());
            assertEquals("9788965402602", book.getIsbn());
            assertEquals("이동욱", book.getBookAuthors().get(0).getAuthor().getName());
            assertEquals("프리렉", book.getPublisher().getName());
            assertEquals("XL", book.getImage().getName());
            assertEquals("jpeg", book.getImage().getFormat());

            BasicResult result = bookService.saveBook(bookDTO);
            assertEquals("책 저장이 완료 되었습니다.", result.getMessage());
        }
    }

    /*
     * 책 조회시 사용하는 Specification은 단위 테스트가 어려우므로, 책 조회와 관련된 테스트는 통합 테스트로 대체함.
     */

    @Nested
    @DisplayName("책 수정")
    class UpdateBook {
        
        @Test
        @DisplayName("성공")
        void success() {
            BookDTO.Save bookDTO = getModifyBookDTOSave();

            Mockito.when(bookRepository.findById(1L)).thenReturn(Optional.of(modelMapper.map(bookDTO, Book.class)));
            Book book = bookRepository.findById(1L).orElseThrow(() -> new NoSuchElementException("조회된 책이 존재하지 않습니다."));

            Mockito.when(imageRepository.findById(2L)).thenReturn(Optional.ofNullable(Image.builder()
                                                                                               .id(2L)
                                                                                               .name("XL2")
                                                                                               .proxyName("XL2.jpeg")
                                                                                               .format("jpeg")
                                                                                               .path("src/test/java/me/retrotv/bookmanagement/unit/domain/image/")
                                                                                               .build()));
            Image image = imageRepository.findById(bookDTO.getImageDTO().getId()).orElse(null);
            book.updateImage(image);

            Mockito.when(publisherRepository.findByName(bookDTO.getPublisherDTO().getName())).thenReturn(Optional.of(modelMapper.map(bookDTO.getPublisherDTO(), Publisher.class)));
            Publisher publisher = publisherRepository.findByName(bookDTO.getPublisherDTO().getName()).orElse(Publisher.builder()
                                                                                                                      .id(null)
                                                                                                                      .name(bookDTO.getPublisherDTO().getName())
                                                                                                                      .build());
            
            assertEquals(bookDTO.getPublisherDTO().getName(), publisher.getName());
            book.updatePublisher(publisher);

            List<Author> authors = new ArrayList<>();
            bookDTO.getAuthorDTOs().forEach(authorDTO -> {
                Mockito.when(authorRepository.findByName(authorDTO.getName())).thenReturn(Optional.of(modelMapper.map(authorDTO, Author.class)));
                Author author = authorRepository.findByName(authorDTO.getName()).orElse(
                    Author.builder()
                          .id(null)
                          .name(authorDTO.getName())
                          .build()
                );
                
                Mockito.when(authorRepository.save(author)).thenReturn(author);
                author = authorRepository.save(author);

                assertEquals(bookDTO.getAuthorDTOs().get(0).getName(), author.getName());
                authors.add(author);
            });

            List<BookAuthor> bookAuthors = new ArrayList<>();
            for(Author author : authors) {
                BookAuthor bookAuthor = bookAuthorRespository.save(BookAuthor.builder()
                                                                             .book(book)
                                                                             .author(author)
                                                                             .build());
                bookAuthors.add(bookAuthor);
            }

            book.updateBookAuthors(bookAuthors);
            Mockito.when(bookRepository.saveAndFlush(book)).thenReturn(book);
            Book updatedBook = bookRepository.saveAndFlush(book);

            assertEquals(book, updatedBook);
            
            BasicResult result = bookService.updateBook(bookDTO);
            
            assertEquals("책 수정이 완료 되었습니다.", result.getMessage());
            assertEquals(HttpStatus.OK, result.getStatus());
        }
    }

    @Nested
    @DisplayName("책 삭제")
    class DeleteBook {

        @Test
        @DisplayName("성공")
        void success() {
            BookDTO bookDTO = getFindBookDTO();

            Mockito.when(bookRepository.findById(1L)).thenReturn(Optional.of(modelMapper.map(bookDTO, Book.class)));
            Book book = bookRepository.findById(1L)
                                      .orElseThrow(() -> new NoSuchElementException("조회된 책이 존재하지 않습니다."));
            Mockito.verify(bookRepository, times(1)).findById(1L);

            bookRepository.delete(book);
            Mockito.verify(bookRepository, times(1)).delete(book);
            
            BasicResult result = bookService.deleteBook(1L);

            assertEquals("책 삭제가 완료 되었습니다.", result.getMessage());
            assertEquals(HttpStatus.OK, result.getStatus());
        }
    }
}
