# Order Fulfillment and Delivery Tracking Platform

## Descripción General

Este proyecto implementa el backend de una plataforma de delivery enfocada en order fulfillment, inspirada en sistemas de producto masivo como los de food delivery a gran escala. El objetivo principal es modelar y resolver problemas reales de sistemas críticos, donde la confiabilidad, la consistencia y la experiencia del usuario son prioritarias.

La plataforma cubre todo el ciclo de vida de un pedido, desde su creación hasta su entrega (o fallo), poniendo especial énfasis en el manejo de estados complejos, eventos, reintentos y trazabilidad.

## Arquitectura

### Estilo Arquitectónico

- **Arquitectura de microservicios**: Separación clara de responsabilidades por dominio
- **Comunicación asincrónica**: Basada en eventos usando Apache Kafka
- **Event-Driven Architecture**: Desacoplamiento mediante eventos de dominio

### Servicios Principales

1. **Order Service** (`com.delivery.fulfillment.order`)
   - Creación y consulta de pedidos
   - Gestión del estado del pedido
   - API REST: `/api/orders`

2. **Fulfillment Service** (`com.delivery.fulfillment.fulfillment`)
   - Orquestación del flujo completo del pedido
   - Consume eventos y coordina acciones entre servicios
   - Maneja transiciones de estado complejas

3. **Delivery Service** (`com.delivery.fulfillment.delivery`)
   - Gestión de entregas y sus estados
   - Asignación de couriers
   - API REST: `/api/deliveries`

4. **Notification Service** (`com.delivery.fulfillment.notification`)
   - Simulación de feedback al usuario
   - Consume eventos y genera notificaciones
   - En producción, integraría con servicios de email/SMS/push

### Entidades Principales

- **Order**: Representa el pedido realizado por el usuario
- **Delivery**: Representa la ejecución logística del pedido
- **Courier**: Actor responsable de realizar la entrega

## Flujo de Estados

### Estados del Pedido (OrderStatus)

```
PLACED → ACCEPTED → PICKED_UP → IN_TRANSIT → DELIVERED
   ↓         ↓          ↓            ↓
CANCELED  CANCELED    FAILED      FAILED
   ↓         ↓
 FAILED    FAILED
```

**Transiciones válidas:**
- `PLACED` puede transicionar a: `ACCEPTED`, `CANCELED`, `FAILED`
- `ACCEPTED` puede transicionar a: `PICKED_UP`, `CANCELED`, `FAILED`
- `PICKED_UP` puede transicionar a: `IN_TRANSIT`, `FAILED`
- `IN_TRANSIT` puede transicionar a: `DELIVERED`, `FAILED`
- Estados terminales: `DELIVERED`, `CANCELED`, `FAILED`

### Estados de la Entrega (DeliveryStatus)

```
PENDING → COURIER_ASSIGNED → PICKED_UP → IN_TRANSIT → DELIVERED
   ↓            ↓               ↓            ↓
 FAILED       FAILED          FAILED       FAILED
```

## Eventos de Dominio

El sistema se basa en eventos de dominio para desacoplar servicios:

- **OrderPlaced**: Emitido cuando un usuario crea un pedido
- **OrderAccepted**: Emitido cuando el sistema acepta un pedido
- **CourierAssigned**: Emitido cuando se asigna un courier a una entrega
- **DeliveryDelayed**: Emitido cuando una entrega se retrasa
- **OrderCompleted**: Emitido cuando un pedido se completa exitosamente
- **OrderFailed**: Emitido cuando un pedido falla

Todos los eventos incluyen:
- **EventId**: Identificador único para idempotencia
- **CorrelationId**: Identificador de correlación para trazabilidad end-to-end
- **OccurredAt**: Timestamp del evento

## Confiabilidad y Resiliencia

### Idempotencia

- Los eventos incluyen un `EventId` único
- El sistema mantiene una tabla `processed_events` en PostgreSQL con constraint único
- Los eventos duplicados se detectan y se ignoran automáticamente
- La idempotencia es persistente y sobrevive reinicios de la aplicación

### Trazabilidad

- **CorrelationId**: Permite trazar el flujo completo de un pedido a través de todos los servicios
- Todos los eventos están correlacionados con el pedido original
- Logs estructurados incluyen correlationId para debugging

### Manejo de Errores

- Los errores en el procesamiento de eventos se registran
- En producción, eventos fallidos después de N reintentos irían a Dead Letter Queue (DLQ)
- El sistema marca pedidos como `FAILED` cuando no puede procesarlos

