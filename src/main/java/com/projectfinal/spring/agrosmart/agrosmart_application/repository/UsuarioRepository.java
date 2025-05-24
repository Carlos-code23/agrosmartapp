package com.projectfinal.spring.agrosmart.agrosmart_application.repository;

import com.projectfinal.spring.agrosmart.agrosmart_application.model.Usuario; // Importa tu entidad Usuario
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional; // Necesario para findByEmail

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // JpaRepository<T, ID> donde T es la entidad y ID es el tipo de su clave primaria.

    // Método personalizado: Spring Data JPA puede generar automáticamente este método
    // si sigues las convenciones de nombres.
    Optional<Usuario> findByEmail(String email);

    // Puedes añadir otros métodos basados en las propiedades de Usuario, por ejemplo:
    // List<Usuario> findByNombreContainingIgnoreCase(String nombre);
}