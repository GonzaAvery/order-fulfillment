# Quick Start Guide

Este guía te permite levantar el sistema completo en minutos.

## Prerrequisitos

- Docker y Docker Compose instalados
- Java 21
- Maven 3.6+

## Pasos para levantar el sistema

### 1. Levantar infraestructura (PostgreSQL + Kafka)

```bash
docker-compose up -d
```

Esto levanta:
- **PostgreSQL** en puerto `5432`
- **Zookeeper** en puerto `2181`
- **Kafka** en puerto `9092`
- **Kafka UI** en puerto `8081` (opcional, para visualizar topics y mensajes)

Verifica que los servicios estén listos:

```bash
docker-compose ps
```

Todos los servicios deben estar en estado "Up" y saludables.

### 2. Levantar la aplicación

```bash
mvn spring-boot:run
```

La aplicación se levantará en `http://localhost:8080`

### 3. Verificar que todo funciona

#### Health Check

```bash
curl http://localhost:8080/actuator/health
```

#### Crear un pedido

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "550e8400-e29b-41d4-a716-446655440000",
    "deliveryAddress": "Av. Corrientes 1234, CABA",
    "totalAmount": 1500.50
  }'
```

#### Consultar el pedido

```bash
curl http://localhost:8080/api/orders/{orderId}
```

Reemplaza `{orderId}` con el ID retornado en el paso anterior.

#### Verificar eventos en Kafka UI

Abre tu navegador en `http://localhost:8081` y navega a:
- Topics → `order-events` → Messages
- Deberías ver los eventos publicados

## Detener el sistema

```bash
# Detener la aplicación (Ctrl+C en la terminal donde corre)

# Detener infraestructura
docker-compose down

# Detener y eliminar volúmenes (limpiar datos)
docker-compose down -v
```

## Troubleshooting

### Kafka no está disponible

Si ves errores de conexión a Kafka:
1. Verifica que Kafka esté corriendo: `docker-compose ps`
2. Espera unos segundos para que Kafka termine de inicializar
3. Revisa logs: `docker-compose logs kafka`

### PostgreSQL no está disponible

Si ves errores de conexión a PostgreSQL:
1. Verifica que PostgreSQL esté corriendo: `docker-compose ps`
2. Verifica que la base de datos exista: `docker-compose exec postgres psql -U postgres -l`
3. Si no existe, créala: `docker-compose exec postgres psql -U postgres -c "CREATE DATABASE order_fulfillment;"`

### La aplicación no arranca

1. Verifica que Java 21 esté instalado: `java -version`
2. Verifica que Maven esté instalado: `mvn -version`
3. Revisa los logs de la aplicación para errores específicos

## Próximos pasos

Una vez que el sistema esté corriendo, puedes:
- Explorar la API en `http://localhost:8080/actuator`
- Ver métricas en `http://localhost:8080/actuator/metrics`
- Explorar eventos en Kafka UI en `http://localhost:8081`
- Revisar los logs estructurados con `correlationId` para seguir el flujo de un pedido

