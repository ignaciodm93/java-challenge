package com.ignaciodm.challenge.controllers;

import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RMapReactive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.ignaciodm.challenge.models.SellingCost;
import com.ignaciodm.challenge.service.CacheService;
import com.ignaciodm.challenge.service.PathService;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class SellingCostsControllerTest {

	private static final String FARE = "fare";

	private static final String PATH = "path";

	@Mock
	private CacheService cacheService;

	@Mock
	private PathService pathService;

	@Mock
	private RMapReactive<String, SellingCost> sellingCostsCache;

	@InjectMocks
	private SellingCostsController controller;

	private SellingCost sellingCost;

	@BeforeEach
	public void setUp() {
		sellingCost = new SellingCost(1, 2, 10);
	}

	@Test
	public void addOrUpdateSellingCostTest() {
		when(cacheService.getCostsCache()).thenReturn(sellingCostsCache);
		when(sellingCostsCache.put(sellingCost.getIdentifierKeyRoute(), sellingCost))
				.thenReturn(Mono.just(sellingCost));
		Mono<ResponseEntity<String>> response = controller.addOrUpdateSellingCost(sellingCost);
		StepVerifier.create(response).expectNextMatches(resp -> resp.getStatusCode() == HttpStatus.CREATED)
				.verifyComplete();
	}

	@Test
	public void removeSellingCost_whenOkTest() {
		when(cacheService.getCostsCache()).thenReturn(sellingCostsCache);
		when(sellingCostsCache.containsKey("1-2")).thenReturn(Mono.just(true));
		when(sellingCostsCache.remove("1-2")).thenReturn(Mono.just(sellingCost));
		Mono<ResponseEntity<String>> response = controller.removeSellingCost(1, 2);
		StepVerifier.create(response).expectNextMatches(resp -> resp.getStatusCode() == HttpStatus.OK).verifyComplete();
	}

	@Test
	public void removeSellingCost_whenKeyDoesNotExistTest() {
		when(cacheService.getCostsCache()).thenReturn(sellingCostsCache);
		when(sellingCostsCache.containsKey("1-2")).thenReturn(Mono.just(false));
		Mono<ResponseEntity<String>> response = controller.removeSellingCost(1, 2);
		StepVerifier.create(response).expectNextMatches(resp -> resp.getStatusCode() == HttpStatus.BAD_REQUEST)
				.verifyComplete();
	}

	@Test
	public void getDirectConnectionTest() {
		Map<Integer, Integer> directConnections = new HashMap<>();
		directConnections.put(2, 10);
		when(pathService.getDirectConnection(1)).thenReturn(Mono.just(directConnections));
		Mono<ResponseEntity<Map<Integer, Integer>>> response = controller.getDirectConnection(1);
		StepVerifier.create(response).expectNextMatches(
				resp -> resp.getStatusCode() == HttpStatus.OK && resp.getBody() != null && resp.getBody().size() == 1)
				.verifyComplete();
	}

	@Test
	public void getDirectConnection_whenNoConnectionFoundTest() {
		when(pathService.getDirectConnection(1)).thenReturn(Mono.just(new HashMap<>()));
		Mono<ResponseEntity<Map<Integer, Integer>>> response = controller.getDirectConnection(1);
		StepVerifier.create(response).expectNextMatches(resp -> resp.getStatusCode() == HttpStatus.NOT_FOUND)
				.verifyComplete();
	}

	@Test
	public void getFullCheapestPathTest() {
		Map<String, Object> expectedPathAndFare = Map.of(PATH, List.of(1, 2), FARE, 10);
		when(pathService.getFullCheapestPath(1, 2)).thenReturn(Mono.just(expectedPathAndFare));

		Mono<ResponseEntity<Object>> response = controller.getFullCheapestPath(1, 2);

		StepVerifier.create(response)
				.expectNextMatches(resp -> resp.getStatusCode() == HttpStatus.OK && resp.getBody() != null)
				.verifyComplete();
	}
}