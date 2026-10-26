package com.backend.ecorruta.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table (name = "ubicacion_unidad")
@Getter 
@Setter 
@AllArgsConstructor 
@NoArgsConstructor 
public class UbicacionUnidad {
    
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Column (name = "id_ubicacion_unidad", nullable = false)
    private Long id; 

    @ManyToOne (optional = false)
    @JoinColumn (name = "id_ejecucion", nullable = false)
    private EjecucionRuta ejecucion;

    @NotNull 
    @DecimalMin ("-90.0")
    @DecimalMax ("90.0")
    @Column (precision = 8, scale = 6, nullable = false)
    private BigDecimal latitud;

    @NotNull 
    @DecimalMin ("-180.0")
    @DecimalMax ("180.0")
    @Column (precision = 9, scale = 6, nullable = false)
    private BigDecimal longitud;

    @NotNull 
    @Column (name="fecha_hora")
    private OffsetDateTime fechaYhora;
}
