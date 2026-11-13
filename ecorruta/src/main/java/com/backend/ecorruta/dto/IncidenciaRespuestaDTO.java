package com.backend.ecorruta.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record IncidenciaRespuestaDTO(
                Integer idIncidencia,
                Integer folioEjecucion,
                String titulo,
                String descripcion,
                String estado,
                OffsetDateTime fechaHora,
                BigDecimal latitud,
                BigDecimal longitud,
                String imagenUrl) {
}
