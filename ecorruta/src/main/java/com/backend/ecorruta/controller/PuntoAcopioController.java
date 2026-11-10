package com.backend.ecorruta.controller;

import com.backend.ecorruta.repository.PuntoAcopioRepository;
import com.backend.ecorruta.model.TipoResiduo;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/puntos-acopio")
public class PuntoAcopioController {

    private final PuntoAcopioRepository repositorio;

    public PuntoAcopioController(
            PuntoAcopioRepository repositorio) {
        this.repositorio = repositorio;
    }

    public record PuntoAcopioMapaDTO(
            String coloniaNombre,
            String coloniaCodigoPostal,
            String nombre,
            String direccion,
            BigDecimal latitud,
            BigDecimal longitud,
            List<String> tiposResiduos) {
    }

    @GetMapping
    public List<PuntoAcopioMapaDTO> obtenerPuntos() {
        return repositorio.buscarPuntosActivos()
                .stream()
                .map(p -> new PuntoAcopioMapaDTO(
                        p.getColoniaNombre(),
                        p.getColoniaCodigoPostal(),
                        p.getNombre(),
                        p.getDireccion(),
                        p.getLatitud(),
                        p.getLongitud(),
                        p.getTiposResiduos()
                                .stream()
                                .map(TipoResiduo::getNombre)
                                .sorted()
                                .toList()))
                .toList();
    }
}