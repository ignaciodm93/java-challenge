package com.ignaciodm.challenge.service;

import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RMapReactive;

import com.ignaciodm.challenge.models.SellingCost;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class PathServiceTest {

	private PathService pathService;

	@Mock
	private RMapReactive<String, SellingCost> sellingCostsCache;

	@Mock
	private CacheService cacheService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		Map<String, SellingCost> sellingCosts = new HashMap<>();
		sellingCosts.put("1-2", new SellingCost(1, 2, 100));
		sellingCosts.put("1-3", new SellingCost(1, 3, 150));

		when(cacheService.getCostsCache()).thenReturn(sellingCostsCache);
		when(sellingCostsCache.readAllMap()).thenReturn(Mono.just(sellingCosts));

		pathService = new PathService(cacheService);
	}

	@Test
	public void getFullCheapestPathTest() {
		StepVerifier.create(pathService.getFullCheapestPath(1, 3))
				.expectNextMatches(map -> map.containsKey("path: ") && map.containsKey("final fare: "))
				.verifyComplete();
	}

	@Test
	public void getFullCheapestPathWhenErrorTest() {
		StepVerifier.create(pathService.getFullCheapestPath(7, 10)).expectNextMatches(map -> map.isEmpty())
				.verifyComplete();
	}
}
