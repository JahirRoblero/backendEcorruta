package com.backend.ecorruta.dto;

import java.util.List;

/** Configuración fija de la ruta y unidad del conductor, independiente de cada jornada. */
public record AsignacionOperadorDTO(
        Long idAsignacion,
        String correoOperador,
        String nombreOperador,
        String rutaNombre,
        String rutaDescripcion,
        String placaVehiculo,
        String numeroEconomico,
        List<Integer> diasSemana,
        boolean activa,
        boolean iniciadaHoy) {
}
