package com.backend.ecorruta.dto;

import java.time.OffsetDateTime;

public record OperadorAdminDTO(
        String correo,
        String nombre,
        String apellidoPaterno,
        String apellidoMaterno,
        boolean activo,
        OffsetDateTime fechaRegistro) {
}
