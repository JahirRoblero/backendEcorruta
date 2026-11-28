package com.backend.ecorruta.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.ecorruta.dto.EstadisticasAdminDTO;
import com.backend.ecorruta.service.ServicioAdminEstadisticas;

import lombok.RequiredArgsConstructor;

@RestController 
@RequestMapping("/api/admin/estadisticas")
@RequiredArgsConstructor 
public class ControladorAdminEstadisticas {
    private final ServicioAdminEstadisticas servicioAdminEstadisticas;

    @GetMapping 
    public EstadisticasAdminDTO obtener() {
        return servicioAdminEstadisticas.obtener();
    }
}
