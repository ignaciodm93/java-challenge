package com.ignaciodm.challenge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//@EnableReactiveMongoRepositories(basePackages = "com.ignaciodm.challenge.repository")
public class JavaChallengeApplication {

	public static void main(String[] args) {
		SpringApplication.run(JavaChallengeApplication.class, args);
	}
}