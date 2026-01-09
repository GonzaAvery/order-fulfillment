package com.delivery.fulfillment.common.events;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio para eventos procesados.
 * Permite verificar si un evento ya fue procesado por un consumer específico.
 */
@Repository
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, UUID> {
	
	/**
	 * Verifica si un evento ya fue procesado por un consumer específico.
	 */
	Optional<ProcessedEvent> findByEventIdAndConsumer(UUID eventId, String consumer);
	
	/**
	 * Verifica si un evento existe (sin importar el consumer).
	 */
	boolean existsByEventId(UUID eventId);
}


