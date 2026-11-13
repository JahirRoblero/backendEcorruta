package com.backend.ecorruta.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.*;

/**
 * Datos de una incidencia registrada por el operador en campo.
 */
public record SolicitudCrearIncidenciaDTO(

                @NotBlank(message = "El título es obligatorio") @Size(max = 60, message = "El título no puede superar los 60 caracteres") String titulo,

                @NotBlank(message = "La descripción es obligatoria") String descripcion,

                @DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal latitud,

                @DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal longitud

) {
}
