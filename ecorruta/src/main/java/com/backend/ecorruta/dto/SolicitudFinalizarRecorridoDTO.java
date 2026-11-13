package com.backend.ecorruta.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Datos para cerrar un recorrido.
 * Si el recorrido no se completó, las observaciones son obligatorias.
 */
public record SolicitudFinalizarRecorridoDTO(

                @NotNull(message = "Indica si el recorrido se completó") Boolean completado,

                @Size(max = 1000, message = "Las observaciones no pueden superar los 1000 caracteres") String observaciones

) {
}
