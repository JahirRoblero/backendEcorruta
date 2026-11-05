
package com.backend.ecorruta.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.backend.ecorruta.dto.RutaInicioDTO;
import com.backend.ecorruta.service.ServicioRutaInicio;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/rutas")
@RequiredArgsConstructor
public class ControladorRutaInicio {

    private final ServicioRutaInicio servicioRutaInicio;

    @GetMapping("/mi-ruta")
    public ResponseEntity<RutaInicioDTO> obtenerMiRuta(
            Authentication authentication) {

        return ResponseEntity.ok(
                servicioRutaInicio.obtenerRuta(
                        authentication.getName()));
    }
}
