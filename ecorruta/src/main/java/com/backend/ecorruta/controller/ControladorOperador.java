package com.backend.ecorruta.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.backend.ecorruta.dto.EjecucionOperadorDTO;
import com.backend.ecorruta.dto.IncidenciaRespuestaDTO;
import com.backend.ecorruta.dto.SolicitudCrearIncidenciaDTO;
import com.backend.ecorruta.dto.SolicitudFinalizarRecorridoDTO;
import com.backend.ecorruta.dto.SolicitudUbicacionDTO;
import com.backend.ecorruta.dto.UbicacionRespuestaDTO;
import com.backend.ecorruta.service.ServicioOperador;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Endpoints del operador de unidad recolectora.
 * Solo accesibles para usuarios con rol CONDUCTOR.
 */
@RestController
@RequestMapping("/api/operador")
@PreAuthorize("hasRole('CONDUCTOR')")
@RequiredArgsConstructor
public class ControladorOperador {

        private final ServicioOperador servicioOperador;

        /** Ejecuciones asignadas al operador en una fecha (por defecto, hoy). */
        @GetMapping("/ejecuciones")
        public List<EjecucionOperadorDTO> obtenerEjecuciones(
                        @RequestParam(value = "fecha", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                        Authentication authentication) {

                return servicioOperador.obtenerEjecuciones(authentication.getName(), fecha);
        }

        @GetMapping("/ejecuciones/{folio}")
        public EjecucionOperadorDTO obtenerEjecucion(
                        @PathVariable("folio") Integer folio,
                        Authentication authentication) {

                return servicioOperador.obtenerEjecucion(authentication.getName(), folio);
        }

        @PostMapping("/ejecuciones/{folio}/iniciar")
        public EjecucionOperadorDTO iniciarRecorrido(
                        @PathVariable("folio") Integer folio,
                        Authentication authentication) {

                return servicioOperador.iniciarRecorrido(authentication.getName(), folio);
        }

        @PostMapping("/ejecuciones/{folio}/finalizar")
        public EjecucionOperadorDTO finalizarRecorrido(
                        @PathVariable("folio") Integer folio,
                        @Valid @RequestBody SolicitudFinalizarRecorridoDTO datos,
                        Authentication authentication) {

                return servicioOperador.finalizarRecorrido(authentication.getName(), folio, datos);
        }

        @PostMapping("/ejecuciones/{folio}/ubicaciones")
        public ResponseEntity<UbicacionRespuestaDTO> registrarUbicacion(
                        @PathVariable("folio") Integer folio,
                        @Valid @RequestBody SolicitudUbicacionDTO datos,
                        Authentication authentication) {

                UbicacionRespuestaDTO respuesta = servicioOperador
                                .registrarUbicacion(authentication.getName(), folio, datos);

                return ResponseEntity
                                .status(respuesta.guardada() ? HttpStatus.CREATED : HttpStatus.OK)
                                .body(respuesta);
        }

        @GetMapping("/ejecuciones/{folio}/ubicaciones")
        public List<UbicacionRespuestaDTO> obtenerUbicaciones(
                        @PathVariable("folio") Integer folio,
                        Authentication authentication) {

                return servicioOperador.obtenerUbicaciones(authentication.getName(), folio);
        }

        @PostMapping(value = "/ejecuciones/{folio}/incidencias", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<IncidenciaRespuestaDTO> registrarIncidencia(
                        @PathVariable("folio") Integer folio,
                        @Valid @RequestPart("datos") SolicitudCrearIncidenciaDTO datos,
                        @RequestPart(value = "imagen", required = false) MultipartFile imagen,
                        Authentication authentication) {

                IncidenciaRespuestaDTO respuesta = servicioOperador
                                .registrarIncidencia(authentication.getName(), folio, datos, imagen);

                return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
        }

        @GetMapping("/ejecuciones/{folio}/incidencias")
        public List<IncidenciaRespuestaDTO> obtenerIncidencias(
                        @PathVariable("folio") Integer folio,
                        Authentication authentication) {

                return servicioOperador.obtenerIncidencias(authentication.getName(), folio);
        }
}
