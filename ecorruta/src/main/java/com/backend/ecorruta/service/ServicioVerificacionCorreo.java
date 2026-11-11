package com.backend.ecorruta.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.Locale;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.backend.ecorruta.model.VerificacionCorreo;
import com.backend.ecorruta.repository.UsuarioRepository;
import com.backend.ecorruta.repository.VerificacionCorreoRepository;

@Service
public class ServicioVerificacionCorreo {

    private static final int MAX_INTENTOS = 5;
    private static final int MAX_ENVIOS_HORA = 5;
    private static final String RESPUESTA_GENERICA =
            "Si el correo puede registrarse, recibirás un código";

    private final UsuarioRepository usuarioRepository;
    private final VerificacionCorreoRepository verificacionRepository;
    private final JdbcTemplate jdbcTemplate;
    private final ServicioCorreo servicioCorreo;
    private final SecretKeySpec claveHmac;
    private final SecureRandom random = new SecureRandom();

    public ServicioVerificacionCorreo(
            UsuarioRepository usuarioRepository,
            VerificacionCorreoRepository verificacionRepository,
            JdbcTemplate jdbcTemplate,
            ServicioCorreo servicioCorreo,
            @Value("${verificacion.hmac-secret}") String secreto) {
        if (secreto == null || secreto.length() < 32) {
            throw new IllegalStateException(
                    "VERIFICACION_HMAC_SECRET debe tener al menos 32 caracteres");
        }
        this.usuarioRepository = usuarioRepository;
        this.verificacionRepository = verificacionRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.servicioCorreo = servicioCorreo;
        this.claveHmac = new SecretKeySpec(
                secreto.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Transactional
    public String enviarCodigo(String correoIngresado) {
        String correo = correoIngresado.trim().toLowerCase(Locale.ROOT);

        // Evitar indicar si una cuenta ya está registrada.
        if (usuarioRepository.findByCorreoIgnoreCase(correo).isPresent()) {
            return RESPUESTA_GENERICA;
        }

        // Garantiza que la fila exista antes de obtener un bloqueo exclusivo.
        // Evita que dos peticiones simultáneas para un correo nuevo envíen dos códigos.
        jdbcTemplate.update("""
                INSERT INTO verificacion_correo
                  (correo, codigo_hash, fecha_expiracion, fecha_ultimo_envio,
                   intentos, inicio_ventana, envios_ventana)
                VALUES (?, '', CURRENT_TIMESTAMP,
                        CURRENT_TIMESTAMP - INTERVAL '1 day',
                        0, CURRENT_TIMESTAMP, 0)
                ON CONFLICT (correo) DO NOTHING
                """, correo);

        VerificacionCorreo verificacion = verificacionRepository
                .buscarConBloqueo(correo)
                .orElseThrow(() -> new IllegalStateException(
                        "No se pudo crear la verificación"));

        OffsetDateTime ahora = OffsetDateTime.now(ZoneOffset.UTC);

        if (ahora.isBefore(verificacion.getFechaUltimoEnvio().plusSeconds(60))) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Espera 60 segundos antes de solicitar otro código");
        }

        if (!ahora.isBefore(verificacion.getInicioVentana().plusHours(1))) {
            verificacion.setInicioVentana(ahora);
            verificacion.setEnviosVentana(0);
        }

        if (verificacion.getEnviosVentana() >= MAX_ENVIOS_HORA) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Límite de códigos por hora alcanzado");
        }

        String codigo = "%05d".formatted(random.nextInt(100_000));
        verificacion.setCodigoHash(calcularHmac(correo, codigo));
        verificacion.setFechaUltimoEnvio(ahora);
        verificacion.setFechaExpiracion(ahora.plusMinutes(10));
        verificacion.setIntentos(0);
        verificacion.setEnviosVentana(verificacion.getEnviosVentana() + 1);

        // Si Resend rechaza el envío, la transacción se revierte.
        servicioCorreo.enviarCodigo(correo, codigo);
        return RESPUESTA_GENERICA;
    }

    // Se llama dentro de la transacción de ServicioAutenticacion.registrar().
    // La transacción externa usa noRollbackFor=ResponseStatusException.class
    // para CONSERVAR los intentos incorrectos aunque se devuelva 400.
    public void validarYConsumir(String correo, String codigo) {
        VerificacionCorreo verificacion = verificacionRepository
                .buscarConBloqueo(correo)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Solicita primero un código"));

        OffsetDateTime ahora = OffsetDateTime.now(ZoneOffset.UTC);
        if (!ahora.isBefore(verificacion.getFechaExpiracion())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El código venció. Solicita otro");
        }

        if (verificacion.getIntentos() >= MAX_INTENTOS) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Demasiados intentos. Solicita otro código");
        }

        String hmacIngresado = calcularHmac(correo, codigo);
        boolean coincide = MessageDigest.isEqual(
                verificacion.getCodigoHash().getBytes(StandardCharsets.US_ASCII),
                hmacIngresado.getBytes(StandardCharsets.US_ASCII));

        if (!coincide) {
            verificacion.setIntentos(verificacion.getIntentos() + 1);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El código es incorrecto");
        }

        // Si falla la creación del usuario, la transacción revierte también
        // el borrado, y no se consume indebidamente el código.
        verificacionRepository.delete(verificacion);
    }

    private String calcularHmac(String correo, String codigo) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(claveHmac);
            byte[] hash = mac.doFinal(
                    (correo + ":" + codigo).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception ex) {
            throw new IllegalStateException("Error criptográfico", ex);
        }
    }
}
