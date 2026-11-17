package com.backend.ecorruta.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.backend.ecorruta.dto.SolicitudCambioRolDTO;
import com.backend.ecorruta.dto.UsuarioAdminDTO;
import com.backend.ecorruta.service.ServicioAdminUsuario;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/usuarios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class ControladorAdminUsuario {

    private final ServicioAdminUsuario servicioAdminUsuario;

    // Día 14
    @GetMapping
    public List<UsuarioAdminDTO> listar(
            @RequestParam(name = "rol", required = false) String rol) {
        return servicioAdminUsuario.listar(rol);
    }

    // Día 15
    @PatchMapping("/{correo}/activar")
    public ResponseEntity<Void> activar(
            @PathVariable("correo") String correo,
            Authentication authentication) {
        servicioAdminUsuario.cambiarEstado(correo, true, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{correo}/desactivar")
    public ResponseEntity<Void> desactivar(
            @PathVariable("correo") String correo,
            Authentication authentication) {
        servicioAdminUsuario.cambiarEstado(correo, false, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    // Día 16
    @PatchMapping("/{correo}/rol")
    public ResponseEntity<Void> cambiarRol(
            @PathVariable("correo") String correo,
            @Valid @RequestBody SolicitudCambioRolDTO solicitud,
            Authentication authentication) {
        servicioAdminUsuario.cambiarRol(correo, solicitud.rol(), authentication.getName());
        return ResponseEntity.noContent().build();
    }
}