package com.projectfinal.spring.agrosmart.agrosmart_application.repository;

import com.projectfinal.spring.agrosmart.agrosmart_application.model.PlaneacionCultivo;
import com.projectfinal.spring.agrosmart.agrosmart_application.model.Usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

public interface PlaneacionCultivoRepository extends JpaRepository<PlaneacionCultivo, Long> {// Encuentra planeaciones por usuario
    // Encuentra planeaciones por usuario
    List<PlaneacionCultivo> findByUsuario(Usuario usuario); // Cambiado de findByUsuarioId a findByUsuario

    // Encuentra una planeación por su ID y el usuario al que pertenece
    Optional<PlaneacionCultivo> findByIdAndUsuario(Long id, Usuario usuario);

    // Encontrar planeaciones por parcela
    List<PlaneacionCultivo> findByParcelaId(Long parcelaId);

    // Encontrar planeaciones por tipo de cultivo
    List<PlaneacionCultivo> findByTipoCultivoId(Long tipoCultivoId);

    // Encontrar planeaciones dentro de un rango de fechas de inicio
    List<PlaneacionCultivo> findByFechaInicioBetween(LocalDate fechaInicioDesde, LocalDate fechaInicioHasta);

    // Encontrar planeaciones por estado
    List<PlaneacionCultivo> findByEstado(String estado);
}