-- ============================================================
-- Esquema
-- ============================================================

CREATE EXTENSION IF NOT EXISTS citext;

-- ======================
-- Tabla: clientes
-- ======================
CREATE TABLE clientes (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    email CITEXT UNIQUE NOT NULL,
    telefono VARCHAR(10) UNIQUE
);

-- ======================
-- Tabla: mesas
-- ======================
CREATE TABLE mesas (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(20) UNIQUE NOT NULL,
    capacidad INT NOT NULL CHECK (capacidad > 0),
    ubicacion VARCHAR(50)
);

-- ======================
-- Tabla: turnos
-- ======================
CREATE TABLE turnos (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(50) UNIQUE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    CHECK (hora_fin > hora_inicio)
);

-- ======================
-- Tabla: reservas
-- ======================
CREATE TABLE reservas (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL REFERENCES clientes(id) ON DELETE CASCADE,
    mesa_id BIGINT NOT NULL REFERENCES mesas(id) ON DELETE CASCADE,
    turno_id BIGINT NOT NULL REFERENCES turnos(id) ON DELETE CASCADE,
    fecha DATE NOT NULL CHECK (fecha >= CURRENT_DATE),
    comensales INT NOT NULL CHECK (comensales > 0),
    estado VARCHAR(15) NOT NULL CHECK (estado IN ('CREATED','CONFIRMED','CANCELLED','NO_SHOW','COMPLETED')),
    notas TEXT,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- Índice único parcial: evita más de una reserva activa (RN-01, RN-06)
CREATE UNIQUE INDEX ux_reserva_activa
  ON reservas(mesa_id, fecha, turno_id)
  WHERE estado IN ('CREATED','CONFIRMED');

-- ============================================================
-- TRIGGER 1: Validar comensales <= capacidad de la mesa (RN-02)
-- ============================================================

CREATE OR REPLACE FUNCTION validar_capacidad_mesa()
RETURNS TRIGGER AS $$
DECLARE
    cap INT;
BEGIN
    SELECT capacidad INTO cap FROM mesas WHERE id = NEW.mesa_id;
    IF NEW.comensales > cap THEN
        RAISE EXCEPTION 'La cantidad de comensales (%) excede la capacidad de la mesa (%)', NEW.comensales, cap;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tg_validar_capacidad_mesa
BEFORE INSERT OR UPDATE ON reservas
FOR EACH ROW
EXECUTE FUNCTION validar_capacidad_mesa();

-- ============================================================
-- TRIGGER 2: Evitar cambiar capacidad si hay reservas activas (RN-09)
-- ============================================================

CREATE OR REPLACE FUNCTION evitar_cambio_capacidad_activa()
RETURNS TRIGGER AS $$
DECLARE
    reservas_activas INT;
BEGIN
    SELECT COUNT(*) INTO reservas_activas
    FROM reservas
    WHERE mesa_id = OLD.id
      AND estado IN ('CREATED','CONFIRMED');

    IF reservas_activas > 0 AND NEW.capacidad <> OLD.capacidad THEN
        RAISE EXCEPTION 'No se puede cambiar la capacidad de una mesa con reservas activas';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tg_evitar_cambio_capacidad_activa
BEFORE UPDATE ON mesas
FOR EACH ROW
EXECUTE FUNCTION evitar_cambio_capacidad_activa();

-- ============================================================
-- TRIGGER 3: Evitar solapamiento de turnos (RN-16)
-- ============================================================

CREATE OR REPLACE FUNCTION validar_solapamiento_turnos()
RETURNS TRIGGER AS $$
DECLARE
    conflicto INT;
BEGIN
    SELECT COUNT(*) INTO conflicto
    FROM turnos
    WHERE id <> COALESCE(NEW.id, 0)
      AND (
        (NEW.hora_inicio, NEW.hora_fin) OVERLAPS (hora_inicio, hora_fin)
      );

    IF conflicto > 0 THEN
        RAISE EXCEPTION 'El turno % (% - %) se solapa con otro turno existente',
            NEW.nombre, NEW.hora_inicio, NEW.hora_fin;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tg_validar_solapamiento_turnos
BEFORE INSERT OR UPDATE ON turnos
FOR EACH ROW
EXECUTE FUNCTION validar_solapamiento_turnos();

-- ============================================================
-- TRIGGER 4: Actualizar automáticamente updated_at
-- ============================================================

CREATE OR REPLACE FUNCTION actualizar_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tg_reservas_updated
BEFORE UPDATE ON reservas
FOR EACH ROW
EXECUTE FUNCTION actualizar_updated_at();

-- ============================================================
-- FIN DEL ESQUEMA
-- ============================================================
