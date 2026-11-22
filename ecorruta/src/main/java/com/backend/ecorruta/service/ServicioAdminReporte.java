package com.backend.ecorruta.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.backend.ecorruta.dto.ReporteAdminDTO;
import com.backend.ecorruta.enumeraciones.EstadoReporte;
import com.backend.ecorruta.model.Reporte;
import com.backend.ecorruta.repository.ReporteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServicioAdminReporte {

    private static final Set<EstadoReporte> ESTADOS_PERMITIDOS =
            Set.of(EstadoReporte.EN_PROCESO, EstadoReporte.ATENDIDO, EstadoReporte.RECHAZADO);

    private final ReporteRepository reporteRepository;

    @Transactional(readOnly = true)
    public List<ReporteAdminDTO> listar(String estado) {
        List<Reporte> reportes = (estado == null || estado.isBlank())
                ? reporteRepository.buscarTodosParaAdmin()
                : reporteRepository.buscarPorEstadoParaAdmin(convertirEstado(estado));

        return reportes.stream().map(this::aDTO).toList();
    }

    @Transactional
    public void cambiarEstado(Integer idReporte, String estadoNuevo) {
        EstadoReporte estado = convertirEstado(estadoNuevo);

        if (!ESTADOS_PERMITIDOS.contains(estado)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Solo se permite EN_PROCESO, ATENDIDO o RECHAZADO");
        }

        Reporte reporte = reporteRepository.findById(idReporte)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Reporte no encontrado"));

        reporte.setEstado(estado);
        reporte.setFechaAtencion(estado == EstadoReporte.ATENDIDO ? OffsetDateTime.now() : null);
    }

    private EstadoReporte convertirEstado(String texto) {
        try {
            return EstadoReporte.valueOf(texto.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado no válido: " + texto);
        }
    }

    private ReporteAdminDTO aDTO(Reporte r) {
        return new ReporteAdminDTO(
                r.getIdReporte(),
                r.getTitulo(),
                r.getDescripcion(),
                r.getImagenUrl(),
                r.getEstado().name(),
                r.getLatitud(),
                r.getLongitud(),
                r.getFechaReporte(),
                r.getFechaAtencion(),
                r.getUsuario().getNombre() + " " + r.getUsuario().getApellidoPaterno(),
                r.getUsuario().getCorreo(),
                r.getColonia().getNombreColonia(),
                r.getColonia().getCodigoPostal());
    }
}
