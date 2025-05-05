package com.ignaciodm.challenge.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveListOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;

import com.ignaciodm.challenge.models.AccreditationDocument;
import com.ignaciodm.challenge.models.SellingPoint;
import com.ignaciodm.challenge.repository.AccreditationsRepository;
import com.ignaciodm.challenge.service.AccreditationsService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
public class AccreditationsServiceTest {

	@InjectMocks
	private AccreditationsService accreditationsService;

	@Mock
	private ReactiveRedisTemplate<String, SellingPoint> redisTemplate;

	@Mock
	private ReactiveListOperations<String, SellingPoint> redisListOps;

	@Mock
	private AccreditationsRepository accreditationsRepository;

	@Test
	void saveAccreditationTest() {
		Integer sellingPointId = 1;
		Double amount = 150.0;
		SellingPoint mockSellingPoint = new SellingPoint();
		mockSellingPoint.setId(sellingPointId);
		mockSellingPoint.setName("La Matanza");
		when(redisTemplate.opsForList()).thenReturn(redisListOps);
		when(redisListOps.range("sellingPointsList", 0, -1)).thenReturn(Flux.just(mockSellingPoint));
		when(accreditationsRepository.save(any())).thenAnswer(ans -> Mono.just(ans.getArgument(0)));
		StepVerifier.create(accreditationsService.saveAccreditation(sellingPointId, amount))
				.expectNextMatches(doc -> doc.getSellingPointId() == sellingPointId && doc.getAmount().equals(amount)
						&& doc.getSellingPointName().equals("La Matanza") && doc.getReceptionDate() != null)
				.verifyComplete();
	}

	@Test
	public void findByAccreditationIdTest_ReturnsLatest() {
		AccreditationDocument doc1 = new AccreditationDocument();
		doc1.setReceptionDate(LocalDateTime.now().minusDays(2));
		AccreditationDocument doc2 = new AccreditationDocument();
		doc2.setReceptionDate(LocalDateTime.now());
		AccreditationDocument doc3 = new AccreditationDocument();
		doc3.setReceptionDate(LocalDateTime.now().minusDays(1));
		when(accreditationsRepository.findBySellingPointId(1)).thenReturn(Flux.just(doc1, doc3, doc2));
		StepVerifier.create(accreditationsService.findByAccreditationId(1))
				.expectNextMatches(doc -> doc != null && doc.getReceptionDate().equals(doc2.getReceptionDate()))
				.verifyComplete();
	}

}
