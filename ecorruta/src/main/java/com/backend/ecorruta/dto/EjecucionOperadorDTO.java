package com.backend.ecorruta.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Datos de una ejecución de ruta asignada al operador (conductor).
 */
public record EjecucionOperadorDTO(
                Integer folioEjecucion,
                String rutaNombre,
                String rutaDescripcion,
                String geometria,
                String placaVehiculo,
                String numeroEconomico,
                LocalDate fechaProgramada,
                OffsetDateTime fechaInicio,
                OffsetDateTime fechaFin,
                String estado,
                String observaciones) {
}
