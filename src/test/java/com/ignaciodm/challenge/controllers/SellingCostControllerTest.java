package com.ignaciodm.challenge.controllers;

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
import com.ignaciodm.challenge.service.SellingCostsService;

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

	@Mock
	private SellingCostsService sellingCostService;

	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);
		when(redisTemplateSellingCost.opsForValue()).thenReturn(redisValueOps);
	}

	@Test
	public void addSellingCostTest() {
		SellingCost sellingCost = new SellingCost(1, 2, 100);
		when(sellingCostService.addSellingCost(sellingCost)).thenReturn(Mono.just(sellingCost));
		StepVerifier.create(sellingCostController.addSellingCost(sellingCost)).expectNextMatches(
				response -> response.getStatusCode() == HttpStatus.CREATED && response.getBody().equals(sellingCost))
				.verifyComplete();
	}

	@Test
	public void getSellingPointsPathsTest() {
		Map<Integer, Map<Integer, Integer>> expectedPaths = new HashMap<>();
		expectedPaths.put(1, Map.of(2, 100, 3, 200));
		expectedPaths.put(2, Map.of(3, 50));
		when(sellingCostService.getSellingPointsPaths()).thenReturn(Mono.just(expectedPaths));
		StepVerifier.create(sellingCostController.getSellingPointsPaths()).expectNext(expectedPaths).verifyComplete();
	}

	@Test
	public void updateSellingCostWhenUsingRedisTest() {
		SellingCost updated = new SellingCost(1, 2, 150);
		when(sellingCostService.updateSellingCost(1, 2, updated)).thenReturn(Mono.just(updated));
		StepVerifier.create(sellingCostController.updateSellingCost(1, 2, updated))
				.expectNextMatches(
						response -> response.getStatusCode().is2xxSuccessful() && response.getBody().equals(updated))
				.verifyComplete();
	}

	@Test
	public void updateSellingCostWhenNOtFoundOnCacheTest() {
		Integer startingPoint = 1;
		Integer endingPoint = 2;
		SellingCost updated = new SellingCost(startingPoint, endingPoint, 200);
		when(sellingCostService.updateSellingCost(startingPoint, endingPoint, updated)).thenReturn(Mono.just(updated));
		StepVerifier.create(sellingCostController.updateSellingCost(startingPoint, endingPoint, updated))
				.expectNextMatches(
						response -> response.getStatusCode().is2xxSuccessful() && response.getBody().equals(updated))
				.verifyComplete();
	}

	@Test
	public void updateSellingCostWhenNotFoundTest() {
		SellingCost updated = new SellingCost(1, 2, 100);
		when(sellingCostService.updateSellingCost(1, 2, updated)).thenReturn(Mono.empty());
		StepVerifier.create(sellingCostController.updateSellingCost(1, 2, updated))
				.expectNextMatches(response -> response.getStatusCode() == HttpStatus.NOT_FOUND).verifyComplete();
	}

	@Test
	public void deleteSellingCostTest() {
		when(sellingCostService.deleteSellingCost(1, 2)).thenReturn(Mono.empty());
		StepVerifier.create(sellingCostController.deleteSellingCost(1, 2))
				.expectNextMatches(response -> response.getStatusCode() == HttpStatus.NO_CONTENT).verifyComplete();
	}

	@Test
	public void getDirectConnectionsTest() {
		Map<Integer, Integer> directConnections = Map.of(2, 100);
		when(sellingCostService.getDirectConnections(1)).thenReturn(Mono.just(directConnections));
		StepVerifier.create(sellingCostController.getDirectConnections(1)).expectNextMatches(
				response -> response.getStatusCode().is2xxSuccessful() && response.getBody().equals(directConnections))
				.verifyComplete();
	}

	@Test
	public void getDirectConnectionsWhenNOtInRedis() {
		Integer startingPoint = 1;
		Map<Integer, Integer> directConnections = Map.of(2, 100, 3, 200);
		when(sellingCostService.getDirectConnections(startingPoint)).thenReturn(Mono.just(directConnections));
		StepVerifier.create(sellingCostController.getDirectConnections(startingPoint)).expectNextMatches(
				response -> response.getStatusCode().is2xxSuccessful() && response.getBody().equals(directConnections))
				.verifyComplete();
	}

	@Test
	public void getFullCheapestPathTest() {
		Integer startingPoint = 1;
		Integer endingPoint = 3;
		Map<String, Object> mockedResult = Map.of("path", List.of(1, 2, 3), "cost", 100);
		when(sellingCostService.getFullCheapestPath(startingPoint, endingPoint)).thenReturn(Mono.just(mockedResult));
		StepVerifier.create(sellingCostController.getFullCheapestPath(startingPoint, endingPoint))
				.expectNext(ResponseEntity.ok(mockedResult)).verifyComplete();
	}
}
