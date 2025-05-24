package com.projectfinal.spring.agrosmart.agrosmart_application.service;

import com.projectfinal.spring.agrosmart.agrosmart_application.model.InsumoPlaneacion;
import com.projectfinal.spring.agrosmart.agrosmart_application.model.Insumo;
import com.projectfinal.spring.agrosmart.agrosmart_application.model.PlaneacionCultivo;
import com.projectfinal.spring.agrosmart.agrosmart_application.repository.InsumoPlaneacionRepository;
import com.projectfinal.spring.agrosmart.agrosmart_application.repository.InsumoRepository;
import com.projectfinal.spring.agrosmart.agrosmart_application.repository.PlaneacionCultivoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

@Service
@Transactional
public class InsumoPlaneacionService {

    private final InsumoPlaneacionRepository insumoPlaneacionRepository;
    private final PlaneacionCultivoRepository planeacionCultivoRepository;
    private final InsumoRepository insumoRepository;

    public InsumoPlaneacionService(InsumoPlaneacionRepository insumoPlaneacionRepository,
                                  PlaneacionCultivoRepository planeacionCultivoRepository,
                                  InsumoRepository insumoRepository) {
        this.insumoPlaneacionRepository = insumoPlaneacionRepository;
        this.planeacionCultivoRepository = planeacionCultivoRepository;
        this.insumoRepository = insumoRepository;
    }

    /**
     * Guarda un insumo en una planeación de cultivo, calculando el costo total.
     * @param insumoPlaneacion El objeto InsumoPlaneacion a guardar.
     * @return El insumoPlaneacion guardado.
     */
    public InsumoPlaneacion saveInsumoPlaneacion(InsumoPlaneacion insumoPlaneacion) {
        // Asegúrate de que PlaneacionCultivo e Insumo existen y están enlazados
        PlaneacionCultivo planeacion = planeacionCultivoRepository.findById(insumoPlaneacion.getPlaneacion().getId())
                .orElseThrow(() -> new RuntimeException("Planeación de Cultivo no encontrada."));
        Insumo insumo = insumoRepository.findById(insumoPlaneacion.getInsumo().getId())
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado."));

        insumoPlaneacion.setPlaneacion(planeacion);
        insumoPlaneacion.setInsumo(insumo);

        // Calcular costo total
        BigDecimal cantidad = insumoPlaneacion.getCantidad();
        BigDecimal precioUnitario = insumo.getPrecioUnitario();
        BigDecimal costoTotal = cantidad.multiply(precioUnitario);
        insumoPlaneacion.setCostoTotal(costoTotal);

        return insumoPlaneacionRepository.save(insumoPlaneacion);
    }

    @Transactional(readOnly = true)
    public Optional<InsumoPlaneacion> getInsumoPlaneacionById(Long id) {
        return insumoPlaneacionRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<InsumoPlaneacion> getAllInsumosPlaneacion() {
        return insumoPlaneacionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<InsumoPlaneacion> getInsumosByPlaneacionId(Long planeacionId) {
        return insumoPlaneacionRepository.findByPlaneacionId(planeacionId);
    }

    public InsumoPlaneacion updateInsumoPlaneacion(Long id, InsumoPlaneacion insumoPlaneacionDetails) {
        InsumoPlaneacion existingInsumoPlaneacion = insumoPlaneacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("InsumoPlaneacion no encontrada con ID: " + id));

        // Actualizar campos básicos
        existingInsumoPlaneacion.setCantidad(insumoPlaneacionDetails.getCantidad());
        existingInsumoPlaneacion.setFechaRegistro(insumoPlaneacionDetails.getFechaRegistro());
        existingInsumoPlaneacion.setObservaciones(insumoPlaneacionDetails.getObservaciones());

        // Recalcular costo total si la cantidad o el insumo cambian (el insumo podría haber cambiado de precio)
        boolean needsRecalculation = false;
        if (insumoPlaneacionDetails.getInsumo() != null &&
            !existingInsumoPlaneacion.getInsumo().getId().equals(insumoPlaneacionDetails.getInsumo().getId())) {
            Insumo nuevoInsumo = insumoRepository.findById(insumoPlaneacionDetails.getInsumo().getId())
                    .orElseThrow(() -> new RuntimeException("Nuevo Insumo no encontrado."));
            existingInsumoPlaneacion.setInsumo(nuevoInsumo);
            needsRecalculation = true;
        }

        if (needsRecalculation || !existingInsumoPlaneacion.getCantidad().equals(insumoPlaneacionDetails.getCantidad())) {
            BigDecimal cantidad = existingInsumoPlaneacion.getCantidad();
            BigDecimal precioUnitario = existingInsumoPlaneacion.getInsumo().getPrecioUnitario();
            BigDecimal costoTotal = cantidad.multiply(precioUnitario);
            existingInsumoPlaneacion.setCostoTotal(costoTotal);
        }

        return insumoPlaneacionRepository.save(existingInsumoPlaneacion);
    }

    public void deleteInsumoPlaneacion(Long id) {
        insumoPlaneacionRepository.deleteById(id);
    }
}