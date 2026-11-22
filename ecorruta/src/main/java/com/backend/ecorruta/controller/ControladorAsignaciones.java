package com.backend.ecorruta.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.backend.ecorruta.dto.EjecucionOperadorDTO;
import com.backend.ecorruta.dto.AsignacionOperadorDTO;
import com.backend.ecorruta.dto.OpcionesAsignacionDTO;
import com.backend.ecorruta.dto.SolicitudAsignacionRecurrenteDTO;
import com.backend.ecorruta.service.ServicioAsignaciones;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@RequiredArgsConstructor
public class ControladorAsignaciones {

    private final ServicioAsignaciones servicio;

    @GetMapping("/opciones-asignacion")
    public OpcionesAsignacionDTO opciones() {
        return servicio.opciones();
    }

    @GetMapping("/ejecuciones")
    public List<EjecucionOperadorDTO> ejecuciones(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return servicio.ejecuciones(fecha);
    }

    @GetMapping("/asignaciones")
    public List<AsignacionOperadorDTO> listar() {
        return servicio.listar();
    }

    @PostMapping("/asignaciones")
    public ResponseEntity<AsignacionOperadorDTO> asignar(
            @Valid @RequestBody SolicitudAsignacionRecurrenteDTO solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.asignarRecurrente(solicitud));
    }

    @PatchMapping("/asignaciones/{id}/desactivar")
    public ResponseEntity<Void> desactivar(@PathVariable("id") Long id) {
        servicio.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
