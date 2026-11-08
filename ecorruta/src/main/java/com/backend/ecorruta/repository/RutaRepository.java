
package com.backend.ecorruta.repository;

import com.backend.ecorruta.model.Ruta;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RutaRepository
    extends JpaRepository<Ruta, String> {

  @Query(value = """
      SELECT r.*
      FROM ruta r
      INNER JOIN ruta_colonia rc
          ON rc.ruta_nombre = r.nombre
      INNER JOIN usuario u
          ON u.colonia_nombre = rc.colonia_nombre
         AND u.colonia_codigo_postal = rc.colonia_codigo_postal
      WHERE u.correo = :correo
        AND r.activo = TRUE
        AND r.geometria IS NOT NULL
        AND BTRIM(r.geometria) <> ''
      ORDER BY r.fecha_creacion DESC, r.nombre
      LIMIT 1
      """, nativeQuery = true)
  Optional<Ruta> buscarRutaDeMiColonia(
      @Param("correo") String correo);

  @Query(value = """
      SELECT r.*
      FROM ruta r
      WHERE r.activo = TRUE
        AND r.geometria IS NOT NULL
        AND BTRIM(r.geometria) <> ''
      ORDER BY RANDOM()
      LIMIT 1
      """, nativeQuery = true)
  Optional<Ruta> buscarRutaAleatoria();

}
