
package com.backend.ecorruta.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.backend.ecorruta.dto.ReporteResumenDTO;
import com.backend.ecorruta.service.ServicioReporte;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ControladorReporte {

    private final ServicioReporte servicioReporte;

    @GetMapping("/aleatorios")
    public List<ReporteResumenDTO> obtenerAleatorios() {

        return servicioReporte.obtenerAleatorios();
    }
}
