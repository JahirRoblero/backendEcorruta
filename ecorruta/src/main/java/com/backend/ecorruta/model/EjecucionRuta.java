
package com.backend.ecorruta.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import com.backend.ecorruta.enumeraciones.EstadoEjecucion;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import lombok.*;

@Entity
@Table(name = "ejecucion_ruta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EjecucionRuta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "folio_ejecucion")
    private Integer folioEjecucion;

    @NotNull(message = "La ruta es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ruta_nombre", referencedColumnName = "nombre", nullable = false)
    private Ruta ruta;

    @NotNull(message = "El vehículo es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "placa_vehiculo", referencedColumnName = "placa", nullable = false)
    private Vehiculo vehiculo;

    @NotNull(message = "El operador es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "correo_operador", referencedColumnName = "correo", nullable = false)
    private Usuario operador;

    @NotNull(message = "La fecha programada es obligatoria")
    @Column(name = "fecha_programada", nullable = false)
    private LocalDate fechaProgramada;

    @Column(name = "fecha_inicio")
    private OffsetDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private OffsetDateTime fechaFin;

    @NotNull(message = "El estado es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoEjecucion estado = EstadoEjecucion.PENDIENTE;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @AssertTrue(message = "La fecha de fin requiere una fecha de inicio y no puede ser anterior a ella")
    @JsonIgnore
    public boolean isFechasValidas() {
        return fechaFin == null ||
                (fechaInicio != null &&
                        !fechaFin.isBefore(fechaInicio));
    }
}
