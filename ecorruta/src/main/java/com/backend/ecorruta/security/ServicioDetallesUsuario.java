
package com.backend.ecorruta.security;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import com.backend.ecorruta.repository.UsuarioRepository;

@Service
@RequiredArgsConstructor
public class ServicioDetallesUsuario implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String correo)
            throws UsernameNotFoundException {

        return usuarioRepository
                .findByCorreoIgnoreCase(correo)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado"));
    }
}
