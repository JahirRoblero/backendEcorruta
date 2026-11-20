package com.backend.ecorruta.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.ecorruta.enumeraciones.EstadoVehiculo;
import com.backend.ecorruta.model.Vehiculo;

public interface VehiculoRepository
                extends JpaRepository<Vehiculo, String> {

        // ===== Panel admin (Ángel) =====

        @Query("select v from Vehiculo v order by v.numeroEconomico")
        List<Vehiculo> buscarTodosParaAdmin();

        @Query("select v from Vehiculo v where v.estado = :estado order by v.numeroEconomico")
        List<Vehiculo> buscarPorEstadoParaAdmin(@Param("estado") EstadoVehiculo estado);
}
