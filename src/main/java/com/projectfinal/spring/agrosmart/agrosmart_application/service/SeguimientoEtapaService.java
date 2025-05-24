package com.projectfinal.spring.agrosmart.agrosmart_application.service;

import com.projectfinal.spring.agrosmart.agrosmart_application.model.SeguimientoEtapa;
import com.projectfinal.spring.agrosmart.agrosmart_application.model.PlaneacionCultivo;
import com.projectfinal.spring.agrosmart.agrosmart_application.model.EtapaCultivo;
import com.projectfinal.spring.agrosmart.agrosmart_application.repository.SeguimientoEtapaRepository;
import com.projectfinal.spring.agrosmart.agrosmart_application.repository.PlaneacionCultivoRepository;
import com.projectfinal.spring.agrosmart.agrosmart_application.repository.EtapaCultivoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SeguimientoEtapaService {

    private final SeguimientoEtapaRepository seguimientoEtapaRepository;
    private final PlaneacionCultivoRepository planeacionCultivoRepository;
    private final EtapaCultivoRepository etapaCultivoRepository;

    public SeguimientoEtapaService(SeguimientoEtapaRepository seguimientoEtapaRepository,
                                   PlaneacionCultivoRepository planeacionCultivoRepository,
                                   EtapaCultivoRepository etapaCultivoRepository) {
        this.seguimientoEtapaRepository = seguimientoEtapaRepository;
        this.planeacionCultivoRepository = planeacionCultivoRepository;
        this.etapaCultivoRepository = etapaCultivoRepository;
    }

    public SeguimientoEtapa saveSeguimientoEtapa(SeguimientoEtapa seguimientoEtapa) {
        // Asegurarse de que PlaneacionCultivo y EtapaCultivo existen y están enlazados
        PlaneacionCultivo planeacion = planeacionCultivoRepository.findById(seguimientoEtapa.getPlaneacion().getId())
                .orElseThrow(() -> new RuntimeException("Planeación de Cultivo no encontrada."));
        EtapaCultivo etapa = etapaCultivoRepository.findById(seguimientoEtapa.getEtapa().getId())
                .orElseThrow(() -> new RuntimeException("Etapa de Cultivo no encontrada."));

        seguimientoEtapa.setPlaneacion(planeacion);
        seguimientoEtapa.setEtapa(etapa);

        return seguimientoEtapaRepository.save(seguimientoEtapa);
    }

    @Transactional(readOnly = true)
    public Optional<SeguimientoEtapa> getSeguimientoEtapaById(Long id) {
        return seguimientoEtapaRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<SeguimientoEtapa> getAllSeguimientosEtapa() {
        return seguimientoEtapaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<SeguimientoEtapa> getSeguimientosByPlaneacionId(Long planeacionId) {
        return seguimientoEtapaRepository.findByPlaneacionId(planeacionId);
    }

    public SeguimientoEtapa updateSeguimientoEtapa(Long id, SeguimientoEtapa seguimientoEtapaDetails) {
        SeguimientoEtapa existingSeguimiento = seguimientoEtapaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Seguimiento de Etapa no encontrado con ID: " + id));

        existingSeguimiento.setFechaInicioReal(seguimientoEtapaDetails.getFechaInicioReal());
        existingSeguimiento.setFechaFinReal(seguimientoEtapaDetails.getFechaFinReal());
        existingSeguimiento.setEstado(seguimientoEtapaDetails.getEstado());
        existingSeguimiento.setObservaciones(seguimientoEtapaDetails.getObservaciones());

        // Si la planeacion o la etapa cambian, actualizar y revalidar
        if (seguimientoEtapaDetails.getPlaneacion() != null &&
            !existingSeguimiento.getPlaneacion().getId().equals(seguimientoEtapaDetails.getPlaneacion().getId())) {
            PlaneacionCultivo nuevaPlaneacion = planeacionCultivoRepository.findById(seguimientoEtapaDetails.getPlaneacion().getId())
                    .orElseThrow(() -> new RuntimeException("Nueva Planeación de Cultivo no encontrada."));
            existingSeguimiento.setPlaneacion(nuevaPlaneacion);
        }
        if (seguimientoEtapaDetails.getEtapa() != null &&
            !existingSeguimiento.getEtapa().getId().equals(seguimientoEtapaDetails.getEtapa().getId())) {
            EtapaCultivo nuevaEtapa = etapaCultivoRepository.findById(seguimientoEtapaDetails.getEtapa().getId())
                    .orElseThrow(() -> new RuntimeException("Nueva Etapa de Cultivo no encontrada."));
            existingSeguimiento.setEtapa(nuevaEtapa);
        }

        return seguimientoEtapaRepository.save(existingSeguimiento);
    }

    public void deleteSeguimientoEtapa(Long id) {
        seguimientoEtapaRepository.deleteById(id);
    }
}
