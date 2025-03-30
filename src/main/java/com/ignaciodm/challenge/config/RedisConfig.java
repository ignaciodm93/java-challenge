package com.ignaciodm.challenge.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.ignaciodm.challenge.models.SellingPoint;

@Configuration
public class RedisConfig {

	@Bean
	public ReactiveRedisTemplate<String, SellingPoint> reactiveRedisTemplate(ReactiveRedisConnectionFactory factory) {
		StringRedisSerializer keySerializer = new StringRedisSerializer();
		Jackson2JsonRedisSerializer<SellingPoint> valueSerializer = new Jackson2JsonRedisSerializer<>(
				SellingPoint.class);

		RedisSerializationContext<String, SellingPoint> serializationContext = RedisSerializationContext
				.<String, SellingPoint>newSerializationContext(keySerializer).value(valueSerializer).build();

		return new ReactiveRedisTemplate<>(factory, serializationContext);
	}
}