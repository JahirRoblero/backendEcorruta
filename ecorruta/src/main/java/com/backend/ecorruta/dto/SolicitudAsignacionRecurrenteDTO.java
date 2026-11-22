package com.backend.ecorruta.dto;

import java.util.List;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record SolicitudAsignacionRecurrenteDTO(
        @NotBlank @Email String correoOperador,
        @NotBlank String rutaNombre,
        @NotBlank String placaVehiculo,
        @NotEmpty List<Integer> diasSemana) {
}
