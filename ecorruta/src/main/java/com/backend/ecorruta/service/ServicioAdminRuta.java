package com.backend.ecorruta.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.backend.ecorruta.dto.ColoniaAdminDTO;
import com.backend.ecorruta.dto.ColoniaRefDTO;
import com.backend.ecorruta.dto.RutaAdminDTO;
import com.backend.ecorruta.dto.SolicitudCrearRutaDTO;
import com.backend.ecorruta.model.Colonia;
import com.backend.ecorruta.model.Ruta;
import com.backend.ecorruta.repository.ColoniaRepository;
import com.backend.ecorruta.repository.EjecucionRutaRepository;
import com.backend.ecorruta.repository.HorarioRutaRepository;
import com.backend.ecorruta.repository.RutaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServicioAdminRuta {

    private final RutaRepository rutaRepository;
    private final ColoniaRepository coloniaRepository;
    private final HorarioRutaRepository horarioRutaRepository;
    private final EjecucionRutaRepository ejecucionRutaRepository;

    @Transactional(readOnly = true)
    public List<RutaAdminDTO> listar() {
        return rutaRepository.buscarTodasParaAdmin();
    }

    @Transactional(readOnly = true)
    public List<ColoniaAdminDTO> listarColonias() {
        return coloniaRepository.findByActivoTrueOrderByNombreColoniaAscCodigoPostalAsc()
                .stream()
                .map(c -> new ColoniaAdminDTO(c.getNombreColonia(), c.getCodigoPostal()))
                .toList();
    }

    @Transactional
    public RutaAdminDTO crear(SolicitudCrearRutaDTO solicitud) {
        String nombre = solicitud.nombre().trim();

        if (rutaRepository.existsById(nombre)) {
            throw nombreRepetido();
        }

        Ruta ruta = new Ruta();
        ruta.setNombre(nombre);
        ruta.setDescripcion(limpiar(solicitud.descripcion()));
        ruta.setDistanciaKm(solicitud.distanciaKm());
        ruta.setGeometria(limpiar(solicitud.geometria()));
        ruta.setActivo(true);
        ruta.setColonias(resolverColonias(solicitud.colonias()));

        try {
            rutaRepository.saveAndFlush(ruta);
        } catch (DataIntegrityViolationException e) {
            throw nombreRepetido();
        }

        return rutaRepository.buscarUnaParaAdmin(nombre)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo leer la ruta creada"));
    }

    @Transactional
    public void cambiarEstado(String nombre, boolean activo) {
        Ruta ruta = buscar(nombre);
        ruta.setActivo(activo);
        rutaRepository.save(ruta);
    }

    @Transactional
    public void eliminar(String nombre) {
        Ruta ruta = buscar(nombre);

        if (ejecucionRutaRepository.existsByRutaNombre(nombre)
                || horarioRutaRepository.existsByRutaNombre(nombre)) {
            throw conflicto();
        }

        try {
            rutaRepository.delete(ruta);
            rutaRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw conflicto();
        }
    }

    private Set<Colonia> resolverColonias(List<ColoniaRefDTO> referencias) {
        Set<Colonia> colonias = new HashSet<>();
        for (ColoniaRefDTO ref : referencias) {
            Colonia colonia = coloniaRepository
                    .findByNombreColoniaAndCodigoPostal(ref.nombre().trim(), ref.codigoPostal())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "La colonia " + ref.nombre() + " (" + ref.codigoPostal() + ") no existe"));
            colonias.add(colonia);
        }
        return colonias;
    }

    private String limpiar(String texto) {
        if (texto == null) {
            return null;
        }
        String limpio = texto.trim();
        return limpio.isEmpty() ? null : limpio;
    }

    private Ruta buscar(String nombre) {
        return rutaRepository.findById(nombre)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "La ruta no existe"));
    }

    private ResponseStatusException nombreRepetido() {
        return new ResponseStatusException(
                HttpStatus.CONFLICT, "Ya existe una ruta con ese nombre");
    }

    private ResponseStatusException conflicto() {
        return new ResponseStatusException(
                HttpStatus.CONFLICT,
                "La ruta tiene horarios o recorridos registrados; desactívala en lugar de eliminarla");
    }
}