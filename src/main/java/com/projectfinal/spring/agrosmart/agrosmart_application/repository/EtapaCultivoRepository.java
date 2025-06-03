package com.projectfinal.spring.agrosmart.agrosmart_application.repository;

import com.projectfinal.spring.agrosmart.agrosmart_application.model.EtapaCultivo;
import com.projectfinal.spring.agrosmart.agrosmart_application.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EtapaCultivoRepository extends JpaRepository<EtapaCultivo, Long> {
    // Buscar etapas por usuario (incluirá las predefinidas y las personalizadas)
    List<EtapaCultivo> findByUsuario(Usuario usuario);
    // Para asegurar que una etapa específica pertenece al usuario al editar/eliminar
    Optional<EtapaCultivo> findByIdAndUsuario(Long id, Usuario usuario);
    // Para encontrar etapas predefinidas por nombre, si el usuario aún no las tiene
    Optional<EtapaCultivo> findByNombreAndUsuario(String nombre, Usuario usuario);
}