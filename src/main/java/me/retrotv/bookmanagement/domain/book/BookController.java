package me.retrotv.bookmanagement.domain.book;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import me.retrotv.bookmanagement.domain.author.AuthorDTO;
import me.retrotv.bookmanagement.domain.member.MemberDTO;
import me.retrotv.bookmanagement.domain.publisher.PublisherDTO;
import me.retrotv.bookmanagement.response.BasicResult;

/**
 * 책({@link Book}) 컨트롤러 계층.
 * @version 1.0
 * @author yjj8353
 */
@ResponseBody
@RestController
@RequiredArgsConstructor
@RequestMapping("api/book")
public class BookController {
    private final BookService bookService;
    
    /**
     * 책 조회 요청을 처리하는 함수.
     * @param title 책 제목
     * @param authorName 저자명
     * @param publisherName 출판사명
     * @param username 사용자명
     * @param pageable 페이징 처리를 위한 정보가 담긴 변수
     * @return 조회 결과값이 담긴 ResponseEntity 객체
     */
    @GetMapping("/search")
    public ResponseEntity<BasicResult> bookSearch(@RequestParam("title") String title, 
                                                  @RequestParam("authorName") String authorName, 
                                                  @RequestParam("publisherName") String publisherName,
                                                  @RequestParam("refreshToken") String refreshToken,
                                                  @PageableDefault(size = 10, sort = "title") Pageable pageable) {

        List<AuthorDTO> authorDTOs = new ArrayList<>();
                        authorDTOs.add(AuthorDTO.builder().name(authorName).build());

        PublisherDTO publisherDTO = PublisherDTO.builder().name(publisherName).build();

        MemberDTO memberDTO = MemberDTO.builder()
                                       .refreshToken(refreshToken)
                                       .build();

        BookDTO bookDTO = BookDTO.builder()
                                 .title(title)
                                 .authorDTOs(authorDTOs)
                                 .publisherDTO(publisherDTO)
                                 .memberDTO(memberDTO)
                                 .build();
        
        BasicResult result = bookService.searchBooks(bookDTO, pageable);
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    /**
     * 책 저장 요청을 처리하는 함수.
     * @param bookDTO 프론트엔드에서 전달받은 JSON 데이터를 {@link BookDTO.Save}로 매핑한 객체
     * @return 저장 결과값이 담긴 ResponseEntity 객체
     */
    @PostMapping("/save")
    public ResponseEntity<BasicResult> bookSave(@Valid @RequestBody BookDTO.Save bookDTO) {
        BasicResult result = bookService.saveBook(bookDTO);
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    /**
     * 책 수정 요청을 처리하는 함수.
     * @param bookDTO 프론트엔드에서 전달받은 JSON 데이터를 {@link BookDTO.Save}로 매핑한 객체
     * @return 수정 결과값이 담긴 ResponseEntity 객체
     */
    @PatchMapping("/update")
    public ResponseEntity<BasicResult> bookUpdate(@Valid @RequestBody BookDTO.Save bookDTO) {
        BasicResult result = bookService.updateBook(bookDTO);
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    /**
     * 책 삭제 요청을 처리하는 함수.
     * @param bookDTO 프론트엔드에서 전달받은 JSON 데이터를 {@link BookDTO}로 매핑한 객체
     * @return 삭제 결과값이 담긴 ResponseEntity 객체
     */
    @DeleteMapping("/delete")
    public ResponseEntity<BasicResult> bookDelete(@Valid @RequestBody BookDTO bookDTO) {
        BasicResult result = bookService.deleteBook(bookDTO.getId());
        return ResponseEntity.status(result.getStatus()).body(result);
    }
}
