package com.backend.ecorruta.controller;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.backend.ecorruta.dto.AutenticacionDTO;
import com.backend.ecorruta.model.Usuario;
import com.backend.ecorruta.repository.UsuarioRepository;
import com.backend.ecorruta.service.ServicioAutenticacion;
import com.backend.ecorruta.service.ServicioVerificacionCorreo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class ControladorAutenticacion {

        private final ServicioAutenticacion servicioAutenticacion;
        private final ServicioVerificacionCorreo servicioVerificacionCorreo;
        private final UsuarioRepository usuarioRepository;

        @PostMapping("/registro/enviar-codigo")
        public ResponseEntity<Map<String, String>> enviarCodigo(
                        @Valid @RequestBody AutenticacionDTO.SolicitudCorreo solicitud) {
                String mensaje = servicioVerificacionCorreo.enviarCodigo(solicitud.correo());
                return ResponseEntity.ok(Map.of("mensaje", mensaje));
        }

        @PostMapping("/registro")
        public ResponseEntity<AutenticacionDTO.Respuesta> registrar(
                        @Valid @RequestBody AutenticacionDTO.SolicitudRegistro solicitud) {
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(servicioAutenticacion.registrar(solicitud));
        }

        @PostMapping("/login")
        public ResponseEntity<AutenticacionDTO.Respuesta> login(
                        @Valid @RequestBody AutenticacionDTO.SolicitudLogin solicitud) {
                return ResponseEntity.ok(servicioAutenticacion.iniciarSesion(solicitud));
        }

        @GetMapping("/privado")
        public ResponseEntity<?> privado(Authentication autenticacion) {

                Usuario usuario = usuarioRepository
                                .findByCorreoIgnoreCase(autenticacion.getName())
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Usuario no encontrado"));

                return ResponseEntity.ok(
                                Map.of(
                                                "mensaje", "JWT funcionando correctamente",
                                                "usuario", usuario.getCorreo(),
                                                "nombre", usuario.getNombre()));
        }

}
