package com.backend.ecorruta.dto;

import jakarta.validation.constraints.NotBlank;

public record SolicitudCambioEstadoDTO(
        @NotBlank(message = "El estado es obligatorio") String estado) {
}
