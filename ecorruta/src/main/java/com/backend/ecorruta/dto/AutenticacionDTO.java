package com.backend.ecorruta.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AutenticacionDTO {

        public record SolicitudCorreo(
                        @NotBlank @Email @Size(max = 254) String correo) {
        }

        public record SolicitudRegistro(
                        @NotBlank @Size(max = 60) String nombre,
                        @NotBlank @Size(max = 60) String apellidoPaterno,
                        @Size(max = 60) String apellidoMaterno,
                        @NotBlank @Email @Size(max = 254) String correo,
                        @NotBlank @Pattern(regexp = "\\d{5}") String codigoPostal,
                        @NotBlank @Size(max = 100) String coloniaNombre,
                        // Minimo 8 caracteres, al menos un numero y al menos un caracter especial
                        @NotBlank @Size(min = 8, max = 72) @Pattern(regexp = "^(?=.*\\d)(?=.*[^A-Za-z0-9\\u00C1\\u00C9\\u00CD\\u00D3\\u00DA\\u00DC\\u00D1\\u00E1\\u00E9\\u00ED\\u00F3\\u00FA\\u00FC\\u00F1\\s]).+$", message = "La contrasena debe incluir al menos un numero y un caracter especial") String contrasena,
                        @NotBlank String confirmarContrasena,
                        @NotBlank @Pattern(regexp = "\\d{5}") String codigoVerificacion) {
        }

        public record SolicitudLogin(
                        @NotBlank @Email String correo,
                        @NotBlank String contrasena) {
        }

        public record Respuesta(String token, String correo, String rol) {
        }
}
