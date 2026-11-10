package com.backend.ecorruta.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SolicitudRegistro(

        @NotBlank @Size(max = 60) String nombre,

        @NotBlank @Size(max = 60) String apellidoPaterno,

        @Size(max = 60) String apellidoMaterno,

        @NotBlank @Email @Size(max = 254) String correo,

        @NotBlank @Pattern(regexp = "\\d{5}") String codigoPostal,

        @NotBlank @Size(max = 100) String coloniaNombre,

        @NotBlank @Size(min = 8, max = 72) String contrasena,

        @NotBlank String confirmarContrasena) {
}
