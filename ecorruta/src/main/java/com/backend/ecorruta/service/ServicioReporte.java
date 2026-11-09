
package com.backend.ecorruta.service;

import java.nio.file.Files;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.backend.ecorruta.dto.ReporteRespuestaDTO;
import com.backend.ecorruta.dto.ReporteResumenDTO;
import com.backend.ecorruta.dto.SolicitudCrearReporteDTO;
import com.backend.ecorruta.enumeraciones.EstadoReporte;
import com.backend.ecorruta.model.Colonia;
import com.backend.ecorruta.model.Reporte;
import com.backend.ecorruta.model.Usuario;
import com.backend.ecorruta.repository.ReporteRepository;
import com.backend.ecorruta.repository.UsuarioRepository;

import com.backend.ecorruta.repository.ColoniaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServicioReporte {

        private final ReporteRepository reporteRepository;
        private final UsuarioRepository usuarioRepository;
        private final ColoniaRepository coloniaRepository;
        private final ServicioStorageNeon servicioStorageNeon;

        public List<ReporteResumenDTO> obtenerAleatorios() {

                return reporteRepository
                                .buscarAleatorios()
                                .stream()
                                .map(reporte -> new ReporteResumenDTO(
                                                reporte.getIdReporte(),
                                                reporte.getTitulo(),
                                                reporte.getDescripcion(),
                                                reporte.getImagenUrl(),
                                                reporte.getEstado(),
                                                reporte.getNombre(),
                                                reporte.getColoniaNombre(),
                                                reporte.getColoniaCodigoPostal()))
                                .toList();
        }

        public List<ReporteResumenDTO> obtenerTodosReportes() {
                return reporteRepository.todosLosReportes()
                                .stream()
                                .map(reporte -> new ReporteResumenDTO(
                                                reporte.getIdReporte(),
                                                reporte.getTitulo(),
                                                reporte.getDescripcion(),
                                                reporte.getImagenUrl(),
                                                reporte.getEstado(),
                                                reporte.getNombre(),
                                                reporte.getColoniaNombre(),
                                                reporte.getColoniaCodigoPostal()))
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
                                                reporte.getEstado(),
                                                reporte.getNombre(),
                                                reporte.getColoniaNombre(),
                                                reporte.getColoniaCodigoPostal()))
                                .toList();
        }

        @Transactional
        public ReporteRespuestaDTO crearReporte(
                        SolicitudCrearReporteDTO datos,
                        MultipartFile imagen,
                        String correoUsuario) {

                Usuario usuario = usuarioRepository
                                .findByCorreoIgnoreCase(correoUsuario)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "El usuario no existe"));

                Colonia colonia = coloniaRepository
                                .findByNombreColoniaAndCodigoPostal(
                                                datos.coloniaNombre().trim(),
                                                datos.coloniaCodigoPostal().trim())
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.BAD_REQUEST,
                                                "La colonia no existe"));

                if (!colonia.isActivo()) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "La colonia está inactiva");
                }

                String imagenUrl = servicioStorageNeon.guardarImagen(imagen);

                try {
                        Reporte reporte = new Reporte();

                        reporte.setUsuario(usuario);
                        reporte.setColonia(colonia);
                        reporte.setTitulo(datos.titulo().trim());
                        reporte.setDescripcion(datos.descripcion().trim());
                        reporte.setImagenUrl(imagenUrl);
                        reporte.setLatitud(datos.latitud());
                        reporte.setLongitud(datos.longitud());
                        reporte.setEstado(EstadoReporte.PENDIENTE);

                        Reporte guardado = reporteRepository.saveAndFlush(reporte);

                        return new ReporteRespuestaDTO(
                                        guardado.getIdReporte(),
                                        guardado.getTitulo(),
                                        guardado.getDescripcion(),
                                        guardado.getImagenUrl(),
                                        guardado.getEstado().name());

                } catch (RuntimeException error) {

                        try {
                                servicioStorageNeon.eliminarImagen(imagenUrl);
                        } catch (RuntimeException errorEliminacion) {
                                error.addSuppressed(errorEliminacion);
                        }

                        throw error;
                }
        }

}
