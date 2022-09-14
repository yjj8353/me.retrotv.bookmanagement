package me.retrotv.bookmanagement.domain.image;

import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 표지 데이터 전송 객체
 * @version 1.0
 * @author yjj8353
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageDTO {

    /*
     * 식별을 위한 고유 ID 값
     */
    private Long id;

    /*
     * 원본 파일명
     */
    @NotBlank(message = "파일명은 필수 입니다.")
    private String name;

    /*
     * 프록시 파일명 (실제 저장시 사용되는 파일명)
     */
    @NotBlank(message = "프록시 파일명은 필수 입니다.")
    private String proxyName;

    /*
     * 확장자명
     */
    private String format;

    /*
     * 저장경로
     */
    @NotBlank(message = "저장경로는 필수 입니다.")
    private String path;
}