package com.backend.ecorruta.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.backend.ecorruta.enumeraciones.EstadoReporte;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "reporte")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Reporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reporte")
    private Long idReporte;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_colonia", nullable = false)
    private Colonia colonia;

    @Column(nullable = false, length = 60)
    private String tipo;

    @Column(columnDefinition = "Text", nullable = false)
    private String descripcion;

    @Column(name = "imagen_url", columnDefinition = "TEXT")
    private String imagenUrl;

    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    @Column(precision = 8, scale = 6)
    private BigDecimal latitud;

    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    @Column(precision = 9, scale = 6)
    private BigDecimal longitud;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoReporte estado = EstadoReporte.PENDIENTE;

    @Column(name = "fecha_reporte", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime fechaReporte;

    @Column(name = "fecha_atencion")
    private OffsetDateTime fechaAtencion;

}
