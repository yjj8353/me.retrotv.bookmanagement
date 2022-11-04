package me.retrotv.bookmanagement.domain.image;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.retrotv.bookmanagement.domain.common.CommonEntity;

/**
 * 이미지 엔티티
 * @version 1.0
 * @author yjj8353
 */
@Getter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "IMAGE")
public class Image extends CommonEntity {

    /*
     * 원본 파일명 [NOT NULL]
     */
    @Column(name = "IMAGE_NAME", nullable = false)
    private String name;

    /*
     * 실제 저장시 사용되는 파일명 [UNIQUE, NOT NULL]
     */
    @Column(name = "IMAGE_PROXY_NAME", nullable = false, unique = true)
    private String proxyName;

    /*
     * 확장자명
     */
    @Column(name = "IMAGE_FORMAT", length = 10)
    private String format;

    /*
     * 저장경로
     */
    @Column(name = "IMAGE_PATH", nullable = false)
    private String path;

    public ImageDTO toDTO() {
        return ImageDTO.builder()
                       .id(this.getId())
                       .name(this.name)
                       .proxyName(this.proxyName)
                       .format(this.format)
                       .path(this.path)
                       .build();
    }
}
