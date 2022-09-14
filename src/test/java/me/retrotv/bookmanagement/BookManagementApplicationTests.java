package me.retrotv.bookmanagement;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BookManagementApplicationTests {
	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Value("${file-save-path}")
    private String fileSavePath;

	@Test
	@DisplayName("fileSavePath 확인")
	void checkFileSavePath() {
		log.info("file 저장 경로: " + fileSavePath);
		Assertions.assertTrue(fileSavePath != null && !"".equals(fileSavePath));
	}
}
