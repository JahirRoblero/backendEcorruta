package com.backend.ecorruta.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.backend.ecorruta.dto.AsignacionOperadorDTO;
import com.backend.ecorruta.dto.EjecucionOperadorDTO;
import com.backend.ecorruta.dto.OpcionesAsignacionDTO;
import com.backend.ecorruta.dto.SolicitudAsignacionRecurrenteDTO;
import com.backend.ecorruta.model.EjecucionRuta;
import com.backend.ecorruta.model.Ruta;
import com.backend.ecorruta.model.Usuario;
import com.backend.ecorruta.model.Vehiculo;
import com.backend.ecorruta.repository.UsuarioRepository;
import com.backend.ecorruta.enumeraciones.EstadoVehiculo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServicioAsignaciones {

    private static final ZoneId ZONA_OAXACA = ZoneId.of("America/Mexico_City");
    private final EntityManager entityManager;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public OpcionesAsignacionDTO opciones() {
        List<OpcionesAsignacionDTO.Operador> operadores = entityManager.createQuery("""
                select u from Usuario u
                where u.activo = true and u.rol.nombre = 'CONDUCTOR'
                order by u.nombre, u.apellidoPaterno
                """, Usuario.class).getResultList().stream()
                .map(u -> new OpcionesAsignacionDTO.Operador(
                        u.getCorreo(), u.getNombre() + " " + u.getApellidoPaterno()))
                .toList();

        List<OpcionesAsignacionDTO.Ruta> rutas = entityManager.createQuery("""
                select r from Ruta r where r.activo = true order by r.nombre
                """, Ruta.class).getResultList().stream()
                .map(r -> new OpcionesAsignacionDTO.Ruta(r.getNombre())).toList();

        List<OpcionesAsignacionDTO.Vehiculo> vehiculos = entityManager.createQuery("""
                select v from Vehiculo v where v.activo = true
                order by v.numeroEconomico
                """, Vehiculo.class).getResultList().stream()
                .map(v -> new OpcionesAsignacionDTO.Vehiculo(
                        v.getPlaca(), v.getNumeroEconomico(), v.getEstado().name()))
                .toList();

        return new OpcionesAsignacionDTO(operadores, rutas, vehiculos);
    }

    /** Historia diaria (se mantiene por compatibilidad con los recorridos previos). */
    @Transactional(readOnly = true)
    public List<EjecucionOperadorDTO> ejecuciones(LocalDate fecha) {
        LocalDate dia = fecha == null ? LocalDate.now(ZONA_OAXACA) : fecha;
        return entityManager.createQuery("""
                select e from EjecucionRuta e
                join fetch e.ruta join fetch e.vehiculo
                where e.fechaProgramada = :fecha
                order by e.folioEjecucion
                """, EjecucionRuta.class)
                .setParameter("fecha", dia).getResultList()
                .stream().map(this::aDTO).toList();
    }

    public static int mascaraDias(List<Integer> dias) {
        if (dias == null || dias.isEmpty() || dias.stream().anyMatch(d -> d == null || d < 1 || d > 7)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Selecciona al menos un día válido (lunes=1 a domingo=7)");
        }
        int mascara = 0;
        for (Integer dia : dias) mascara |= (1 << (dia - 1));
        return mascara;
    }

    public static List<Integer> diasDeMascara(int mascara) {
        List<Integer> resultado = new ArrayList<>();
        for (int dia = 1; dia <= 7; dia++) {
            if ((mascara & (1 << (dia - 1))) != 0) resultado.add(dia);
        }
        return resultado;
    }

    @Transactional(readOnly = true)
    public List<AsignacionOperadorDTO> listar() {
        return obtenerAsignaciones(null);
    }

    @Transactional(readOnly = true)
    public List<AsignacionOperadorDTO> listarDelOperador(String correo) {
        return obtenerAsignaciones(correo);
    }

    @SuppressWarnings("unchecked")
    private List<AsignacionOperadorDTO> obtenerAsignaciones(String correo) {
        var consulta = entityManager.createNativeQuery("""
                SELECT a.id_asignacion, a.correo_operador,
                       u.nombre || ' ' || u.apellido_paterno,
                       a.ruta_nombre, r.descripcion,
                       a.placa_vehiculo, v.numero_economico, a.dias_semana_mask, a.activo,
                       EXISTS (
                           SELECT 1 FROM ejecucion_ruta e
                           WHERE e.asignacion_id = a.id_asignacion
                             AND e.fecha_programada = CAST((NOW() AT TIME ZONE 'America/Mexico_City') AS DATE)
                       ) AS iniciada_hoy
                FROM asignacion_operador a
                JOIN usuario u ON u.correo = a.correo_operador
                JOIN ruta r ON r.nombre = a.ruta_nombre
                JOIN vehiculo v ON v.placa = a.placa_vehiculo
                WHERE a.activo = TRUE
                """ + (correo == null
                        ? " ORDER BY u.nombre, a.ruta_nombre"
                        : " AND LOWER(a.correo_operador) = LOWER(:correo) ORDER BY u.nombre, a.ruta_nombre"));
        if (correo != null) consulta.setParameter("correo", correo);
        List<Object[]> filas = consulta.getResultList();
        return filas.stream().map(fila -> new AsignacionOperadorDTO(
                ((Number) fila[0]).longValue(),
                (String) fila[1], (String) fila[2],
                (String) fila[3], (String) fila[4],
                (String) fila[5], (String) fila[6],
                diasDeMascara(((Number) fila[7]).intValue()),
                (Boolean) fila[8], (Boolean) fila[9])).toList();
    }

    /**
     * Crea una asignación fija con días de trabajo. El conductor iniciará un
     * recorrido nuevo cada día permitido; no se precargan cientos de ejecuciones.
     */
    @Transactional
    public AsignacionOperadorDTO asignarRecurrente(SolicitudAsignacionRecurrenteDTO solicitud) {
        int mascara = mascaraDias(solicitud.diasSemana());
        String correo = solicitud.correoOperador().trim().toLowerCase(Locale.ROOT);

        Usuario operador = usuarioRepository.findByCorreoIgnoreCase(correo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El conductor no existe"));
        entityManager.lock(operador, LockModeType.PESSIMISTIC_WRITE);

        if (!operador.isActivo() || !"CONDUCTOR".equals(operador.getRol().getNombre())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecciona un conductor activo");
        }

        Ruta ruta = entityManager.find(Ruta.class, solicitud.rutaNombre().trim());
        if (ruta == null || !ruta.isActivo()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecciona una ruta activa");
        }

        Vehiculo vehiculo = entityManager.find(
                Vehiculo.class, solicitud.placaVehiculo().trim(), LockModeType.PESSIMISTIC_WRITE);
        if (vehiculo == null || !vehiculo.isActivo() ||
                vehiculo.getEstado() == EstadoVehiculo.MANTENIMIENTO ||
                vehiculo.getEstado() == EstadoVehiculo.FUERA_SERVICIO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La unidad está inactiva o en mantenimiento");
        }

        Number existente = (Number) entityManager.createNativeQuery("""
                SELECT count(*) FROM asignacion_operador
                WHERE correo_operador = :correo AND activo = TRUE
                """).setParameter("correo", operador.getCorreo()).getSingleResult();
        if (existente.longValue() > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El conductor ya tiene una asignación permanente. Desactívala para cambiarla.");
        }

        Number conflicto = (Number) entityManager.createNativeQuery("""
                SELECT count(*) FROM asignacion_operador
                WHERE placa_vehiculo = :placa AND activo = TRUE
                  AND (dias_semana_mask & :mascara) <> 0
                """)
                .setParameter("placa", vehiculo.getPlaca())
                .setParameter("mascara", mascara)
                .getSingleResult();
        if (conflicto.longValue() > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La unidad ya está asignada a otro conductor en alguno de esos días");
        }

        Number id = (Number) entityManager.createNativeQuery("""
                INSERT INTO asignacion_operador
                  (correo_operador, ruta_nombre, placa_vehiculo, dias_semana_mask, activo)
                VALUES (:correo, :ruta, :placa, :mascara, TRUE)
                RETURNING id_asignacion
                """)
                .setParameter("correo", operador.getCorreo())
                .setParameter("ruta", ruta.getNombre())
                .setParameter("placa", vehiculo.getPlaca())
                .setParameter("mascara", mascara)
                .getSingleResult();

        return new AsignacionOperadorDTO(id.longValue(), operador.getCorreo(),
                operador.getNombre() + " " + operador.getApellidoPaterno(),
                ruta.getNombre(), ruta.getDescripcion(), vehiculo.getPlaca(),
                vehiculo.getNumeroEconomico(), diasDeMascara(mascara), true, false);
    }

    @Transactional
    public void desactivar(Long idAsignacion) {
        List<?> filas = entityManager.createNativeQuery("""
                SELECT correo_operador FROM asignacion_operador
                WHERE id_asignacion = :id AND activo = TRUE
                """).setParameter("id", idAsignacion).getResultList();
        if (filas.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Asignación no encontrada");
        }

        Usuario conductor = entityManager.find(Usuario.class, (String) filas.get(0),
                LockModeType.PESSIMISTIC_WRITE);
        if (conductor == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        // No revoca un recorrido que ya comenzó: este puede finalizar normalmente.
        entityManager.createNativeQuery("""
                UPDATE asignacion_operador SET activo = FALSE
                WHERE id_asignacion = :id
                """).setParameter("id", idAsignacion).executeUpdate();
    }

    private EjecucionOperadorDTO aDTO(EjecucionRuta e) {
        return new EjecucionOperadorDTO(
                e.getFolioEjecucion(), e.getRuta().getNombre(),
                e.getRuta().getDescripcion(), e.getRuta().getGeometria(),
                e.getVehiculo().getPlaca(), e.getVehiculo().getNumeroEconomico(),
                e.getFechaProgramada(), e.getFechaInicio(), e.getFechaFin(),
                e.getEstado().name(), e.getObservaciones());
    }
}
