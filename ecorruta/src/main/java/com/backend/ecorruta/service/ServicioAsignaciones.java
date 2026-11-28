package com.backend.ecorruta.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.backend.ecorruta.dto.EjecucionOperadorDTO;
import com.backend.ecorruta.dto.OpcionesAsignacionDTO;
import com.backend.ecorruta.dto.SolicitudAsignacionDTO;
import com.backend.ecorruta.enumeraciones.EstadoEjecucion;
import com.backend.ecorruta.enumeraciones.EstadoVehiculo;
import com.backend.ecorruta.model.EjecucionRuta;
import com.backend.ecorruta.model.Ruta;
import com.backend.ecorruta.model.Usuario;
import com.backend.ecorruta.model.Vehiculo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServicioAsignaciones {

    private static final ZoneId ZONA_OAXACA = ZoneId.of("America/Mexico_City");
    private final EntityManager entityManager;

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

    @Transactional(readOnly = true)
    public List<EjecucionOperadorDTO> ejecuciones(LocalDate fecha) {
        LocalDate dia = fecha == null ? LocalDate.now(ZONA_OAXACA) : fecha;
        return entityManager.createQuery("""
                select e from EjecucionRuta e
                join fetch e.ruta
                join fetch e.vehiculo
                where e.fechaProgramada = :fecha
                order by e.folioEjecucion
                """, EjecucionRuta.class)
                .setParameter("fecha", dia)
                .getResultList().stream().map(this::aDTO).toList();
    }

    @Transactional
    public EjecucionOperadorDTO asignar(SolicitudAsignacionDTO solicitud) {
        LocalDate fecha = solicitud.fechaProgramada();
        if (fecha.isBefore(LocalDate.now(ZONA_OAXACA))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No puedes asignar recorridos en fechas pasadas");
        }

        String correo = solicitud.correoOperador().trim().toLowerCase();
        Usuario operador = entityManager.find(Usuario.class, correo, LockModeType.PESSIMISTIC_WRITE);
        if (operador == null || !operador.isActivo() ||
                !"CONDUCTOR".equals(operador.getRol().getNombre())) {
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
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La unidad no está disponible");
        }

        // No asignar unidad a un recorrido simultáneo que ya esté en curso.
        if (vehiculo.getEstado() == EstadoVehiculo.EN_RUTA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La unidad está en ruta");
        }

        Long asignacionesConductor = entityManager.createQuery("""
                select count(e) from EjecucionRuta e
                where e.operador = :operador and e.fechaProgramada = :fecha
                  and e.estado <> :cancelada
                """, Long.class)
                .setParameter("operador", operador)
                .setParameter("fecha", fecha)
                .setParameter("cancelada", EstadoEjecucion.CANCELADA)
                .getSingleResult();

        if (asignacionesConductor > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Este conductor ya tiene un recorrido asignado para esa fecha");
        }

        Long asignacionesUnidad = entityManager.createQuery("""
                select count(e) from EjecucionRuta e
                where e.vehiculo = :vehiculo and e.fechaProgramada = :fecha
                  and e.estado <> :cancelada
                """, Long.class)
                .setParameter("vehiculo", vehiculo)
                .setParameter("fecha", fecha)
                .setParameter("cancelada", EstadoEjecucion.CANCELADA)
                .getSingleResult();

        if (asignacionesUnidad > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Esta unidad ya tiene un recorrido asignado para esa fecha");
        }

        EjecucionRuta ejecucion = new EjecucionRuta();
        ejecucion.setOperador(operador);
        ejecucion.setRuta(ruta);
        ejecucion.setVehiculo(vehiculo);
        ejecucion.setFechaProgramada(fecha);
        ejecucion.setEstado(EstadoEjecucion.PENDIENTE);
        entityManager.persist(ejecucion);
        entityManager.flush();
        return aDTO(ejecucion);
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
