package com.backend.ecorruta.service;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.backend.ecorruta.dto.SeguimientoColoniaDTO;
import com.backend.ecorruta.model.Colonia;
import com.backend.ecorruta.model.Usuario;
import com.backend.ecorruta.repository.UsuarioRepository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

/**
 * Consulta ciudadana de la ubicación de las unidades (HU-22):
 * última posición de cada recorrido EN_CURSO en las rutas que atienden
 * la colonia, y los horarios programados para cuando no hay recorrido.
 */
@Service
@RequiredArgsConstructor
public class ServicioSeguimiento {

        /** Minutos sin reportar posición para marcarla como "no actualizada" (HU-22 CA3). */
        private static final int MINUTOS_DESACTUALIZADA = 5;

        private final EntityManager entityManager;
        private final UsuarioRepository usuarioRepository;

        @Transactional(readOnly = true)
        public SeguimientoColoniaDTO seguimientoDeMiColonia(String correo) {

                Usuario usuario = usuarioRepository
                                .findByCorreoIgnoreCase(correo)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "El usuario no existe"));

                Colonia colonia = usuario.getColonia();

                if (colonia == null) {
                        throw new ResponseStatusException(
                                        HttpStatus.UNPROCESSABLE_ENTITY,
                                        "Tu perfil no tiene una colonia registrada");
                }

                return seguimientoDeColonia(colonia.getNombreColonia(), colonia.getCodigoPostal());
        }

        @Transactional(readOnly = true)
        public SeguimientoColoniaDTO seguimientoDeColonia(String nombreColonia, String codigoPostal) {

                List<?> filasUnidades = entityManager.createNativeQuery("""
                                select e.ruta_nombre, r.geometria, v.numero_economico,
                                       u.latitud, u.longitud, u.fecha_hora, e.folio_ejecucion
                                from ejecucion_ruta e
                                join ruta r on r.nombre = e.ruta_nombre
                                join vehiculo v on v.placa = e.placa_vehiculo
                                join ruta_colonia rc on rc.ruta_nombre = e.ruta_nombre
                                left join lateral (
                                    select latitud, longitud, fecha_hora
                                    from ubicacion_unidad
                                    where folio_ejecucion = e.folio_ejecucion
                                    order by numero_muestra desc
                                    limit 1
                                ) u on true
                                where e.estado = 'EN_CURSO'
                                  and r.activo = true
                                  and rc.colonia_nombre = :colonia
                                  and rc.colonia_codigo_postal = :cp
                                order by e.ruta_nombre
                                """)
                                .setParameter("colonia", nombreColonia)
                                .setParameter("cp", codigoPostal)
                                .getResultList();

                Instant ahora = Instant.now();

                List<SeguimientoColoniaDTO.Unidad> unidades = filasUnidades.stream()
                                .map(fila -> (Object[]) fila)
                                .map(fila -> {
                                        OffsetDateTime fechaHora = aFecha(fila[5]);
                                        Long minutos = fechaHora == null
                                                        ? null
                                                        : Duration.between(fechaHora.toInstant(), ahora).toMinutes();
                                        Integer folio = ((Number) fila[6]).intValue();

                                        return new SeguimientoColoniaDTO.Unidad(
                                                        (String) fila[0],
                                                        (String) fila[1],
                                                        (String) fila[2],
                                                        (BigDecimal) fila[3],
                                                        (BigDecimal) fila[4],
                                                        fechaHora,
                                                        minutos,
                                                        minutos == null || minutos > MINUTOS_DESACTUALIZADA,
                                                        recorridoDe(folio),
                                                        avisosDe(folio));
                                })
                                .toList();

                List<?> filasHorarios = entityManager.createNativeQuery("""
                                select h.ruta_nombre, h.tipo_residuo_nombre, h.dia_semana,
                                       h.hora_inicio, h.hora_fin
                                from horario_ruta h
                                join ruta r on r.nombre = h.ruta_nombre
                                join ruta_colonia rc on rc.ruta_nombre = h.ruta_nombre
                                where r.activo = true
                                  and rc.colonia_nombre = :colonia
                                  and rc.colonia_codigo_postal = :cp
                                order by h.dia_semana, h.hora_inicio
                                """)
                                .setParameter("colonia", nombreColonia)
                                .setParameter("cp", codigoPostal)
                                .getResultList();

                List<SeguimientoColoniaDTO.Horario> horarios = filasHorarios.stream()
                                .map(fila -> (Object[]) fila)
                                .map(fila -> new SeguimientoColoniaDTO.Horario(
                                                (String) fila[0],
                                                (String) fila[1],
                                                ((Number) fila[2]).intValue(),
                                                aHora(fila[3]),
                                                aHora(fila[4])))
                                .toList();

                return new SeguimientoColoniaDTO(
                                nombreColonia,
                                codigoPostal,
                                MINUTOS_DESACTUALIZADA,
                                unidades,
                                horarios);
        }

        /** Últimos puntos del recorrido (como máximo 500), en orden, como [longitud, latitud]. */
        private List<double[]> recorridoDe(Integer folio) {
                List<?> filas = entityManager.createNativeQuery("""
                                select longitud, latitud from (
                                    select numero_muestra, longitud, latitud
                                    from ubicacion_unidad
                                    where folio_ejecucion = :folio
                                    order by numero_muestra desc
                                    limit 500
                                ) ultimas
                                order by numero_muestra
                                """)
                                .setParameter("folio", folio)
                                .getResultList();

                return filas.stream()
                                .map(fila -> (Object[]) fila)
                                .map(fila -> new double[] {
                                                ((Number) fila[0]).doubleValue(),
                                                ((Number) fila[1]).doubleValue() })
                                .toList();
        }

        /** Incidencias abiertas (PENDIENTE o EN_PROCESO) del recorrido: solo tipo y hora. */
        private List<SeguimientoColoniaDTO.Aviso> avisosDe(Integer folio) {
                List<?> filas = entityManager.createNativeQuery("""
                                select titulo, fecha_hora
                                from incidencia
                                where folio_ejecucion = :folio
                                  and estado in ('PENDIENTE', 'EN_PROCESO')
                                order by fecha_hora desc
                                """)
                                .setParameter("folio", folio)
                                .getResultList();

                return filas.stream()
                                .map(fila -> (Object[]) fila)
                                .map(fila -> new SeguimientoColoniaDTO.Aviso(
                                                (String) fila[0],
                                                aFecha(fila[1])))
                                .toList();
        }

        private OffsetDateTime aFecha(Object valor) {
                if (valor == null) {
                        return null;
                }
                if (valor instanceof OffsetDateTime fecha) {
                        return fecha;
                }
                if (valor instanceof Instant instante) {
                        return instante.atOffset(ZoneOffset.UTC);
                }
                if (valor instanceof Timestamp timestamp) {
                        return timestamp.toInstant().atOffset(ZoneOffset.UTC);
                }
                throw new IllegalStateException("Tipo de fecha no soportado: " + valor.getClass());
        }

        /** Devuelve la hora como "HH:mm". */
        private String aHora(Object valor) {
                if (valor == null) {
                        return null;
                }
                if (valor instanceof Time hora) {
                        return hora.toLocalTime().toString().substring(0, 5);
                }
                String texto = valor.toString();
                return texto.length() >= 5 ? texto.substring(0, 5) : texto;
        }
}
