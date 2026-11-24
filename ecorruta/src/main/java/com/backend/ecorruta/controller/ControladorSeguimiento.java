package com.backend.ecorruta.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.ecorruta.dto.SeguimientoColoniaDTO;
import com.backend.ecorruta.service.ServicioSeguimiento;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/seguimiento")
@RequiredArgsConstructor
public class ControladorSeguimiento {

        private final ServicioSeguimiento servicioSeguimiento;

        @GetMapping("/mi-colonia")
        public SeguimientoColoniaDTO miColonia(Authentication authentication) {
                return servicioSeguimiento.seguimientoDeMiColonia(authentication.getName());
        }

        @GetMapping("/colonia")
        public SeguimientoColoniaDTO colonia(
                        @RequestParam("nombre") @NotBlank String nombre,
                        @RequestParam("codigoPostal") @Pattern(regexp = "\\d{5}") String codigoPostal) {

                return servicioSeguimiento.seguimientoDeColonia(nombre.trim(), codigoPostal.trim());
        }
}
