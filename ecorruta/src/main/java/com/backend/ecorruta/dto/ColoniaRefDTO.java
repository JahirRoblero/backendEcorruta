package com.backend.ecorruta.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ColoniaRefDTO(
        @NotBlank(message = "El nombre de la colonia es obligatorio")
        String nombre,
        @NotBlank(message = "El código postal es obligatorio")
        @Pattern(regexp = "\\d{5}", message = "El código postal debe tener 5 dígitos")
        String codigoPostal) {
}
