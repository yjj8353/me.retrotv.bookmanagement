package me.retrotv.bookmanagement.domain.image;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.validation.ValidationException;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.retrotv.bookmanagement.response.BasicError;
import me.retrotv.bookmanagement.response.BasicResult;
import me.retrotv.bookmanagement.util.FileUtil;
import me.retrotv.bookmanagement.util.SystemUtil;

/**
 * 표지({@link Image}) 서비스 계층.
 * @version 1.0
 * @author yjj8353
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {
    private final ImageRepository imageRepository;
    private final ModelMapper modelMapper;

    private static final String JPG = "data:image/jpeg;base64,";
    private static final String PNG = "data:image/png;base64,";
    private static final String GIF = "data:image/gif;base64,";
    private static final String WEBP = "data:image/webp;base64,";

    private static final String IMAGE_DATA = "imageData";
    
    @Value("${file-save-path}")
    private String fileSavePath;

    @Value("${windows-drive}")
    private String windowsDrive;

    /**
     * 표지 다운로드 요청을 받아 처리하고 결과를 돌려주는 함수.
     * @param id 다운로드를 요청할 표지의 고유한 ID 값
     * @return 다운로드를 요청한 이미지의 Base64로 인코딩 된 문자열이 담긴 {@link BasicResult} 객체
     */
    @Transactional
    public BasicResult downloadImage(Long id) {
        Image image = null;
        ImageDTO imageDTO = null;
        
        if(id == null) {
            return this.getDefaultNoImage();
        }

        image = imageRepository.findById(id).orElseThrow(() -> new NoSuchElementException("해당 ID 값으로 조회된 이미지가 없습니다."));
        imageDTO = modelMapper.map(image, ImageDTO.class);

        Path filePath = Paths.get(imageDTO.getPath());
             filePath = filePath.resolve(imageDTO.getProxyName());

        File file = filePath.toFile();
        byte[] data = new byte[(int) file.length()];

        // try 뒤의 괄호는, close() 메소드가 필요한 stream 계열의 클래스를 자동으로 닫아주는 역할을 한다.
        try(FileInputStream stream = new FileInputStream(file)) {
            stream.read(data, 0, data.length);
        } catch(IOException e) {
            log.error(e.getMessage());
            return new BasicError("요청을 처리할 수 없습니다.\n증상이 계속될 경우, 관리자에게 연락하시기 바랍니다.");
        }

        String prefix;

        switch(imageDTO.getFormat()) {
            case "jpeg":
            case "jpg":
                prefix = JPG;
                break;

            case "png":
                prefix = PNG;
                break;

            case "gif":
                prefix = GIF;
                break;

            case "webp":
                prefix = WEBP;
                break;
                
            default:
                prefix = null;
                break;
        }

        Map<String, Object> map = new HashMap<>();
        map.put(IMAGE_DATA, prefix + Base64.getEncoder().encodeToString(data));
        
        return new BasicResult(null, map);
    }

    /**
     * 표지 업로드 요청을 받아 처리하고 결과를 돌려주는 함수.
     * @param multipartFile 업로드 할 표지의 데이터가 담긴 {@link MultipartFile} 객체
     * @return 업로드 된 표지의 식별을 위해 부여된 고유의 ID 값이 data로 담긴 {@link BasicResult} 객체
     */
    @Transactional
    public BasicResult saveImage(MultipartFile multipartFile) {
        Map<String, Object> data = new HashMap<>();

        /*
         * fileName     : 사용자에게 보여지는 파일명
         * ext          : 확장자명
         * proxyFileName: 실제 서버에 저장되는 파일명
         */
        String fileName = multipartFile.getOriginalFilename();
        String ext = null;

        // 파일명을 '.' 으로 split 해서 마지막 값(확장자명)을 가져온다.
        if(fileName != null && !"".equals(fileName)) {
            ext = fileName.lastIndexOf(".") != 0 ? fileName.substring(fileName.lastIndexOf(".") + 1) : null;
        }

        if(fileName != null && fileName.contains("\\|/|:|[*]|[?]|\"|<|>|[|]")) {
            throw new ValidationException("파일 명에 사용할 수 없는 문자가 포함되어 있습니다.");
        }

        String[] imageExtensions = { "jpg", "png", "gif", "jpeg", "webp" };
        if(ext == null || !Arrays.asList(imageExtensions).contains(ext)) {
            throw new ValidationException("이미지 파일이 아닌 것 같습니다.\n허용되는 확장자: .jpg, .png, .gif, .jpeg, .webp");
        }

        String proxyFileName = String.valueOf(System.currentTimeMillis()) + "." + ext;
        String path = SystemUtil.isWindows() ? windowsDrive + ":\\" + fileSavePath.replace("/", "\\") : fileSavePath;
                                   
        log.debug("저장경로: {}", path);
        log.debug("실제 파일명: {}", fileName);
        log.debug("프록시 파일명: {}", proxyFileName);
        log.debug("확장자명: {}", ext);
        log.debug("콘텐츠 타입: {}", multipartFile.getContentType());

        try {
            FileUtil.saveFile(multipartFile, path, proxyFileName);
        } catch(IOException e) {
            return new BasicError("표지 저장에 실패했습니다\n잠시뒤에 다시 시도해 주세요");
        }

        Image image = Image.builder()
                           .name(fileName)
                           .proxyName(proxyFileName)
                           .format(ext)
                           .path(path)
                           .build();

        // image의 id 값을 return 해야하므로, 명시적으로 flush 처리함.
        Image savedImage = imageRepository.saveAndFlush(image);

        log.debug("이미지 ID: {}", savedImage.getId());

        /*
         * data 변수(Map<String, Object>)의 imageId Key에 대응해 원래 Value 값으로 image.getId()를 담았는데, 이렇게 할 경우 unit test에서 imageId 값이 항상 null인 문제가 있어서
         * imageRepository.saveAndFlush(image); 처리를 한 후, savedImage 변수에 저장된 Image 엔티티 객체를 직접 담아 savedImage.getId()로 아이디 값을 가져온다.
         */
        data.put("imageId", savedImage.getId());
        
        return new BasicResult("표지 저장에 성공했습니다.", data);
    }

    public BasicResult saveImageByUrl(String url) {
        log.debug("url: " + url);

        Map<String, Object> data = new HashMap<>();
        String imageUrl = url;
        
        log.debug("image url" + imageUrl);

        String imageName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        String ext = null;

        if(imageName != null && !"".equals(imageName)) {
            ext = imageName.lastIndexOf(".") != 0 ? imageName.substring(imageName.lastIndexOf(".") + 1) : null;
        }

        if(imageName != null && imageName.contains("\\|/|:|[*]|[?]|\"|<|>|[|]")) {
            throw new ValidationException("파일 명에 사용할 수 없는 문자가 포함되어 있습니다.");
        }

        String[] imageExtensions = { "jpg", "png", "gif", "jpeg", "webp" };
        if(ext == null || !Arrays.asList(imageExtensions).contains(ext)) {
            throw new ValidationException("이미지 파일이 아닌 것 같습니다.\n허용되는 확장자: .jpg, .png, .gif, .jpeg, .webp");
        }

        String proxyFileName = String.valueOf(System.currentTimeMillis()) + "." + ext;
        String path = SystemUtil.isWindows() ? windowsDrive + ":\\" + fileSavePath.replace("/", "\\") : fileSavePath;

        log.debug("저장경로: {}", path);
        log.debug("실제 파일명: {}", imageName);
        log.debug("프록시 파일명: {}", proxyFileName);
        log.debug("확장자명: {}", ext);
        
        try(InputStream is = new URL(imageUrl).openStream()) {
            byte[] buffer = new byte[5120];
            int n = -1;

            try(OutputStream os = new FileOutputStream(path + "/" + proxyFileName)) {
                while((n = is.read(buffer)) != -1) {
                    os.write(buffer, 0, n);
                }
            }
        } catch(IOException exception) {
            return new BasicResult("이미지에 문제가 있는 것 같습니다.\n지속적으로 문제가 발생할 경우 수동으로 이미지를 업로드 해주세요.", HttpStatus.BAD_REQUEST);
        }

        Image image = Image.builder()
                            .name(imageName)
                            .proxyName(proxyFileName)
                            .format(ext)
                            .path(path)
                            .build();

        Image savedImage = imageRepository.saveAndFlush(image);
        data.put("imageId", savedImage.getId());

        return new BasicResult("표지 저장에 성공했습니다.", data);
    }

    /**
     * 다운로드 할 수 있는 이미지가 없을 시, 기본으로 설정된 noimage.jpg 이미지가 표시될 수 있도록 noimage.jpg 파일의 정보가 담긴 {@link BasicError} 객체를 반환하는 함수.
     * @param exception downloadImage 함수에서 발생한 {@link IllegalArgumentException} 혹은 {@link NoSuchElementException} 예외 객체
     * @return noimage.jpg 파일의 정보 혹은 IOException 정보가 담긴 {@link BasicError} 객체
     */
    private BasicResult getDefaultNoImage() {
        File file = new File("src/main/resources/static/images/noimage.jpg");
        byte[] data = new byte[(int) file.length()];

        try(FileInputStream stream = new FileInputStream(file)) {
            stream.read(data, 0, data.length);
            Map<String, Object> map = new HashMap<>();
            map.put(IMAGE_DATA, JPG + Base64.getEncoder().encodeToString(data));
            
            return new BasicResult(null, map);
        } catch(IOException e) {
            log.error(e.getMessage());

            return new BasicError("요청을 처리할 수 없습니다.\n증상이 계속될 경우, 관리자에게 연락하시기 바랍니다.");
        }
    }
}
