package com.ignaciodm.challenge.service;

import org.springframework.stereotype.Service;

import com.ignaciodm.challenge.models.AccreditationDocument;
import com.ignaciodm.challenge.repository.AccreditationRepository;
import com.ignaciodm.challenge.repository.SellingPointRepository;

import reactor.core.publisher.Mono;

@Service
public class AccreditationService {

	private final AccreditationRepository accreditationRepository;
	private final SellingPointRepository sellingPointRepository;

	public AccreditationService(AccreditationRepository accreditationRepository,
			SellingPointRepository sellingPointRepository) {
		this.accreditationRepository = accreditationRepository;
		this.sellingPointRepository = sellingPointRepository;
	}

	public Mono<AccreditationDocument> getAccreditationBySellingPointId(int sellingPointId) {
		return accreditationRepository.findBySellingPointId(sellingPointId);
	}

	public Mono<AccreditationDocument> createAccreditation(AccreditationDocument accreditation) {
		return accreditationRepository.save(accreditation);
	}

	public Mono<AccreditationDocument> updateAccreditation(AccreditationDocument accreditation) {
		return accreditationRepository.save(accreditation);
	}

	public Mono<Void> deleteAllAccreditations() {
		return accreditationRepository.deleteAll();
	}

}