package com.ignaciodm.challenge.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CacheServiceTest {

//	@Mock
//	private RedissonReactiveClient redissonReactiveClient;
//
//	@Mock
//	private RMapReactive<Integer, SellingPoint> sellingPointsCache;
//
//	@Mock
//	private RMapReactive<String, SellingCost> sellingCostsCache;
//
//	@InjectMocks
//	private CacheService cacheService;
//
//	@BeforeEach
//	void setUp() throws Exception {
//		when(redissonReactiveClient.getMap(eq("sellingPointsCache"), Mockito.<JsonJacksonCodec>any()))
//				.thenReturn((RMapReactive) sellingPointsCache);
//		when(redissonReactiveClient.getMap(eq("sellingCostsCache"), Mockito.<JsonJacksonCodec>any()))
//				.thenReturn((RMapReactive) sellingCostsCache);
//		when(sellingPointsCache.putAll(anyMap())).thenReturn(Mono.empty());
//		when(sellingCostsCache.putAll(anyMap())).thenReturn(Mono.empty());
//
//		callPrivateMethod("initializeSellingPointsCache");
//		callPrivateMethod("initializeSellingCostsCache");
//		callPrivateMethod("initializeCaches");
//	}
//
//	private void callPrivateMethod(String methodName) throws Exception {
//		Method method = CacheService.class.getDeclaredMethod(methodName);
//		method.setAccessible(true);
//		method.invoke(cacheService);
//	}
//
//	@Test
//	void getSellingPointsCacheTest() {
//		assert cacheService.getSellingPointsCache() == sellingPointsCache;
//	}
//
//	@Test
//	void getCostsCacheTest() {
//		assert cacheService.getCostsCache() == sellingCostsCache;
//	}
}
