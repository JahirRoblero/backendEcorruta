package com.backend.ecorruta.repository;

import com.backend.ecorruta.model.Reporte;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReporteRepository
        extends JpaRepository<Reporte, Integer> {
}
