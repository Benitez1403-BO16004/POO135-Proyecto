# API de Reservas de Restaurante — POO135 · Ciclo II/2025

> Proyecto para diseñar e implementar una API REST que gestione **reservas de mesas** por **fecha** y **turno** (Almuerzo/Cena), validando disponibilidad y capacidad.

- **Estado del proyecto:** Entrega 1 (diseño, sin código de backend)

---

## 🧭 Índice
- [Objetivo de la Entrega 1](#objetivo-de-la-entrega-1)
- [Alcance de la Entrega 1](#alcance-de-la-entrega-1)
- [Reglas de Negocio](#reglas-de-negocio)
- [Modelo de Dominio (UML)](#modelo-de-dominio-uml)
- [Modelo ER (PostgreSQL)](#modelo-er-postgresql)
- [Casos de Uso + Criterios de Aceptación](#casos-de-uso--criterios-de-aceptación)
- [Próximas entregas (placeholder)](#próximas-entregas-placeholder)

---

## Objetivo de la Entrega 1
Documentar el **diseño funcional y de datos** del sistema:
- Reglas de negocio claras.
- **UML** de clases del dominio.
- **ER** de base de datos (ajustado a PostgreSQL).
- Casos de uso con **criterios de aceptación**.

> No incluye código; la implementación llega en la Entrega 2.

---

## Alcance de la Entrega 1
 - Incluye crear reserva, consultar disponibilidad, confirmar/cancelar (definidos a nivel de diseño)
 - No incluye autenticación, pagos, notificaciones ni reservas por hora exacta (solo por turnos) o reportes.

---

## Reglas de Negocio
- **RN-01** Una mesa **no puede** tener **más de una reserva activa** (estados `CREATED`/`CONFIRMED`) para la **misma fecha** y **mismo turno**.
- **RN-02** `comensales ≤ capacidad` de la mesa.
- **RN-03** No se aceptan reservas con **fecha en el pasado**.
- **RN-04** Deben existir **cliente, mesa y turno**.
- **RN-05** Estados: `CREATED`, `CONFIRMED`, `CANCELLED`, `NO_SHOW`, `COMPLETED`.
- **RN-06** Solo `CREATED` o `CONFIRMED` cuentan como activas para la validación de choques.

---

## Modelo de Dominio (UML)
Diagrama:
![ER PostgreSQL](docs/Diagrama_de_Clases.png)

> PlantUML.

```plantuml
@startuml
title Diagrama de Clases - Sistema de Reservas de Restaurante

class Cliente {
  Long id
  String nombre
  String email
  String telefono
}

class Mesa {
  Long id
  String codigo
  Integer capacidad
  String ubicacion
}

class Turno {
  Long id
  String nombre
  LocalTime horaInicio
  LocalTime horaFin
}

enum EstadoReserva {
  CREATED
  CONFIRMED
  CANCELLED
  NO_SHOW
  COMPLETED
}

class Reserva {
  Long id
  LocalDate fecha
  Integer comensales
  EstadoReserva estado
  String notas
  LocalDateTime createdAt
  LocalDateTime updatedAt
}

Reserva "1" *-- "1" Cliente : cliente
Reserva "1" *-- "1" Mesa : mesa
Reserva "1" *-- "1" Turno : turno

note left of Reserva::estado
  Estado de la reserva
  según enum EstadoReserva
end note
@enduml
```

---

## Modelo ER (PostgreSQL)
Diagrama:  
![ER PostgreSQL](docs/diagrama-ER.png)

Puntos clave:
- `clientes(email)` como **CITEXT UNIQUE** (case-insensitive).
- `mesas(codigo)` **UNIQUE**.
- `turnos(nombre)` **UNIQUE**; `hora_fin > hora_inicio`.
- `reservas` con `fecha: DATE`, `created_at/updated_at: TIMESTAMPTZ`.

**Índice único parcial (disponibilidad):**
```sql
CREATE UNIQUE INDEX ux_reserva_activa
  ON reservas(mesa_id, fecha, turno_id)
  WHERE estado IN ('CREATED','CONFIRMED');
```

---

## Casos de Uso + Criterios de Aceptación

### CU-001 Crear Reserva
**Entrada:** `clienteId, mesaId, turnoId, fecha, comensales, notas?`  
**Flujo:** validar existencia (RN-04) → fecha (RN-03) → capacidad (RN-02) → disponibilidad (RN-01) → crear `CREATED`.  
**Errores:** `404` inexistentes, `400` fecha/capacidad, `409` mesa ocupada.

### CU-002 Consultar Disponibilidad
**Entrada:** `fecha, turnoId, capacidadMin?`  
**Flujo:** listar mesas **sin** reservas activas para `(fecha, turno)`; filtrar por `capacidadMin`.

### CU-003 Confirmar / Cancelar
**Entrada:** `reservaId`  
**Flujo:** buscar → actualizar `estado` (`CONFIRMED`/`CANCELLED`) → `updatedAt`.

### Casos de Aplicación (CA)

- **CA-01:** Dado una mesa de capacidad 4 y un turno Cena el 2025-10-01 sin reservas, cuando creo una reserva por 4 comensales para esa mesa, fecha y turno, entonces la reserva se crea en `CREATED`.
- **CA-02:** Dado una reserva activa existente para Mesa M-01, 2025-10-01, Turno Cena, cuando intento crear otra para los mismos (mesa, fecha, turno), entonces recibo **409 Conflict** y no se crea una segunda reserva.
- **CA-03:** Dado una mesa de capacidad 2, cuando intento reservar 3 comensales, entonces recibo **400 Bad Request** por capacidad excedida.
- **CA-04:** Dado la fecha de ayer, cuando intento reservar para ayer, entonces recibo **400 Bad Request** por fecha en el pasado.
- **CA-05:** Dado fecha 2025-10-01 y turno Cena, cuando consulto disponibilidad, entonces veo **todas las mesas sin reserva activa** para ese (fecha, turno).

---

## Próximas entregas (placeholder)
- **Entrega 2 (Implementación):** Spring Boot + JPA + Flyway + controladores + validaciones + Postman.  
- **Entrega 3 (Pruebas y robustez):** Manejo de errores global, tests JUnit/WebMvcTest, documentación final.
