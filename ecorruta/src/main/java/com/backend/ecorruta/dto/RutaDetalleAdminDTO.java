package com.backend.ecorruta.dto;

import java.math.BigDecimal;
import java.util.List;

public record RutaDetalleAdminDTO(
        String nombre,
        String descripcion,
        BigDecimal distanciaKm,
        boolean activo,
        List<ColoniaAdminDTO> colonias) {
}