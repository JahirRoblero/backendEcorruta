package com.backend.ecorruta.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.ecorruta.dto.ColoniaAdminDTO;
import com.backend.ecorruta.service.ServicioAdminRuta;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/colonias")
@RequiredArgsConstructor
public class ControladorAdminColonia {

    private final ServicioAdminRuta servicioAdminRuta;

    @GetMapping
    public List<ColoniaAdminDTO> listar() {
        return servicioAdminRuta.listarColonias();
    }
}