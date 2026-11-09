
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

        String getNombre();

        String getColoniaNombre();

        String getColoniaCodigoPostal();
    }

    @Query(value = """
                        SELECT
                            r.id_reporte AS "idReporte",
                            r.titulo AS "titulo",
                            r.descripcion AS "descripcion",
                            r.imagen_url AS "imagenUrl",
                            r.estado AS "estado",
                            u.nombre AS "nombre",

            r.colonia_nombre AS "coloniaNombre",
            r.colonia_codigo_postal AS "coloniaCodigoPostal"

                        FROM reporte r
                        INNER JOIN usuario u
                            ON r.correo_usuario = u.correo
                        ORDER BY RANDOM()
                        """, nativeQuery = true)
    List<ReporteVista> buscarAleatorios();

    @Query(value = """
            SELECT
                r.id_reporte AS "idReporte",
                r.titulo AS "titulo",
                r.descripcion AS "descripcion",
                r.imagen_url AS "imagenUrl",
                r.estado AS "estado",
                autor.nombre AS "nombre",
                r.colonia_nombre AS "coloniaNombre",
                r.colonia_codigo_postal AS "coloniaCodigoPostal"

            FROM reporte r
            INNER JOIN usuario autor
                ON r.correo_usuario = autor.correo
            INNER JOIN usuario lector
                ON r.colonia_nombre = lector.colonia_nombre
                AND r.colonia_codigo_postal = lector.colonia_codigo_postal
            WHERE lector.correo = :correo
            ORDER BY r.fecha_reporte DESC, r.id_reporte DESC
            LIMIT 5
            """, nativeQuery = true)
    List<ReporteVista> buscarRecientesPorColoniaUsuario(
            @Param("correo") String correo);

    @Query(value = """
            SELECT
                r.id_reporte AS "idReporte",
                r.titulo AS "titulo",
                r.descripcion AS "descripcion",
                r.imagen_url AS "imagenUrl",
                r.estado AS "estado",
                u.nombre AS "nombre",
                r.colonia_nombre AS "coloniaNombre",
                r.colonia_codigo_postal AS "coloniaCodigoPostal"
            FROM reporte r
            INNER JOIN usuario u
                ON r.correo_usuario = u.correo
            ORDER BY r.fecha_reporte DESC, r.id_reporte DESC
            """, nativeQuery = true)
    List<ReporteVista> todosLosReportes();

}