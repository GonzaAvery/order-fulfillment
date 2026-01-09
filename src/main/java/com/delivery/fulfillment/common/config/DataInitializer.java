package com.delivery.fulfillment.common.config;

import com.delivery.fulfillment.delivery.domain.Courier;
import com.delivery.fulfillment.delivery.repository.CourierRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Data initializer for development.
 * Creates sample couriers to test the system.
 */
@Configuration
public class DataInitializer {
	
	private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
	
	@Bean
	public CommandLineRunner initData(CourierRepository courierRepository) {
		return args -> {
			if (courierRepository.count() == 0) {
				logger.info("Initializing sample couriers...");
				
				courierRepository.save(new Courier("Juan Pérez", "+5491112345678"));
				courierRepository.save(new Courier("María García", "+5491198765432"));
				courierRepository.save(new Courier("Carlos López", "+5491155555555"));
				
				logger.info("Sample couriers created");
			}
		};
	}
}

