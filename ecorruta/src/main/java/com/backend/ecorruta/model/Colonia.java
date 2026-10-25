package com.backend.ecorruta.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.*;

@Entity
@Table(name = "colonia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Colonia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_colonia", nullable = false)
    private Long idColonia;

    @NotBlank(message = "El nombre de la colonia es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombreColonia;

    @NotBlank(message = "El código postal es obligatorio")
    @Pattern(regexp = "\\d{5}", message = "El código postal debe contener exactamente 5 dígitos")
    @Column(name = "codigo_postal", nullable = false, length = 5, columnDefinition = "CHAR(5)")
    private String codigoPostal;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;
}