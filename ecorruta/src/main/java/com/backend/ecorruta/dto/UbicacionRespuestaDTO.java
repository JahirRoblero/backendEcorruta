package com.backend.ecorruta.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Muestra de ubicación registrada.
 * guardada = false cuando la posición no cambió lo suficiente y no se almacenó (RNF10).
 */
public record UbicacionRespuestaDTO(
                boolean guardada,
                Integer numeroMuestra,
                BigDecimal latitud,
                BigDecimal longitud,
                OffsetDateTime fechaHora) {
}
