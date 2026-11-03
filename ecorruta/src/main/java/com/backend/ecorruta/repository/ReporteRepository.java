
package com.backend.ecorruta.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.ecorruta.model.Reporte;

public interface ReporteRepository
                extends JpaRepository<Reporte, Integer> {

        interface ReporteVista {
                Integer getIdReporte();

                String getTitulo();

                String getDescripcion();

                String getImagenUrl();

                String getEstado();
        }

        @Query(value = """
                        SELECT
                            id_reporte AS "idReporte",
                            titulo AS "titulo",
                            descripcion AS "descripcion",
                            imagen_url AS "imagenUrl",
                            estado AS "estado"
                        FROM reporte
                        ORDER BY RANDOM()
                        LIMIT 5
                        """, nativeQuery = true)
        List<ReporteVista> buscarAleatorios();

}
