package com.projectfinal.spring.agrosmart.agrosmart_application.repository;

import com.projectfinal.spring.agrosmart.agrosmart_application.model.TipoCultivo;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional; // Necesario para findByNombre

public interface TipoCultivoRepository extends JpaRepository<TipoCultivo, Long> {
    // Encontrar un tipo de cultivo por su nombre (útil para asegurar unicidad o buscar)
    Optional<TipoCultivo> findByNombre(String nombre);
}