package com.delivery.fulfillment.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger configuration for API documentation.
 */
@Configuration
public class OpenApiConfig {
	
	@Bean
	public OpenAPI orderFulfillmentOpenAPI() {
		return new OpenAPI()
			.info(new Info()
				.title("Order Fulfillment API")
				.description("API for order and delivery management. Fulfillment system " +
					"inspired by large-scale delivery platforms.")
				.version("1.0.0")
				.contact(new Contact()
					.name("Order Fulfillment Team")
					.email("support@orderfulfillment.com"))
				.license(new License()
					.name("Apache 2.0")
					.url("https://www.apache.org/licenses/LICENSE-2.0.html")));
	}
}


