package me.retrotv.bookmanagement.domain.naver;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import me.retrotv.bookmanagement.response.BasicResult;

@ResponseBody
@RestController
@RequiredArgsConstructor
@RequestMapping("api/naver-book-api")
public class NaverController {
    private final NaverService naverService;
    
    @GetMapping("/search")
    public ResponseEntity<BasicResult> bookSearch(@RequestParam("isbn") String isbn) {
        BasicResult result = naverService.searchBookByIsbn(isbn);
        return ResponseEntity.status(result.getStatus()).body(result);
    }
}
