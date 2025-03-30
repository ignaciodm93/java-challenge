package com.ignaciodm.challenge.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/accreditations")
public class AccreditationsController {

//	@Autowired
//	private AccreditationRedisService accreditationRedisService;
//
//	@GetMapping("/{key}")
//	public Mono<ResponseEntity<AccreditationDocument>> getAccreditation(@PathVariable String key) {
//		return accreditationRedisService.getAccreditation(key).map(ResponseEntity::ok)
//				.defaultIfEmpty(ResponseEntity.notFound().build());
//	}
//
//	@PostMapping("/{key}")
//	public Mono<ResponseEntity<Boolean>> saveAccreditation(@PathVariable String key,
//			@RequestBody AccreditationDocument accreditation) {
//		return accreditationRedisService.saveAccreditation(key, accreditation).map(ResponseEntity::ok);
//	}
//
//	@DeleteMapping("/{key}")
//	public Mono<ResponseEntity<Boolean>> deleteAccreditation(@PathVariable String key) {
//		return accreditationRedisService.deleteAccreditation(key).map(ResponseEntity::ok);
//	}

	// ... otros métodos ...
}