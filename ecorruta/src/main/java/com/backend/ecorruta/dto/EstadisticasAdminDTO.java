package com.backend.ecorruta.dto;

import java.util.Map;

public record EstadisticasAdminDTO(
        long usuariosActivos,
        Map<String, Long> usuariosPorRol,
        long rutasActivas,
        long rutasTotales,
        Map<String, Long> reportesPorEstado,
        long recorridosHoy,
        long recorridosEnCurso,
        long incidenciasAbiertas) {
}