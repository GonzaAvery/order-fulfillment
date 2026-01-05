package com.delivery.fulfillment.common.events;

/**
 * Interfaz para publicar eventos de dominio.
 * Abstrae la implementación del broker de mensajería.
 */
public interface EventPublisher {
	
	/**
	 * Publica un evento de dominio.
	 * @param event El evento a publicar
	 */
	void publish(DomainEvent event);
}

