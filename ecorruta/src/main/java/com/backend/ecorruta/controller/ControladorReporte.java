
package com.backend.ecorruta.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.backend.ecorruta.dto.ReporteResumenDTO;
import com.backend.ecorruta.service.ServicioReporte;
import org.springframework.security.core.Authentication;
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

    @GetMapping("/recientes")
    public List<ReporteResumenDTO> obtenerRecientes(
            Authentication authentication) {

        String correo = authentication.getName();

        return servicioReporte.obtenerRecientesPorColonia(correo);
    }

}
