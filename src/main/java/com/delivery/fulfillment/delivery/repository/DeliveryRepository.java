package com.delivery.fulfillment.delivery.repository;

import com.delivery.fulfillment.delivery.domain.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {
	
	Optional<Delivery> findByOrderId(UUID orderId);
}

