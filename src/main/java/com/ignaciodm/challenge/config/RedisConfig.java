package com.ignaciodm.challenge.config;

import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
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

	@Bean
	public ReactiveRedisTemplate<String, Map<Integer, Integer>> reactiveRedisTemplateSellingCost(
			ReactiveRedisConnectionFactory factory) {
		StringRedisSerializer keySerializer = new StringRedisSerializer();

		JavaType valueType = TypeFactory.defaultInstance().constructMapType(Map.class, Integer.class, Integer.class);
		Jackson2JsonRedisSerializer<Map<Integer, Integer>> valueSerializer = new Jackson2JsonRedisSerializer<>(
				valueType);

		RedisSerializationContext<String, Map<Integer, Integer>> serializationContext = RedisSerializationContext
				.<String, Map<Integer, Integer>>newSerializationContext(keySerializer).value(valueSerializer).build();

		return new ReactiveRedisTemplate<>(factory, serializationContext);
	}

	@Bean
	public ReactiveRedisTemplate<String, Map<String, Object>> reactiveRedisTemplateCheapestPath(
			ReactiveRedisConnectionFactory factory) {
		StringRedisSerializer keySerializer = new StringRedisSerializer();

		JavaType valueType = TypeFactory.defaultInstance().constructMapType(Map.class, String.class, Object.class);
		Jackson2JsonRedisSerializer<Map<String, Object>> valueSerializer = new Jackson2JsonRedisSerializer<>(valueType);

		RedisSerializationContext<String, Map<String, Object>> serializationContext = RedisSerializationContext
				.<String, Map<String, Object>>newSerializationContext(keySerializer).value(valueSerializer).build();

		return new ReactiveRedisTemplate<>(factory, serializationContext);
	}

}