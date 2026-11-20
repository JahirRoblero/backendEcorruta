package com.backend.ecorruta.dto;

import java.math.BigDecimal;

public record VehiculoAdminDTO(
        String placa,
        String numeroEconomico,
        String marca,
        String modelo,
        BigDecimal capacidadKg,
        String estado,
        boolean activo) {
}
