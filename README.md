# API de Reservas de Restaurante — POO135 · Ciclo II/2025

> Proyecto para diseñar e implementar una API REST que gestione **reservas de mesas** por **fecha** y **turno** (turnos por hora), validando disponibilidad y capacidad.

- **Estado del proyecto:** Entrega 1 (diseño, sin código de backend)
Tutor GT03:
Ing. Francisco Javier Morales Ayala

Integrantes

- Emerson Arístides Alam Álvarez Figueroa AF97011 
- Héctor Danilo Benítez Ortéz BO16004
- Samuel Timoteo Cortez Hernández CH21024
- Rodrigo Ernesto García Portillo GP24005
- Marlon Alexis Núñez Ramos NR24002
- Jonás Eduardo Villalobos Moran VM24042


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

Reserva "n" *-- "1" Cliente : cliente
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
**Flujo:** Validar `turnoId` → listar mesas **sin** reservas activas para `(fecha, turno)`(RN-01/06) → filtrar por `capacidadMin` → Devolver lista.
**Errores:** `404` Inexistente.

### CU-003 Confirmar / Cancelar
**Entrada:** `reservaId`  
**Flujo:** buscar → actualizar `estado` (`CONFIRMED`/`CANCELLED`) → `updatedAt`.
**Errores:** `404` Inexistente, `422` Transición inválida.

### CU-004 Marcar No-Show
**Entrada:** `reservaUD`
**Flujo:** Verificar `estado`(`CONFIRMED`) → disponibilidad → Actualizar `estado`(`NO_SHOW`) → `updatedAt`.
**Errores:** `404` Inexistente, `422` Transición inválida.

### CU-005 Completar reserva
**Entrada:** `reservaUD`
**Flujo:** Validar atencion → actualizar `estado` (`COMPLETED`) → `updatedAt`.
**Errores:** `404` Inexistente, `422` Transición inválida.

### CU-006 Reprogramar Reserva
**Entrada:** `reservaId, nuevaFecha, nuevoTurnoId, nuevaMesaId`  
**Flujo:** Verificar existencia `reservaId` → Verificar existencia de recursos (RN-04) → Validar fecha (RN-03) → capacidad (RN-02) → disponibilidad (RN-06) → `updatedAt`.
**Errores:** `404` Inexistente,  `400` fecha/capacidad, `409` mesa ocupada.

### CU-007 Ver Detalle de Reserva
**Entrada:** `reservaId`
**Flujo:** Buscar `reservaId` → Devolver `cliente, mesa, turno, fecha, comensales, estado, notas`
**Errores:** `404` Inexistente.

### CU-008 Listar Reservas
**Entrada:** `fecha, turnoId`
**Flujo:** Buscar por `fecha, turnoId` → Aplicar filtros y orden Desc → Devolver pagina con totales.
**Errores:** `404` Inexistente.

### CU-009 Eliminar Reserva 
**Entrada:** `reservaId`
**Flujo:** Verificar `estado`(`CREATED`) → actualizar `estado` (`CANCELLED`) → Eliminar
**Errores:** `404` Inexistente, `422` Transición inválida.

### CU-010 Registrar Cliente
**Entrada:** `nombre, telefono, email`
**Flujo:** Validar formato `telefono, email` → Verificar unicidad `telefono`(Unico) → Crear cliente → Devolver `clienteId`.
**Errores:** `400` Datos invalidos, `409` Dato duplicado.

### CU-011 Actualizar Cliente 
**Entrada:** `clienteId, nombre, telefono, email`
**Flujo:** Verificar existencia `clienteId` → Validar nuevos datos → Verificar unicidad `telefono` → Guardar cambios `updatedAt` 
**Errores:** `400` Datos invalidos, `409` Dato duplicado.

### CU-011 Eliminar Cliente
**Entrada:** `clienteId`
**Flujo:** Verificar `clienteId` → Verificar reservas activas → Eliminar
**Errores:** `400` Datos invalidos, `409` Conflicto de estado.

### CU-012 Lista Clientes
**Entrada:** `clienteId`
**Flujo:** Verificar `clienteId` → Ordear alfabeticamente desc → Devolver resultados
**Errores:** `400` Datos invalidos.

### CU-013 Registrar Mesa
**Entrada:** `codigo, capacidad, ubicacion?`
**Flujo:** Validar unicidad `codigo` → Crear mesa
**Errores:** `400` Capacidad,  `409` Duplicado.

