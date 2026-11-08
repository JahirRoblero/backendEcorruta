
package com.backend.ecorruta.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.*;

public record SolicitudCrearReporteDTO(

                @NotBlank(message = "La colonia es obligatoria") @Size(max = 100) String coloniaNombre,

                @NotBlank(message = "El código postal es obligatorio") @Pattern(regexp = "\\d{5}", message = "El código postal debe tener 5 dígitos") String coloniaCodigoPostal,

                @NotBlank(message = "El título es obligatorio") @Size(max = 60) String titulo,

                @NotBlank(message = "La descripción es obligatoria") String descripcion,

                @DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal latitud,

                @DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal longitud

) {
}
