package me.retrotv.bookmanagement.unit.domain.book;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import me.retrotv.bookmanagement.config.security.SpringSecurityConfig;
import me.retrotv.bookmanagement.domain.author.AuthorDTO;
import me.retrotv.bookmanagement.domain.book.BookController;
import me.retrotv.bookmanagement.domain.book.BookDTO;
import me.retrotv.bookmanagement.domain.book.BookService;
import me.retrotv.bookmanagement.domain.publisher.PublisherDTO;
import me.retrotv.bookmanagement.response.BasicResult;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.mockito.ArgumentMatchers.*;

@WebMvcTest(controllers = BookController.class,
            excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SpringSecurityConfig.class)
            })
@AutoConfigureMockMvc(addFilters = false)
class BookControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

    private BookDTO.Save getBookDTOSave() {
        PublisherDTO publisherDTO = PublisherDTO.builder()
                                                .id(null)
                                                .name("?????????")
                                                .build();

        AuthorDTO authorDTO = AuthorDTO.builder()
                                        .id(null)
                                        .name("?????????")
                                        .build();
        List<AuthorDTO> authorDTOs = new ArrayList<AuthorDTO>();
        authorDTOs.add(authorDTO);

        BookDTO.Save bookDTO = BookDTO.Save.builder()
                                           .id(null)
                                           .title("????????? ????????? AWS??? ?????? ???????????? ??? ?????????")
                                           .isbn("9788965402602")
                                           .publisherDTO(publisherDTO)
                                           .authorDTOs(authorDTOs)
                                           .build();

        return bookDTO;
    }

    private BookDTO getBookDTOSearch() {
        PublisherDTO publisherDTO = PublisherDTO.builder()
                                                .id(1L)
                                                .name("?????????")
                                                .build();

        AuthorDTO authorDTO = AuthorDTO.builder()
                                        .id(2L)
                                        .name("?????????")
                                        .build();
        List<AuthorDTO> authorDTOs = new ArrayList<AuthorDTO>();
        authorDTOs.add(authorDTO);

        BookDTO bookDTO = BookDTO.builder()
                                 .id(3L)
                                 .title("????????? ????????? AWS??? ?????? ???????????? ??? ?????????")
                                 .isbn("9788965402602")
                                 .publisherDTO(publisherDTO)
                                 .authorDTOs(authorDTOs)
                                 .build();

        return bookDTO;
    }

    @Test
    @DisplayName("??? ??????")
    void bookSave() throws Exception {
        
        /* give */
        String book = this.objectMapper.writeValueAsString(getBookDTOSave());
        Mockito.when(bookService.saveBook(any(BookDTO.Save.class))).thenReturn(new BasicResult("??? ????????? ?????? ???????????????."));
        
        /* when */
        mockMvc.perform(MockMvcRequestBuilders.post("/api/book/save")
                                              .contentType(APPLICATION_JSON)
                                              .content(book))
               .andDo(print())

               /* then */
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.success").value("true"))
               .andExpect(jsonPath("$.message").value("??? ????????? ?????? ???????????????."))
               .andExpect(jsonPath("$.data").isEmpty())
               .andExpect(jsonPath("$.count").value(0));
    }

    @Test
    @DisplayName("??? ??????")
    void bookSearch() throws Exception {

        /* give */
        List<BookDTO> bookDTOs = new ArrayList<>();
        bookDTOs.add(getBookDTOSearch());
        Mockito.when(bookService.searchBooks(any(BookDTO.class), any(PageRequest.class)))
               .thenReturn(new BasicResult(null, bookDTOs, bookDTOs.size()));

        /* when */
        mockMvc.perform(MockMvcRequestBuilders.get("/api/book/search")
                                              .param("title", "AWS")
                                              .param("authorName", "?????????")
                                              .param("publisherName", "?????????"))
               .andDo(print())

               /* then */
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.success").value("true"))
               .andExpect(jsonPath("$.message").isEmpty())
               .andExpect(jsonPath("$.data").isNotEmpty())
               .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    @DisplayName("??? ??????")
    void bookUpdate() throws Exception {

        /* give */
        String book = this.objectMapper.writeValueAsString(getBookDTOSave());
        Mockito.when(bookService.updateBook(any(BookDTO.Save.class))).thenReturn(new BasicResult("??? ????????? ?????? ???????????????."));

        /* when */
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/book/update")
                                              .contentType(APPLICATION_JSON)
                                              .content(book))
               .andDo(print())

               /* then */
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.success").value("true"))
               .andExpect(jsonPath("$.message").value("??? ????????? ?????? ???????????????."))
               .andExpect(jsonPath("$.data").isEmpty())
               .andExpect(jsonPath("$.count").value(0));
    }

    @Test
    @DisplayName("??? ??????")
    void bookDelete() throws Exception {

        /* give */
        BookDTO bookDTO = BookDTO.builder()
                                 .id(1L)
                                 .isbn("1234567890123")
                                 .build();
        String book = this.objectMapper.writeValueAsString(bookDTO);
        Mockito.when(bookService.deleteBook(anyLong())).thenReturn(new BasicResult("??? ????????? ?????? ???????????????."));

        /* when */
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/book/delete")
                                              .contentType(APPLICATION_JSON)
                                              .content(book))
               .andDo(print())

               /* then */
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.success").value("true"))
               .andExpect(jsonPath("$.message").value("??? ????????? ?????? ???????????????."))
               .andExpect(jsonPath("$.data").isEmpty())
               .andExpect(jsonPath("$.count").value(0));
    }
}
