package com.backend.ecorruta.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.backend.ecorruta.model.Usuario;

@Component
public class UtilidadJwt {

    @Value("${jwt.secret}")
    private String secreto;

    @Value("${jwt.expiracion-milisegunbdos}")
    private Long expiracionMilisegundos;

    public String generarToken(Usuario usuario) {
        return null;
    }
}
