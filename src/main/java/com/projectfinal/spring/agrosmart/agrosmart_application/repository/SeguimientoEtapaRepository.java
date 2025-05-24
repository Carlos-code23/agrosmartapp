package com.projectfinal.spring.agrosmart.agrosmart_application.repository;

import com.projectfinal.spring.agrosmart.agrosmart_application.model.SeguimientoEtapa;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

public interface SeguimientoEtapaRepository extends JpaRepository<SeguimientoEtapa, Long> {
    // Encontrar seguimientos para una planeación de cultivo
    List<SeguimientoEtapa> findByPlaneacionId(Long planeacionId);

    // Encontrar un seguimiento específico para una planeación y etapa
    Optional<SeguimientoEtapa> findByPlaneacionIdAndEtapaId(Long planeacionId, Long etapaId);

    // Encontrar seguimientos por estado para una planeación
    List<SeguimientoEtapa> findByPlaneacionIdAndEstado(Long planeacionId, String estado);

    // Encontrar seguimientos completados en un rango de fechas
    List<SeguimientoEtapa> findByFechaFinRealBetween(LocalDate fechaDesde, LocalDate fechaHasta);
}
