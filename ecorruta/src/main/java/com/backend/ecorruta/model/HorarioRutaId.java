
package com.backend.ecorruta.model;

import java.io.Serializable;
import java.time.LocalTime;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class HorarioRutaId implements Serializable {

    private String rutaNombre;
    private String tipoResiduoNombre;
    private Short diaSemana;
    private LocalTime horaInicio;
}
