package com.delivery.fulfillment.common.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

/**
 * Servicio para registrar métricas custom del sistema.
 * Expone métricas de negocio para observabilidad.
 */
@Service
public class MetricsService {
	
	private final Counter ordersCreatedCounter;
	private final Counter eventsProcessedCounter;
	private final Counter eventsFailedCounter;
	
	public MetricsService(MeterRegistry meterRegistry) {
		this.ordersCreatedCounter = Counter.builder("orders_created_total")
			.description("Total number of orders created")
			.tag("status", "created")
			.register(meterRegistry);
		
		this.eventsProcessedCounter = Counter.builder("events_processed_total")
			.description("Total number of events processed successfully")
			.register(meterRegistry);
		
		this.eventsFailedCounter = Counter.builder("events_failed_total")
			.description("Total number of events that failed to process")
			.register(meterRegistry);
	}
	
	/**
	 * Incrementa el contador de pedidos creados.
	 */
	public void incrementOrdersCreated() {
		ordersCreatedCounter.increment();
	}
	
	/**
	 * Incrementa el contador de eventos procesados exitosamente.
	 */
	public void incrementEventsProcessed() {
		eventsProcessedCounter.increment();
	}
	
	/**
	 * Incrementa el contador de eventos fallidos.
	 */
	public void incrementEventsFailed() {
		eventsFailedCounter.increment();
	}
}