### Retries

- Kafka está configurado con `retries: 3` para el producer
- El consumer usa `enable-auto-commit: false` para control manual de commits
- En producción, implementar exponential backoff para reintentos

## Tecnologías Utilizadas

- **Java 21**: Lenguaje de programación
- **Spring Boot 3.5.9**: Framework principal
- **Spring Data JPA**: Persistencia de datos
- **PostgreSQL**: Base de datos relacional
- **Apache Kafka**: Broker de mensajería para eventos
- **Spring Kafka**: Integración con Kafka
- **Jackson**: Serialización/deserialización JSON

## Configuración

### Requisitos Previos

- Java 21
- Maven 3.6+
- PostgreSQL 12+
- Apache Kafka 2.8+ (o usar Docker Compose)

### Variables de Entorno

El archivo `application.yaml` contiene la configuración. Ajusta según tu entorno:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/order_fulfillment
    username: postgres
    password: postgres
  
  kafka:
    bootstrap-servers: localhost:9092
```

### Setup de Base de Datos

La base de datos se crea automáticamente con Docker Compose. Si usas PostgreSQL local:

```sql
CREATE DATABASE order_fulfillment;
```

### Setup de Kafka

Los topics se crean automáticamente al iniciar la aplicación:
- `order-events`: Topic principal para eventos (3 particiones)
- `order-events-dlq`: Dead Letter Queue (preparado para uso futuro)

### Contratos de Eventos

Los esquemas JSON de los eventos están documentados en `/contracts/events`:
- `OrderPlaced.v1.json`
- `OrderAccepted.v1.json`
- `CourierAssigned.v1.json`
- `OrderCompleted.v1.json`
- `OrderFailed.v1.json`

## Quick Start

Para levantar el sistema completo, consulta [QUICK_START.md](QUICK_START.md).

### Resumen Rápido

1. **Levantar infraestructura**:
   ```bash
   docker-compose up -d
   ```

2. **Levantar la aplicación**:
   ```bash
   mvn spring-boot:run
   ```

3. **Verificar health**:
   ```bash
   curl http://localhost:8080/actuator/health
   ```

## Uso

### Crear un Pedido

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "550e8400-e29b-41d4-a716-446655440000",
    "deliveryAddress": "Av. Corrientes 1234, CABA",
    "totalAmount": 1500.50
  }'
```

### Consultar un Pedido

```bash
curl http://localhost:8080/api/orders/{orderId}
```

### Consultar Métricas

```bash
# Ver todas las métricas
curl http://localhost:8080/actuator/metrics

# Ver métrica específica
curl http://localhost:8080/actuator/metrics/orders_created_total
curl http://localhost:8080/actuator/metrics/events_processed_total
curl http://localhost:8080/actuator/metrics/events_failed_total

# Métricas en formato Prometheus
curl http://localhost:8080/actuator/prometheus
```

### Completar una Entrega

```bash
curl -X POST http://localhost:8080/api/deliveries/{deliveryId}/complete
```

### Ver Eventos en Kafka UI

Abre `http://localhost:8081` en tu navegador para explorar topics y mensajes.

## Flujo Completo de un Pedido

1. **Usuario crea pedido** → `POST /api/orders`
   - Se crea `Order` con estado `PLACED`
   - Se publica evento `OrderPlaced`

2. **Fulfillment Service procesa OrderPlaced**
   - Cambia estado a `ACCEPTED`
   - Publica evento `OrderAccepted`
   - Crea `Delivery` con estado `PENDING`
   - Asigna courier disponible (si hay)
   - Publica evento `CourierAssigned` (si se asignó)

3. **Fulfillment Service procesa CourierAssigned**
   - Cambia estado del pedido a `PICKED_UP` y luego a `IN_TRANSIT`
   - Actualiza estado de la entrega

4. **Courier completa entrega** → `POST /api/deliveries/{id}/complete`
   - Cambia estado del pedido a `DELIVERED`
   - Cambia estado de la entrega a `DELIVERED`
   - Publica evento `OrderCompleted`

5. **Notification Service** consume todos los eventos y genera notificaciones (simuladas en logs)

## Decisiones de Diseño

### 1. Monolito Modular vs Microservicios Reales

**Decisión**: Implementar como monolito modular con separación clara de dominios.

**Razón**: Para un MVP y demostración de conceptos, un monolito modular permite:
- Desarrollo más rápido
- Testing más simple
- Misma separación de responsabilidades que microservicios
- Fácil migración a microservicios reales después

