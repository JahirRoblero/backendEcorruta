package com.backend.ecorruta.service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.backend.ecorruta.dto.EjecucionOperadorDTO;
import com.backend.ecorruta.dto.IncidenciaRespuestaDTO;
import com.backend.ecorruta.dto.SolicitudCrearIncidenciaDTO;
import com.backend.ecorruta.dto.SolicitudFinalizarRecorridoDTO;
import com.backend.ecorruta.dto.SolicitudUbicacionDTO;
import com.backend.ecorruta.dto.UbicacionRespuestaDTO;
import com.backend.ecorruta.enumeraciones.EstadoEjecucion;
import com.backend.ecorruta.enumeraciones.EstadoIncidencia;
import com.backend.ecorruta.enumeraciones.EstadoVehiculo;
import com.backend.ecorruta.model.EjecucionRuta;
import com.backend.ecorruta.model.Incidencia;
import com.backend.ecorruta.model.Usuario;
import com.backend.ecorruta.model.Vehiculo;
import com.backend.ecorruta.repository.IncidenciaRepository;
import com.backend.ecorruta.repository.UsuarioRepository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

/**
 * Lógica de negocio del operador (rol CONDUCTOR):
 * consultar sus ejecuciones, iniciar y finalizar recorridos (CU-06),
 * registrar la ubicación de la unidad (CU-07) y registrar incidencias (HU-16).
 *
 * Las consultas se hacen con EntityManager porque los repositorios
 * compartidos no deben modificarse.
 */
@Service
@RequiredArgsConstructor
public class ServicioOperador {

        private static final ZoneId ZONA_OAXACA = ZoneId.of("America/Mexico_City");

        /** Distancia mínima (en metros) para guardar una nueva muestra de ubicación (RNF10). */
        private static final double DISTANCIA_MINIMA_METROS = 10.0;

        private static final String PREFIJO_INCOMPLETO = "[Recorrido incompleto] ";

        private final EntityManager entityManager;
        private final UsuarioRepository usuarioRepository;
        private final IncidenciaRepository incidenciaRepository;
        private final ServicioStorageNeon servicioStorageNeon;

        // ------------------------------------------------------------
        // Consultar ejecuciones (RF25 / HU-12 CA4)
        // ------------------------------------------------------------

        @Transactional(readOnly = true)
        public List<EjecucionOperadorDTO> obtenerEjecuciones(String correo, LocalDate fecha) {

                LocalDate dia = fecha != null ? fecha : LocalDate.now(ZONA_OAXACA);

                return entityManager.createQuery("""
                                select e from EjecucionRuta e
                                join fetch e.ruta
                                join fetch e.vehiculo
                                where lower(e.operador.correo) = lower(:correo)
                                and e.fechaProgramada = :fecha
                                order by e.folioEjecucion
                                """, EjecucionRuta.class)
                                .setParameter("correo", correo)
                                .setParameter("fecha", dia)
                                .getResultList()
                                .stream()
                                .map(this::aDTO)
                                .toList();
        }

        @Transactional(readOnly = true)
        public EjecucionOperadorDTO obtenerEjecucion(String correo, Integer folio) {
                return aDTO(buscarEjecucionPropia(correo, folio));
        }

        // ------------------------------------------------------------
        // Iniciar recorrido (CU-06 / HU-12)
        // ------------------------------------------------------------

        @Transactional
        public EjecucionOperadorDTO iniciarRecorrido(String correo, Integer folio) {

                EjecucionRuta ejecucion = buscarEjecucionPropia(correo, folio);

                if (ejecucion.getEstado() != EstadoEjecucion.PENDIENTE) {
                        throw new ResponseStatusException(
                                        HttpStatus.CONFLICT,
                                        "Solo se puede iniciar un recorrido pendiente");
                }

                if (!ejecucion.getFechaProgramada().equals(LocalDate.now(ZONA_OAXACA))) {
                        throw new ResponseStatusException(
                                        HttpStatus.CONFLICT,
                                        "Este recorrido no está programado para hoy");
                }

                Long enCurso = entityManager.createQuery("""
                                select count(e) from EjecucionRuta e
                                where lower(e.operador.correo) = lower(:correo)
                                and e.estado = :estado
                                """, Long.class)
                                .setParameter("correo", correo)
                                .setParameter("estado", EstadoEjecucion.EN_CURSO)
                                .getSingleResult();

                if (enCurso > 0) {
                        throw new ResponseStatusException(
                                        HttpStatus.CONFLICT,
                                        "Ya tienes un recorrido en curso");
                }

                if (!ejecucion.getRuta().isActivo()) {
                        throw new ResponseStatusException(
                                        HttpStatus.CONFLICT,
                                        "La ruta está inactiva");
                }

                Vehiculo vehiculo = ejecucion.getVehiculo();

                if (!vehiculo.isActivo() || vehiculo.getEstado() != EstadoVehiculo.DISPONIBLE) {
                        throw new ResponseStatusException(
                                        HttpStatus.CONFLICT,
                                        "El vehículo no está disponible");
                }

                ejecucion.setFechaInicio(OffsetDateTime.now(ZONA_OAXACA));
                ejecucion.setEstado(EstadoEjecucion.EN_CURSO);
                vehiculo.setEstado(EstadoVehiculo.EN_RUTA);

                return aDTO(ejecucion);
        }

