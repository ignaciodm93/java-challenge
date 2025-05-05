package com.ignaciodm.challenge.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.ignaciodm.challenge.models.SellingPoint;
import com.ignaciodm.challenge.service.SellingPointService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class SellingPointControllerTest {

	@Mock
	private SellingPointService sellingPointService;

	@InjectMocks
	private SellingPointController sellingPointsController;

	private final SellingPoint mockSellingPoint = new SellingPoint(1, "CABA");

	@Test
	public void getAllSellingPointsTest() {
		List<SellingPoint> listToBeReturned = List.of(new SellingPoint(1, "CABA"), new SellingPoint(2, "La Pampa"));
		when(sellingPointService.findAll()).thenReturn(Flux.fromIterable(listToBeReturned));
		StepVerifier.create(sellingPointsController.getAllSellingPoints()).expectNextSequence(listToBeReturned)
				.verifyComplete();
	}

	@Test
	public void getSellingPointByIdTest() {
		when(sellingPointService.findById(1)).thenReturn(Mono.just(mockSellingPoint));
		StepVerifier.create(sellingPointsController.getSellingPointById(1))
				.expectNextMatches(
						res -> res.getStatusCode().is2xxSuccessful() && res.getBody().equals(mockSellingPoint))
				.verifyComplete();
	}

	@Test
	public void getSellingPointByIdWhenNotFoundTest() {
		when(sellingPointService.findById(108)).thenReturn(Mono.empty());
		StepVerifier.create(sellingPointsController.getSellingPointById(108))
				.expectNextMatches(res -> res.getStatusCode().is4xxClientError()).verifyComplete();
	}

	@Test
	public void createSellingPointTest() {
		SellingPoint sp = new SellingPoint(2, "La Pampa");
		when(sellingPointService.findById(sp.getId())).thenReturn(Mono.empty());
		when(sellingPointService.save(sp)).thenReturn(Mono.just(sp));
		StepVerifier.create(sellingPointsController.createSellingPoint(sp))
				.expectNextMatches(res -> res.getStatusCode() == HttpStatus.CREATED && res.getBody().equals(sp))
				.verifyComplete();
	}

	@Test
	public void createSellingPointWhenDuplicatedTest() {
		when(sellingPointService.findById(mockSellingPoint.getId())).thenReturn(Mono.just(mockSellingPoint));
		when(sellingPointService.save(any())).thenReturn(Mono.just(mockSellingPoint));
		StepVerifier.create(sellingPointsController.createSellingPoint(mockSellingPoint))
				.expectNextMatches(res -> res.getStatusCode() == HttpStatus.BAD_REQUEST).verifyComplete();
	}

	@Test
	public void updateSellingPointTest() {
		when(sellingPointService.update(1, mockSellingPoint)).thenReturn(Mono.just(mockSellingPoint));
		StepVerifier.create(sellingPointsController.updateOrCreateSellingPoint(1, mockSellingPoint));
	}

	@Test
	public void deleteSellingPointTest() {
		when(sellingPointService.deleteById(1)).thenReturn(Mono.just(1L));
		StepVerifier.create(sellingPointsController.deleteSellingPoint(1))
				.expectNextMatches(res -> res.getStatusCode() == HttpStatus.OK).verifyComplete();
		;
	}

	@Test
	public void deleteSellingPointWhenBadRequestTest() {
		when(sellingPointService.deleteById(1)).thenReturn(Mono.just(0L));
		StepVerifier.create(sellingPointsController.deleteSellingPoint(1))
				.expectNextMatches(res -> res.getStatusCode() == HttpStatus.BAD_REQUEST).verifyComplete();
	}

	// saveInitialData y clearCache en principio no los testeo porque se hicieron
	// para facilitar la creación y limpieza de cache de redis, no para el
	// funcionamiento propio de la aplicacion.
}
