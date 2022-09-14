package me.retrotv.bookmanagement.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration.AccessLevel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link ModelMapper}의 설정을 위한 객체
 * @version 1.0
 * @author yjj8353
 */
@Configuration
public class ModelMapperConfig {

    /**
     * Setter 생성을 권장하지 않는 Entity 및 DTO 객체가 Setter가 존재하지 않더라도 매핑되게 하는 빈(Bean) 객체.
     * @return 설정이 변경된 {@link ModelMapper} 객체
     */
    @Bean
    ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()

                   // 기본적으로 Setter 생성을 권장하지 않는 Entity 및 DTO가 public setter가 존재하지 않더라도 매핑되도록 해줌.
                   .setFieldAccessLevel(AccessLevel.PRIVATE)
                   .setFieldMatchingEnabled(true);

        return modelMapper;
    }
}