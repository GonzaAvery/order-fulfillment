package com.delivery.fulfillment.integration;

import com.delivery.fulfillment.delivery.domain.Courier;
import com.delivery.fulfillment.delivery.domain.Delivery;
import com.delivery.fulfillment.delivery.domain.DeliveryStatus;
import com.delivery.fulfillment.delivery.repository.CourierRepository;
import com.delivery.fulfillment.delivery.repository.DeliveryRepository;
import com.delivery.fulfillment.order.domain.Order;
import com.delivery.fulfillment.order.domain.OrderStatus;
import com.delivery.fulfillment.order.dto.CreateOrderRequest;
import com.delivery.fulfillment.order.dto.OrderResponse;
import com.delivery.fulfillment.order.repository.OrderRepository;
import com.delivery.fulfillment.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Test de integración end-to-end del flujo completo de un pedido.
 * Usa Testcontainers para levantar PostgreSQL y Kafka en contenedores.
 */
@SpringBootTest
@Testcontainers
@TestPropertySource(properties = {
	"spring.kafka.consumer.auto-offset-reset=earliest"
})
class OrderFulfillmentIntegrationTest {
	
	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
		DockerImageName.parse("postgres:15-alpine")
	)
		.withDatabaseName("order_fulfillment_test")
		.withUsername("test")
		.withPassword("test");
	
	@Container
	static KafkaContainer kafka = new KafkaContainer(
		DockerImageName.parse("confluentinc/cp-kafka:7.5.0")
	);
	
	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
		registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
	}
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private OrderRepository orderRepository;
	
	@Autowired
	private DeliveryRepository deliveryRepository;
	
	@Autowired
	private CourierRepository courierRepository;
	
	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;
	
	@BeforeEach
	void setUp() {
		// Limpiar datos de tests anteriores
		deliveryRepository.deleteAll();
		orderRepository.deleteAll();
		courierRepository.deleteAll();
		
		// Crear courier de prueba
		Courier courier = new Courier("Test Courier", "+5491111111111");
		courierRepository.save(courier);
	}
	
	@Test
	void testCompleteOrderFlow() {
		// 1. Crear un pedido
		UUID customerId = UUID.randomUUID();
		CreateOrderRequest request = new CreateOrderRequest(
			customerId,
			"Av. Corrientes 1234, CABA",
			BigDecimal.valueOf(1500.50)
		);
		
		OrderResponse orderResponse = orderService.createOrder(request);
		UUID orderId = orderResponse.id();
		
		// Verificar que el pedido se creó con estado PLACED
		assertThat(orderResponse.status()).isEqualTo(OrderStatus.PLACED);
		
		// 2. Esperar a que el evento OrderPlaced sea procesado
		// El FulfillmentService debería:
		// - Cambiar el estado a ACCEPTED
		// - Crear una Delivery
		// - Asignar un courier (si hay disponible)
		
		await().atMost(10, TimeUnit.SECONDS).until(() -> {
			Order order = orderRepository.findById(orderId).orElse(null);
			return order != null && order.getStatus() == OrderStatus.ACCEPTED;
		});
		
		Order order = orderRepository.findById(orderId).orElseThrow();
		assertThat(order.getStatus()).isEqualTo(OrderStatus.ACCEPTED);
		
		// Verificar que se creó una Delivery
		Delivery delivery = deliveryRepository.findByOrderId(orderId).orElseThrow();
		assertThat(delivery).isNotNull();
		assertThat(delivery.getOrderId()).isEqualTo(orderId);
		
		// 3. Esperar a que se asigne un courier (si hay disponible)
		// Esto puede tomar un momento ya que es asíncrono
		await().atMost(10, TimeUnit.SECONDS).until(() -> {
			Delivery updatedDelivery = deliveryRepository.findByOrderId(orderId).orElse(null);
			return updatedDelivery != null && 
			       (updatedDelivery.getStatus() == DeliveryStatus.COURIER_ASSIGNED ||
			        updatedDelivery.getStatus() == DeliveryStatus.PICKED_UP ||
			        updatedDelivery.getStatus() == DeliveryStatus.IN_TRANSIT);
		});
		
		Delivery updatedDelivery = deliveryRepository.findByOrderId(orderId).orElseThrow();
		assertThat(updatedDelivery.getCourierId()).isNotNull();
		assertThat(updatedDelivery.getCourierName()).isNotNull();
		
		// 4. Verificar que el pedido avanzó a PICKED_UP o IN_TRANSIT
		Order updatedOrder = orderRepository.findById(orderId).orElseThrow();
		assertThat(updatedOrder.getStatus())
			.isIn(OrderStatus.PICKED_UP, OrderStatus.IN_TRANSIT);
	}
	
	@Test
	void testOrderStateTransitions() {
		// Crear pedido
		UUID customerId = UUID.randomUUID();
		CreateOrderRequest request = new CreateOrderRequest(
			customerId,
			"Test Address",
			BigDecimal.valueOf(100.00)
		);
		
		OrderResponse orderResponse = orderService.createOrder(request);
		UUID orderId = orderResponse.id();
		
		// Verificar estado inicial
		assertThat(orderResponse.status()).isEqualTo(OrderStatus.PLACED);
		
		// Esperar procesamiento asíncrono
		await().atMost(10, TimeUnit.SECONDS).until(() -> {
			Order order = orderRepository.findById(orderId).orElse(null);
			return order != null && order.getStatus() != OrderStatus.PLACED;
		});
		
		// Verificar que el estado cambió
		Order order = orderRepository.findById(orderId).orElseThrow();
		assertThat(order.getStatus()).isNotEqualTo(OrderStatus.PLACED);
		assertThat(order.getStatus())
			.isIn(OrderStatus.ACCEPTED, OrderStatus.PICKED_UP, OrderStatus.IN_TRANSIT);
	}
}


