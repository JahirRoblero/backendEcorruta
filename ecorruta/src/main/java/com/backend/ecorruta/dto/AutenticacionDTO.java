
package com.backend.ecorruta.dto;

import jakarta.validation.constraints.*;

public class AutenticacionDTO {

        public record SolicitudRegistro(

                        @NotBlank(message = "El nombre es obligatorio") @Size(max = 60) String nombre,

                        @NotBlank(message = "El apellido paterno es obligatorio") @Size(max = 60) String apellidoPaterno,

                        @Size(max = 60) String apellidoMaterno,

                        @NotBlank(message = "El correo es obligatorio") @Email(message = "El correo no es válido") @Size(max = 254) String correo,

                        @NotBlank(message = "La contraseña es obligatoria") @Size(min = 8, message = "La contraseña debe tener mínimo 8 caracteres") String contrasena

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
