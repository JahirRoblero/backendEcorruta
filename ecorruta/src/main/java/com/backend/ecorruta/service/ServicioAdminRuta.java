package com.backend.ecorruta.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.backend.ecorruta.dto.RutaAdminDTO;
import com.backend.ecorruta.model.Ruta;
import com.backend.ecorruta.repository.EjecucionRutaRepository;
import com.backend.ecorruta.repository.HorarioRutaRepository;
import com.backend.ecorruta.repository.RutaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServicioAdminRuta {

    private final RutaRepository rutaRepository;
    private final HorarioRutaRepository horarioRutaRepository;
    private final EjecucionRutaRepository ejecucionRutaRepository;

    @Transactional(readOnly = true)
    public List<RutaAdminDTO> listar() {
        return rutaRepository.buscarTodasParaAdmin();
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

    private Ruta buscar(String nombre) {
        return rutaRepository.findById(nombre)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "La ruta no existe"));
    }

    private ResponseStatusException conflicto() {
        return new ResponseStatusException(
                HttpStatus.CONFLICT,
                "La ruta tiene horarios o recorridos registrados; desactívala en lugar de eliminarla");
    }
}