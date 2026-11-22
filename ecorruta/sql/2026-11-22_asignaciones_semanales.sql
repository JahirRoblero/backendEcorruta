-- Ejecutar UNA VEZ en Neon antes de desplegar el nuevo backend.
-- No borra ejecuciones históricas ni ubicaciones GPS.
BEGIN;

CREATE TABLE IF NOT EXISTS asignacion_operador (
    id_asignacion BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    correo_operador VARCHAR(254) NOT NULL REFERENCES usuario(correo),
    ruta_nombre VARCHAR(60) NOT NULL REFERENCES ruta(nombre),
    placa_vehiculo VARCHAR(15) NOT NULL REFERENCES vehiculo(placa),
    -- Lunes=1, martes=2, miércoles=4, jueves=8, viernes=16, sábado=32, domingo=64.
    -- Se suman los bits de los días seleccionados.
    dias_semana_mask INTEGER NOT NULL CHECK (dias_semana_mask BETWEEN 1 AND 127),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Cada conductor mantiene una asignación activa; puede recibir otra al desactivar la anterior.
CREATE UNIQUE INDEX IF NOT EXISTS uq_asignacion_conductor_activa
    ON asignacion_operador (correo_operador) WHERE activo = TRUE;

ALTER TABLE ejecucion_ruta ADD COLUMN IF NOT EXISTS asignacion_id BIGINT
    REFERENCES asignacion_operador(id_asignacion);

-- Evita volver a iniciar una misma asignación dos veces en un mismo día.
-- Los recorridos anteriores tienen NULL y se conservan.
CREATE UNIQUE INDEX IF NOT EXISTS uq_ejecucion_asignacion_fecha
    ON ejecucion_ruta (asignacion_id, fecha_programada)
    WHERE asignacion_id IS NOT NULL;

COMMIT;
