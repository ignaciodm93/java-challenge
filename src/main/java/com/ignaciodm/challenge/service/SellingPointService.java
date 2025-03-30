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

	@Autowired
	private ReactiveRedisTemplate<String, SellingPoint> redisTemplate;

	@Autowired
	private SellingPointRepository sellingPointRepository;

	private static final Logger logger = LoggerFactory.getLogger(SellingPointService.class);

	// pasar los textos a constantes luego
	public Flux<SellingPoint> findAll() {
		logger.info("Intentando obtener todos los SellingPoints de Redis.");

		return redisTemplate.opsForList().range("sellingPointsList", 0, -1)
				.switchIfEmpty(sellingPointRepository.findAll().collectList().flatMapMany(sellingPoints -> {
					logger.info("SellingPoints no encontrados en Redis. Obteniendo de MongoDB y guardando en Redis.");
					return redisTemplate.opsForList().rightPushAll("sellingPointsList", sellingPoints)
							.then(redisTemplate.expire("sellingPointsList", Duration.ofSeconds(10)))
							.thenMany(Flux.fromIterable(sellingPoints));
				}));
	}

	public Mono<SellingPoint> findById(Integer id) {
		String key = "sellingPoint:" + id;
		logger.info("Intentando obtener SellingPoint de Redis con clave: {}", key);

		Mono<SellingPoint> redisValue = redisTemplate.opsForValue().get(key);

		Mono<SellingPoint> mongoValue = sellingPointRepository.findById(id).flatMap(sellingPoint -> {
			logger.info("No se encontró en redis. Obteniendo de MongoDB y guardando en Redis.");
			return redisTemplate.opsForValue().set(key, sellingPoint, Duration.ofSeconds(10)).thenReturn(sellingPoint);
		});

		return redisValue.switchIfEmpty(mongoValue)
				.doOnNext(sellingPoint -> logger.info("SellingPoint encontrado: {}", sellingPoint));
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