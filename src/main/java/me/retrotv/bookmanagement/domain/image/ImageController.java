package me.retrotv.bookmanagement.domain.image;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import me.retrotv.bookmanagement.response.BasicResult;

/**
 * 표지({@link Image}) 컨트롤러 계층.
 * @version 1.0
 * @author yjj8353
 */
@ResponseBody
@RestController
@RequiredArgsConstructor
@RequestMapping("api/image")
public class ImageController {
    private final ImageService imageService;

    /**
     * 표지 다운로드를 요청을 처리하는 함수.
     * @param imageId 다운로드가 필요한 표지의 고유 ID 값
     * @return 표지 다운로드에 대한 결과가 담긴 ResponseEntity 객체
     */
    @GetMapping("/download")
    public ResponseEntity<BasicResult> downloadImage(@RequestParam(required = false) Long imageId) {
        BasicResult result = imageService.downloadImage(imageId);
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    /**
     * 표지 업로드를 요청을 처리하는 함수.
     * @param multipartFile 프론드엔드에서 전송한 multipart/form-data 유형의 데이터가 담긴 객체
     * @return 표지 업로드에 대한 결과가 담긴 ResponseEntity 객체
     */
    @PostMapping("/save")
    public ResponseEntity<BasicResult> saveImage(@RequestPart(value = "files", required = true) MultipartFile multipartFile) {
        BasicResult result = imageService.saveImage(multipartFile);
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    /**
     * 표지 URL을 이용해 업로드를 요청을 처리하는 함수.
     * @param multipartFile 프론드엔드에서 전송한 multipart/form-data 유형의 데이터가 담긴 객체
     * @return 표지 업로드에 대한 결과가 담긴 ResponseEntity 객체
     */
    @PostMapping("/save-url")
    public ResponseEntity<BasicResult> saveImageByUrl(@RequestParam String url) {
        BasicResult result = imageService.saveImageByUrl(url);
        return ResponseEntity.status(result.getStatus()).body(result);
    }
}
