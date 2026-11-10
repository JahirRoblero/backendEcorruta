
package com.backend.ecorruta.dto;

import jakarta.validation.constraints.*;

public class AutenticacionDTO {

        public record SolicitudRegistro(

                        @NotBlank(message = "El nombre es obligatorio") @Size(max = 60) String nombre,

                        @NotBlank(message = "El apellido paterno es obligatorio") @Size(max = 60) String apellidoPaterno,

                        @Size(max = 60) String apellidoMaterno,

                        @NotBlank(message = "El correo es obligatorio") @Email(message = "El correo no es válido") @Size(max = 254) String correo,

                        @NotBlank(message = "El código postal es obligatorio") @Pattern(regexp = "\\d{5}", message = "El código postal debe tener 5 dígitos") String codigoPostal,

                        @NotBlank(message = "La colonia es obligatoria") @Size(max = 100) String coloniaNombre,

                        @NotBlank(message = "La contraseña es obligatoria") @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres") String contrasena,

                        @NotBlank(message = "Debes confirmar la contraseña") String confirmarContrasena

        ) {
        }

        public record SolicitudLogin(

                        @NotBlank(message = "El correo es obligatorio") @Email(message = "El correo no es válido") String correo,

                        @NotBlank(message = "La contraseña es obligatoria") String contrasena

        ) {
        }

        public record Respuesta(
                        String token,
                        String correo,
                        String rol) {
        }
}
