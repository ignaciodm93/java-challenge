package com.ignaciodm.challenge.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ignaciodm.challenge.service.AccreditationsService;
import com.ignaciodm.interfaces.AccreditationsApi;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/accreditations")
public class AccreditationsController implements AccreditationsApi {

	private static final String ACCREDITATION_POINT_TO_GET_NOT_FOUND = "Accreditation point to get not found.";

	private static final String SELLING_POINT_NOT_FOUND_IN_CACHE_TRY_ANOTHER_ONE_OR_UPDATE_THE_CACHE = "Selling point not found in cache, try another one or update the selling points list cache.";

	@Autowired
	private AccreditationsService accreditationsService;

	@Override
	public Mono<ResponseEntity<Object>> getAccreditation(@PathVariable Integer accreditationId) {
		return accreditationsService.findByAccreditationId(accreditationId).map(result -> {
			if (result.getReceptionDate() != null) {
				return ResponseEntity.ok(result);
			} else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ACCREDITATION_POINT_TO_GET_NOT_FOUND);
			}
		});
	}

	@Override
	public Mono<ResponseEntity<Object>> saveAccreditation(@PathVariable Integer sellingPointId,
			@PathVariable Double amount) {
		return accreditationsService.saveAccreditation(sellingPointId, amount).map(response -> {
			if (response.getReceptionDate() != null) {
				return ResponseEntity.ok(response);
			} else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(SELLING_POINT_NOT_FOUND_IN_CACHE_TRY_ANOTHER_ONE_OR_UPDATE_THE_CACHE);
			}
		});
	}

}