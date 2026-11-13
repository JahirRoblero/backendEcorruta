package com.backend.ecorruta.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.*;

/**
 * Posición GPS enviada por el operador durante un recorrido.
 */
public record SolicitudUbicacionDTO(

                @NotNull(message = "La latitud es obligatoria") @DecimalMin(value = "-90.0", message = "La latitud mínima es -90") @DecimalMax(value = "90.0", message = "La latitud máxima es 90") BigDecimal latitud,

                @NotNull(message = "La longitud es obligatoria") @DecimalMin(value = "-180.0", message = "La longitud mínima es -180") @DecimalMax(value = "180.0", message = "La longitud máxima es 180") BigDecimal longitud

) {
}