### CU-014 Actualizar Mesa
**Entrada:** `mesaId, capacidad, ubicacion?`
**Flujo:** Validar existencia `mesaId` → Verificar capacidad/ afeccion a reservas → Guardar cambios `updatedAt`. 
**Errores:** `404` Inexistente, `409` Capacidad invalida.

### CU-015 Eliminar Mesa
**Entrada:** `mesaId`
**Flujo:** Buscar `mesaId` → Verificar referencia a reservas → Eliminar
**Errores:** `404` Inexistente, `409` Conflicto por referencias.

### CU-016 Listar Mesa
**Entrada:** `capacidadMin?, activa?`
**Flujo:** Listar mesas → Aplicar filtros → Devolver paginado
**Errores:** `404` Inexistente.

### CU-017 Registrar Turno
**Entrada:** `nombre, horaInicio, horaFin`
**Flujo:** Validar rango horario → Verificar que no haya conflicto → Crear turno
**Errores:** `400` Datos invalidos,  `409` Conflicto.

### CU-018 Actualizar Turno
**Entrada:** `turnoId, nombre?, horaInicio?, horaFin?`
**Flujo:** Validar existencia `turnoId` → Validar nuevo rango → Guardar cambios `updatedAt`.
**Errores:** `400` Rango invalido, `409` Conflicto.

### CU-019 Desactivar Turno
**Entrada:** `turnoId`
**Flujo:** Buscar `turnoId` → Comprobar conflictos con reservas → Eliminar Turno
**Errores:** `404` Inexistente, `409` Conflicto.

### CU-020 Listar Turno
**Entrada:** `turnoId,soloActivos?`
**Flujo:** Buscar por `turnoId` → Filtrar solo activos → Devolver turnos 
**Errores:** `404` Inexistente. 


### Casos de Aplicación (CA)

