
package com.backend.ecorruta.model;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import lombok.*;

@Entity
@Table(name = "horario_ruta")
@IdClass(HorarioRutaId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HorarioRuta {

    @Id
    @NotBlank(message = "El nombre de la ruta es obligatorio")
    @Size(max = 60)
    @Column(name = "ruta_nombre", nullable = false, length = 60)
    private String rutaNombre;

    @Id
    @NotBlank(message = "El tipo de residuo es obligatorio")
    @Size(max = 60)
    @Column(name = "tipo_residuo_nombre", nullable = false, length = 60)
    private String tipoResiduoNombre;

    @Id
    @NotNull(message = "El día de la semana es obligatorio")
    @Min(value = 1, message = "El día mínimo es 1")
    @Max(value = 7, message = "El día máximo es 7")
    @Column(name = "dia_semana", nullable = false)
    private Short diaSemana;

    @Id
    @NotNull(message = "La hora de inicio es obligatoria")
    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @NotNull(message = "La hora de fin es obligatoria")
    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ruta_nombre", referencedColumnName = "nombre", insertable = false, updatable = false)
    private Ruta ruta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tipo_residuo_nombre", referencedColumnName = "nombre", insertable = false, updatable = false)
    private TipoResiduo tipoResiduo;

    @AssertTrue(message = "La hora de fin debe ser posterior a la hora de inicio")
    @JsonIgnore
    public boolean isHorarioValido() {
        return horaInicio == null
                || horaFin == null
                || horaFin.isAfter(horaInicio);
    }

    public void setRuta(Ruta ruta) {
        if (ruta == null) {
            throw new IllegalArgumentException("La ruta es obligatoria");
        }

        this.ruta = ruta;
        this.rutaNombre = ruta.getNombre();
    }

    public void setTipoResiduo(TipoResiduo tipoResiduo) {
        if (tipoResiduo == null) {
            throw new IllegalArgumentException("El tipo de residuo es obligatorio");
        }

        this.tipoResiduo = tipoResiduo;
        this.tipoResiduoNombre = tipoResiduo.getNombre();
    }
}
