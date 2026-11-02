
package com.backend.ecorruta.model;

import java.io.Serializable;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PuntoAcopioId implements Serializable {

    private String coloniaNombre;
    private String coloniaCodigoPostal;
    private String nombre;
}