- **CA-01:** Dado una mesa de capacidad 4 y un turno 8:00 - 8:59 el 2025-10-01 sin reservas, cuando creo una reserva por 4 comensales para esa mesa, fecha y turno, entonces la reserva se crea en `CREATED`.
- **CA-02:** Dado una reserva activa existente para la mesa con `Mesa M-01`, 2025-10-01, Turno 9:00 - 9:59 , cuando intento crear otra para los mismos (mesa, fecha, turno), entonces recibo **409 Conflict** y no se crea una segunda reserva.
- **CA-03:** Dado una mesa de capacidad 2, cuando intento reservar 3 comensales, entonces recibo **400 Bad Request** por capacidad excedida.
- **CA-04:** Dado la fecha de ayer, cuando intento reservar para ayer, entonces recibo **400 Bad Request** por fecha en el pasado.
- **CA-05:** Dado fecha 2025-10-01 y turno 11:00 - 11:59, cuando consulto disponibilidad, entonces veo **todas las mesas sin reserva activa** para ese (fecha, turno).
- **CA-06:** Dado que `clienteId = 9999`, `mesaId = 9999` o `turnoId = 9999` no existen, cuando intento crear una reserva, recibo **404 Not Found** detallando la entidad y la reserva no se crea.
- **CA-07:** Dada una reserva existente con estado `CREATED`, al realizar una solicitud para confirmarla (`CONFIRMED`) o cancelarla (`CANCELLED`) su estado cambia al estado solicitado (`CONFIRMED/CANCELLED`)
- **CA-08:** Dada una reserva en estado `CANCELLED`, al intentar confirmarla (Actualizarla a CONFIRMED),  entonces recibo **400 Bad Request** o **422 Unprocessable Entity**
- **CA-09:** Dada una reserva `CONFIRMED` para la mesa con `id = 05`, Turno 9:00 - 9:59, Fecha 2025-11-15, al reprogramar para Turno 8:00 - 8:59, Fecha 2025-11-15 (Con Turno disponible = True), la reserva se actualiza correctamente.
- **CA-10:** Dada una reserva `CONFIRMED` para la mesa  `Mesa M-01`, Turno 11:00 - 11:59, Fecha 2025-11-15, al reprogramar otra reserva (O a si misma) para los mismos valores, entonces recibo **409 Conclict** y la reprogramacion no se realiza. 
- **CA-11:** Dada una resreva para 4 comensales para la mesa `Mesa M-03` con Capacidad de 5 personas, cuando intento cambiar a la mesa `Mesa M-06` con Capacidad para 3 personas, entonces recibo **400 Bad Request** por capacidad insuficiente.
- **CA-12:** Dada una reserva en estado `CREATED` para `reservaId = 124`, al solicitar su eliminacion, entonces la reserva se elimina exitosamente.
- **CA-13:** Dada una reserva en estado `CONFIRMED` para `reservaId = 574`, cuando solicito su eliminacion, entonces recibo **409 Conflict** o **422 Unprocessable Entity** y está no se elimina.
- **CA-14:** Dado que la mesa con  `Mesa M-01` existe, al intentar crear otra mesa con el mismo código, entonces recibo **409 Conflict** y la nueva mesa no se crea.
- **CA-15:** Dada la mesa `Mesa M-16` con capacidad 5 y con una reserva activa para 5 comensales, cuando intento modificar la capacidad de `M-16` a 4, recibo **409 Conflict** y la capacidad no cambia.
- **CA-16:** Dada la mesa `Mesa M-19` con capacidad 3 y su reserva activa maxima de 3 comensales, cuando intento modificar la capacidad de `M-19`a 4, entonces la capacidad se actualiza.
- **CA-17:** Dada la mesa `Mesa M-81` al no tener reservas activas, cuando se solicita su eliminacion, entonces la mesa es eliminada con exito. De lo contrario recibo **409 Conflict** y la mesa no se elimina.
- **CA-18:** Dado el `Cliente C-154` ya registrado con `télefono: 8457-5132`, al intentar crear un nuevo `Cliente C-155` con el mismo télefono, recibo **409 Conflict** y el nuevo cliente no se registra.
- **CA-19:** Dado el `Cliente C-54` con `télefono: 5216-5421`, al intentar actualizar el telefono a un valor ya utilizado por con `C-71` entonces recibo **409 Conflict** y el télefono de `C-54` no cambia.
- **CA-20:** Dado el `Cliente C-62` que tiene una reserva activa (`CONFIRMED`), cuando intento eliminarlo, recibo **409 Conflict** y el cliente no es eliminado.
- **CA-21:** Dado el `Cliente C-73` que tiene solo reservas historicas (`COMPLETED`), al solicitar su desactivacion, entonces la operacion se realiza con exito.
- **CA-22:** Dado que un turno llamado "Tarde" no existe, al crear un turno con `nombre:Tarde, horaInicio: 14:00, horaFin: 18:00`, entonces el turno se crea con exito.
- **CA-23:** Dado que `Turno Almuerzo` existe (`12:00 - 14:00`) cuando intento crear un nuevo turno con `nombre: Almuerzo, horaInicio: 12:00`, entonces recibo **409 Conflict** por nombre duplicado.
- **CA-24:** Cuando intento crear un turno con `horaInicio: 20:00, horaFin: 19:00`, entonces recibo **400 Bad Request** por rango horario inválido.
- **CA-25:** Dado `Turno X` (`17:00 - 19:00`), cuanto intenro crear `Turno Y` (`18:00 - 20:00`), que solapa al existente, entonces recibo **409 Conflict** por solapamiento de horarios y el turno no se crea.
- **CA-26:** Dado el `Turno Cena`(`19:00 - 23:00`), cuando lo actualizo a `horaFin: 22:00` (manteniendo un rango valido), entonces se actualiza con exito.
- **CA-27:**  Dado el `Turno Cena`(`19:00 - 23:00`) y el `Turno Noche`(`23:00 - 01:00`), cuando actualizo `Turno Cena` a `horaFin: 23:30`, entonces recibo **409 Conflict** (Por solapamiento) y la actualizacion no se realiza.
- **CA-28:** Dado un `Turno Noche` que no tiene reservas activas o futuras que lo referencien, cuando se solicita su eliminacion, entonces la operacion sucede con exito.
- **CA-29:** Dado un `Turno Noche` inexistente, cuando el usuario intenta crear una reserva para ese turno, entonces la operacion es rechazada y recibo **404 Not Found** o **409 Conflict**.
- **CA-30:** Dada la lista completa de turnos (activos e inactivos), cuando listo los turnos con el filtro agrupar por `horaInicio = desc`, recibo la lista de turnos de forma descendente.

---

## Próximas entregas (placeholder)
- **Entrega 2 (Implementación):** Spring Boot + JPA + Flyway + controladores + validaciones + Postman.  
- **Entrega 3 (Pruebas y robustez):** Manejo de errores global, tests JUnit/WebMvcTest, documentación final.
