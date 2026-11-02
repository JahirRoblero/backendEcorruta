
package com.backend.ecorruta.repository;

import com.backend.ecorruta.model.UbicacionUnidad;
import com.backend.ecorruta.model.UbicacionUnidadId;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UbicacionUnidadRepository
        extends JpaRepository<UbicacionUnidad, UbicacionUnidadId> {
}
