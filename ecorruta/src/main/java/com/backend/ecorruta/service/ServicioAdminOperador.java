package com.backend.ecorruta.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.backend.ecorruta.dto.OperadorAdminDTO;
import com.backend.ecorruta.dto.VehiculoAdminDTO;
import com.backend.ecorruta.enumeraciones.EstadoVehiculo;
import com.backend.ecorruta.model.Usuario;
import com.backend.ecorruta.model.Vehiculo;
import com.backend.ecorruta.repository.UsuarioRepository;
import com.backend.ecorruta.repository.VehiculoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServicioAdminOperador {

    private static final String ROL_OPERADOR = "CONDUCTOR";

    private final UsuarioRepository usuarioRepository;
    private final VehiculoRepository vehiculoRepository;

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
