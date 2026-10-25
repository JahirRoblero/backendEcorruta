package com.backend.ecorruta.security;

import java.io.IOException;

import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FiltroJwt extends OncePerRequestFilter {

    // Aquí puedes implementar la lógica para validar el JWT y autenticar al usuario
    // Por ejemplo, puedes extraer el token del encabezado Authorization y verificar
    // su validez
    @Override
    protected void doFilterInternal(HttpServletRequest solicitud, HttpServletResponse respuesta, FilterChain cadena)
            throws ServletException, IOException {
        String encabezado = solicitud.getHeader("Authorization");

        if (encabezado == null || !encabezado.startsWith("Bearer")) {
            cadena.doFilter(solicitud, respuesta);
            return;
        }

        String token = encabezado.substring(7);

        try {
            // String correo = utilidadJwt.obtenerCorreo(token);

        } catch (Exception e) {
            respuesta.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token no válido");
            return;
        }
    }

}
