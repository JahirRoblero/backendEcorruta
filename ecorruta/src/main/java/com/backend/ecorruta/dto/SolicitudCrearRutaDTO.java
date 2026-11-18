package com.backend.ecorruta.dto;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record SolicitudCrearRutaDTO(
        @NotBlank(message = "El nombre de la ruta es obligatorio")
        @Size(max = 60, message = "El nombre no puede superar los 60 caracteres")
        String nombre,
        String descripcion,
        @PositiveOrZero(message = "La distancia no puede ser negativa")
        @Digits(integer = 6, fraction = 2, message = "La distancia debe tener máximo 6 enteros y 2 decimales")
        BigDecimal distanciaKm,
        String geometria,
        @NotEmpty(message = "Selecciona al menos una colonia")
        List<@Valid ColoniaRefDTO> colonias) {
}