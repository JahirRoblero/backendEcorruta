
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

        @Query(value = """
                        SELECT
                            r.id_reporte AS "idReporte",
                            r.titulo AS "titulo",
                            r.descripcion AS "descripcion",
                            r.imagen_url AS "imagenUrl",
                            r.estado AS "estado"
                        FROM reporte r
                        INNER JOIN usuario u
                            ON r.colonia_nombre = u.colonia_nombre
                            AND r.colonia_codigo_postal = u.colonia_codigo_postal
                        WHERE u.correo = :correo
                        ORDER BY r.fecha_reporte DESC, r.id_reporte DESC
                        LIMIT 5
                        """, nativeQuery = true)
        List<ReporteVista> buscarRecientesPorColoniaUsuario(
                        @Param("correo") String correo);

}
