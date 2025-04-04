package com.ignaciodm.challenge.controllers;

import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import com.ignaciodm.challenge.models.AccreditationDocument;
import com.ignaciodm.challenge.service.AccreditationsService;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
public class AccreditationsControllerTest {

	@Mock
	private AccreditationsService accreditationsService;

	@InjectMocks
	private AccreditationsController controller;

	@Test
	public void getAccreditationTest() {
		AccreditationDocument response = new AccreditationDocument();
		response.setReceptionDate(LocalDateTime.now());
		when(accreditationsService.findByAccreditationId(1)).thenReturn(Mono.just(response));
		StepVerifier.create(controller.getAccreditation(1))
				.expectNextMatches(res -> res.getStatusCode().is2xxSuccessful() && res.getBody().equals(response))
				.verifyComplete();
	}

	@Test
	public void getAccreditationWhenDateIsNullTest() {
		when(accreditationsService.findByAccreditationId(1)).thenReturn(Mono.just(new AccreditationDocument()));
		StepVerifier.create(controller.getAccreditation(1))
				.expectNextMatches(r -> r.getStatusCode() == HttpStatus.NOT_FOUND).verifyComplete();
	}

}
