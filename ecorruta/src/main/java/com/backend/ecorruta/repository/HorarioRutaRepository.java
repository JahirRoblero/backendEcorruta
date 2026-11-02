
package com.backend.ecorruta.repository;

import com.backend.ecorruta.model.HorarioRuta;
import com.backend.ecorruta.model.HorarioRutaId;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HorarioRutaRepository
        extends JpaRepository<HorarioRuta, HorarioRutaId> {
}
