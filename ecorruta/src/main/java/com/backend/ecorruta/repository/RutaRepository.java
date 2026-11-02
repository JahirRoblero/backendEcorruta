
package com.backend.ecorruta.repository;

import com.backend.ecorruta.model.Ruta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RutaRepository
        extends JpaRepository<Ruta, String> {
}
