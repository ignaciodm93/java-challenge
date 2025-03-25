package com.ignaciodm.challenge.service;

import org.redisson.api.RMapReactive;
import org.redisson.api.RedissonReactiveClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ignaciodm.challenge.models.SellingPointDocument;

@Service
public class IntegratedCacheService {

	@Autowired
	private RedissonReactiveClient redissonReactiveClient;

	public RMapReactive<Integer, SellingPointDocument> getSellingPointsCache() {
		return redissonReactiveClient.getMap("sellingPointsCache");
	}
}