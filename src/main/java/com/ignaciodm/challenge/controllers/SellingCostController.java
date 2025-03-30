package com.ignaciodm.challenge.controllers;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ignaciodm.challenge.models.SellingCost;
import com.ignaciodm.challenge.repository.SellingCostRepository;
import com.ignaciodm.challenge.service.PathsService;
import com.ignaciodm93.interfaces.SellingCostApi;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/selling-costs")
public class SellingCostController implements SellingCostApi {

	@Autowired
	private PathsService integratedPathService;

	@Autowired
	private SellingCostRepository sellingCostDocumentRepository;

	@Autowired
	private ReactiveRedisTemplate<String, Map<Integer, Integer>> redisTemplateSellingCost;

	@Autowired
	private ReactiveRedisTemplate<String, Map<String, Object>> redisTemplateCheapestPath;

	@Autowired
	private ReactiveRedisConnectionFactory redisConnectionFactory;

	private static final Logger log = LoggerFactory.getLogger(SellingCostApi.class);

	@GetMapping("/get-all-selling-points-paths")
	private Mono<Map<Integer, Map<Integer, Integer>>> getSellingPointsPaths() {
		return sellingCostDocumentRepository.findAll().collectList().map(sellingCosts -> {
			Map<Integer, Map<Integer, Integer>> sellingPointsPaths = new HashMap<>();
			sellingCosts.forEach(sellingCost -> {
				Map<Integer, Integer> startPointConnections = sellingPointsPaths
						.computeIfAbsent(sellingCost.getStartingPoint(), path -> new HashMap<>());
				startPointConnections.put(sellingCost.getEndingPoint(), sellingCost.getCost());
				Map<Integer, Integer> endPointConnections = sellingPointsPaths
						.computeIfAbsent(sellingCost.getEndingPoint(), path -> new HashMap<>());
				endPointConnections.put(sellingCost.getStartingPoint(), sellingCost.getCost());
			});
			return sellingPointsPaths;
		});
	}

	// arreglar---------------------------------------------------------------------------------
	@Override
	@GetMapping("/direct-connection")
	public Mono<ResponseEntity<Map<Integer, Integer>>> getDirectConnection(
			@RequestParam Integer sellingPointToDiscoverPathsId) {
		String key = "directConnections:" + sellingPointToDiscoverPathsId;
		Mono<Map<Integer, Integer>> redisData = redisTemplateSellingCost.opsForValue().get(key);
		Mono<ResponseEntity<Map<Integer, Integer>>> redisMono = redisData.map(ResponseEntity::ok)
				.defaultIfEmpty(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of()));

		Mono<Map<Integer, Integer>> mongoData = getSellingPointsPaths().flatMap(sellingPointsPaths -> {

			log.debug("Fetching direct connections for sellingPoint {}", sellingPointToDiscoverPathsId);
			return integratedPathService.getDirectConnection(sellingPointsPaths, sellingPointToDiscoverPathsId)
					.doOnNext(directConnections -> {
						log.debug("Direct connections found: {}", directConnections);
					}).flatMap(directConnections -> {
						return redisTemplateSellingCost.opsForValue()
								.set(key, directConnections, Duration.ofSeconds(60)).thenReturn(directConnections);
					});
		});

		return redisMono.switchIfEmpty(mongoData.map(ResponseEntity::ok));
	}
	// fin_arreglar---------------------------------------------------------------------------------

	@Override
	@GetMapping("/full-cheapest-path")
	public Mono<ResponseEntity<Map<String, Object>>> getFullCheapestPath(@RequestParam Integer startingPoint,
			@RequestParam Integer endingPoint) {
		String key = "cheapestPath:" + startingPoint + "-" + endingPoint;

		// intentamos tomarlo de redis
		Mono<ResponseEntity<Map<String, Object>>> redisMono = redisTemplateCheapestPath.opsForValue().get(key)
				.map(ResponseEntity::ok);

		// si no lo encontramos en Redis, lo calculamos y lo almacenamos
		Mono<ResponseEntity<Map<String, Object>>> mongoMono = getSellingPointsPaths().flatMap(sellingPointsPaths -> {
			log.debug("Fetching full cheapest path from {} to {}", startingPoint, endingPoint);
			return integratedPathService.getFullCheapestPath(sellingPointsPaths, startingPoint, endingPoint)
					.doOnNext(pathAndFare -> {
						log.debug("Cheapest path found: {}", pathAndFare);
					}).flatMap(pathAndFare -> {
						// a redis
						return redisTemplateCheapestPath.opsForValue().set(key, pathAndFare, Duration.ofSeconds(60))
								.thenReturn(ResponseEntity.ok(pathAndFare));
					});
		});

		// evaluamos si esta en la primera consulta de redis, si no hacemos toda la
		// movida de mongo, y redis desde 0
		return redisMono.switchIfEmpty(mongoMono).defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@PostMapping("/initialize-selling-costs")
	public Mono<ResponseEntity<String>> saveInitialData() {
		List<SellingCost> initialSellingCosts = getInitialSellingCosts();

		return Flux.fromIterable(initialSellingCosts)
				.flatMap(sellingCost -> sellingCostDocumentRepository.save(sellingCost)).then(Mono.defer(() -> {

					Map<String, Object> sellingCostMap = new HashMap<>();
					initialSellingCosts.forEach(sellingCost -> {
						sellingCostMap.put(sellingCost.getIdentifierKeyRoute(), sellingCost.getCost());
					});

					String redisKey = "sellingCostsData";
					return redisTemplateCheapestPath.opsForValue().set(redisKey, sellingCostMap, Duration.ofHours(1))
							.thenReturn(ResponseEntity.status(HttpStatus.CREATED)
									.body("Initial selling costs saved successfully and cached in Redis."));
				}));
	}

	private List<SellingCost> getInitialSellingCosts() {
		return List.of(new SellingCost(1, 2, 2), new SellingCost(1, 3, 3), new SellingCost(2, 3, 5),
				new SellingCost(2, 4, 10), new SellingCost(1, 4, 11), new SellingCost(4, 5, 5),
				new SellingCost(2, 5, 14), new SellingCost(6, 7, 32), new SellingCost(8, 9, 11),
				new SellingCost(10, 7, 5), new SellingCost(3, 8, 10), new SellingCost(5, 8, 30),
				new SellingCost(10, 5, 5), new SellingCost(4, 6, 6));
	};

	// borrar luego, para pruebas locales
	@DeleteMapping("/flushall")
	public Mono<String> flushAll() {
		return redisConnectionFactory.getReactiveConnection().serverCommands().flushAll()
				.then(Mono.just("Todas las keys borradas ok."));
	}

}
