package me.retrotv.bookmanagement.util;

import java.io.File;
import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

/**
 * 파일처리 관련 기능을 집약한 객체
 * @version 1.0
 * @author yjj8353
 */
@Slf4j
public class FileUtil {

    // 인스턴스화 방지
    private FileUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * 파일/저장경로/파일명 데이터를 받아 저장소에 파일을 저장하는 함수.
     * @param file 저장할 파일
     * @param fileSavePath 저장 경로
     * @param fileName 파일명 (확장자명 포함)
     * @throws IOException
     */
    public static void saveFile(MultipartFile file, String fileSavePath, String fileName) throws IOException {
        File saveFile = new File(fileSavePath, fileName);

        try {
            file.transferTo(saveFile);
        } catch(IOException e) {
            log.error(e.getMessage());
            throw new IOException("파일을 저장하는데 실패 했습니다.");
        }
    }
}
