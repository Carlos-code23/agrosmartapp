package com.projectfinal.spring.agrosmart.agrosmart_application.service;

import com.projectfinal.spring.agrosmart.agrosmart_application.model.Siembra;
import com.projectfinal.spring.agrosmart.agrosmart_application.model.PlaneacionCultivo;
import com.projectfinal.spring.agrosmart.agrosmart_application.repository.SiembraRepository;
import com.projectfinal.spring.agrosmart.agrosmart_application.repository.PlaneacionCultivoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SiembraService {

    private final SiembraRepository siembraRepository;
    private final PlaneacionCultivoRepository planeacionCultivoRepository;

    public SiembraService(SiembraRepository siembraRepository, PlaneacionCultivoRepository planeacionCultivoRepository) {
        this.siembraRepository = siembraRepository;
        this.planeacionCultivoRepository = planeacionCultivoRepository;
    }

    public Siembra saveSiembra(Siembra siembra) {
        // Asegurarse de que PlaneacionCultivo existe y está enlazada
        PlaneacionCultivo planeacion = planeacionCultivoRepository.findById(siembra.getPlaneacion().getId())
                .orElseThrow(() -> new RuntimeException("Planeación de Cultivo no encontrada."));
        siembra.setPlaneacion(planeacion);

        return siembraRepository.save(siembra);
    }

    @Transactional(readOnly = true)
    public Optional<Siembra> getSiembraById(Long id) {
        return siembraRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Siembra> getAllSiembras() {
        return siembraRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Siembra> getSiembrasByPlaneacionId(Long planeacionId) {
        return siembraRepository.findByPlaneacionId(planeacionId);
    }

    public Siembra updateSiembra(Long id, Siembra siembraDetails) {
        Siembra siembra = siembraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Siembra no encontrada con ID: " + id));

        siembra.setFechaReal(siembraDetails.getFechaReal());
        siembra.setObservaciones(siembraDetails.getObservaciones());

        // Si la planeacion cambia
        if (siembraDetails.getPlaneacion() != null &&
            !siembra.getPlaneacion().getId().equals(siembraDetails.getPlaneacion().getId())) {
            PlaneacionCultivo nuevaPlaneacion = planeacionCultivoRepository.findById(siembraDetails.getPlaneacion().getId())
                    .orElseThrow(() -> new RuntimeException("Nueva Planeación de Cultivo para la siembra no encontrada."));
            siembra.setPlaneacion(nuevaPlaneacion);
        }

        return siembraRepository.save(siembra);
    }

    public void deleteSiembra(Long id) {
        siembraRepository.deleteById(id);
    }
}
