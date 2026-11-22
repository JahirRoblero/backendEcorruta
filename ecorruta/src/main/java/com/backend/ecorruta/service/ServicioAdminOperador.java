package com.backend.ecorruta.service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.backend.ecorruta.dto.OperadorAdminDTO;
import com.backend.ecorruta.dto.SolicitudCrearOperadorDTO;
import com.backend.ecorruta.dto.VehiculoAdminDTO;
import com.backend.ecorruta.enumeraciones.EstadoVehiculo;
import com.backend.ecorruta.model.Rol;
import com.backend.ecorruta.model.Usuario;
import com.backend.ecorruta.model.Vehiculo;
import com.backend.ecorruta.repository.RolRepository;
import com.backend.ecorruta.repository.UsuarioRepository;
import com.backend.ecorruta.repository.VehiculoRepository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServicioAdminOperador {

    private static final String ROL_OPERADOR = "CONDUCTOR";

    private final UsuarioRepository usuarioRepository;
    private final VehiculoRepository vehiculoRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;

    // Alta de conductor. La contraseña temporal se guarda cifrada (BCrypt)
    // y no se incluye en la respuesta.
    @Transactional
    public OperadorAdminDTO crearOperador(SolicitudCrearOperadorDTO datos) {
        String correo = datos.correo().trim().toLowerCase(Locale.ROOT);

        if (datos.contrasenaTemporal().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "La contraseña es demasiado larga");
        }

        if (usuarioRepository.findByCorreoIgnoreCase(correo).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Ya existe una cuenta con ese correo");
        }

        Rol rol = rolRepository.findById(ROL_OPERADOR)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE, "El rol CONDUCTOR no está configurado"));

        String apellidoMaterno = datos.apellidoMaterno() == null || datos.apellidoMaterno().isBlank()
                ? null
                : datos.apellidoMaterno().trim();

        Usuario usuario = Usuario.builder()
                .correo(correo)
                .rol(rol)
                .nombre(datos.nombre().trim())
                .apellidoPaterno(datos.apellidoPaterno().trim())
                .apellidoMaterno(apellidoMaterno)
                .passwordHash(passwordEncoder.encode(datos.contrasenaTemporal()))
                .activo(true)
                .build();

        Usuario guardado;
        try {
            // Como el id (correo) ya viene asignado, Spring Data usa merge y
            // devuelve OTRA instancia: esa es la que queda administrada.
            guardado = usuarioRepository.saveAndFlush(usuario);
        } catch (DataIntegrityViolationException e) {
            // Por si dos altas con el mismo correo llegan al mismo tiempo.
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Ya existe una cuenta con ese correo");
        }

        // fecha_registro la pone la base de datos; se recarga para devolverla.
        entityManager.refresh(guardado);
        return aDTO(guardado);
    }

    // Operadores = usuarios con rol CONDUCTOR. Reutiliza la consulta de Usuarios.
    @Transactional(readOnly = true)
    public List<OperadorAdminDTO> listarOperadores() {
        return usuarioRepository.buscarPorRolParaAdmin(ROL_OPERADOR)
                .stream()
                .map(this::aDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VehiculoAdminDTO> listarVehiculos(String estado) {
        List<Vehiculo> vehiculos = (estado == null || estado.isBlank())
                ? vehiculoRepository.buscarTodosParaAdmin()
                : vehiculoRepository.buscarPorEstadoParaAdmin(convertirEstado(estado));

        return vehiculos.stream().map(this::aDTO).toList();
    }

    private EstadoVehiculo convertirEstado(String texto) {
        try {
            return EstadoVehiculo.valueOf(texto.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado no válido: " + texto);
        }
    }

    private OperadorAdminDTO aDTO(Usuario u) {
        return new OperadorAdminDTO(
                u.getCorreo(),
                u.getNombre(),
                u.getApellidoPaterno(),
                u.getApellidoMaterno(),
                u.isActivo(),
                u.getFechaRegistro());
    }

    private VehiculoAdminDTO aDTO(Vehiculo v) {
        return new VehiculoAdminDTO(
                v.getPlaca(),
                v.getNumeroEconomico(),
                v.getMarca(),
                v.getModelo(),
                v.getCapacidadKg(),
                v.getEstado().name(),
                v.isActivo());
    }
}
