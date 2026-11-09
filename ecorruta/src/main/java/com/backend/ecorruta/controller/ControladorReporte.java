
package com.backend.ecorruta.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.backend.ecorruta.dto.ReporteRespuestaDTO;
import com.backend.ecorruta.dto.ReporteResumenDTO;
import com.backend.ecorruta.dto.SolicitudCrearReporteDTO;

import com.backend.ecorruta.service.ServicioReporte;
import com.backend.ecorruta.service.ServicioStorageNeon;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ControladorReporte {

        private final ServicioReporte servicioReporte;
        private final ServicioStorageNeon servicioStorageNeon;

        @GetMapping("/aleatorios")
        public List<ReporteResumenDTO> obtenerAleatorios() {

                return servicioReporte.obtenerAleatorios();
        }

        @GetMapping("/recientes")
        public List<ReporteResumenDTO> obtenerRecientes(
                        Authentication authentication) {

                String correo = authentication.getName();

                return servicioReporte
                                .obtenerRecientesPorColonia(correo);
        }

        @GetMapping("/todos-reportes")
        public List<ReporteResumenDTO> todosReportes() {
                return servicioReporte.obtenerTodosReportes();
        }

        @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<ReporteRespuestaDTO> crearReporte(

                        @Valid @RequestPart("datos") SolicitudCrearReporteDTO datos,

                        @RequestPart(value = "imagen", required = false) MultipartFile imagen,

                        Authentication authentication) {

                ReporteRespuestaDTO respuesta = servicioReporte.crearReporte(
                                datos,
                                imagen,
                                authentication.getName());

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(respuesta);
        }

        @GetMapping("/imagenes/{nombre:.+}")
        public ResponseEntity<byte[]> obtenerImagen(
                        @PathVariable("nombre") String nombre) {

                if (!nombre.matches(
                                "[0-9a-fA-F-]{36}\\.(jpg|png|webp)")) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Nombre de imagen inválido");
                }

                byte[] imagen = servicioStorageNeon.obtenerImagen(nombre);

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
