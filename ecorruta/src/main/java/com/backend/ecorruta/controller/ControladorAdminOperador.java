package com.backend.ecorruta.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.ecorruta.dto.OperadorAdminDTO;
import com.backend.ecorruta.dto.SolicitudCrearOperadorDTO;
import com.backend.ecorruta.dto.VehiculoAdminDTO;
import com.backend.ecorruta.service.ServicioAdminOperador;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class ControladorAdminOperador {

    private final ServicioAdminOperador servicioAdminOperador;

    @GetMapping("/operadores")
    public List<OperadorAdminDTO> listarOperadores() {
        return servicioAdminOperador.listarOperadores();
    }

    @PostMapping("/operadores")
    public ResponseEntity<OperadorAdminDTO> crearOperador(
            @Valid @RequestBody SolicitudCrearOperadorDTO solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(servicioAdminOperador.crearOperador(solicitud));
    }

    @GetMapping("/vehiculos")
    public List<VehiculoAdminDTO> listarVehiculos(
            @RequestParam(name = "estado", required = false) String estado) {
        return servicioAdminOperador.listarVehiculos(estado);
    }
}
