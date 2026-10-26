package com.backend.ecorruta.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.backend.ecorruta.enumeraciones.EstadoIncidencia;

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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table (name = "incidencia")
@Getter 
@Setter 
@AllArgsConstructor 
@NoArgsConstructor 
public class Incidencia {
    
    @Id 
    @Column (name = "id_incidencia")
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne (optional = false)
    @JoinColumn  (name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column (length = 60)
    private String tipo;

    @Column (columnDefinition = "TEXT")
    private String descripcion;

    @Column (name = "fecha_hora", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime fechaHora;

    @Enumerated (EnumType.STRING)
    @Column (nullable = false)
    private EstadoIncidencia estado = EstadoIncidencia.PENDIENTE;

    @Column (precision = 8, scale = 6)
    private BigDecimal latitud;

    @Column (precision = 9, scale = 6)
    private BigDecimal longitud;

    @Column (columnDefinition = "TEXT")
    private String imagenUrl;

}
