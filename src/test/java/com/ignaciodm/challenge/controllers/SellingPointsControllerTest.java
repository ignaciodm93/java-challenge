package com.ignaciodm.challenge.controllers;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SellingPointsControllerTest {

//	@Mock
//	private CacheService cacheService;
//
//	@Mock
//	private RMapReactive<Integer, SellingPoint> sellingPointsCache;
//
//	@InjectMocks
//	private SellingPointsController sellingPointsController;
//
//	private SellingPoint sellingPoint;
//
//	@BeforeEach
//	public void setUp() {
//		sellingPoint = new SellingPoint(1, "Test");
//		when(cacheService.getSellingPointsCache()).thenReturn(sellingPointsCache);
//	}
//
//	@Test
//	public void addTest() {
//		when(sellingPointsCache.containsKey(sellingPoint.getId())).thenReturn(Mono.just(false));
//		when(sellingPointsCache.put(sellingPoint.getId(), sellingPoint)).thenReturn(Mono.just(sellingPoint));
//		StepVerifier.create(sellingPointsController.add(sellingPoint))
//				.expectNextMatches(resp -> resp.getStatusCode() == HttpStatus.OK).verifyComplete();
//	}
//
//	@Test
//	public void add_whenPointExistsTest() {
//		when(sellingPointsCache.containsKey(sellingPoint.getId())).thenReturn(Mono.just(true));
//		StepVerifier.create(sellingPointsController.add(sellingPoint))
//				.expectNextMatches(resp -> resp.getStatusCode() == HttpStatus.BAD_REQUEST).verifyComplete();
//	}
//
//	@Test
//	public void update_whenPointExistsTest() {
//		when(sellingPointsCache.containsKey(sellingPoint.getId())).thenReturn(Mono.just(true));
//		when(sellingPointsCache.put(sellingPoint.getId(), sellingPoint)).thenReturn(Mono.just(sellingPoint));
//		StepVerifier.create(sellingPointsController.update(sellingPoint))
//				.expectNextMatches(resp -> resp.getStatusCode() == HttpStatus.OK).verifyComplete();
//	}
//
//	@Test
//	public void update_whenPointDoesNotExistTest() {
//		when(sellingPointsCache.containsKey(sellingPoint.getId())).thenReturn(Mono.just(false));
//		StepVerifier.create(sellingPointsController.update(sellingPoint))
//				.expectNextMatches(resp -> resp.getStatusCode() == HttpStatus.BAD_REQUEST).verifyComplete();
//	}
//
//	@Test
//	public void delete_whenPointExistsTest() {
//		when(sellingPointsCache.remove(sellingPoint.getId())).thenReturn(Mono.just(sellingPoint));
//		StepVerifier.create(sellingPointsController.delete(sellingPoint.getId()))
//				.expectNextMatches(resp -> resp.getStatusCode() == HttpStatus.NO_CONTENT).verifyComplete();
//	}
//
//	@Test
//	public void delete_whenPointDoesNotExistTest() {
//		when(sellingPointsCache.remove(sellingPoint.getId())).thenReturn(Mono.empty());
//		StepVerifier.create(sellingPointsController.delete(sellingPoint.getId()))
//				.expectNextMatches(resp -> resp.getStatusCode() == HttpStatus.NOT_FOUND).verifyComplete();
//	}
}