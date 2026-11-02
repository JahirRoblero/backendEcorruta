
package com.backend.ecorruta.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.backend.ecorruta.model.Usuario;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class UtilidadJwt {

    @Value("${jwt.secret}")
    private String secreto;

    @Value("${jwt.expiracion-milisegundos}")
    private long expiracionMilisegundos;

    public String generarToken(Usuario usuario) {
        Date fechaActual = new Date();
        Date fechaExpiracion = new Date(fechaActual.getTime() + expiracionMilisegundos);

        return Jwts.builder()
                .subject(usuario.getCorreo())
                .claim("rol", usuario.getRol().getNombre())
                .issuedAt(fechaActual)
                .expiration(fechaExpiracion)
                .signWith(obtenerClave())
                .compact();
    }

    public String obtenerCorreo(String token) {
        return obtenerClaims(token).getSubject();
    }

    public boolean esValido(
            String token,
            UserDetails usuario) {
        try {
            Claims claims = obtenerClaims(token);

            return claims.getSubject().equalsIgnoreCase(usuario.getUsername())
                    && claims.getExpiration().after(new Date())
                    && usuario.isEnabled();

        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims obtenerClaims(String token) {
        return Jwts.parser()
                .verifyWith(obtenerClave())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey obtenerClave() {
        byte[] bytes = Decoders.BASE64.decode(secreto);
        return Keys.hmacShaKeyFor(bytes);
    }
}
