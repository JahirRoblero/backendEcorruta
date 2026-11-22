package com.backend.ecorruta.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SolicitudAsignacionDTO(
        @NotBlank @Email String correoOperador,
        @NotBlank String rutaNombre,
        @NotBlank String placaVehiculo,
        @NotNull LocalDate fechaProgramada) {
}
