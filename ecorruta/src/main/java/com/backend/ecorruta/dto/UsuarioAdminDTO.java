package com.backend.ecorruta.dto;

import java.time.OffsetDateTime;

public record UsuarioAdminDTO(
        String correo,
        String nombre,
        String apellidoPaterno,
        String apellidoMaterno,
        String rol,
        boolean activo,
        String coloniaNombre,
        String coloniaCodigoPostal,
        OffsetDateTime fechaRegistro) {
}