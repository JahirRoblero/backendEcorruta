package com.backend.ecorruta.model;

import java.time.OffsetDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "verificacion_correo")
@Getter
@Setter
@NoArgsConstructor
public class VerificacionCorreo {

    @Id
    @Column(name = "correo", nullable = false, length = 254)
    private String correo;

    @Column(name = "codigo_hash", nullable = false, length = 64)
    private String codigoHash;

    @Column(name = "fecha_expiracion", nullable = false)
    private OffsetDateTime fechaExpiracion;

    @Column(name = "fecha_ultimo_envio", nullable = false)
    private OffsetDateTime fechaUltimoEnvio;

    @Column(name = "intentos", nullable = false)
    private int intentos;

    @Column(name = "inicio_ventana", nullable = false)
    private OffsetDateTime inicioVentana;

    @Column(name = "envios_ventana", nullable = false)
    private int enviosVentana;
}
