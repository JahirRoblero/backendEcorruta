package com.backend.ecorruta.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.ecorruta.dto.RutaAdminDTO;
import com.backend.ecorruta.dto.SolicitudCrearRutaDTO;
import com.backend.ecorruta.dto.SolicitudEditarRutaDTO;
import com.backend.ecorruta.service.ServicioAdminRuta;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/rutas")
@RequiredArgsConstructor
public class ControladorAdminRuta {

    private final ServicioAdminRuta servicioAdminRuta;

    @GetMapping
    public List<RutaAdminDTO> listar() {
        return servicioAdminRuta.listar();
    }

    @PostMapping
    public ResponseEntity<RutaAdminDTO> crear(
            @Valid @RequestBody SolicitudCrearRutaDTO solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(servicioAdminRuta.crear(solicitud));
    }

    @PutMapping("/{nombre}")
    public RutaAdminDTO editar(
            @PathVariable String nombre,
            @Valid @RequestBody SolicitudEditarRutaDTO solicitud) {
        return servicioAdminRuta.editar(nombre, solicitud);
    }

    @PatchMapping("/{nombre}/activar")
    public ResponseEntity<Void> activar(@PathVariable String nombre) {
        servicioAdminRuta.cambiarEstado(nombre, true);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{nombre}/desactivar")
    public ResponseEntity<Void> desactivar(@PathVariable String nombre) {
        servicioAdminRuta.cambiarEstado(nombre, false);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{nombre}")
    public ResponseEntity<Void> eliminar(@PathVariable String nombre) {
        servicioAdminRuta.eliminar(nombre);
        return ResponseEntity.noContent().build();
    }
}