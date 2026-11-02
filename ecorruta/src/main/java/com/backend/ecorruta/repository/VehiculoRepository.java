package com.backend.ecorruta.repository;

import com.backend.ecorruta.model.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehiculoRepository
                extends JpaRepository<Vehiculo, String> {
}