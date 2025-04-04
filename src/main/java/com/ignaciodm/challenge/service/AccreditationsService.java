package com.ignaciodm.challenge.service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;

import com.ignaciodm.challenge.models.AccreditationDocument;
import com.ignaciodm.challenge.models.SellingPoint;
import com.ignaciodm.challenge.repository.AccreditationsRepository;

import reactor.core.publisher.Mono;

@Service
public class AccreditationsService {

	@Autowired
	private AccreditationsRepository accreditationsRepository;

	@Autowired
	private ReactiveRedisTemplate<String, SellingPoint> redisTemplate;

	private static final Logger logger = LoggerFactory.getLogger(SellingPointService.class);

	public Mono<AccreditationDocument> saveAccreditation(Integer sellingPointId, Double amount) {
		return redisTemplate.opsForList().range("sellingPointsList", 0, -1)
				.filter(sellingPoint -> sellingPoint.getId().equals(sellingPointId)).next().flatMap(sellingPoint -> {
					logger.info("entró al flatMap porque encontró un valor");
					AccreditationDocument acred = new AccreditationDocument();
					acred.setAmount(amount);
					acred.setReceptionDate(LocalDateTime.now());
					acred.setSellingPointId(sellingPoint.getId());
					acred.setSellingPointName(sellingPoint.getName());
					return accreditationsRepository.save(acred).thenReturn(acred);
				}).switchIfEmpty(Mono.<AccreditationDocument>just(new AccreditationDocument()));
	}

	public Mono<AccreditationDocument> findByAccreditationId(Integer accreditationId) {
		return accreditationsRepository.findBySellingPointId(accreditationId)
				.switchIfEmpty(Mono.just(new AccreditationDocument()));
	}
}
