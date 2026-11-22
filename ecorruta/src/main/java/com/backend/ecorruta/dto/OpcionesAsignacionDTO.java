package com.backend.ecorruta.dto;

import java.util.List;

public record OpcionesAsignacionDTO(
        List<Operador> operadores,
        List<Ruta> rutas,
        List<Vehiculo> vehiculos) {

    public record Operador(String correo, String nombre) {}
    public record Ruta(String nombre) {}
    public record Vehiculo(String placa, String numeroEconomico, String estado) {}
}