### 2. Event-Driven con Kafka

**Decisión**: Usar Kafka para comunicación asíncrona entre servicios.

**Razón**: 
- Desacoplamiento real entre servicios
- Escalabilidad horizontal
- Replay de eventos para debugging
- Preparado para sistemas distribuidos reales

### 3. Máquina de Estados Explícita

**Decisión**: Validar transiciones de estado en las entidades.

**Razón**:
- Previene estados inconsistentes
- Documenta claramente el flujo válido
- Facilita debugging y testing
- Garantiza consistencia de datos

### 4. CorrelationId para Trazabilidad

**Decisión**: Usar CorrelationId en todos los eventos.

**Razón**:
- Permite trazar el flujo completo de un pedido
- Esencial para debugging en sistemas distribuidos
- Facilita observabilidad y monitoreo

### 5. Idempotencia con EventId

**Decisión**: Cada evento tiene un EventId único y se verifica antes de procesar.

**Razón**:
- Previene procesamiento duplicado
- Crítico en sistemas distribuidos donde eventos pueden duplicarse
- En producción, usar Redis o base de datos para persistir eventos procesados

## Estado de Implementación

### ✅ Completado

1. **Levante local reproducible**: Docker Compose con PostgreSQL, Kafka y Kafka UI
2. **Dominio funcionando end-to-end**: Order, Delivery, Courier con validación de transiciones
3. **Contrato de eventos formalizado**: JSON Schemas versionados en `/contracts/events`
4. **Kafka: publicación y consumo real**: Sistema event-driven completamente funcional
5. **Idempotencia persistente**: Tabla `processed_events` con constraint único
6. **Observabilidad visible**: 
   - Logs estructurados con CorrelationId
   - Métricas custom: `orders_created_total`, `events_processed_total`, `events_failed_total`
   - Actuator habilitado con health, metrics y prometheus
7. **Tests del sistema**: 
   - Unit tests para validación de transiciones de estado
   - Integration tests con Testcontainers (Postgres + Kafka)
   - Test end-to-end del flujo completo

### 🚀 Próximos Pasos (Mejoras Futuras)

1. **Dead Letter Queue (DLQ)**: Implementar routing de eventos fallidos a DLQ después de N reintentos
2. **Retry con Exponential Backoff**: Implementar estrategia de reintentos más sofisticada
3. **Saga Pattern**: Para transacciones distribuidas complejas
4. **CQRS**: Separar comandos y consultas para mejor escalabilidad
5. **Event Sourcing**: Para auditoría completa y replay de eventos
6. **Circuit Breaker**: Para resiliencia ante fallas de servicios externos
7. **Distributed Tracing**: Integrar con Zipkin/Jaeger para trazabilidad completa
8. **Métricas avanzadas**: Prometheus + Grafana para dashboards de monitoreo
9. **API Documentation**: OpenAPI/Swagger para documentación de APIs
10. **Performance Testing**: Tests de carga y stress

## Estructura del Proyecto

```
order-fulfillment/
├── contracts/           # Contratos de eventos (JSON Schemas)
│   └── events/         # Esquemas versionados de eventos
├── src/main/java/com/delivery/fulfillment/
│   ├── common/              # Código compartido
│   │   ├── config/         # Configuraciones (Kafka, Jackson, etc.)
│   │   ├── events/         # Eventos de dominio base y consumidores
│   │   └── observability/  # Métricas, logging, etc.
│   ├── order/              # Order Service
│   │   ├── controller/     # REST endpoints
│   │   ├── domain/         # Entidades y value objects
│   │   ├── dto/            # Data Transfer Objects
│   │   ├── events/         # Eventos de dominio del pedido
│   │   ├── repository/     # Repositorios JPA
│   │   └── service/        # Lógica de negocio
│   ├── delivery/           # Delivery Service
│   │   ├── controller/     # REST endpoints
│   │   ├── domain/         # Entidades
│   │   ├── events/         # Eventos de dominio de entregas
│   │   ├── repository/     # Repositorios JPA
│   │   └── service/        # Lógica de negocio
│   ├── fulfillment/        # Fulfillment Service
│   │   └── service/        # Orquestación del flujo
│   └── notification/       # Notification Service
│       └── service/        # Generación de notificaciones
└── src/test/java/          # Tests unitarios e integración
    ├── com/delivery/fulfillment/
    │   ├── order/domain/    # Tests de dominio
    │   └── integration/    # Tests de integración con Testcontainers
```

## Licencia

Este proyecto es para fines educativos y demostración.

