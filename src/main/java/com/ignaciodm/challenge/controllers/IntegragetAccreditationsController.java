package com.ignaciodm.challenge.controllers;

import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ignaciodm.challenge.models.AccreditationDocument;
import com.ignaciodm.challenge.service.AccreditationService;

import reactor.core.publisher.Mono;

//OK - FALTA REDIS, solo mongo por ahora
@RestController
@RequestMapping("/integrated-accreditations")
public class IntegragetAccreditationsController implements IntegratedAccreditationsApi {

	private AccreditationService accreditationService;

	public IntegragetAccreditationsController(AccreditationService accreditationService) {
		this.accreditationService = accreditationService;
	}

	@PostMapping
	public Mono<ResponseEntity<AccreditationDocument>> createOrUpdateAccreditation(@RequestParam Double amount,
			@RequestParam int sellingPointId, @RequestParam String sellingPointName) {
		return accreditationService.getAccreditationBySellingPointId(sellingPointId).flatMap(existingAccreditation -> {
			existingAccreditation.setAmount(amount);
			return accreditationService.updateAccreditation(existingAccreditation);
		}).switchIfEmpty(Mono.defer(() -> {
			AccreditationDocument accreditation = new AccreditationDocument();
			accreditation.setAmount(amount);
			accreditation.setSellingPointId(sellingPointId);
			accreditation.setSellingPointName(sellingPointName);
			accreditation.setReceptionDate(LocalDateTime.now());
			return accreditationService.createAccreditation(accreditation);
		})).map(ResponseEntity::ok).onErrorResume(error -> Mono.just(ResponseEntity.badRequest().body(null)));
	}

	@GetMapping("/{id}")
	public Mono<ResponseEntity<AccreditationDocument>> getAccreditation(@PathVariable int id) {
		return accreditationService.getAccreditationBySellingPointId(id).map(ResponseEntity::ok)
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@DeleteMapping("/all")
	public Mono<ResponseEntity<Void>> deleteAllAccreditations() {
		return accreditationService.deleteAllAccreditations().then(Mono.just(ResponseEntity.noContent().build()));
	}

	// prueba redis
//	@GetMapping("/{id}")
//	@Cacheable(value = "accreditations", key = "#id")
//	public Mono<ResponseEntity<AccreditationDocument>> getAccreditation(@PathVariable int id) {
//		return accreditationService.getAccreditationById(id).map(ResponseEntity::ok)
//				.defaultIfEmpty(ResponseEntity.notFound().build());
//	}
}