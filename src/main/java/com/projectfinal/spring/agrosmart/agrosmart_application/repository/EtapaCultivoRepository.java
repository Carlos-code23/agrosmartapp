package com.projectfinal.spring.agrosmart.agrosmart_application.repository;

import com.projectfinal.spring.agrosmart.agrosmart_application.model.EtapaCultivo;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EtapaCultivoRepository extends JpaRepository<EtapaCultivo, Long> {
    // Encontrar etapas para un tipo de cultivo específico
    List<EtapaCultivo> findByTipoCultivoId(Long tipoCultivoId);

    // Encontrar una etapa específica por nombre dentro de un tipo de cultivo
    Optional<EtapaCultivo> findByTipoCultivoIdAndNombre(Long tipoCultivoId, String nombre);
}