package me.retrotv.bookmanagement;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.extern.slf4j.Slf4j;

// @SpringBootApplication(exclude = SecurityAutoConfiguration.class) // 임시로 Spring Security를 끄기 위해 사용
@Slf4j
@SpringBootApplication
public class BookManagementApplication {

	@Value("${spring.profiles.active}")
	private String springProfile;

	public static void main(String[] args) {
		SpringApplication.run(BookManagementApplication.class, args);
	}

	@PostConstruct
	private void start() {
		log.debug("mode: {}", springProfile);
	}
}
