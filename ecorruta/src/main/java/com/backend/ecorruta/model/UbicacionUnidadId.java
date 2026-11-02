
package com.backend.ecorruta.model;

import java.io.Serializable;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UbicacionUnidadId implements Serializable {

    private Integer folioEjecucion;
    private Integer numeroMuestra;
}
