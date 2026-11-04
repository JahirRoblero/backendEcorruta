
package com.backend.ecorruta.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.backend.ecorruta.dto.ReporteResumenDTO;
import com.backend.ecorruta.repository.ReporteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServicioReporte {

    private final ReporteRepository reporteRepository;

    public List<ReporteResumenDTO> obtenerAleatorios() {

        return reporteRepository
                .buscarAleatorios()
                .stream()
                .map(reporte -> new ReporteResumenDTO(
                        reporte.getIdReporte(),
                        reporte.getTitulo(),
                        reporte.getDescripcion(),
                        reporte.getImagenUrl(),
                        reporte.getEstado()))
                .toList();
    }

    public List<ReporteResumenDTO> obtenerRecientesPorColonia(
            String correo) {

        return reporteRepository
                .buscarRecientesPorColoniaUsuario(correo)
                .stream()
                .map(reporte -> new ReporteResumenDTO(
                        reporte.getIdReporte(),
                        reporte.getTitulo(),
                        reporte.getDescripcion(),
                        reporte.getImagenUrl(),
                        reporte.getEstado()))
                .toList();
    }

}
