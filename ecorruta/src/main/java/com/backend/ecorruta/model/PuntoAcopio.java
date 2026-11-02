
package com.backend.ecorruta.model;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import lombok.*;

@Entity
@Table(name = "punto_acopio")
@IdClass(PuntoAcopioId.class)
@Getter
@Setter
@NoArgsConstructor
public class PuntoAcopio {

    @Id
    @NotBlank(message = "El nombre de la colonia es obligatorio")
    @Size(max = 100)
    @Column(name = "colonia_nombre", nullable = false, length = 100)
    private String coloniaNombre;

    @Id
    @NotBlank(message = "El código postal es obligatorio")
    @Pattern(regexp = "\\d{5}", message = "El código postal debe contener exactamente 5 dígitos")
    @Column(name = "colonia_codigo_postal", nullable = false, columnDefinition = "CHAR(5)")
    private String coloniaCodigoPostal;

    @Id
    @NotBlank(message = "El nombre del punto de acopio es obligatorio")
    @Size(max = 60, message = "El nombre no puede superar los 60 caracteres")
    @Column(name = "nombre", nullable = false, length = 60)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "colonia_nombre", referencedColumnName = "nombre", insertable = false, updatable = false),
            @JoinColumn(name = "colonia_codigo_postal", referencedColumnName = "codigo_postal", insertable = false, updatable = false)
    })
    private Colonia colonia;

    @Column(name = "direccion", columnDefinition = "TEXT")
    private String direccion;

    @NotNull(message = "La latitud es obligatoria")
    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    @Digits(integer = 2, fraction = 6)
    @Column(name = "latitud", nullable = false, precision = 8, scale = 6)
    private BigDecimal latitud;

    @NotNull(message = "La longitud es obligatoria")
    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    @Digits(integer = 3, fraction = 6)
    @Column(name = "longitud", nullable = false, precision = 9, scale = 6)
    private BigDecimal longitud;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "punto_acopio_tipo_residuo", joinColumns = {
            @JoinColumn(name = "colonia_nombre", referencedColumnName = "colonia_nombre"),
            @JoinColumn(name = "colonia_codigo_postal", referencedColumnName = "colonia_codigo_postal"),
            @JoinColumn(name = "punto_acopio_nombre", referencedColumnName = "nombre")
    }, inverseJoinColumns = {
            @JoinColumn(name = "tipo_residuo_nombre", referencedColumnName = "nombre")
    })
    private Set<TipoResiduo> tiposResiduos = new HashSet<>();

    public void setColonia(Colonia colonia) {
        if (colonia == null) {
            throw new IllegalArgumentException("La colonia es obligatoria");
        }

        this.colonia = colonia;
        this.coloniaNombre = colonia.getNombreColonia();
        this.coloniaCodigoPostal = colonia.getCodigoPostal();
    }
}
