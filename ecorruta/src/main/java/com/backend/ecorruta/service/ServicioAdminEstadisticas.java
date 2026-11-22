package com.backend.ecorruta.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.ecorruta.dto.EstadisticasAdminDTO;
import com.backend.ecorruta.enumeraciones.EstadoEjecucion;
import com.backend.ecorruta.enumeraciones.EstadoIncidencia;
import com.backend.ecorruta.enumeraciones.EstadoReporte;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServicioAdminEstadisticas {

    private static final ZoneId ZONA = ZoneId.of("America/Mexico_City");
    private static final List<String> ROLES = List.of("CIUDADANO", "CONDUCTOR", "ADMINISTRADOR");

    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public EstadisticasAdminDTO obtener() {
        return new EstadisticasAdminDTO(
                contar("select count(u) from Usuario u where u.activo = true"),
                usuariosPorRol(),
                contar("select count(r) from Ruta r where r.activo = true"),
                contar("select count(r) from Ruta r"),
                reportesPorEstado(),
                recorridosHoy(),
                recorridosEnCurso(),
                incidenciasAbiertas());
    }

    private long contar(String consulta) {
        return entityManager.createQuery(consulta, Long.class).getSingleResult();
    }

    private Map<String, Long> usuariosPorRol() {
        Map<String, Long> mapa = new LinkedHashMap<>();
        ROLES.forEach(rol -> mapa.put(rol, 0L));

        List<Object[]> filas = entityManager.createQuery(
                "select u.rol.nombre, count(u) from Usuario u "
                        + "where u.activo = true group by u.rol.nombre",
                Object[].class).getResultList();

        for (Object[] fila : filas) {
            mapa.put((String) fila[0], (Long) fila[1]);
        }
        return mapa;
    }

    private Map<String, Long> reportesPorEstado() {
        Map<String, Long> mapa = new LinkedHashMap<>();
        for (EstadoReporte estado : EstadoReporte.values()) {
            mapa.put(estado.name(), 0L);
        }

        List<Object[]> filas = entityManager.createQuery(
                "select r.estado, count(r) from Reporte r group by r.estado",
                Object[].class).getResultList();

        for (Object[] fila : filas) {
            mapa.put(((EstadoReporte) fila[0]).name(), (Long) fila[1]);
        }
        return mapa;
    }

    private long recorridosHoy() {
        return entityManager.createQuery(
                "select count(e) from EjecucionRuta e where e.fechaProgramada = :hoy",
                Long.class)
                .setParameter("hoy", LocalDate.now(ZONA))
                .getSingleResult();
    }

    private long recorridosEnCurso() {
        return entityManager.createQuery(
                "select count(e) from EjecucionRuta e where e.estado = :estado",
                Long.class)
                .setParameter("estado", EstadoEjecucion.EN_CURSO)
                .getSingleResult();
    }

    private long incidenciasAbiertas() {
        return entityManager.createQuery(
                "select count(i) from Incidencia i where i.estado in :estados",
                Long.class)
                .setParameter("estados",
                        List.of(EstadoIncidencia.PENDIENTE, EstadoIncidencia.EN_PROCESO))
                .getSingleResult();
    }
}