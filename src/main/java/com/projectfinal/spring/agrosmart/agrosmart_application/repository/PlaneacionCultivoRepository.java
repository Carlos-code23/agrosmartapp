package com.projectfinal.spring.agrosmart.agrosmart_application.repository;

import com.projectfinal.spring.agrosmart.agrosmart_application.model.PlaneacionCultivo;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.time.LocalDate;

public interface PlaneacionCultivoRepository extends JpaRepository<PlaneacionCultivo, Long> {
    // Encontrar planeaciones por usuario
    List<PlaneacionCultivo> findByUsuarioId(Long usuarioId);

    // Encontrar planeaciones por parcela
    List<PlaneacionCultivo> findByParcelaId(Long parcelaId);

    // Encontrar planeaciones por tipo de cultivo
    List<PlaneacionCultivo> findByTipoCultivoId(Long tipoCultivoId);

    // Encontrar planeaciones dentro de un rango de fechas de inicio
    List<PlaneacionCultivo> findByFechaInicioBetween(LocalDate fechaInicioDesde, LocalDate fechaInicioHasta);

    // Encontrar planeaciones por estado
    List<PlaneacionCultivo> findByEstado(String estado);
}