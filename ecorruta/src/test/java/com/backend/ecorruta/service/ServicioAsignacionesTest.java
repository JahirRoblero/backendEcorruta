package com.backend.ecorruta.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class ServicioAsignacionesTest {

    @Test
    void lunesMiercolesViernesTieneMascaraCorrecta() {
        int mascara = ServicioAsignaciones.mascaraDias(List.of(1, 3, 5));
        assertEquals(21, mascara);
        assertEquals(List.of(1, 3, 5), ServicioAsignaciones.diasDeMascara(mascara));
    }

    @Test
    void asignacionTodosLosDias() {
        int mascara = ServicioAsignaciones.mascaraDias(List.of(1, 2, 3, 4, 5, 6, 7));
        assertEquals(127, mascara);
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7),
                ServicioAsignaciones.diasDeMascara(mascara));
    }

    @Test
    void noAceptaDiasInvalidos() {
        assertThrows(ResponseStatusException.class,
                () -> ServicioAsignaciones.mascaraDias(List.of()));
        assertThrows(ResponseStatusException.class,
                () -> ServicioAsignaciones.mascaraDias(List.of(0, 1)));
        assertThrows(ResponseStatusException.class,
                () -> ServicioAsignaciones.mascaraDias(List.of(8)));
    }

    @Test
    void diasDuplicadosNoRepitenAsignacion() {
        assertEquals(3, ServicioAsignaciones.mascaraDias(List.of(1, 1, 2)));
    }
}
