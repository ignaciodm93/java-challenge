//package com.ignaciodm.challenge.service;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//import org.redisson.api.RMapReactive;
//import org.redisson.api.RedissonReactiveClient;
//import org.redisson.codec.JsonJacksonCodec;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import com.ignaciodm.challenge.models.SellingCost;
//import com.ignaciodm.challenge.models.SellingPoint;
//
//import jakarta.annotation.PostConstruct;
//
//@Service
//public class CacheService {
//
//	@Autowired
//	private RedissonReactiveClient redissonReactiveClient;
//
//	private RMapReactive<Integer, SellingPoint> sellingPointsCache;
//	private RMapReactive<String, SellingCost> sellingCostsCache;
//
//	@PostConstruct
//	private void initializeCaches() {
//		// initializeSellingPointsCache();
//		initializeSellingCostsCache();
//	}
//
//	public RMapReactive<Integer, SellingPoint> getSellingPointsCache() {
//		return sellingPointsCache;
//	}
//
//	public RMapReactive<String, SellingCost> getCostsCache() {
//		return sellingCostsCache;
//	}
//
//	private void initializeSellingPointsCache() {
//		sellingPointsCache = redissonReactiveClient.getMap("sellingPointsCache", new JsonJacksonCodec());
//		sellingPointsCache.putAll(getInitialSellingPoints().stream()
//				.collect(Collectors.toMap(SellingPoint::getId, sellingPoint -> sellingPoint))).subscribe();
//	}
//
//	private void initializeSellingCostsCache() {
//		sellingCostsCache = redissonReactiveClient.getMap("sellingCostsCache", new JsonJacksonCodec());
//		sellingCostsCache
//				.putAll(getInitialSellingCosts().stream()
//						.collect(Collectors.toMap(SellingCost::getIdentifierKeyRoute, sellingCost -> sellingCost)))
//				.subscribe();
//	}
//
//	private List<SellingPoint> getInitialSellingPoints() {
//		return List.of(new SellingPoint(1, "CABA"), new SellingPoint(2, "GBA_1"), new SellingPoint(3, "GBA_2"),
//				new SellingPoint(4, "Santa Fe"), new SellingPoint(5, "Córdoba"), new SellingPoint(6, "Misiones"),
//				new SellingPoint(7, "Salta"), new SellingPoint(8, "Chubut"), new SellingPoint(9, "Santa Cruz"),
//				new SellingPoint(10, "Catamarca"));
//	}
//
//	private List<SellingCost> getInitialSellingCosts() {
//		return List.of(new SellingCost(1, 2, 2), new SellingCost(1, 3, 3), new SellingCost(2, 3, 5),
//				new SellingCost(2, 4, 10), new SellingCost(1, 4, 11), new SellingCost(4, 5, 5),
//				new SellingCost(2, 5, 14), new SellingCost(6, 7, 32), new SellingCost(8, 9, 11),
//				new SellingCost(10, 7, 5), new SellingCost(3, 8, 10), new SellingCost(5, 8, 30),
//				new SellingCost(10, 5, 5), new SellingCost(4, 6, 6));
//	}
//}
