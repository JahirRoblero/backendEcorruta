package com.backend.ecorruta.service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.backend.ecorruta.dto.SolicitudRegistro;

@Service
public class ServicioRegistro {

    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwordEncoder;

    public ServicioRegistro(
            JdbcTemplate jdbc,
            PasswordEncoder passwordEncoder) {

        this.jdbc = jdbc;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public String registrar(SolicitudRegistro datos) {

        // 1. Validar contraseñas
        if (!datos.contrasena().equals(
                datos.confirmarContrasena())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Las contraseñas no coinciden");
        }

        if (datos.contrasena()
                .getBytes(StandardCharsets.UTF_8).length > 72) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La contraseña es demasiado larga");
        }

        // 2. Normalizar los datos
        String correo = datos.correo()
                .trim()
                .toLowerCase(Locale.ROOT);

        String codigoPostal = datos.codigoPostal().trim();
        String coloniaNombre = datos.coloniaNombre().trim();

        String nombre = datos.nombre().trim();
        String apellidoPaterno = datos.apellidoPaterno().trim();

        String apellidoMaterno = datos.apellidoMaterno() == null ||
                datos.apellidoMaterno().isBlank()
                        ? null
                        : datos.apellidoMaterno().trim();

        // 3. Verificar que exista el rol CIUDADANO
        Boolean existeRol = jdbc.queryForObject(
                """
                        SELECT EXISTS (
                            SELECT 1
                            FROM rol
                            WHERE nombre = 'CIUDADANO'
                        )
                        """,
                Boolean.class);

        if (!Boolean.TRUE.equals(existeRol)) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "El rol CIUDADANO no está configurado");
        }

        // 4. Validar la colonia y el código postal
        // contra la base de datos de EcoRuta.
        List<String> colonias = jdbc.queryForList(
                """
                        SELECT nombre
                        FROM colonia
                        WHERE codigo_postal = ?
                          AND LOWER(nombre) = LOWER(?)
                          AND activo = TRUE
                        """,
                String.class,
                codigoPostal,
                coloniaNombre);

        if (colonias.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "La colonia seleccionada no está disponible en EcoRuta");
        }

        // Usar el nombre exactamente como está en Neon
        String coloniaRegistrada = colonias.get(0);

        // 5. Cifrar la contraseña con el BCrypt
        // que ya tiene configurado Spring Security.
        String passwordHash = passwordEncoder.encode(datos.contrasena());

        // 6. Registrar el usuario.
        // ON CONFLICT evita duplicados incluso si
        // llegan dos solicitudes simultáneas.
        int filas = jdbc.update(
                """
                        INSERT INTO usuario (
                            correo,
                            rol_nombre,
                            colonia_nombre,
                            colonia_codigo_postal,
                            nombre,
                            apellido_paterno,
                            apellido_materno,
                            password_hash,
                            activo
                        )
                        VALUES (?, 'CIUDADANO', ?, ?, ?, ?, ?, ?, TRUE)
                        ON CONFLICT (correo) DO NOTHING
                        """,
                correo,
                coloniaRegistrada,
                codigoPostal,
                nombre,
                apellidoPaterno,
                apellidoMaterno,
                passwordHash);

        if (filas == 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe una cuenta con ese correo");
        }

        return correo;
    }
}
