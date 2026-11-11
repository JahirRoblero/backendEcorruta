package com.backend.ecorruta.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.backend.ecorruta.model.VerificacionCorreo;
import jakarta.persistence.LockModeType;

public interface VerificacionCorreoRepository
        extends JpaRepository<VerificacionCorreo, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM VerificacionCorreo v WHERE v.correo = :correo")
    Optional<VerificacionCorreo> buscarConBloqueo(@Param("correo") String correo);
}
