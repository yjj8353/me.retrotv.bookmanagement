package me.retrotv.bookmanagement.unit.domain.image;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import me.retrotv.bookmanagement.domain.image.Image;
import me.retrotv.bookmanagement.domain.image.ImageRepository;
import me.retrotv.bookmanagement.domain.image.ImageService;
import me.retrotv.bookmanagement.response.BasicError;
import me.retrotv.bookmanagement.response.BasicResult;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Image Service 계층 단위 테스트")
class ImageServiceTest {
    private ImageRepository imageRepository = Mockito.mock(ImageRepository.class);
    private ImageService imageService;

    private final static String WINDOWS_DRIVE = "C";
    private final static String FILE_SAVE_PATH = "/home/retrotv/thumnail";

    private MockMultipartFile getMockMultipartFile() throws IOException {
        String path = "src/test/java/me/retrotv/bookmanagement/integration/domain/image/XL.jpeg";
        File file = new File(path);
        FileInputStream fis = new FileInputStream(file);
        MockMultipartFile mockFile = new MockMultipartFile("files", "XL.jpeg", "image/jpeg", fis);

        return mockFile;
    }

    @BeforeEach
    void setup() {

        // unit test에서 ModelMapper 설정이 변경된 Bean이 @Autowired로 주입되지 않으므로 직접 설정해야 함. 
        ModelMapper modelMapper = new ModelMapper();
                    modelMapper.getConfiguration()
                               .setFieldAccessLevel(AccessLevel.PRIVATE)
                               .setFieldMatchingEnabled(true);
        imageService = new ImageService(imageRepository, modelMapper);

        ReflectionTestUtils.setField(imageService, "fileSavePath", FILE_SAVE_PATH);
        ReflectionTestUtils.setField(imageService, "windowsDrive", WINDOWS_DRIVE);
    }

    @Nested
    @DisplayName("표지 저장")
    class saveImage {

        @Test
        @DisplayName("성공")
        void success() throws Exception {

            /* give */
            Image returnImage = Image.builder()
                                     .id(1L)
                                     .build();
            MultipartFile mockFile = getMockMultipartFile();

            Map<String, Object> data = new HashMap<>();
            data.put("imageId", String.valueOf(returnImage.getId()));

            // imageRepository.saveAndFlush(image); 가 실행되고 나면, ID 값이 1인 Image 객체가 반환된다.
            Mockito.when(imageRepository.saveAndFlush(any(Image.class))).thenReturn(returnImage);
            
            /* when */
            BasicResult result = imageService.saveImage(mockFile);

            /* then */
            assertEquals("표지 저장에 성공했습니다.", result.getMessage());
            assertEquals(String.valueOf(data), String.valueOf(result.getData()));
            assertEquals(1, result.getCount());

            // imageRepository의 saveAndFlush 함수가 '한번' 실행되었는지 확인한다.
            Mockito.verify(imageRepository, times(1)).saveAndFlush(any(Image.class));
        }

        @Test
        @DisplayName("실패 - 사유: 저장 도중 IOException 발생")
        void imageUploadFailIOException() {
            BasicError result = null;
            
            try {
                throw new IOException();
            } catch(IOException e) {

                /* when */
                result = new BasicError("표지 저장에 실패했습니다.\n잠시뒤에 다시 시도해 주세요.");
            }

            /* then */
            assertEquals("표지 저장에 실패했습니다.\n잠시뒤에 다시 시도해 주세요.", result.getMessage());
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatus());
        }
    }

    @Nested
    @DisplayName("표지 다운로드")
    class downloadImage {

        /* give */
        Image returnImage = Image.builder()
                                 .id(1L)
                                 .name("XL")
                                 .proxyName("XL.jpeg")
                                 .format("jpeg")
                                 .path("src/test/java/me/retrotv/bookmanagement/unit/domain/image/")
                                 .build();

        @Test
        @DisplayName("성공 - 기본")
        void success() throws Exception {

            /* give */
            Mockito.when(imageRepository.findById(1L)).thenReturn(Optional.of(returnImage));

            /* when */
            BasicResult result = imageService.downloadImage(1L);
            
            /* then */
            assertEquals(null, result.getMessage());
            assertNotNull(result.getData());
            assertEquals(1, result.getCount());

            Mockito.verify(imageRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("성공 - ID 값이 null이면 noimage.jpeg 데이터를 반환한다")
        void successIdIsNull() throws Exception {

            /* give */
            Mockito.when(imageRepository.findById(null)).thenReturn(Optional.ofNullable(null));

            /* when */
            BasicResult result = imageService.downloadImage(null);

            /* then */
            assertNull(result.getMessage());
            assertNotNull(result.getData());
            assertEquals(1, result.getCount());
            assertEquals(HttpStatus.OK, result.getStatus());

            Mockito.verify(imageRepository, times(0)).findById(null);
        }

        @Test
        @DisplayName("실패 - 사유: 없는 ID 값으로 조회를 시도")
        void failNoSuchElement() throws Exception {
            try {

                /* when */
                imageService.downloadImage(1L);
            } catch(NoSuchElementException e) {

                /* then */
                assertEquals("해당 ID 값으로 조회된 이미지가 없습니다.", e.getMessage());
            }
        }

        @Test
        @DisplayName("실패 - 사유: 다운로드 도중 IOException 발생")
        void imageDownloadFailIOException() {
            BasicError result = null;

            try {
                throw new IOException();
            } catch(IOException e) {

                /* when */
                result = new BasicError("이미지 다운로드에 실패했습니다.\n잠시뒤에 다시 시도해 주세요.");
            }

            /* then */
            assertEquals("이미지 다운로드에 실패했습니다.\n잠시뒤에 다시 시도해 주세요.", result.getMessage());
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatus());
        }
    }
}
