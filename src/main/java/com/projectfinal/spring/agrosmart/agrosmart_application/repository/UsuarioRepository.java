package com.projectfinal.spring.agrosmart.agrosmart_application.repository;

import com.projectfinal.spring.agrosmart.agrosmart_application.model.Usuario; // Importa tu entidad Usuario
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional; // Necesario para findByEmail

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    Optional<Usuario> findByEmail(String email);

}