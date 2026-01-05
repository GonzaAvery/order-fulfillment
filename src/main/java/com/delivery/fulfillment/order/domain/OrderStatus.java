package com.delivery.fulfillment.order.domain;

/**
 * Estados válidos de un pedido.
 * Define las transiciones permitidas en la máquina de estados.
 */
public enum OrderStatus {
	
	/**
	 * Pedido creado por el usuario, pendiente de aceptación.
	 */
	PLACED,
	
	/**
	 * Pedido aceptado y en proceso de fulfillment.
	 */
	ACCEPTED,
	
	/**
	 * Pedido recogido por el courier.
	 */
	PICKED_UP,
	
	/**
	 * Pedido en tránsito hacia el destino.
	 */
	IN_TRANSIT,
	
	/**
	 * Pedido entregado exitosamente.
	 */
	DELIVERED,
	
	/**
	 * Pedido cancelado (por usuario o sistema).
	 */
	CANCELED,
	
	/**
	 * Pedido falló y no puede completarse.
	 */
	FAILED;
	
	/**
	 * Valida si una transición de estado es válida.
	 * Garantiza que no existan transiciones inválidas.
	 */
	public boolean canTransitionTo(OrderStatus newStatus) {
		return switch (this) {
			case PLACED -> newStatus == ACCEPTED || newStatus == CANCELED || newStatus == FAILED;
			case ACCEPTED -> newStatus == PICKED_UP || newStatus == CANCELED || newStatus == FAILED;
			case PICKED_UP -> newStatus == IN_TRANSIT || newStatus == FAILED;
			case IN_TRANSIT -> newStatus == DELIVERED || newStatus == FAILED;
			case DELIVERED, CANCELED, FAILED -> false; // Estados terminales
		};
	}
	
	/**
	 * Indica si el estado es terminal (no puede cambiar).
	 */
	public boolean isTerminal() {
		return this == DELIVERED || this == CANCELED || this == FAILED;
	}
}

