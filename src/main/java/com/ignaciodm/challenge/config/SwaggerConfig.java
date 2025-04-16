package com.ignaciodm.challenge.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfig {

	private static final String VERSION = "1.0";
	private static final String BEARER_AUTH = "bearerAuth";
	private static final String JWT = "JWT";
	private static final String BEARER = "bearer";

	@Bean
	public OpenAPI customOpenAPI() {
		final String securitySchemeName = BEARER_AUTH;

		return new OpenAPI().info(new Info().version(VERSION))
				.addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
				.components(new Components().addSecuritySchemes(securitySchemeName, new SecurityScheme()
						.name(securitySchemeName).type(SecurityScheme.Type.HTTP).scheme(BEARER).bearerFormat(JWT)));
	}
}
