package com.ignaciodm.challenge.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.ignaciodm.challenge.models.SellingCost;
import com.ignaciodm.challenge.repository.SellingCostRepository;
import com.ignaciodm.challenge.service.PathsService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
public class SellingCostControllerTest {

	@InjectMocks
	private SellingCostController sellingCostController;

	@Mock
	private PathsService pathsService;

	@Mock
	private SellingCostRepository sellingCostRepository;

	@Mock
	private ReactiveValueOperations<String, Map<Integer, Integer>> redisValueOps;

	@Mock
	private ReactiveValueOperations<String, Map<String, Object>> redisValueOpsForCheapestPath;

	@Mock
	private ReactiveRedisTemplate<String, Map<Integer, Integer>> redisTemplateSellingCost;

	@Mock
	private ReactiveRedisTemplate<String, Map<String, Object>> redisTemplateForCheapestPath;

	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);
		when(redisTemplateSellingCost.opsForValue()).thenReturn(redisValueOps);
	}

	@Test
	public void addSellingCostTest() {
		SellingCost sellingCost = new SellingCost(1, 2, 100);
		when(sellingCostRepository.save(sellingCost)).thenReturn(Mono.just(sellingCost));
		StepVerifier.create(sellingCostController.addSellingCost(sellingCost)).expectNextMatches(
				response -> response.getStatusCode() == HttpStatus.CREATED && response.getBody().equals(sellingCost))
				.verifyComplete();
	}

	@Test
	public void updateSellingCostWhenUsingRedis() {
		String redisKey = "directConnections:" + 1;
		SellingCost existing = new SellingCost(1, 2, 100);
		SellingCost updated = new SellingCost(1, 2, 150);
		Map<Integer, Integer> redisMap = new HashMap<>();
		redisMap.put(2, 100);
		when(sellingCostRepository.findByStartingPointAndEndingPoint(1, 2)).thenReturn(Mono.just(existing));
		when(sellingCostRepository.save(existing)).thenReturn(Mono.just(updated));
		when(redisTemplateSellingCost.opsForValue().get(redisKey)).thenReturn(Mono.just(redisMap));
		when(redisTemplateSellingCost.opsForValue().set(eq(redisKey), anyMap(), any())).thenReturn(Mono.just(true));

		StepVerifier.create(sellingCostController.updateSellingCost(1, 2, updated))
				.expectNextMatches(response -> response.getStatusCode().is2xxSuccessful()).verifyComplete();
	}

	@Test
	public void updateSellingCostWhenNOtFoundOnCache() {
		Integer startingPoint = 1;
		Integer endingPoint = 2;
		String redisKey = "directConnections:" + startingPoint;

		SellingCost existing = new SellingCost(startingPoint, endingPoint, 100);
		SellingCost updated = new SellingCost(startingPoint, endingPoint, 200);

		when(sellingCostRepository.findByStartingPointAndEndingPoint(startingPoint, endingPoint))
				.thenReturn(Mono.just(existing));
		when(sellingCostRepository.save(existing)).thenReturn(Mono.just(updated));
		when(redisValueOps.get(redisKey)).thenReturn(Mono.empty());
		when(redisValueOps.set(eq(redisKey), anyMap(), any())).thenReturn(Mono.just(true)); // para que cubra el
																							// thenReturn/no rompa

		StepVerifier.create(sellingCostController.updateSellingCost(startingPoint, endingPoint, updated))
				.expectNextMatches(response -> response.getStatusCode().is2xxSuccessful()).verifyComplete();
	}

	@Test
	public void updateSellingCostWhenNotFoundTest() {
		when(sellingCostRepository.findByStartingPointAndEndingPoint(1, 2))
				.thenReturn(Mono.just(new SellingCost(1, 2, 100)));
		StepVerifier.create(sellingCostController.updateSellingCost(1, 2, new SellingCost(1, 2, 100)));
	}

	@Test
	public void deleteSellingCostWhenEmptyTest() {
		SellingCost sellingCost = new SellingCost();

		when(sellingCostRepository.findByStartingPointAndEndingPoint(1, 2)).thenReturn(Mono.just(sellingCost));
		when(sellingCostRepository.delete(sellingCost)).thenReturn(Mono.empty());

		StepVerifier.create(sellingCostController.deleteSellingCost(1, 2));
	}

	@Test
	public void deleteSellingCostTest() {
		SellingCost sellingCost = new SellingCost();
		Map<Integer, Integer> redisMap = new HashMap<>();
		redisMap.put(2, 100);
		when(sellingCostRepository.findByStartingPointAndEndingPoint(1, 2)).thenReturn(Mono.just(sellingCost));
		when(sellingCostRepository.delete(sellingCost)).thenReturn(Mono.empty());
		when(redisTemplateSellingCost.opsForValue()).thenReturn(redisValueOps);
		when(redisValueOps.get("directConnections:" + 1)).thenReturn(Mono.just(redisMap));
		when(redisTemplateSellingCost.delete("directConnections:" + 1)).thenReturn(Mono.empty());
		StepVerifier.create(sellingCostController.deleteSellingCost(1, 2))
				.expectNextMatches(response -> response.getStatusCode().is2xxSuccessful()).verifyComplete();
	}

	@Test
	public void getDirectConnectionsTest() {
		String redisKey = "directConnections:" + 1;
		when(redisTemplateSellingCost.opsForValue()).thenReturn(redisValueOps);
		when(redisValueOps.get(redisKey)).thenReturn(Mono.just(Map.of(2, 100)));
		when(sellingCostRepository.findByStartingPoint(1)).thenReturn(Flux.just(new SellingCost(1, 2, 100)));
		StepVerifier.create(sellingCostController.getDirectConnections(1)).expectNextMatches(
				response -> response.getStatusCode().is2xxSuccessful() && response.getBody().equals(Map.of(2, 100)))
				.verifyComplete();
	}

	@Test
	public void getDirectConnectionsWhenNOtIntRedis() {
		Integer startingPoint = 1;
		String redisKey = "directConnections:" + startingPoint;
		when(redisTemplateSellingCost.opsForValue()).thenReturn(redisValueOps);
		when(redisValueOps.get(redisKey)).thenReturn(Mono.empty());
		when(sellingCostRepository.findByStartingPoint(startingPoint))
				.thenReturn(Flux.just(new SellingCost(1, 2, 100), new SellingCost(1, 3, 200)));
		when(redisValueOps.set(eq(redisKey), eq(Map.of(2, 100, 3, 200)), any())).thenReturn(Mono.just(true));
		StepVerifier.create(sellingCostController.getDirectConnections(startingPoint))
				.expectNextMatches(response -> response.getStatusCode().is2xxSuccessful()
						&& response.getBody().equals(Map.of(2, 100, 3, 200)))
				.verifyComplete();
	}

	@Test
	public void getFullCheapestPathTest() {
		Integer startingPoint = 1;
		Integer endingPoint = 3;
		String redisKey = "cheapestPath:1-3";
		List<SellingCost> sellingPoints = List.of(new SellingCost(1, 2, 50), new SellingCost(2, 3, 50));
		Map<String, Object> mockedResult = Map.of("path", List.of(1, 2, 3), "cost", 100);
		when(redisTemplateForCheapestPath.opsForValue()).thenReturn(redisValueOpsForCheapestPath);
		when(redisValueOpsForCheapestPath.get(redisKey)).thenReturn(Mono.empty());
		when(sellingCostRepository.findAll()).thenReturn(Flux.fromIterable(sellingPoints));
		when(pathsService.getFullCheapestPath(any(Map.class), anyInt(), anyInt())).thenReturn(Mono.just(mockedResult));
		when(redisValueOpsForCheapestPath.set(anyString(), anyMap(), any())).thenReturn(Mono.just(true));
		StepVerifier.create(sellingCostController.getFullCheapestPath(startingPoint, endingPoint))
				.expectNext(ResponseEntity.ok(Map.of("cost", 100, "path", List.of(1, 2, 3)))).verifyComplete();
	}
}
