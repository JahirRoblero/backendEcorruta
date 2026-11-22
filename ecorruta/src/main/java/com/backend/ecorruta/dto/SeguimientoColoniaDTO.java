package com.backend.ecorruta.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Seguimiento de las unidades que atienden una colonia (HU-22).
 * No incluye datos personales del operador (HU-22 CA4).
 */
public record SeguimientoColoniaDTO(
                String colonia,
                String codigoPostal,
                int minutosParaDesactualizada,
                List<Unidad> unidades,
                List<Horario> horarios) {

        /** Unidad con recorrido EN_CURSO en una ruta de la colonia. */
        public record Unidad(
                        String rutaNombre,
                        String geometria,
                        String numeroEconomico,
                        BigDecimal latitud,
                        BigDecimal longitud,
                        OffsetDateTime fechaHora,
                        Long minutosSinActualizar,
                        boolean desactualizada,
                        /** Puntos [longitud, latitud] por donde ya pasó la unidad, en orden. */
                        List<double[]> recorrido,
                        /** Incidencias abiertas del recorrido, sin detalles internos (HU-24 CA2). */
                        List<Aviso> avisos) {
        }

        /** Aviso de retraso o interrupción: solo el tipo y la hora. */
        public record Aviso(
                        String tipo,
                        OffsetDateTime fechaHora) {
        }

        /** Horario programado de una ruta de la colonia (1 = lunes ... 7 = domingo). */
        public record Horario(
                        String rutaNombre,
                        String tipoResiduo,
                        int diaSemana,
                        String horaInicio,
                        String horaFin) {
        }
}
