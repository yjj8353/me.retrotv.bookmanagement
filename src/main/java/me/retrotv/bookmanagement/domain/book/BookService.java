package me.retrotv.bookmanagement.domain.book;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.retrotv.bookmanagement.domain.author.Author;
import me.retrotv.bookmanagement.domain.author.AuthorRepository;
import me.retrotv.bookmanagement.domain.image.Image;
import me.retrotv.bookmanagement.domain.image.ImageRepository;
import me.retrotv.bookmanagement.domain.member.Member;
import me.retrotv.bookmanagement.domain.member.MemberRepository;
import me.retrotv.bookmanagement.domain.publisher.Publisher;
import me.retrotv.bookmanagement.domain.publisher.PublisherRepository;
import me.retrotv.bookmanagement.relation.BookAuthor;
import me.retrotv.bookmanagement.relation.BookAuthorRepository;
import me.retrotv.bookmanagement.response.BasicError;
import me.retrotv.bookmanagement.response.BasicResult;

/**
 * 책({@link Book}) 서비스 계층.
 * @version 1.0
 * @author yjj8353
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BookService {
    private final ModelMapper modelMapper;
    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;
    private final BookAuthorRepository bookAuthorRepository;
    private final PublisherRepository publisherRepository;
    private final ImageRepository imageRepository;
    private final MemberRepository memberRepository;

    /**
     * 책 조회 요청을 받아 처리하고 결과를 돌려주는 함수.
     * @param bookDTO 책 조회조건을 담은 {@link BookDTO} 객체
     * @return 조회된 책 정보들이 담긴 List&lt;{@link BookDTO}&gt; 객체가 data로 담긴 {@link BasicResult} 객체
     */
    public BasicResult searchBooks(BookDTO bookDTO, Pageable pageable) {
        Optional<Member> member = memberRepository.findByRefreshToken(bookDTO.getMemberDTO().getRefreshToken());
        String email = member.isPresent() ? member.get().getEmail() : "";

        Specification<Book> spec = (root, query, builder) -> null;

        // 책 제목
        if(!"".equals(bookDTO.getTitle())) {
            log.debug("book title: {}", bookDTO.getTitle());
            spec = spec.and(BookSpecs.equalTitle(bookDTO.getTitle()));
        }

        // 저자 이름
        if(!"".equals(bookDTO.getAuthorDTOs().get(0).getName())) {
            log.debug("author name: {}", bookDTO.getAuthorDTOs().get(0).getName());
            spec = spec.and(BookSpecs.equalAuthorName(bookDTO.getAuthorDTOs().get(0).getName()));
        }

        // 출판사 이름
        if(!"".equals(bookDTO.getPublisherDTO().getName())) {
            log.debug("publisher name: {}", bookDTO.getPublisherDTO().getName());
            spec = spec.and(BookSpecs.equalPublisherName(bookDTO.getPublisherDTO().getName()));
        }

        // 사용자 이메일
        spec = spec.and(BookSpecs.equalMemberEmail(email));

        Page<Book> books = bookRepository.findAll(spec, pageable);
        List<BookDTO> bookDTOs = new ArrayList<>();

        books.getContent().forEach(book -> bookDTOs.add(book.toDTO()));

        log.debug("book count: {}", bookDTOs.size());

        // count에 페이지 개수를 넣었으므로 주의, 가능하다면 추후 수정할 것.
        return new BasicResult(null, bookDTOs, books.getTotalPages());
    }

    /**
     * 책 저장 요청을 받아 처리하고 결과를 돌려주는 함수.
     * @param bookDTO 저장할 책 정보를 담은 {@link BookDTO} 객체
     * @return 저장 결과를 담은 {@link BasicResult} 객체
     */
    public BasicResult saveBook(BookDTO.Save bookDTO) {
        
        // 현재 로그인 중인 계정정보를 가져옴.
        Member principal = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<Member> member = memberRepository.findByUsername(principal.getUsername());

        if(member.isPresent()) {
            boolean isDuplicate = !bookDTO.getIsbn().isEmpty() && bookRepository.findByMemberAndIsbn(member.get(), bookDTO.getIsbn()).isPresent();
            if(isDuplicate) {
                return new BasicError("이미 등록된 동일한 책이 존재합니다.", HttpStatus.CONFLICT);
            }
        } else {
            return new BasicError("부정한 계정 사용이 감지되었습니다.\n다시 로그인해 주십시오.", HttpStatus.BAD_REQUEST);
        }
        
        Book book = modelMapper.map(bookDTO, Book.class);
        List<Author> authors = new ArrayList<>();

        // 동일한 저자가 존재할 경우에는 해당 정보를 가져옴, 없다면 새로운 저자 정보를 생성함.
        bookDTO.getAuthorDTOs().forEach(authorDTO -> 
            authorRepository.findByName(authorDTO.getName())
                            .ifPresentOrElse(
                                authors::add,
                                () -> authors.add(modelMapper.map(authorDTO, Author.class))
                            )
        );
        
        // 저자와 마찬가지로 동일한 출판사가 존재할 경우에는 해당 정보를 가져옴, 없다면 새로운 출판사 정보를 생성함.
        Optional<Publisher> findPublisher = publisherRepository.findByName(bookDTO.getPublisherDTO().getName());
        Publisher publisher = null;

        if(findPublisher.isPresent()) {
            publisher = findPublisher.get();
        } else {
            publisher = modelMapper.map(bookDTO.getPublisherDTO(), Publisher.class);
        }

        book.updatePublisher(publisher);

        Image image = null;
        Long imageId = bookDTO.getImageDTO().getId();

        if(imageId != null) {
            image = imageRepository.findById(imageId).orElse(null);
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
        book.updateMember(member.get());

        bookRepository.save(book);

        authors.forEach(author -> log.debug("author name: {}", author.getName()));
        log.debug("publisher name: {}", publisher.getName());
        log.debug("image file name: {}", (image != null) ? image.getName() : "null");

        return new BasicResult("책 저장이 완료 되었습니다.");
    }

    /**
     * 책 수정 요청을 받아 처리하고 결과를 돌려주는 함수.
     * @param bookDTO 수정할 책 정보를 담은 {@link BookDTO} 객체
     * @return 수정 결과를 담은 {@link BasicResult} 객체
     */
    public BasicResult updateBook(BookDTO.Save bookDTO) {

        /* 책 제목, ISBN 정보 업데이트 */
        Book book = bookRepository.findById(bookDTO.getId())
                                  .orElseThrow(NoSuchElementException::new);

        book.updateTitle(bookDTO.getTitle());
        book.updateIsbn(bookDTO.getIsbn());

        if(bookDTO.getImageDTO().getId() != null) {
            imageRepository.findById(bookDTO.getImageDTO().getId())
                           .ifPresentOrElse(
                               book::updateImage,
                               () -> book.updateImage(null)
                           );
        }

        /* 출판사 정보 업데이트 */
        Publisher publisher = publisherRepository.findByName(bookDTO.getPublisherDTO().getName()).orElse(
            Publisher.builder()
                     .name(bookDTO.getPublisherDTO().getName())
                     .build()
        );

        book.updatePublisher(publisher);

        /* 저자 정보 업데이트 */
        List<Author> authors = new ArrayList<>();
        bookDTO.getAuthorDTOs().forEach(authorDTO -> {
            Author author = authorRepository.findByName(authorDTO.getName()).orElse(
                Author.builder()
                      .name(authorDTO.getName())
                      .build()
            );
            
            authors.add(author);
        });

        // BookAuthor 테이블에 추가하기 전에, 이전 데이터를 모두 삭제
        bookAuthorRepository.deleteByBookId(bookDTO.getId());

        // forEach를 사용하면 book 변수명을 인식하지 못해서 for문 사용.
        List<BookAuthor> bookAuthors = new ArrayList<>();
        for(Author author : authors) {
            BookAuthor bookAuthor = bookAuthorRepository.save(BookAuthor.builder()
                                                                        .book(book)
                                                                        .author(author)
                                                                        .build());
            bookAuthors.add(bookAuthor);
        }

        book.updateBookAuthors(bookAuthors);
        bookRepository.save(book);

        return new BasicResult("책 수정이 완료 되었습니다.");
    }

    /**
     * 책 삭제 요청을 받아 처리하고 결과를 돌려주는 함수.
     * @param id 삭제할 책의 고유한 ID 값
     * @return 삭제 결과를 담은 {@link BasicResult} 객체
     */
    public BasicResult deleteBook(Long id) {
        Book book = bookRepository.findById(id)
                                  .orElseThrow(NoSuchElementException::new);

        bookRepository.delete(book);
        return new BasicResult("책 삭제가 완료 되었습니다.");
    }
}
