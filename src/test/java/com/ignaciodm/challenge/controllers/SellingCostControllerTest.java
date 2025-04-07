package com.ignaciodm.challenge.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.HashMap;
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

import com.ignaciodm.challenge.models.SellingCost;
import com.ignaciodm.challenge.repository.SellingCostRepository;
import com.ignaciodm.challenge.service.PathsService;

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
	private ReactiveRedisTemplate<String, Map<Integer, Integer>> redisTemplateSellingCost;

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

//	@Test
//	public void getDirectConnectionsTest() {
//		when(redisTemplateSellingCost.opsForValue()).thenReturn(redisValueOps);
//		StepVerifier.create(sellingCostController.getDirectConnections(1));
//	}
}
