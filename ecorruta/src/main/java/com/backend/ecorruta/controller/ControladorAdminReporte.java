package com.backend.ecorruta.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.backend.ecorruta.dto.ReporteAdminDTO;
import com.backend.ecorruta.dto.SolicitudCambioEstadoDTO;
import com.backend.ecorruta.service.ServicioAdminReporte;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/reportes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class ControladorAdminReporte {

    private final ServicioAdminReporte servicioAdminReporte;

    @GetMapping
    public List<ReporteAdminDTO> listar(
            @RequestParam(name = "estado", required = false) String estado) {
        return servicioAdminReporte.listar(estado);
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Void> cambiarEstado(
            @PathVariable("id") Integer id,
            @Valid @RequestBody SolicitudCambioEstadoDTO solicitud) {
        servicioAdminReporte.cambiarEstado(id, solicitud.estado());
        return ResponseEntity.noContent().build();
    }
}
