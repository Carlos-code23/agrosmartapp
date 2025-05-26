package com.projectfinal.spring.agrosmart.agrosmart_application.repository;

import com.projectfinal.spring.agrosmart.agrosmart_application.model.Insumo;
import com.projectfinal.spring.agrosmart.agrosmart_application.model.Usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface InsumoRepository extends JpaRepository<Insumo, Long> {
    // Encontrar insumos por nombre (puede haber nombres duplicados, si necesitas exacto usa Optional)
    Optional<Insumo> findByNombre(String nombre);

    // Encontrar insumos por tipo
    List<Insumo> findByTipo(String tipo);

    // Encontrar insumos por nombre que contenga una cadena (ignorando mayúsculas/minúsculas)
    List<Insumo> findByNombreContainingIgnoreCase(String nombre);

    List<Insumo> findByUsuario(Usuario usuario);

    Optional<Insumo> findByIdAndUsuario(Long id, Usuario usuario);
}