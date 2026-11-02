
package com.backend.ecorruta.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import lombok.*;

@Entity
@Table(name = "ubicacion_unidad")
@IdClass(UbicacionUnidadId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UbicacionUnidad {

    @Id
    @NotNull
    @Column(name = "folio_ejecucion", nullable = false)
    private Integer folioEjecucion;

    @Id
    @Column(name = "numero_muestra", nullable = false, insertable = false, updatable = false)
    private Integer numeroMuestra;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "folio_ejecucion", referencedColumnName = "folio_ejecucion", insertable = false, updatable = false)
    private EjecucionRuta ejecucionRuta;

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

    @Column(name = "fecha_hora", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime fechaHora;

    public void setEjecucionRuta(EjecucionRuta ejecucionRuta) {
        if (ejecucionRuta == null ||
                ejecucionRuta.getFolioEjecucion() == null) {
            throw new IllegalArgumentException(
                    "La ejecución de ruta es obligatoria");
        }

        this.ejecucionRuta = ejecucionRuta;
        this.folioEjecucion = ejecucionRuta.getFolioEjecucion();
    }
}
