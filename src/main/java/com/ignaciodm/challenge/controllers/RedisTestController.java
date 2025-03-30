package com.ignaciodm.challenge.controllers;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ignaciodm.challenge.models.SellingPoint;

import reactor.core.publisher.Mono;

@RestController
public class RedisTestController {

	@Autowired
	private ReactiveRedisTemplate<String, SellingPoint> redisTemplate;

	@PostMapping("/test-redis-ttl")
	public Mono<Void> testRedisTtl() {
		SellingPoint sellingPoint = new SellingPoint(3, "Test");
		return redisTemplate.opsForValue().set("sellingPoint:33", sellingPoint, Duration.ofSeconds(10)).then();
	}
}