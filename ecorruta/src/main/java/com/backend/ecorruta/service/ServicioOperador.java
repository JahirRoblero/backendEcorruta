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
                                and (e.fechaProgramada = :fecha or e.estado = :enCurso)
                                order by e.folioEjecucion
                                """, EjecucionRuta.class)
                                .setParameter("correo", correo)
                                .setParameter("fecha", dia)
                                .setParameter("enCurso", EstadoEjecucion.EN_CURSO)
                                .getResultList()
                                .stream()
                                .map(this::aDTO)
                                .toList();
        }

        @Transactional(readOnly = true)
        public EjecucionOperadorDTO obtenerEjecucion(String correo, Integer folio) {
                return aDTO(buscarEjecucionPropia(correo, folio));
        }

        /**
         * Crea una ejecución nueva a partir de la asignación fija que el administrador
         * configuró. Se llama una vez por día de trabajo, al tocar "Iniciar".
         */
        @Transactional
        public EjecucionOperadorDTO iniciarAsignacion(String correo, Long idAsignacion) {
                Usuario operador = usuarioRepository.findByCorreoIgnoreCase(correo)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "El conductor no existe"));
                entityManager.lock(operador, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);

                if (!operador.isActivo() || !"CONDUCTOR".equals(operador.getRol().getNombre())) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                "Solo un conductor activo puede iniciar recorridos");
                }

                List<?> filas = entityManager.createNativeQuery("""
                        SELECT ruta_nombre, placa_vehiculo, dias_semana_mask
                        FROM asignacion_operador
                        WHERE id_asignacion = :id AND correo_operador = :correo AND activo = TRUE
                        FOR UPDATE
                        """)
                        .setParameter("id", idAsignacion)
                        .setParameter("correo", operador.getCorreo())
                        .getResultList();

                if (filas.isEmpty()) {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "No tienes esa asignación activa");
                }

                Object[] fila = (Object[]) filas.get(0);
                LocalDate hoy = LocalDate.now(ZONA_OAXACA);
                int bitDia = 1 << (hoy.getDayOfWeek().getValue() - 1);
                int mascaraDias = ((Number) fila[2]).intValue();
                if ((mascaraDias & bitDia) == 0) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT,
                                "Esta ruta no está asignada para el día de hoy");
                }

                Number ejecutadasHoy = (Number) entityManager.createNativeQuery("""
                        SELECT count(*) FROM ejecucion_ruta
                        WHERE asignacion_id = :id AND fecha_programada = :fecha
                        """)
                        .setParameter("id", idAsignacion)
                        .setParameter("fecha", hoy)
                        .getSingleResult();
                if (ejecutadasHoy.longValue() != 0) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT,
                                "El recorrido de esta asignación ya se inició hoy");
                }

                Number recorridosActivos = (Number) entityManager.createNativeQuery("""
                        SELECT count(*) FROM ejecucion_ruta
                        WHERE correo_operador = :correo AND estado = 'EN_CURSO'
                        """).setParameter("correo", operador.getCorreo()).getSingleResult();
                if (recorridosActivos.longValue() > 0) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT,
                                "Ya tienes otro recorrido en curso");
                }

                com.backend.ecorruta.model.Ruta ruta = entityManager.find(
                        com.backend.ecorruta.model.Ruta.class, (String) fila[0]);
                if (ruta == null || !ruta.isActivo()) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT,
                                "La ruta asignada está inactiva");
                }

                Vehiculo vehiculo = entityManager.find(Vehiculo.class, (String) fila[1],
                        jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
                if (vehiculo == null || !vehiculo.isActivo() ||
                        vehiculo.getEstado() != EstadoVehiculo.DISPONIBLE) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT,
                                "La unidad asignada no está disponible");
                }

                EjecucionRuta ejecucion = new EjecucionRuta();
                ejecucion.setAsignacionId(idAsignacion);
                ejecucion.setRuta(ruta);
                ejecucion.setVehiculo(vehiculo);
                ejecucion.setOperador(operador);
                ejecucion.setFechaProgramada(hoy);
                ejecucion.setFechaInicio(OffsetDateTime.now(ZONA_OAXACA));
                ejecucion.setEstado(EstadoEjecucion.EN_CURSO);
                vehiculo.setEstado(EstadoVehiculo.EN_RUTA);
                entityManager.persist(ejecucion);
                entityManager.flush();

                return aDTO(ejecucion);
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
                entityManager.lock(vehiculo, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);

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

}
