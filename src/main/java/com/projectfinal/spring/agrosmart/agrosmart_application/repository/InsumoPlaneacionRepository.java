package com.projectfinal.spring.agrosmart.agrosmart_application.repository;

import com.projectfinal.spring.agrosmart.agrosmart_application.model.InsumoPlaneacion;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface InsumoPlaneacionRepository extends JpaRepository<InsumoPlaneacion, Long> {
    // Encontrar insumos usados en una planeación específica
    List<InsumoPlaneacion> findByPlaneacionId(Long planeacionId);

    // Encontrar insumos usados en una planeación y un insumo específico
    Optional<InsumoPlaneacion> findByPlaneacionIdAndInsumoId(Long planeacionId, Long insumoId);
}