        // ------------------------------------------------------------
        // Finalizar recorrido (CU-06 / HU-14)
        // ------------------------------------------------------------

        @Transactional
        public EjecucionOperadorDTO finalizarRecorrido(
                        String correo,
                        Integer folio,
                        SolicitudFinalizarRecorridoDTO datos) {

                EjecucionRuta ejecucion = buscarEjecucionPropia(correo, folio);

                if (ejecucion.getEstado() != EstadoEjecucion.EN_CURSO) {
                        throw new ResponseStatusException(
                                        HttpStatus.CONFLICT,
                                        "Solo se puede finalizar un recorrido en curso");
                }

                String observaciones = datos.observaciones() == null
                                ? null
                                : datos.observaciones().trim();

                if (!datos.completado()) {
                        if (observaciones == null || observaciones.isEmpty()) {
                                throw new ResponseStatusException(
                                                HttpStatus.BAD_REQUEST,
                                                "Indica el motivo por el que el recorrido quedó incompleto");
                        }
                        observaciones = PREFIJO_INCOMPLETO + observaciones;
                }

                OffsetDateTime ahora = OffsetDateTime.now(ZONA_OAXACA);

                if (ejecucion.getFechaInicio() != null && ahora.isBefore(ejecucion.getFechaInicio())) {
                        throw new ResponseStatusException(
                                        HttpStatus.CONFLICT,
                                        "La hora de fin no puede ser anterior a la de inicio");
                }

                ejecucion.setFechaFin(ahora);
                ejecucion.setEstado(EstadoEjecucion.FINALIZADA);
                ejecucion.setObservaciones(
                                observaciones == null || observaciones.isEmpty() ? null : observaciones);
                ejecucion.getVehiculo().setEstado(EstadoVehiculo.DISPONIBLE);

                return aDTO(ejecucion);
        }

        // ------------------------------------------------------------
        // Registrar ubicación (CU-07 / HU-13)
        // ------------------------------------------------------------

        @Transactional
        public UbicacionRespuestaDTO registrarUbicacion(
                        String correo,
                        Integer folio,
                        SolicitudUbicacionDTO datos) {

                EjecucionRuta ejecucion = buscarEjecucionPropia(correo, folio);

                if (ejecucion.getEstado() != EstadoEjecucion.EN_CURSO) {
                        throw new ResponseStatusException(
                                        HttpStatus.CONFLICT,
                                        "El recorrido no está en curso");
                }

                BigDecimal latitud = datos.latitud().setScale(6, java.math.RoundingMode.HALF_UP);
                BigDecimal longitud = datos.longitud().setScale(6, java.math.RoundingMode.HALF_UP);

                // RNF10: no guardar posiciones repetidas o sin cambio relevante
                List<?> ultima = entityManager.createNativeQuery("""
                                select latitud, longitud from ubicacion_unidad
                                where folio_ejecucion = :folio
                                order by numero_muestra desc
                                limit 1
                                """)
                                .setParameter("folio", folio)
                                .getResultList();

                if (!ultima.isEmpty()) {
                        Object[] fila = (Object[]) ultima.get(0);
                        double distancia = distanciaMetros(
                                        ((Number) fila[0]).doubleValue(),
                                        ((Number) fila[1]).doubleValue(),
                                        latitud.doubleValue(),
                                        longitud.doubleValue());

                        if (distancia < DISTANCIA_MINIMA_METROS) {
                                return new UbicacionRespuestaDTO(false, null, latitud, longitud, null);
                        }
                }

                // numero_muestra es GENERATED ALWAYS AS IDENTITY dentro de una llave compuesta,
                // por eso se inserta con SQL nativo en lugar de repository.save().
                Object[] insertada = (Object[]) entityManager.createNativeQuery("""
                                insert into ubicacion_unidad (folio_ejecucion, latitud, longitud)
                                values (:folio, :latitud, :longitud)
                                returning numero_muestra, fecha_hora
                                """)
                                .setParameter("folio", folio)
                                .setParameter("latitud", latitud)
                                .setParameter("longitud", longitud)
                                .getSingleResult();

                return new UbicacionRespuestaDTO(
                                true,
                                ((Number) insertada[0]).intValue(),
                                latitud,
                                longitud,
                                aFecha(insertada[1]));
        }

        /** Historial de ubicaciones de una ejecución (RF04 / RF05). */
        @Transactional(readOnly = true)
        public List<UbicacionRespuestaDTO> obtenerUbicaciones(String correo, Integer folio) {

                buscarEjecucionPropia(correo, folio);

                List<?> filas = entityManager.createNativeQuery("""
                                select numero_muestra, latitud, longitud, fecha_hora
                                from ubicacion_unidad
                                where folio_ejecucion = :folio
                                order by numero_muestra
                                """)
                                .setParameter("folio", folio)
                                .getResultList();

                return filas.stream()
                                .map(fila -> (Object[]) fila)
                                .map(fila -> new UbicacionRespuestaDTO(
                                                true,
                                                ((Number) fila[0]).intValue(),
                                                (BigDecimal) fila[1],
                                                (BigDecimal) fila[2],
                                                aFecha(fila[3])))
                                .toList();
        }

