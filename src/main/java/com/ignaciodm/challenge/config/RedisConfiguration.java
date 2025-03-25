package com.ignaciodm.challenge.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfiguration {

	@Value("${redis.host}")
	private String redisHost;

	@Value("${redis.localhost}")
	private String redisLocalhost;

	@Value("${redis.port}")
	private int redisPort;

	@Value("${redis.address}")
	private String redisAddress;

	@Bean
	public RedissonClient redissonClient() {
		Config config = new Config();
		config.setCodec(new JsonJacksonCodec());
		// para correr la app en docker config.useSingleServer().setAddress(redisAddress
		// + redisHost + redisPort);

		// para correr la app local
		config.useSingleServer().setAddress(redisAddress + redisLocalhost + redisPort);

		return Redisson.create(config);
	}

	@Bean
	public RedissonReactiveClient createClient(RedissonClient redissonClient) {
		return redissonClient.reactive();
	}
}
