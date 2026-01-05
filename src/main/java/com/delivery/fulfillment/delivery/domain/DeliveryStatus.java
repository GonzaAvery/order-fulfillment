package com.delivery.fulfillment.delivery.domain;

/**
 * Estados válidos de una entrega.
 */
public enum DeliveryStatus {
	
	/**
	 * Entrega creada, pendiente de asignación de courier.
	 */
	PENDING,
	
	/**
	 * Courier asignado a la entrega.
	 */
	COURIER_ASSIGNED,
	
	/**
	 * Courier recogió el pedido.
	 */
	PICKED_UP,
	
	/**
	 * En tránsito hacia el destino.
	 */
	IN_TRANSIT,
	
	/**
	 * Entrega completada exitosamente.
	 */
	DELIVERED,
	
	/**
	 * Entrega falló.
	 */
	FAILED;
	
	/**
	 * Valida si una transición de estado es válida.
	 */
	public boolean canTransitionTo(DeliveryStatus newStatus) {
		return switch (this) {
			case PENDING -> newStatus == COURIER_ASSIGNED || newStatus == FAILED;
			case COURIER_ASSIGNED -> newStatus == PICKED_UP || newStatus == FAILED;
			case PICKED_UP -> newStatus == IN_TRANSIT || newStatus == FAILED;
			case IN_TRANSIT -> newStatus == DELIVERED || newStatus == FAILED;
			case DELIVERED, FAILED -> false; // Estados terminales
		};
	}
	
	/**
	 * Indica si el estado es terminal.
	 */
	public boolean isTerminal() {
		return this == DELIVERED || this == FAILED;
	}
}

