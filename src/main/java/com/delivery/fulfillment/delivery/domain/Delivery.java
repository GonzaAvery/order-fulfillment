package com.delivery.fulfillment.delivery.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Entidad que representa una entrega.
 * Gestiona el ciclo de vida de la ejecución logística del pedido.
 */
@Entity
@Table(name = "deliveries")
public class Delivery {
	
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;
	
	@Column(nullable = false)
	private UUID orderId;
	
	@Column(nullable = false)
	private String deliveryAddress;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private DeliveryStatus status;
	
	@Column
	private UUID courierId;
	
	@Column
	private String courierName;
	
	@Column
	private String failureReason;
	
	@Column(nullable = false)
	private Instant createdAt;
	
	@Column
	private Instant updatedAt;
	
	@Column
	private Instant estimatedDeliveryTime;
	
	// Constructor para JPA
	protected Delivery() {
	}
	
	public Delivery(UUID orderId, String deliveryAddress) {
		this.id = UUID.randomUUID();
		this.orderId = orderId;
		this.deliveryAddress = deliveryAddress;
		this.status = DeliveryStatus.PENDING;
		this.createdAt = Instant.now();
		this.updatedAt = Instant.now();
	}
	
	/**
	 * Transiciona el estado de la entrega validando que la transición sea válida.
	 */
	public void transitionTo(DeliveryStatus newStatus) {
		if (!this.status.canTransitionTo(newStatus)) {
			throw new IllegalStateException(
				String.format("Invalid state transition from %s to %s for delivery %s", 
					this.status, newStatus, this.id));
		}
		this.status = newStatus;
		this.updatedAt = Instant.now();
	}
	
	/**
	 * Asigna un courier a la entrega.
	 */
	public void assignCourier(UUID courierId, String courierName) {
		if (this.status != DeliveryStatus.PENDING) {
			throw new IllegalStateException(
				String.format("Cannot assign courier to delivery %s in status %s", 
					this.id, this.status));
		}
		this.courierId = courierId;
		this.courierName = courierName;
		transitionTo(DeliveryStatus.COURIER_ASSIGNED);
	}
	
	/**
	 * Marca la entrega como fallida con una razón.
	 */
	public void markAsFailed(String reason) {
		transitionTo(DeliveryStatus.FAILED);
		this.failureReason = reason;
	}
	
	/**
	 * Establece el tiempo estimado de entrega.
	 */
	public void setEstimatedDeliveryTime(Instant estimatedDeliveryTime) {
		this.estimatedDeliveryTime = estimatedDeliveryTime;
		this.updatedAt = Instant.now();
	}
	
	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = Instant.now();
	}
	
	// Getters
	public UUID getId() {
		return id;
	}
	
	public UUID getOrderId() {
		return orderId;
	}
	
	public String getDeliveryAddress() {
		return deliveryAddress;
	}
	
	public DeliveryStatus getStatus() {
		return status;
	}
	
	public UUID getCourierId() {
		return courierId;
	}
	
	public String getCourierName() {
		return courierName;
	}
	
	public String getFailureReason() {
		return failureReason;
	}
	
	public Instant getCreatedAt() {
		return createdAt;
	}
	
	public Instant getUpdatedAt() {
		return updatedAt;
	}
	
	public Instant getEstimatedDeliveryTime() {
		return estimatedDeliveryTime;
	}
}

