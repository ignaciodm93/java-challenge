package com.ignaciodm93.interfaces;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import reactor.core.publisher.Mono;

public interface SellingCostApi {

	String GET_DIRECT_CONNECTION_VALUE = "Get direct connections";
	String GET_DIRECT_CONNECTION_NOTES = "Gets the direct connections for a specific selling point.";
	String GET_CHEAPEST_PATH_VALUE = "Get the cheapest path";
	String GET_CHEAPEST_PATH_NOTES = "Gets the cheapest path between two selling points.";

	@Operation(summary = GET_DIRECT_CONNECTION_VALUE, description = GET_DIRECT_CONNECTION_NOTES, responses = {
			@ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Map.class))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(implementation = Map.class))) })
	Mono<ResponseEntity<Map<Integer, Integer>>> getDirectConnections(
			@Parameter(description = "Selling point ID to discover paths", required = true) @RequestParam Integer sellingPointToDiscoverPathsId);

	@Operation(summary = GET_CHEAPEST_PATH_VALUE, description = GET_CHEAPEST_PATH_NOTES, responses = {
			@ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = Object.class))) })
	Mono<ResponseEntity<Map<String, Object>>> getFullCheapestPath(
			@Parameter(description = "Starting point ID", required = true) @RequestParam Integer startingPoint,
			@Parameter(description = "Ending point ID", required = true) @RequestParam Integer endingPoint);
}
