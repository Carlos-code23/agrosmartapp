package com.projectfinal.spring.agrosmart.agrosmart_application.repository;

import com.projectfinal.spring.agrosmart.agrosmart_application.model.InsumoPlaneacion;
import com.projectfinal.spring.agrosmart.agrosmart_application.model.PlaneacionCultivo; // Importar PlaneacionCultivo
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InsumoPlaneacionRepository extends JpaRepository<InsumoPlaneacion, Long> {
    // Para obtener todos los insumos asociados a una planeación específica
    List<InsumoPlaneacion> findByPlaneacion(PlaneacionCultivo planeacion);
    
    // Si usas el ID directamente en algún otro lugar, también lo puedes tener
    List<InsumoPlaneacion> findByPlaneacionId(Long planeacionId);
}