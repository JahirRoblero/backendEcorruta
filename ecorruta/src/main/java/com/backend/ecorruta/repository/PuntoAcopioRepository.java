
package com.backend.ecorruta.repository;

import com.backend.ecorruta.model.PuntoAcopio;
import com.backend.ecorruta.model.PuntoAcopioId;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PuntoAcopioRepository
        extends JpaRepository<PuntoAcopio, PuntoAcopioId> {
}
