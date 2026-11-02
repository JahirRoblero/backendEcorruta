
package com.backend.ecorruta.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.*;

@Entity
@Table(name = "tipo_residuo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoResiduo {

    @Id
    @NotBlank(message = "El nombre del tipo de residuo es obligatorio")
    @Size(max = 60, message = "El nombre no puede superar los 60 caracteres")
    @Column(name = "nombre", nullable = false, length = 60)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "instrucciones", columnDefinition = "TEXT")
    private String instrucciones;
}
