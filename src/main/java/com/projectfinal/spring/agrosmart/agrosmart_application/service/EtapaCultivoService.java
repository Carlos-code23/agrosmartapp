package com.projectfinal.spring.agrosmart.agrosmart_application.service;

import com.projectfinal.spring.agrosmart.agrosmart_application.model.EtapaCultivo;
import com.projectfinal.spring.agrosmart.agrosmart_application.model.TipoCultivo;
import com.projectfinal.spring.agrosmart.agrosmart_application.repository.EtapaCultivoRepository;
import com.projectfinal.spring.agrosmart.agrosmart_application.repository.TipoCultivoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EtapaCultivoService {

    private final EtapaCultivoRepository etapaCultivoRepository;
    private final TipoCultivoRepository tipoCultivoRepository; // Necesario para validar/obtener TipoCultivo

    public EtapaCultivoService(EtapaCultivoRepository etapaCultivoRepository, TipoCultivoRepository tipoCultivoRepository) {
        this.etapaCultivoRepository = etapaCultivoRepository;
        this.tipoCultivoRepository = tipoCultivoRepository;
    }

    public EtapaCultivo saveEtapaCultivo(EtapaCultivo etapaCultivo) {
        // Asegurarse de que el TipoCultivo asociado exista
        TipoCultivo tipoCultivo = tipoCultivoRepository.findById(etapaCultivo.getTipoCultivo().getId())
                .orElseThrow(() -> new RuntimeException("Tipo de Cultivo no encontrado."));
        etapaCultivo.setTipoCultivo(tipoCultivo); // Asociar el objeto completo

        return etapaCultivoRepository.save(etapaCultivo);
    }

    @Transactional(readOnly = true)
    public Optional<EtapaCultivo> getEtapaCultivoById(Long id) {
        return etapaCultivoRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<EtapaCultivo> getAllEtapasCultivo() {
        return etapaCultivoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<EtapaCultivo> getEtapasByTipoCultivoId(Long tipoCultivoId) {
        return etapaCultivoRepository.findByTipoCultivoId(tipoCultivoId);
    }

    public EtapaCultivo updateEtapaCultivo(Long id, EtapaCultivo etapaCultivoDetails) {
        EtapaCultivo etapaCultivo = etapaCultivoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Etapa de Cultivo no encontrada con ID: " + id));

        etapaCultivo.setNombre(etapaCultivoDetails.getNombre());
        etapaCultivo.setDescripcion(etapaCultivoDetails.getDescripcion());
        etapaCultivo.setDuracionDias(etapaCultivoDetails.getDuracionDias());

        // Si el tipo de cultivo cambia, actualizarlo
        if (etapaCultivoDetails.getTipoCultivo() != null &&
            !etapaCultivo.getTipoCultivo().getId().equals(etapaCultivoDetails.getTipoCultivo().getId())) {
            TipoCultivo nuevoTipoCultivo = tipoCultivoRepository.findById(etapaCultivoDetails.getTipoCultivo().getId())
                    .orElseThrow(() -> new RuntimeException("Nuevo Tipo de Cultivo para la etapa no encontrado."));
            etapaCultivo.setTipoCultivo(nuevoTipoCultivo);
        }

        return etapaCultivoRepository.save(etapaCultivo);
    }

    public void deleteEtapaCultivo(Long id) {
        etapaCultivoRepository.deleteById(id);
    }
}
