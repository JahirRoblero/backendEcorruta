package com.backend.ecorruta.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Alta de un conductor desde el panel admin.
 * La contraseña temporal la define el administrador, se guarda cifrada
 * y nunca se devuelve en ninguna respuesta.
 */
public record SolicitudCrearOperadorDTO(

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no es válido")
        @Size(max = 254, message = "El correo no puede superar los 254 caracteres")
        String correo,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 60, message = "El nombre no puede superar los 60 caracteres")
        String nombre,

        @NotBlank(message = "El apellido paterno es obligatorio")
        @Size(max = 60, message = "El apellido paterno no puede superar los 60 caracteres")
        String apellidoPaterno,

        @Size(max = 60, message = "El apellido materno no puede superar los 60 caracteres")
        String apellidoMaterno,

        @NotBlank(message = "La contraseña temporal es obligatoria")
        @Pattern(regexp = "^(?=.*\\d)(?=.*[^A-Za-z0-9\\s]).{8,72}$",
                message = "La contraseña debe tener de 8 a 72 caracteres, un número y un carácter especial")
        String contrasenaTemporal) {
}
