package com.backend.ecorruta.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ReporteAdminDTO(
        Integer idReporte,
        String titulo,
        String descripcion,
        String imagenUrl,
        String estado,
        BigDecimal latitud,
        BigDecimal longitud,
        OffsetDateTime fechaReporte,
        OffsetDateTime fechaAtencion,
        String autorNombre,
        String autorCorreo,
        String coloniaNombre,
        String coloniaCodigoPostal) {
}
