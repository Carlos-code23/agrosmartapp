package com.projectfinal.spring.agrosmart.agrosmart_application.repository;

import com.projectfinal.spring.agrosmart.agrosmart_application.model.Parcela; // Importa tu entidad Parcela
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List; // Necesario si usas métodos que retornan listas

public interface ParcelaRepository extends JpaRepository<Parcela, Long> {
    // Ejemplo de método personalizado: encontrar parcelas por un usuario específico
    List<Parcela> findByUsuarioId(Long usuarioId);

    // List<Parcela> findByNombreContainingIgnoreCase(String nombre);
}