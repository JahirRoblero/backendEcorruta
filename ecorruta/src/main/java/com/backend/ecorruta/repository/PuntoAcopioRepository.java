package com.backend.ecorruta.repository;

import com.backend.ecorruta.model.PuntoAcopio;
import com.backend.ecorruta.model.PuntoAcopioId;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PuntoAcopioRepository
                extends JpaRepository<PuntoAcopio, PuntoAcopioId> {

        @Query("""
                            SELECT DISTINCT p
                            FROM PuntoAcopio p
                            LEFT JOIN FETCH p.tiposResiduos
                            WHERE p.activo = true
                        """)
        List<PuntoAcopio> buscarPuntosActivos();
}