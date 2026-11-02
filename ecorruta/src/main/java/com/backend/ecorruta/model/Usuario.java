
package com.backend.ecorruta.model;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import lombok.*;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario implements UserDetails {

    @Id
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no es válido")
    @Size(max = 254, message = "El correo no puede superar los 254 caracteres")
    @Column(name = "correo", nullable = false, length = 254)
    private String correo;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "rol_nombre", referencedColumnName = "nombre", nullable = false)
    private Rol rol;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "colonia_nombre", referencedColumnName = "nombre"),
            @JoinColumn(name = "colonia_codigo_postal", referencedColumnName = "codigo_postal")
    })
    private Colonia colonia;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 60, message = "El nombre no puede superar los 60 caracteres")
    @Column(name = "nombre", nullable = false, length = 60)
    private String nombre;

    @NotBlank(message = "El apellido paterno es obligatorio")
    @Size(max = 60, message = "El apellido paterno no puede superar los 60 caracteres")
    @Column(name = "apellido_paterno", nullable = false, length = 60)
    private String apellidoPaterno;

    @Size(max = 60, message = "El apellido materno no puede superar los 60 caracteres")
    @Column(name = "apellido_materno", length = 60)
    private String apellidoMaterno;

    @JsonIgnore
    @NotBlank(message = "La contraseña es obligatoria")
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Builder.Default
    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @Column(name = "fecha_registro", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime fechaRegistro;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
                new SimpleGrantedAuthority("ROLE_" + rol.getNombre()));
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return correo;
    }

    @Override
    public boolean isEnabled() {
        return activo;
    }
}
