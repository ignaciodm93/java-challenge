package com.ignaciodm.challenge.service;

import java.time.Duration;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;

import com.ignaciodm.challenge.models.SellingPoint;
import com.ignaciodm.challenge.repository.SellingPointRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class SellingPointService {

	private static final String LOG_SELLING_POINT_NOT_FOUNDED_ON_REDIS_GETTING_FROM_DB = "Selling point not founded on Redis. Getting it from db and storing it on redis...";

	private static final String LOG_FOUNDED_SELLING_POINT = "SellingPoint encontrado: {}";

	private static final String REDIS_KEY_SELLING_POINTS_LIST = "sellingPointsList";

	private static final String LOG_TRYING_TO_GET_SELLING_POINT_WITH_KEY = "Trying to get selling point with key: {}";

	private static final String REDIS_KEY_SELLING_POINT_KEY = "sellingPoint:";

	private static final String FOUNDED_DATA_ON_REDIS = "Founded data on Redis: ";

	private static final String TRYING_TO_GET_SELLING_POINTS_FROM_REDIS = "Trying to get selling points from Redis.";

	@Autowired
	private ReactiveRedisTemplate<String, SellingPoint> redisTemplate;

	@Autowired
	private SellingPointRepository sellingPointRepository;

	private static final Logger logger = LoggerFactory.getLogger(SellingPointService.class);

	public Flux<SellingPoint> findAll() {
		logger.info(TRYING_TO_GET_SELLING_POINTS_FROM_REDIS);

		redisTemplate.opsForList().range(REDIS_KEY_SELLING_POINTS_LIST, 0, -1).subscribe(isEmpty -> {
			logger.info("redisList is empty: {}", isEmpty);
		});

		return redisTemplate.opsForList().range(REDIS_KEY_SELLING_POINTS_LIST, 0, -1)
				.doOnNext(sellingPoint -> System.out.println(FOUNDED_DATA_ON_REDIS + sellingPoint))
				.switchIfEmpty(sellingPointRepository.findAll().collectList().flatMapMany(sellingPoints -> {
					logger.info(LOG_SELLING_POINT_NOT_FOUNDED_ON_REDIS_GETTING_FROM_DB);
					return redisTemplate.opsForList().rightPushAll(REDIS_KEY_SELLING_POINTS_LIST, sellingPoints)
							.then(redisTemplate.expire(REDIS_KEY_SELLING_POINTS_LIST, Duration.ofSeconds(10000)))
							.thenMany(Flux.fromIterable(sellingPoints));
				}));
	}

	public Mono<SellingPoint> findById(Integer id) {
		String key = REDIS_KEY_SELLING_POINT_KEY + id;
		logger.info(LOG_TRYING_TO_GET_SELLING_POINT_WITH_KEY, key);

		Mono<SellingPoint> redisValue = redisTemplate.opsForValue().get(key);

		Mono<SellingPoint> mongoValue = sellingPointRepository.findById(id).flatMap(sellingPoint -> {
			logger.info(LOG_SELLING_POINT_NOT_FOUNDED_ON_REDIS_GETTING_FROM_DB);
			return redisTemplate.opsForValue().set(key, sellingPoint, Duration.ofSeconds(10)).thenReturn(sellingPoint);
		});

		return redisValue.switchIfEmpty(mongoValue)
				.doOnNext(sellingPoint -> logger.info(LOG_FOUNDED_SELLING_POINT, sellingPoint));
	}

	public Mono<SellingPoint> save(SellingPoint sellingPoint) {
		return sellingPointRepository.save(sellingPoint)
				.flatMap(savedSellingPoint -> redisTemplate.opsForValue()
						.set(REDIS_KEY_SELLING_POINT_KEY + savedSellingPoint.getId(), savedSellingPoint)
						.thenReturn(savedSellingPoint));
	}

	public Mono<SellingPoint> update(Integer id, SellingPoint sellingPoint) {
		return sellingPointRepository.findById(id).flatMap(existingSellingPoint -> {
			sellingPoint.setId(id);
			return sellingPointRepository.save(sellingPoint)
					.flatMap(updatedSellingPoint -> redisTemplate.opsForValue()
							.set(REDIS_KEY_SELLING_POINT_KEY + updatedSellingPoint.getId(), updatedSellingPoint)
							.thenReturn(updatedSellingPoint));
		}).switchIfEmpty(Mono.empty());
	}

	public Mono<Long> deleteById(Integer id) {
		return sellingPointRepository.findById(id)
				.flatMap(existingSellingPoint -> sellingPointRepository.deleteById(id)
						.then(redisTemplate.delete(REDIS_KEY_SELLING_POINT_KEY + id)).thenReturn(1L))
				.switchIfEmpty(Mono.just(0L));
	}

	public Mono<Void> saveInitialData() {
		Map<Integer, String> initialData = Map.of(1, "CABA", 2, "GBA_1", 3, "GBA_2", 4, "Santa Fe", 5, "Córdoba", 6,
				"Misiones", 7, "Salta", 8, "Chubut", 9, "Santa Cruz", 10, "Catamarca");

		return Flux.fromIterable(initialData.entrySet())
				.flatMap(entry -> save(new SellingPoint(entry.getKey(), entry.getValue()))).then();
	}

	public Mono<Void> clearCache() {
		return redisTemplate.getConnectionFactory().getReactiveConnection().serverCommands().flushAll().then();
	}
}