package com.backend.ecorruta.repository;

import com.backend.ecorruta.model.TipoResiduo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TipoResiduoRepository
                extends JpaRepository<TipoResiduo, String> {
}
