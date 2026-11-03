
package com.backend.ecorruta.dto;

public record ReporteResumenDTO(
        Integer idReporte,
        String titulo,
        String descripcion,
        String imagenUrl,
        String estado) {
}
