package com.backend.ecorruta.service;

import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.backend.ecorruta.dto.UsuarioAdminDTO;
import com.backend.ecorruta.model.Colonia;
import com.backend.ecorruta.model.Rol;
import com.backend.ecorruta.model.Usuario;
import com.backend.ecorruta.repository.RolRepository;
import com.backend.ecorruta.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServicioAdminUsuario {

    private static final Set<String> ROLES_PERMITIDOS =
            Set.of("CIUDADANO", "CONDUCTOR", "ADMINISTRADOR");

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;

    // ===== Día 14: listar =====
    @Transactional(readOnly = true)
    public List<UsuarioAdminDTO> listar(String rol) {
        List<Usuario> usuarios;

        if (rol == null || rol.isBlank()) {
            usuarios = usuarioRepository.buscarTodosParaAdmin();
        } else {
            String rolNormalizado = rol.trim().toUpperCase();
            if (!rolRepository.existsById(rolNormalizado)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Rol no válido: " + rol);
            }
            usuarios = usuarioRepository.buscarPorRolParaAdmin(rolNormalizado);
        }

        return usuarios.stream().map(this::aDTO).toList();
    }

    // ===== Día 15: activar / desactivar =====
    @Transactional
    public void cambiarEstado(String correo, boolean activo, String correoAdmin) {
        Usuario usuario = buscarUsuario(correo);

        if (!activo && usuario.getCorreo().equalsIgnoreCase(correoAdmin)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "No puedes desactivar tu propia cuenta");
        }

        usuario.setActivo(activo);
    }

    // ===== Día 16: cambiar rol =====
    @Transactional
    public void cambiarRol(String correo, String rolNuevo, String correoAdmin) {
        String rolNormalizado = rolNuevo.trim().toUpperCase();

        if (!ROLES_PERMITIDOS.contains(rolNormalizado)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Rol no válido: " + rolNuevo);
        }

        Usuario usuario = buscarUsuario(correo);

        if (usuario.getCorreo().equalsIgnoreCase(correoAdmin)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "No puedes cambiar tu propio rol");
        }

        Rol rol = rolRepository.findById(rolNormalizado)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Rol no válido: " + rolNuevo));

        usuario.setRol(rol);
    }

    private Usuario buscarUsuario(String correo) {
        return usuarioRepository.findByCorreoIgnoreCase(correo.trim())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    private UsuarioAdminDTO aDTO(Usuario usuario) {
        Colonia colonia = usuario.getColonia();
        return new UsuarioAdminDTO(
                usuario.getCorreo(),
                usuario.getNombre(),
                usuario.getApellidoPaterno(),
                usuario.getApellidoMaterno(),
                usuario.getRol().getNombre(),
                usuario.isActivo(),
                colonia != null ? colonia.getNombreColonia() : null,
                colonia != null ? colonia.getCodigoPostal() : null,
                usuario.getFechaRegistro());
    }
}