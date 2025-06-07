package com.projectfinal.spring.agrosmart.agrosmart_application.repository;

import com.projectfinal.spring.agrosmart.agrosmart_application.model.TipoCultivo;
import com.projectfinal.spring.agrosmart.agrosmart_application.model.Usuario; 
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List; 
import java.util.Optional;

public interface TipoCultivoRepository extends JpaRepository<TipoCultivo, Long> {
    Optional<TipoCultivo> findByNombre(String nombre);

    List<TipoCultivo> findByUsuario(Usuario usuario);

    Optional<TipoCultivo> findByIdAndUsuario(Long id, Usuario usuario);

    Optional<TipoCultivo> findByNombreAndUsuario(String nombre, Usuario usuario);
}