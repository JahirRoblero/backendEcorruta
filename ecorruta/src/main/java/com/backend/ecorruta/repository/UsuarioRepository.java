package com.backend.ecorruta.repository;

import com.backend.ecorruta.model.Usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository
        extends JpaRepository<Usuario, String> {

    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    Optional<Usuario> findByCorreo(String correo);

    boolean existsByCorreo(String correo);

    // ===== Panel admin (Ángel) =====

    @Query("""
            select u from Usuario u
            join fetch u.rol
            left join fetch u.colonia
            order by u.nombre, u.apellidoPaterno
            """)
    List<Usuario> buscarTodosParaAdmin();

    @Query("""
            select u from Usuario u
            join fetch u.rol r
            left join fetch u.colonia
            where r.nombre = :rol
            order by u.nombre, u.apellidoPaterno
            """)
    List<Usuario> buscarPorRolParaAdmin(@Param("rol") String rol);
}