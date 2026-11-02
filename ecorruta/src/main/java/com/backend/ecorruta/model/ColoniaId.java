package com.backend.ecorruta.model;

import java.io.Serializable;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ColoniaId implements Serializable {

    private String nombreColonia;
    private String codigoPostal;
}
