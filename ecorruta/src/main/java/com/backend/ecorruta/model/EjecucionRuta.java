package com.backend.ecorruta.model;

import java.sql.Date;
import java.time.OffsetDateTime;

import com.backend.ecorruta.enumeraciones.EstadoEjecucionRuta;

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
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table (name = "ejecucion_ruta")
@Getter 
@Setter 
@AllArgsConstructor 
@NoArgsConstructor 
public class EjecucionRuta {
    
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Column (name = "id_ejecucion")
    private Long id;

    @NotNull 
    @ManyToOne (optional = false)
    @JoinColumn (name = "id_ruta", nullable = false)
    private Ruta ruta;

    @NotNull 
    @ManyToOne (optional = false)
    @JoinColumn (name = "id_vehiculo", nullable = false)
    private Vehiculo vehiculo;

    @NotNull 
    @ManyToOne (optional = false)
    @JoinColumn (name = "id_operador", nullable = false)
    private Usuario operador;

    @NotNull 
    @Column (name = "fecha_progreamada", nullable = false)
    private Date fechaProgramada;

    @Column (name = "fecha_inicio")
    private OffsetDateTime fechaInicio;

    @Column (name = "fecha_fin")
    private OffsetDateTime fechaFin;

    @NotNull 
    @Enumerated (EnumType.STRING)
    @JoinColumn (name = "estado", nullable = false)
    private EstadoEjecucionRuta estado = EstadoEjecucionRuta.PENDIENTE;

    @Column (columnDefinition = "TEXT")
    private String observaciones;
}
