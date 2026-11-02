
package com.backend.ecorruta.repository;

import com.backend.ecorruta.model.Colonia;
import com.backend.ecorruta.model.ColoniaId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ColoniaRepository
        extends JpaRepository<Colonia, ColoniaId> {
}
