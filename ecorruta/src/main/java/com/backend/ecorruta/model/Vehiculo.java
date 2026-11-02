
package com.backend.ecorruta.model;

import java.math.BigDecimal;

import com.backend.ecorruta.enumeraciones.EstadoVehiculo;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import lombok.*;

@Entity
@Table(name = "vehiculo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehiculo {

    @Id
    @NotBlank()
    @Size(max = 15)
    @Column(name = "placa", nullable = false, length = 15)
    private String placa;

    @NotBlank()
    @Size(max = 60)
    @Column(name = "numero_economico", nullable = false, unique = true, length = 60)
    private String numeroEconomico;

    @Size(max = 50)
    @Column(name = "marca", length = 50)
    private String marca;

    @Size(max = 40)
    @Column(name = "modelo", length = 40)
    private String modelo;

    @Positive()
    @Digits(integer = 8, fraction = 2, message = "La capacidad debe tener máximo 8 enteros y 2 decimales")
    @Column(name = "capacidad_kg", precision = 10, scale = 2)
    private BigDecimal capacidadKg;

    @NotNull()
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoVehiculo estado = EstadoVehiculo.DISPONIBLE;

    @Builder.Default
    @Column(name = "activo", nullable = false)
    private boolean activo = true;
}
