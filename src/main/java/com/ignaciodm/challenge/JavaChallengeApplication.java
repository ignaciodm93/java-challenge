package com.ignaciodm.challenge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@SpringBootApplication
@EnableReactiveMongoRepositories(basePackages = "com.ignaciodm.challenge.repository")
@EnableAutoConfiguration(exclude = RedisRepositoriesAutoConfiguration.class)
public class JavaChallengeApplication {

	public static void main(String[] args) {
		SpringApplication.run(JavaChallengeApplication.class, args);
	}
}