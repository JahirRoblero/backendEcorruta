
package com.backend.ecorruta.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import lombok.*;

@Entity
@Table(name = "ruta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ruta {

        @Id
        @NotBlank(message = "El nombre de la ruta es obligatorio")
        @Size(max = 60, message = "El nombre no puede superar los 60 caracteres")
        @Column(name = "nombre", nullable = false, length = 60)
        private String nombre;

        @Column(name = "descripcion", columnDefinition = "TEXT")
        private String descripcion;

        @PositiveOrZero(message = "La distancia no puede ser negativa")
        @Digits(integer = 6, fraction = 2, message = "La distancia debe tener máximo 6 enteros y 2 decimales")
        @Column(name = "distancia_km", precision = 8, scale = 2)
        private BigDecimal distanciaKm;

        @Column(name = "activo", nullable = false)
        private boolean activo = true;

        @Column(name = "fecha_creacion", nullable = false, insertable = false, updatable = false)
        private OffsetDateTime fechaCreacion;

        @Column(name = "geometria", columnDefinition = "TEXT")
        private String geometria;

        @Column(name = "imagen_url", columnDefinition = "TEXT")
        private String imagenUrl;

        @ManyToMany(fetch = FetchType.LAZY)
        @JoinTable(name = "ruta_colonia", joinColumns = {
                        @JoinColumn(name = "ruta_nombre", referencedColumnName = "nombre")
        }, inverseJoinColumns = {
                        @JoinColumn(name = "colonia_nombre", referencedColumnName = "nombre"),
                        @JoinColumn(name = "colonia_codigo_postal", referencedColumnName = "codigo_postal")
        })
        private Set<Colonia> colonias = new HashSet<>();
}
