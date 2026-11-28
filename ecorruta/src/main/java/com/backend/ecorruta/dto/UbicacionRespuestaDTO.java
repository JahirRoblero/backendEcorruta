package com.backend.ecorruta.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Muestra de ubicación registrada.
 * Cada clic en actualizar ubicación genera una nueva muestra del recorrido.
 */
public record UbicacionRespuestaDTO(
                boolean guardada,
                Integer numeroMuestra,
                BigDecimal latitud,
                BigDecimal longitud,
                OffsetDateTime fechaHora) {
}
