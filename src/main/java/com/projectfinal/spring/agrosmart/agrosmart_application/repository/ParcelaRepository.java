package com.projectfinal.spring.agrosmart.agrosmart_application.repository;

import com.projectfinal.spring.agrosmart.agrosmart_application.model.Parcela; // Importa tu entidad Parcela
import com.projectfinal.spring.agrosmart.agrosmart_application.model.Usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List; // Necesario si usas métodos que retornan listas
import java.util.Optional;

public interface ParcelaRepository extends JpaRepository<Parcela, Long> {
    // Encuentra parcelas por un usuario específico (por el objeto Usuario)
    List<Parcela> findByUsuario(Usuario usuario);

    // Encuentra una parcela por su ID y el usuario al que pertenece
    Optional<Parcela> findByIdAndUsuario(Long id, Usuario usuario);
}