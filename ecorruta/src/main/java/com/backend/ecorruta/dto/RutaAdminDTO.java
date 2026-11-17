package com.backend.ecorruta.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record RutaAdminDTO(
        String nombre,
        String descripcion,
        BigDecimal distanciaKm,
        boolean activo,
        OffsetDateTime fechaCreacion,
        Integer totalColonias) {
}
