package com.backend.ecorruta.dto;

import jakarta.validation.constraints.NotBlank;

public record SolicitudCambioRolDTO(
        @NotBlank(message = "El rol es obligatorio") String rol) {
}