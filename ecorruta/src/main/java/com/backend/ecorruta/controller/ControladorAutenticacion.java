
package com.backend.ecorruta.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.backend.ecorruta.dto.AutenticacionDTO;
import com.backend.ecorruta.service.ServicioAutenticacion;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class ControladorAutenticacion {

        private final ServicioAutenticacion servicioAutenticacion;

        @PostMapping("/registro")
        public ResponseEntity<AutenticacionDTO.Respuesta> registrar(
                        @Valid @RequestBody AutenticacionDTO.SolicitudRegistro solicitud) {

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(
                                                servicioAutenticacion.registrar(solicitud));
        }

        @PostMapping("/login")
        public ResponseEntity<AutenticacionDTO.Respuesta> login(
                        @Valid @RequestBody AutenticacionDTO.SolicitudLogin solicitud) {

                return ResponseEntity.ok(
                                servicioAutenticacion.iniciarSesion(solicitud));
        }

        @GetMapping("/privado")
        public ResponseEntity<?> privado(
                        Authentication autenticacion) {

                return ResponseEntity.ok(
                                Map.of(
                                                "mensaje", "JWT funcionando correctamente",
                                                "usuario", autenticacion.getName()));
        }
}
