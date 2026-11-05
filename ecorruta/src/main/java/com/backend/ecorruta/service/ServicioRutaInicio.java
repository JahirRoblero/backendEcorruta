
package com.backend.ecorruta.service;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.backend.ecorruta.dto.RutaInicioDTO;
import com.backend.ecorruta.model.Ruta;
import com.backend.ecorruta.repository.RutaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServicioRutaInicio {

    private final RutaRepository rutaRepository;

    @Transactional(readOnly = true)
    public RutaInicioDTO obtenerRuta(String correo) {

        Optional<Ruta> rutaDeMiColonia = rutaRepository.buscarRutaDeMiColonia(correo);

        Ruta ruta = rutaDeMiColonia.orElseGet(() -> rutaRepository.buscarRutaAleatoria()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No hay rutas disponibles con geometría")));

        return new RutaInicioDTO(
                ruta.getNombre(),
                ruta.getDescripcion(),
                ruta.getGeometria(),
                rutaDeMiColonia.isPresent());
    }
}
