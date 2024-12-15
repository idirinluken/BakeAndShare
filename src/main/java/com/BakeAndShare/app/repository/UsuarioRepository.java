package com.BakeAndShare.app.repository;

import  com.BakeAndShare.app.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    List<Usuario> findByRol(int rol); // Ejemplo de método personalizado para buscar por rol

    // Método para buscar un usuario por su email
    Optional<Usuario> findByDatosUsuarioEmail(String email);

    // Buscar por ID
    Optional<Usuario> findById(Long id);

    // Método para encontrar usuarios que han pedido al menos un pastel
    List<Usuario> findByPastelesIsNotNull();
}