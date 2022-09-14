package me.retrotv.bookmanagement.domain.image;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
public class Image {
    
    /*
     * 식별을 위한 고유 ID 값 [PRIMARY KEY]
     */
    @Id
    @Column(name = "IMAGE_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

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

    /*
     * 등록 시간
     */
    @Column(name = "IMAGE_REGIST_DATE", nullable = false)
    @CreationTimestamp
    private LocalDateTime registDate;

    /*
     * 수정 시간
     */
    @Column(name = "IMAGE_UPDATE_DATE", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updateDate;

    public ImageDTO toDTO() {
        return ImageDTO.builder()
                       .id(this.id)
                       .name(this.name)
                       .proxyName(this.proxyName)
                       .format(this.format)
                       .path(this.path)
                       .build();
    }
}
