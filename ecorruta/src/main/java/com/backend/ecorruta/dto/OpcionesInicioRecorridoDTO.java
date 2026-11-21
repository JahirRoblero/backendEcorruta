package com.backend.ecorruta.dto;

import java.util.List;

/**
 * Rutas activas y unidades disponibles que el operador puede elegir
 * para iniciar un recorrido.
 */
public record OpcionesInicioRecorridoDTO(
                List<RutaOpcion> rutas,
                List<VehiculoOpcion> vehiculos) {

        public record RutaOpcion(
                        String nombre,
                        String descripcion,
                        List<String> colonias) {
        }

        public record VehiculoOpcion(
                        String placa,
                        String numeroEconomico,
                        String marca,
                        String modelo) {
        }
}
