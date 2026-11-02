
package com.backend.ecorruta.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.backend.ecorruta.enumeraciones.EstadoReporte;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import lombok.*;

@Entity
@Table(name = "reporte")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Reporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reporte")
    private Integer idReporte;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "correo_usuario", referencedColumnName = "correo", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "colonia_nombre", referencedColumnName = "nombre", nullable = false),
            @JoinColumn(name = "colonia_codigo_postal", referencedColumnName = "codigo_postal", nullable = false)
    })
    private Colonia colonia;

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 60, message = "El título no puede superar los 60 caracteres")
    @Column(name = "titulo", nullable = false, length = 60)
    private String titulo;

    @NotBlank(message = "La descripción es obligatoria")
    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "imagen_url", columnDefinition = "TEXT")
    private String imagenUrl;

    @DecimalMin(value = "-90.0", message = "La latitud mínima es -90")
    @DecimalMax(value = "90.0", message = "La latitud máxima es 90")
    @Digits(integer = 2, fraction = 6)
    @Column(name = "latitud", precision = 8, scale = 6)
    private BigDecimal latitud;

    @DecimalMin(value = "-180.0", message = "La longitud mínima es -180")
    @DecimalMax(value = "180.0", message = "La longitud máxima es 180")
    @Digits(integer = 3, fraction = 6)
    @Column(name = "longitud", precision = 9, scale = 6)
    private BigDecimal longitud;

    @NotNull(message = "El estado es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoReporte estado = EstadoReporte.PENDIENTE;

    @Column(name = "fecha_reporte", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime fechaReporte;

    @Column(name = "fecha_atencion")
    private OffsetDateTime fechaAtencion;
}
