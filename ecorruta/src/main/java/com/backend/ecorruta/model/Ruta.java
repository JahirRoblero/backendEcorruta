package com.backend.ecorruta.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ruta")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Ruta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ruta")
    private Long idRuta;

    @NotBlank
    @Column(nullable = false, length = 60)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @PositiveOrZero
    @Column(name = "distancia_km", precision = 8, scale = 2)
    private BigDecimal distanciaKm;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "fecha_creacion", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime fechaCreacion;

    @Column(columnDefinition = "TEXT")
    private String geometria;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "ruta_colonia", joinColumns = @JoinColumn(name = "id_ruta"), inverseJoinColumns = @JoinColumn(name = "id_colonia"))
    private Set<Colonia> colonias = new HashSet<>();
}
