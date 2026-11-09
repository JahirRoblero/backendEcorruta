
package com.backend.ecorruta.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.backend.ecorruta.dto.RutaCatalogoDTO;
import com.backend.ecorruta.dto.RutaInicioDTO;
import com.backend.ecorruta.service.ServicioRutaInicio;
import com.backend.ecorruta.service.ServicioStorageNeon;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/rutas")
@RequiredArgsConstructor
public class ControladorRutaInicio {

    private final ServicioRutaInicio servicioRutaInicio;
    private final ServicioStorageNeon servicioStorageNeon;

    @GetMapping("/mi-ruta")
    public ResponseEntity<RutaInicioDTO> obtenerMiRuta(
            Authentication authentication) {

        return ResponseEntity.ok(
                servicioRutaInicio.obtenerRuta(
                        authentication.getName()));
    }

    @GetMapping("/disponibles")
    public List<RutaCatalogoDTO> obtenerDisponibles() {
        return servicioRutaInicio.obtenerCatalogoRutas();
    }

    @GetMapping("/imagenes/{nombre:.+}")
    public ResponseEntity<byte[]> obtenerImagenRuta(
            @PathVariable("nombre") String nombre) {

        if (!nombre.matches(
                "[a-zA-Z0-9_-]+\\.(png|jpg|jpeg|webp)")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Nombre de imagen inválido");
        }

        byte[] imagen = servicioStorageNeon.obtenerImagenRuta(nombre);

        MediaType tipo = nombre.endsWith(".png")
                ? MediaType.IMAGE_PNG
                : nombre.endsWith(".webp")
                        ? MediaType.parseMediaType("image/webp")
                        : MediaType.IMAGE_JPEG;

        return ResponseEntity.ok()
                .contentType(tipo)
                .header(
                        HttpHeaders.CACHE_CONTROL,
                        "public, max-age=3600")
                .body(imagen);
    }

}