        // ------------------------------------------------------------
        // Incidencias (HU-16 / RF31 / RF32)
        // ------------------------------------------------------------

        @Transactional
        public IncidenciaRespuestaDTO registrarIncidencia(
                        String correo,
                        Integer folio,
                        SolicitudCrearIncidenciaDTO datos,
                        MultipartFile imagen) {

                EjecucionRuta ejecucion = buscarEjecucionPropia(correo, folio);

                if (ejecucion.getEstado() != EstadoEjecucion.EN_CURSO) {
                        throw new ResponseStatusException(
                                        HttpStatus.CONFLICT,
                                        "Solo puedes registrar incidencias durante un recorrido en curso");
                }

                Usuario usuario = usuarioRepository
                                .findByCorreoIgnoreCase(correo)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "El usuario no existe"));

                String imagenUrl = servicioStorageNeon.guardarImagen(imagen);

                try {
                        Incidencia incidencia = new Incidencia();
                        incidencia.setEjecucionRuta(ejecucion);
                        incidencia.setUsuario(usuario);
                        incidencia.setTitulo(datos.titulo().trim());
                        incidencia.setDescripcion(datos.descripcion().trim());
                        incidencia.setEstado(EstadoIncidencia.PENDIENTE);
                        incidencia.setLatitud(datos.latitud());
                        incidencia.setLongitud(datos.longitud());
                        incidencia.setImagenUrl(imagenUrl);

                        Incidencia guardada = incidenciaRepository.saveAndFlush(incidencia);

                        // fecha_hora la asigna la base de datos
                        entityManager.refresh(guardada);

                        return aDTO(guardada, folio);

                } catch (RuntimeException error) {
                        try {
                                servicioStorageNeon.eliminarImagen(imagenUrl);
                        } catch (RuntimeException errorEliminacion) {
                                error.addSuppressed(errorEliminacion);
                        }
                        throw error;
                }
        }

        @Transactional(readOnly = true)
        public List<IncidenciaRespuestaDTO> obtenerIncidencias(String correo, Integer folio) {

                buscarEjecucionPropia(correo, folio);

                return entityManager.createQuery("""
                                select i from Incidencia i
                                where i.ejecucionRuta.folioEjecucion = :folio
                                order by i.idIncidencia desc
                                """, Incidencia.class)
                                .setParameter("folio", folio)
                                .getResultList()
                                .stream()
                                .map(incidencia -> aDTO(incidencia, folio))
                                .toList();
        }

        // ------------------------------------------------------------
        // Utilidades
        // ------------------------------------------------------------

        /**
         * Busca la ejecución y verifica que pertenezca al operador.
         * Si no es suya responde 404 para no revelar ejecuciones de otros.
         */
        private EjecucionRuta buscarEjecucionPropia(String correo, Integer folio) {

                List<EjecucionRuta> resultado = entityManager.createQuery("""
                                select e from EjecucionRuta e
                                join fetch e.ruta
                                join fetch e.vehiculo
                                where e.folioEjecucion = :folio
                                and lower(e.operador.correo) = lower(:correo)
                                """, EjecucionRuta.class)
                                .setParameter("folio", folio)
                                .setParameter("correo", correo)
                                .getResultList();

                if (resultado.isEmpty()) {
                        throw new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "No tienes asignada esa ejecución de ruta");
                }

                return resultado.get(0);
        }

        private EjecucionOperadorDTO aDTO(EjecucionRuta e) {
                return new EjecucionOperadorDTO(
                                e.getFolioEjecucion(),
                                e.getRuta().getNombre(),
                                e.getRuta().getDescripcion(),
                                e.getRuta().getGeometria(),
                                e.getVehiculo().getPlaca(),
                                e.getVehiculo().getNumeroEconomico(),
                                e.getFechaProgramada(),
                                e.getFechaInicio(),
                                e.getFechaFin(),
                                e.getEstado().name(),
                                e.getObservaciones());
        }

        private IncidenciaRespuestaDTO aDTO(Incidencia i, Integer folio) {
                return new IncidenciaRespuestaDTO(
                                i.getIdIncidencia(),
                                folio,
                                i.getTitulo(),
                                i.getDescripcion(),
                                i.getEstado().name(),
                                i.getFecha(),
                                i.getLatitud(),
                                i.getLongitud(),
                                i.getImagenUrl());
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

        /** Distancia aproximada entre dos coordenadas (fórmula de Haversine). */
        private double distanciaMetros(double lat1, double lon1, double lat2, double lon2) {
                double radioTierra = 6_371_000;
                double dLat = Math.toRadians(lat2 - lat1);
                double dLon = Math.toRadians(lon2 - lon1);
                double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                                                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
                return 2 * radioTierra * Math.asin(Math.sqrt(a));
        }
}
