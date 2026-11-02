package com.backend.ecorruta.repository;

import com.backend.ecorruta.model.EjecucionRuta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EjecucionRutaRepository
                extends JpaRepository<EjecucionRuta, Integer> {
}
