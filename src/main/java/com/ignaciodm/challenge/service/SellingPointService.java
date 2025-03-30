package com.ignaciodm.challenge.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;

import com.ignaciodm.challenge.models.SellingPoint;
import com.ignaciodm.challenge.repository.SellingPointRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class SellingPointService {

	@Autowired
	private ReactiveRedisTemplate<String, SellingPoint> redisTemplate;

	@Autowired
	private SellingPointRepository sellingPointRepository;

	public Flux<SellingPoint> findAll() {
		return redisTemplate.keys("sellingPoint:*").collectList().flatMapMany(
				keys -> redisTemplate.opsForValue().multiGet(keys).flatMapIterable(sellingPoints -> sellingPoints))
				.switchIfEmpty(sellingPointRepository.findAll().flatMap(sellingPoint -> redisTemplate.opsForValue()
						.set("sellingPoint:" + sellingPoint.getId(), sellingPoint).thenReturn(sellingPoint)));
	}

	public Mono<SellingPoint> findById(Integer id) {
		return redisTemplate.opsForValue().get("sellingPoint:" + id)
				.switchIfEmpty(sellingPointRepository.findById(id).flatMap(sellingPoint -> redisTemplate.opsForValue()
						.set("sellingPoint:" + sellingPoint.getId(), sellingPoint).thenReturn(sellingPoint)));
	}

	public Mono<SellingPoint> save(SellingPoint sellingPoint) {
		return sellingPointRepository.save(sellingPoint).flatMap(savedSellingPoint -> redisTemplate.opsForValue()
				.set("sellingPoint:" + savedSellingPoint.getId(), savedSellingPoint).thenReturn(savedSellingPoint));
	}

	public Mono<SellingPoint> update(Integer id, SellingPoint sellingPoint) {
		return sellingPointRepository.findById(id).flatMap(existingSellingPoint -> {
			sellingPoint.setId(id);
			return sellingPointRepository.save(sellingPoint)
					.flatMap(updatedSellingPoint -> redisTemplate.opsForValue()
							.set("sellingPoint:" + updatedSellingPoint.getId(), updatedSellingPoint)
							.thenReturn(updatedSellingPoint));
		});
	}

	public Mono<Long> deleteById(Integer id) {
		return sellingPointRepository.deleteById(id).then(redisTemplate.delete("sellingPoint:" + id));
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