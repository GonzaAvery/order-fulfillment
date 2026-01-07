# Contratos de Eventos

Este directorio contiene los esquemas JSON (JSON Schema) que definen la estructura de los eventos del sistema.

## Estructura de Eventos

Todos los eventos siguen la misma estructura base:

```json
{
  "eventId": "uuid",
  "correlationId": "uuid",
  "occurredAt": "ISO 8601 timestamp",
  "eventType": "string",
  "payload": { ... }
}
```

### Campos Obligatorios

- **eventId**: Identificador único del evento para garantizar idempotencia
- **correlationId**: Identificador de correlación para trazabilidad end-to-end (generalmente el orderId)
- **occurredAt**: Timestamp ISO 8601 del momento en que ocurrió el evento
- **eventType**: Tipo del evento (OrderPlaced, OrderAccepted, etc.)
- **payload**: Contenido específico del evento según su tipo

## Eventos Disponibles

### OrderPlaced.v1.json
Emitido cuando un usuario crea un pedido.

**Payload:**
- `orderId`: UUID del pedido
- `customerId`: UUID del cliente
- `deliveryAddress`: Dirección de entrega
- `totalAmount`: Monto total del pedido

### OrderAccepted.v1.json
Emitido cuando el sistema acepta un pedido.

**Payload:**
- `orderId`: UUID del pedido aceptado

### CourierAssigned.v1.json
Emitido cuando se asigna un courier a una entrega.

**Payload:**
- `deliveryId`: UUID de la entrega
- `orderId`: UUID del pedido asociado
- `courierId`: UUID del courier asignado
- `courierName`: Nombre del courier

### OrderCompleted.v1.json
Emitido cuando un pedido se completa exitosamente.

**Payload:**
- `orderId`: UUID del pedido completado
- `deliveryId`: UUID de la entrega completada

### OrderFailed.v1.json
Emitido cuando un pedido falla.

**Payload:**
- `orderId`: UUID del pedido que falló
- `failureReason`: Razón del fallo

## Versionado

Los eventos están versionados explícitamente en el nombre del archivo (`.v1.json`). Cuando se necesite hacer cambios incompatibles, se creará una nueva versión (`.v2.json`).

## Validación

Estos schemas pueden ser usados para:
- Validar eventos antes de publicarlos
- Generar documentación automática
- Validar eventos en tests
- Generar tipos/clases en diferentes lenguajes

## Uso en el Código

Los eventos en el código Java deben cumplir con estos contratos. Los schemas sirven como documentación y contrato entre servicios.

