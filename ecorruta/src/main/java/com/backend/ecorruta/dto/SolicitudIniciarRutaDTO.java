package com.backend.ecorruta.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * El operador elige la ruta y la unidad con las que va a salir
 * y el recorrido se crea e inicia en ese momento.
 */
public record SolicitudIniciarRutaDTO(

                @NotBlank(message = "Selecciona la ruta") @Size(max = 60) String rutaNombre,

                @NotBlank(message = "Selecciona la unidad") @Size(max = 15) String placaVehiculo

) {
}
