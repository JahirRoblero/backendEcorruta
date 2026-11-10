
package com.backend.ecorruta.service;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.backend.ecorruta.dto.AutenticacionDTO;
import com.backend.ecorruta.model.Colonia;
import com.backend.ecorruta.model.Rol;
import com.backend.ecorruta.model.Usuario;

import com.backend.ecorruta.repository.ColoniaRepository;
import com.backend.ecorruta.repository.RolRepository;
import com.backend.ecorruta.repository.UsuarioRepository;

import com.backend.ecorruta.security.UtilidadJwt;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServicioAutenticacion {

        private final UsuarioRepository usuarioRepository;
        private final RolRepository rolRepository;
        private final ColoniaRepository coloniaRepository;

        private final PasswordEncoder passwordEncoder;
        private final AuthenticationManager authenticationManager;
        private final UtilidadJwt utilidadJwt;

        @Transactional
        public AutenticacionDTO.Respuesta registrar(
                        AutenticacionDTO.SolicitudRegistro solicitud) {

                if (!solicitud.contrasena().equals(
                                solicitud.confirmarContrasena())) {

                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Las contraseñas no coinciden");
                }

                if (solicitud.contrasena()
                                .getBytes(StandardCharsets.UTF_8).length > 72) {

                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "La contraseña es demasiado larga");
                }

                String correo = solicitud.correo()
                                .trim()
                                .toLowerCase(Locale.ROOT);

                if (usuarioRepository
                                .findByCorreoIgnoreCase(correo)
                                .isPresent()) {

                        throw new ResponseStatusException(
                                        HttpStatus.CONFLICT,
                                        "El correo ya está registrado");
                }
                Rol rol = rolRepository.findById("CIUDADANO")
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.INTERNAL_SERVER_ERROR,
                                                "No existe el rol CIUDADANO"));

                String coloniaNombre = solicitud.coloniaNombre().trim();

                String codigoPostal = solicitud.codigoPostal().trim();

                Colonia colonia = coloniaRepository
                                .findByNombreColoniaAndCodigoPostal(
                                                coloniaNombre,
                                                codigoPostal)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.UNPROCESSABLE_ENTITY,
                                                "La colonia no está registrada en EcoRuta"));

                if (!colonia.isActivo()) {
                        throw new ResponseStatusException(
                                        HttpStatus.UNPROCESSABLE_ENTITY,
                                        "Esta colonia no está disponible en EcoRuta");
                }

                Usuario usuario = Usuario.builder()
                                .correo(correo)
                                .nombre(solicitud.nombre().trim())
                                .apellidoPaterno(
                                                solicitud.apellidoPaterno().trim())
                                .apellidoMaterno(
                                                solicitud.apellidoMaterno() == null ||
                                                                solicitud.apellidoMaterno().isBlank()
                                                                                ? null
                                                                                : solicitud.apellidoMaterno().trim())
                                .passwordHash(
                                                passwordEncoder.encode(
                                                                solicitud.contrasena()))
                                .rol(rol)
                                .colonia(colonia)
                                .activo(true)
                                .build();

                usuarioRepository.saveAndFlush(usuario);

                String token = utilidadJwt.generarToken(usuario);

                return new AutenticacionDTO.Respuesta(
                                token,
                                usuario.getCorreo(),
                                usuario.getRol().getNombre());
        }

        public AutenticacionDTO.Respuesta iniciarSesion(
                        AutenticacionDTO.SolicitudLogin solicitud) {

                Authentication autenticacion = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                solicitud.correo().trim(),
                                                solicitud.contrasena()));

                Usuario usuario = (Usuario) autenticacion.getPrincipal();

                String token = utilidadJwt.generarToken(usuario);

                return new AutenticacionDTO.Respuesta(
                                token,
                                usuario.getCorreo(),
                                usuario.getRol().getNombre());
        }
}
