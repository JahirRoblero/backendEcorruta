
package com.backend.ecorruta.repository;

import com.backend.ecorruta.model.Incidencia;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidenciaRepository
        extends JpaRepository<Incidencia, Integer> {
}
