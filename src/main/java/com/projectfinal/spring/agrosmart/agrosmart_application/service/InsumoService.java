package com.projectfinal.spring.agrosmart.agrosmart_application.service;

import com.projectfinal.spring.agrosmart.agrosmart_application.model.Insumo;
import com.projectfinal.spring.agrosmart.agrosmart_application.repository.InsumoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class InsumoService {

    private final InsumoRepository insumoRepository;

    public InsumoService(InsumoRepository insumoRepository) {
        this.insumoRepository = insumoRepository;
    }

    public Insumo saveInsumo(Insumo insumo) {
        return insumoRepository.save(insumo);
    }

    @Transactional(readOnly = true)
    public Optional<Insumo> getInsumoById(Long id) {
        return insumoRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Insumo> getAllInsumos() {
        return insumoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Insumo> getInsumoByNombre(String nombre) {
        return insumoRepository.findByNombre(nombre);
    }

    public Insumo updateInsumo(Long id, Insumo insumoDetails) {
        Insumo insumo = insumoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado con ID: " + id));

        insumo.setNombre(insumoDetails.getNombre());
        insumo.setTipo(insumoDetails.getTipo());
        insumo.setUnidadMedida(insumoDetails.getUnidadMedida());
        insumo.setPrecioUnitario(insumoDetails.getPrecioUnitario());
        insumo.setDescripcion(insumoDetails.getDescripcion());

        return insumoRepository.save(insumo);
    }

    public void deleteInsumo(Long id) {
        insumoRepository.deleteById(id);
    }
}