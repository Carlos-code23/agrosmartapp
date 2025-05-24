package com.projectfinal.spring.agrosmart.agrosmart_application.repository;

import com.projectfinal.spring.agrosmart.agrosmart_application.model.Siembra;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

public interface SiembraRepository extends JpaRepository<Siembra, Long> {
    // Encontrar siembras para una planeación específica
    List<Siembra> findByPlaneacionId(Long planeacionId);

    // Encontrar siembras en un rango de fechas
    List<Siembra> findByFechaRealBetween(LocalDate fechaDesde, LocalDate fechaHasta);

    // Encontrar la siembra más reciente para una planeación (útil si hay varias siembras asociadas)
    Optional<Siembra> findTopByPlaneacionIdOrderByFechaRealDesc(Long planeacionId);
}